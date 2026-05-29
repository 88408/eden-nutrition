package eden.service.impl;

import eden.common.constant.MQConstants;
import eden.common.constant.OrderConstants;
import eden.common.constant.RedisConstants;
import eden.common.exception.BusinessException;
import eden.common.result.ResultCode;
import eden.common.utils.IdGenerator;
import eden.mapper.OrderItemMapper;
import eden.mapper.OrderMapper;
import eden.pojo.*;
import eden.pojo.dto.OrderCreateDTO;
import eden.pojo.vo.AlipayDebugPayVO;
import eden.pojo.vo.CartItemVO;
import eden.pojo.vo.CartVO;
import eden.pojo.vo.PageVO;
import eden.service.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 订单服务实现类
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductService productService;

    @Autowired
    private UserAddressService userAddressService;

    @Autowired
    private CouponService couponService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AlipayService alipayService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Long userId, OrderCreateDTO createDTO) {
        // 防止重复提交（分布式锁）
        String lockKey = RedisConstants.ORDER_CREATE_LOCK + userId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 10, TimeUnit.SECONDS);
        if (locked == null || !locked) {
            throw new BusinessException("订单处理中，请勿重复提交");
        }

        try {
            // 获取收货地址
            UserAddress address = userAddressService.getById(createDTO.getAddressId());
            if (address == null || !address.getUserId().equals(userId)) {
                throw new BusinessException(ResultCode.ADDRESS_NOT_FOUND);
            }

            // 获取购物车中选中的商品
            CartVO cart = cartService.getCart(userId);
            List<CartItemVO> selectedItems = new ArrayList<>();
            for (CartItemVO item : cart.getItems()) {
                if (item.getSelected() != null && item.getSelected()) {
                    selectedItems.add(item);
                }
            }

            if (selectedItems.isEmpty()) {
                throw new BusinessException(ResultCode.CART_EMPTY);
            }

            // 计算订单金额
            BigDecimal totalAmount = BigDecimal.ZERO;
            List<OrderItem> orderItems = new ArrayList<>();

            for (CartItemVO cartItem : selectedItems) {
                // 检查库存并扣减
                boolean success = productService.decreaseStock(cartItem.getProductId(), cartItem.getQuantity());
                if (!success) {
                    throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH, 
                            "商品\"" + cartItem.getProductName() + "\"库存不足");
                }

                totalAmount = totalAmount.add(cartItem.getSubtotal());

                // 创建订单项
                OrderItem orderItem = new OrderItem();
                orderItem.setProductId(cartItem.getProductId());
                orderItem.setProductName(cartItem.getProductName());
                orderItem.setProductImage(cartItem.getProductImage());
                orderItem.setPrice(cartItem.getPrice());
                orderItem.setQuantity(cartItem.getQuantity());
                orderItem.setTotalPrice(cartItem.getSubtotal());
                orderItems.add(orderItem);
            }

            // 计算运费
            BigDecimal freightAmount = BigDecimal.ZERO;
            if (totalAmount.compareTo(BigDecimal.valueOf(OrderConstants.FREE_SHIPPING_THRESHOLD)) < 0) {
                freightAmount = BigDecimal.valueOf(OrderConstants.BASE_SHIPPING_FEE);
            }

            // 处理优惠券
            BigDecimal discountAmount = BigDecimal.ZERO;
            Long couponId = null;
            if (createDTO.getUserCouponId() != null) {
                UserCoupon userCoupon = couponService.getUserCoupons(userId, 0).stream()
                        .filter(c -> c.getId().equals(createDTO.getUserCouponId()))
                        .findFirst()
                        .orElse(null);
                
                if (userCoupon != null) {
                    Coupon coupon = couponService.getById(userCoupon.getCouponId());
                    if (coupon != null && totalAmount.compareTo(coupon.getMinAmount()) >= 0) {
                        if (coupon.getType() == 1) {
                            // 满减
                            discountAmount = coupon.getValue();
                        } else if (coupon.getType() == 2) {
                            // 折扣
                            discountAmount = totalAmount.multiply(BigDecimal.ONE.subtract(
                                    coupon.getValue().divide(BigDecimal.valueOf(100))));
                        }
                        couponId = userCoupon.getCouponId();
                    }
                }
            }

            // 计算实付金额
            BigDecimal payAmount = totalAmount.add(freightAmount).subtract(discountAmount);
            if (payAmount.compareTo(BigDecimal.ZERO) < 0) {
                payAmount = BigDecimal.ZERO;
            }

            // 创建订单
            String orderNo = IdGenerator.generateOrderNo();
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setTotalAmount(totalAmount);
            order.setPayAmount(payAmount);
            order.setFreightAmount(freightAmount);
            order.setDiscountAmount(discountAmount);
            order.setCouponId(couponId);
            order.setStatus(OrderConstants.STATUS_UNPAID);
            order.setOrderType(OrderConstants.TYPE_NORMAL);
            order.setReceiverName(address.getReceiverName());
            order.setReceiverPhone(address.getReceiverPhone());
            order.setReceiverAddress(address.getProvince() + address.getCity() + 
                    address.getDistrict() + address.getDetailAddress());
            order.setRemark(createDTO.getRemark());

            orderMapper.insert(order);

            // 设置订单项的订单ID和订单号
            for (OrderItem item : orderItems) {
                item.setOrderId(order.getId());
                item.setOrderNo(orderNo);
            }
            orderItemMapper.batchInsert(orderItems);

            // 使用优惠券
            if (createDTO.getUserCouponId() != null && couponId != null) {
                couponService.useCoupon(createDTO.getUserCouponId(), order.getId());
            }

            // 清除购物车中已购买的商品
            for (CartItemVO item : selectedItems) {
                cartService.removeFromCart(userId, item.getProductId());
            }

            // 发送延迟消息，处理订单超时
            if (rabbitTemplate != null) {
                rabbitTemplate.convertAndSend(MQConstants.DELAY_EXCHANGE, 
                        MQConstants.ORDER_TIMEOUT_KEY, orderNo, message -> {
                    message.getMessageProperties().setDelay(MQConstants.ORDER_TIMEOUT_DELAY);
                    return message;
                });
            }

            order.setOrderItems(orderItems);
            return order;
        } finally {
            // 释放锁
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    public Order getByOrderNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    @Override
    public Order getById(Long id) {
        return orderMapper.selectById(id);
    }

    @Override
    public PageVO<Order> getUserOrders(Long userId, Integer status, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<Order> orders = orderMapper.selectByUserId(userId, status, offset, pageSize);
        
        // 查询订单项
        for (Order order : orders) {
            List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
            order.setOrderItems(items);
        }

        long total = orderMapper.countByUserId(userId, status);
        return PageVO.of(orders, total, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long userId, String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (order.getStatus() != OrderConstants.STATUS_UNPAID) {
            throw new BusinessException(ResultCode.ORDER_CANNOT_CANCEL);
        }

        // 更新订单状态
        orderMapper.cancel(order.getId());

        // 回滚库存
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
        for (OrderItem item : items) {
            productService.increaseStock(item.getProductId(), item.getQuantity());
        }

        // 返还优惠券
        if (order.getCouponId() != null) {
            // 找到用户优惠券并返还
            List<UserCoupon> userCoupons = couponService.getUserCoupons(userId, 1);
            for (UserCoupon uc : userCoupons) {
                if (uc.getOrderId() != null && uc.getOrderId().equals(order.getId())) {
                    couponService.returnCoupon(uc.getId());
                    break;
                }
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String payOrder(Long userId, String orderNo, Integer payType) {
        if (!Integer.valueOf(OrderConstants.PAY_TYPE_ALIPAY).equals(payType)) {
            throw new BusinessException("当前仅支持支付宝沙箱支付");
        }
        return createAlipayPayment(userId, orderNo);
    }

    @Override
    public String createAlipayPayment(Long userId, String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        validateOrderCanStartAlipayPay(order, userId);
        return alipayService.createPagePayForm(order);
    }

    @Override
    public AlipayDebugPayVO createWeappDebugAlipayPayment(Long userId, String orderNo, String bridgeUrl) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        validateOrderCanStartAlipayPay(order, userId);

        String token = UUID.randomUUID().toString().replace("-", "");
        String tokenKey = RedisConstants.ALIPAY_BRIDGE_TOKEN + token;
        String tokenValue = order.getOrderNo() + ":" + userId;
        // 微信开发者工具无法直接提交 PagePay form，通过短期 token 换取一次性桥接页，避免把订单号裸露成长期入口。
        redisTemplate.opsForValue().set(tokenKey, tokenValue, RedisConstants.EXPIRE_ALIPAY_BRIDGE, TimeUnit.SECONDS);

        AlipayDebugPayVO vo = new AlipayDebugPayVO();
        vo.setOrderNo(order.getOrderNo());
        vo.setExpireSeconds(RedisConstants.EXPIRE_ALIPAY_BRIDGE);
        vo.setBridgeUrl(bridgeUrl + "?token=" + token);
        return vo;
    }

    @Override
    public String createAlipayBridgeHtml(String token, String debugReturnUrl) {
        if (token == null || token.isBlank()) {
            throw new BusinessException("支付宝调试桥接 token 不能为空");
        }

        String tokenKey = RedisConstants.ALIPAY_BRIDGE_TOKEN + token;
        Object value = redisTemplate.opsForValue().get(tokenKey);
        if (value == null) {
            throw new BusinessException("支付宝调试桥接 token 已失效，请返回小程序重新发起支付");
        }
        redisTemplate.delete(tokenKey);

        String orderNo = value.toString().split(":", 2)[0];
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != OrderConstants.STATUS_UNPAID) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_PAID);
        }
        return wrapAutoSubmitHtml(alipayService.createPagePayForm(order, debugReturnUrl));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean handleAlipayNotify(Map<String, String> notifyParams) {
        if (notifyParams == null || notifyParams.isEmpty() || !alipayService.verifyNotify(notifyParams)) {
            return false;
        }
        if (!alipayService.isPaidTrade(notifyParams)) {
            return false;
        }

        String orderNo = notifyParams.get("out_trade_no");
        String tradeNo = notifyParams.get("trade_no");
        String totalAmount = notifyParams.get("total_amount");
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            return false;
        }

        // 支付宝重复通知可能多次到达，已支付订单直接返回成功，避免支付宝持续重试。
        if (order.getStatus() == OrderConstants.STATUS_PAID) {
            return true;
        }
        if (order.getStatus() != OrderConstants.STATUS_UNPAID || !isNotifyAmountMatched(order, totalAmount)) {
            return false;
        }

        int updated = orderMapper.pay(order.getId(), OrderConstants.PAY_TYPE_ALIPAY, tradeNo);
        if (updated <= 0) {
            Order latest = orderMapper.selectByOrderNo(orderNo);
            return latest != null && latest.getStatus() == OrderConstants.STATUS_PAID;
        }

        handlePaySuccessSideEffects(order);
        return true;
    }

    @Override
    public String buildAlipayReturnRedirectUrl(String orderNo) {
        return alipayService.buildReturnRedirectUrl(orderNo);
    }

    /**
     * 支付宝发起阶段只校验订单是否可以支付，不做任何状态修改。
     */
    private void validateOrderCanStartAlipayPay(Order order, Long userId) {
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (order.getStatus() != OrderConstants.STATUS_UNPAID) {
            throw new BusinessException(ResultCode.ORDER_ALREADY_PAID);
        }
        if (order.getPayAmount() == null || order.getPayAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("订单实付金额异常，无法发起支付宝支付");
        }
    }

    /**
     * 支付宝回调金额必须与订单实付金额完全一致，避免串单或篡改金额造成错账。
     */
    private boolean isNotifyAmountMatched(Order order, String totalAmount) {
        if (totalAmount == null || order.getPayAmount() == null) {
            return false;
        }
        try {
            BigDecimal notifyAmount = new BigDecimal(totalAmount).setScale(2, RoundingMode.HALF_UP);
            BigDecimal orderPayAmount = order.getPayAmount().setScale(2, RoundingMode.HALF_UP);
            return orderPayAmount.compareTo(notifyAmount) == 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 只有订单状态成功从待支付切到已支付后，才执行销量、积分和 MQ 消息副作用。
     */
    private void handlePaySuccessSideEffects(Order order) {
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
        for (OrderItem item : items) {
            productService.increaseSales(item.getProductId(), item.getQuantity());
        }

        // 增加用户积分（每消费1元获得1积分）
        int points = order.getPayAmount().intValue();
        userService.updatePoints(order.getUserId(), points);

        // 发送支付成功消息
        if (rabbitTemplate != null) {
            rabbitTemplate.convertAndSend(MQConstants.ORDER_EXCHANGE, 
                    MQConstants.ORDER_PAY_SUCCESS_KEY, order.getOrderNo());
        }
    }

    /**
     * 将支付宝 SDK 生成的表单包进最小 HTML，便于微信小程序 web-view 加载后自动提交。
     */
    private String wrapAutoSubmitHtml(String formHtml) {
        return "<!doctype html><html><head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>正在跳转支付宝沙箱</title></head>"
                + "<body><p style=\"font-family:sans-serif;color:#666;text-align:center;margin-top:48px;\">正在跳转支付宝沙箱...</p>"
                + formHtml
                + "<script>document.addEventListener('DOMContentLoaded',function(){var f=document.forms[0];if(f){f.submit();}});</script>"
                + "</body></html>";
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmReceive(Long userId, String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (order.getStatus() != OrderConstants.STATUS_SHIPPED) {
            throw new BusinessException(ResultCode.ORDER_STATUS_ERROR);
        }

        // 更新订单状态为已收货
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(OrderConstants.STATUS_RECEIVED);
        updateOrder.setReceiveTime(LocalDateTime.now());
        orderMapper.update(updateOrder);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleTimeoutOrders() {
        List<Order> timeoutOrders = orderMapper.selectUnpaidTimeout(OrderConstants.ORDER_TIMEOUT_MINUTES);
        for (Order order : timeoutOrders) {
            try {
                cancelOrder(order.getUserId(), order.getOrderNo());
            } catch (Exception e) {
                // 记录日志，继续处理下一个
            }
        }
    }

    @Override
    public Order getOrderDetail(Long userId, String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        // 查询订单项
        List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
        order.setOrderItems(items);

        return order;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteOrder(Long userId, String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        // 只有已完成或已取消的订单才能删除
        if (order.getStatus() != OrderConstants.STATUS_COMPLETED && 
            order.getStatus() != OrderConstants.STATUS_CANCELLED) {
            throw new BusinessException("只能删除已完成或已取消的订单");
        }

        // 软删除（这里简单处理，实际可以添加delete_flag字段）
        orderMapper.updateStatus(order.getId(), -1);
    }

    @Override
    public PageVO<Order> queryOrders(eden.pojo.dto.OrderQueryDTO queryDTO) {
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }
        
        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        queryDTO.setOffset(offset);

        List<Order> list = orderMapper.selectByCondition(queryDTO);
        long total = orderMapper.countByCondition(queryDTO);

        if (list != null && !list.isEmpty()) {
            for (Order order : list) {
                // 暂时简单循环查询，实际可以用IN查询优化
                List<OrderItem> items = orderItemMapper.selectByOrderId(order.getId());
                order.setOrderItems(items);
            }
        }

        return PageVO.of(list, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (order.getStatus() != OrderConstants.STATUS_PAID) {
            throw new BusinessException("订单状态不正确，无法发货");
        }
        
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setStatus(OrderConstants.STATUS_SHIPPED);
        updateOrder.setShipTime(LocalDateTime.now());
        orderMapper.update(updateOrder);
    }

    // --- 以下为管理员端实现的接口 ---

    @Override
    public PageVO<eden.pojo.vo.OrderAdminVO> getAdminOrderPage(eden.pojo.dto.AdminOrderQueryDTO queryDTO) {
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }
        
        // offset 已经由 PageDTO 提供自动计算，此处无需手动设值，Mybatis 直接调用 getOffset() 即可

        // 使用同样的 mapper，如果需要特定字段可以新建 Mapper 方法，这里复用
        List<Order> list = orderMapper.selectAdminOrderList(queryDTO);
        long total = orderMapper.countAdminOrderList(queryDTO);

        List<eden.pojo.vo.OrderAdminVO> voList = new ArrayList<>();
        if (list != null) {
            for (Order order : list) {
                eden.pojo.vo.OrderAdminVO vo = new eden.pojo.vo.OrderAdminVO();
                org.springframework.beans.BeanUtils.copyProperties(order, vo);
                voList.add(vo);
            }
        }
        return PageVO.of(voList, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public eden.pojo.vo.OrderDetailAdminVO getAdminOrderDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        eden.pojo.vo.OrderDetailAdminVO vo = new eden.pojo.vo.OrderDetailAdminVO();
        org.springframework.beans.BeanUtils.copyProperties(order, vo);
        
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        List<eden.pojo.vo.OrderItemVO> itemVOs = new ArrayList<>();
        if (items != null) {
            for (OrderItem item : items) {
                eden.pojo.vo.OrderItemVO itemVO = new eden.pojo.vo.OrderItemVO();
                org.springframework.beans.BeanUtils.copyProperties(item, itemVO);
                itemVOs.add(itemVO);
            }
        }
        vo.setOrderItemList(itemVOs);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deliverOrder(eden.pojo.dto.OrderDeliverDTO deliverDTO) {
        Order order = orderMapper.selectById(deliverDTO.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        // 只能是待发货状态
        if (order.getStatus() != OrderConstants.STATUS_PAID) {
            throw new BusinessException("订单不是待发货状态，无法发货");
        }
        
        Order updateOrder = new Order();
        updateOrder.setId(order.getId());
        updateOrder.setDeliveryCompany(deliverDTO.getDeliveryCompany());
        updateOrder.setDeliverySn(deliverDTO.getDeliverySn());
        updateOrder.setStatus(OrderConstants.STATUS_SHIPPED);
        updateOrder.setShipTime(LocalDateTime.now());
        
        orderMapper.update(updateOrder);
    }
}
