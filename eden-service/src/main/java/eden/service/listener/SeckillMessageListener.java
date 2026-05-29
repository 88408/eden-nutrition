package eden.service.listener;

import eden.common.constant.MQConstants;
import eden.common.constant.OrderConstants;
import eden.common.constant.RedisConstants;
import eden.common.exception.BusinessException;
import eden.mapper.OrderItemMapper;
import eden.mapper.OrderMapper;
import eden.mapper.ProductMapper;
import eden.mapper.SeckillMapper;
import eden.mapper.SeckillOrderMapper;
import eden.mapper.UserAddressMapper;
import eden.pojo.Order;
import eden.pojo.OrderItem;
import eden.pojo.Product;
import eden.pojo.SeckillOrder;
import eden.pojo.SeckillProduct;
import eden.pojo.UserAddress;
import eden.pojo.dto.SeckillOrderMessage;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀消息监听器
 */
@Component
public class SeckillMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(SeckillMessageListener.class);

    @Autowired
    private SeckillMapper seckillMapper;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 监听秒杀订单消息，异步创建订单。
     */
    @Transactional(rollbackFor = Exception.class)
    @RabbitListener(queues = MQConstants.SECKILL_ORDER_QUEUE)
    public void handleSeckillOrderMessage(SeckillOrderMessage message, Channel channel, Message amqpMessage) throws IOException {
        handleSeckillOrder(message);
        channel.basicAck(amqpMessage.getMessageProperties().getDeliveryTag(), false);
    }

    public void handleSeckillOrder(SeckillOrderMessage message) {
        Long userId = message.getUserId();
        Long seckillId = message.getSeckillId();
        Integer quantity = message.getQuantity() == null ? 1 : message.getQuantity();
        String orderNo = message.getOrderNo();
        String resultKey = RedisConstants.SECKILL_RESULT + orderNo;

        logger.info("收到秒杀订单消息，订单号: {}, 用户ID: {}, 秒杀ID: {}, 数量: {}", orderNo, userId, seckillId, quantity);

        try {
            Order existingOrder = orderMapper.selectByOrderNo(orderNo);
            if (existingOrder != null) {
                writeResult(resultKey, "SUCCESS:" + orderNo);
                return;
            }

            SeckillOrder existingSeckillOrder = seckillOrderMapper.selectByUserAndSeckill(userId, seckillId);
            if (existingSeckillOrder != null) {
                rollbackRedisStock(seckillId, quantity);
                writeResult(resultKey, "FAILED:您已经参与过该秒杀");
                return;
            }

            SeckillProduct seckill = seckillMapper.selectById(seckillId);
            if (seckill == null) {
                failAndRollbackRedis(message, quantity, "秒杀活动不存在", true);
                return;
            }

            UserAddress address = userAddressMapper.selectById(message.getAddressId());
            if (address == null || !userId.equals(address.getUserId())) {
                failAndRollbackRedis(message, quantity, "收货地址不存在", true);
                return;
            }

            Product product = productMapper.selectById(seckill.getProductId());
            if (product == null) {
                failAndRollbackRedis(message, quantity, "商品不存在", true);
                return;
            }

            int rows = seckillMapper.deductStock(seckillId, quantity);
            if (rows <= 0) {
                failAndRollbackRedis(message, quantity, "库存不足", true);
                return;
            }

            Order order = buildOrder(message, seckill, address, quantity);
            orderMapper.insert(order);

            OrderItem orderItem = buildOrderItem(order, product, seckill, quantity);
            orderItemMapper.insert(orderItem);

            SeckillOrder seckillOrder = new SeckillOrder();
            seckillOrder.setUserId(userId);
            seckillOrder.setOrderId(order.getId());
            seckillOrder.setSeckillId(seckillId);
            seckillOrder.setProductId(seckill.getProductId());
            seckillOrderMapper.insert(seckillOrder);

            writeResult(resultKey, "SUCCESS:" + orderNo);
            logger.info("秒杀订单创建成功，订单号: {}, 用户ID: {}", orderNo, userId);
        } catch (BusinessException e) {
            markTransactionRollbackOnly();
            failAndRollbackRedis(message, quantity, e.getMessage(), true);
        } catch (Exception e) {
            logger.error("处理秒杀订单消息失败，订单号: {}, 用户ID: {}, 秒杀ID: {}", orderNo, userId, seckillId, e);
            markTransactionRollbackOnly();
            failAndRollbackRedis(message, quantity, "系统错误", true);
        }
    }

    private Order buildOrder(SeckillOrderMessage message, SeckillProduct seckill, UserAddress address, Integer quantity) {
        BigDecimal amount = seckill.getSeckillPrice().multiply(BigDecimal.valueOf(quantity));
        Order order = new Order();
        order.setOrderNo(message.getOrderNo());
        order.setUserId(message.getUserId());
        order.setTotalAmount(amount);
        order.setPayAmount(amount);
        order.setFreightAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setStatus(OrderConstants.STATUS_UNPAID);
        order.setOrderType(OrderConstants.TYPE_SECKILL);
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(formatAddress(address));
        order.setRemark("秒杀订单");
        return order;
    }

    private OrderItem buildOrderItem(Order order, Product product, SeckillProduct seckill, Integer quantity) {
        OrderItem item = new OrderItem();
        item.setOrderId(order.getId());
        item.setOrderNo(order.getOrderNo());
        item.setProductId(seckill.getProductId());
        item.setProductName(product.getName());
        item.setProductImage(product.getMainImage());
        item.setPrice(seckill.getSeckillPrice());
        item.setQuantity(quantity);
        item.setTotalPrice(seckill.getSeckillPrice().multiply(BigDecimal.valueOf(quantity)));
        return item;
    }

    private String formatAddress(UserAddress address) {
        return safe(address.getProvince()) + safe(address.getCity()) + safe(address.getDistrict()) + safe(address.getDetailAddress());
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private void failAndRollbackRedis(SeckillOrderMessage message, Integer quantity, String reason, boolean removeUserMark) {
        rollbackRedisStock(message.getSeckillId(), quantity);
        if (removeUserMark) {
            redisTemplate.opsForSet().remove(RedisConstants.SECKILL_USER + message.getSeckillId(), message.getUserId().toString());
        }
        writeResult(RedisConstants.SECKILL_RESULT + message.getOrderNo(), "FAILED:" + reason);
    }

    private void rollbackRedisStock(Long seckillId, Integer quantity) {
        redisTemplate.opsForValue().increment(RedisConstants.SECKILL_STOCK + seckillId, quantity);
    }

    private void writeResult(String resultKey, String result) {
        redisTemplate.opsForValue().set(resultKey, result, RedisConstants.EXPIRE_SECKILL_RESULT, TimeUnit.SECONDS);
    }

    private void markTransactionRollbackOnly() {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
        }
    }
}
