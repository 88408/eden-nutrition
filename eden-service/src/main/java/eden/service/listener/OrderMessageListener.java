package eden.service.listener;

import eden.common.constant.MQConstants;
import eden.common.constant.OrderConstants;
import eden.mapper.OrderMapper;
import eden.mapper.OrderItemMapper;
import eden.pojo.Order;
import eden.pojo.OrderItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 订单消息监听器
 */
@Component
public class OrderMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(OrderMessageListener.class);

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 监听订单创建消息
     * 用于异步处理订单创建后的后续逻辑（如发送通知等）
     */
    @RabbitListener(queues = MQConstants.ORDER_CREATE_QUEUE)
    public void handleOrderCreate(String orderId) {
        logger.info("收到订单创建消息，订单ID: {}", orderId);
        try {
            // 可以在这里添加订单创建后的异步处理逻辑
            // 如：发送短信通知、邮件通知等
            Order order = orderMapper.selectById(Long.parseLong(orderId));
            if (order != null) {
                logger.info("订单 {} 创建成功，用户ID: {}, 金额: {}", 
                        orderId, order.getUserId(), order.getPayAmount());
            }
        } catch (Exception e) {
            logger.error("处理订单创建消息失败，订单ID: {}", orderId, e);
        }
    }

    /**
     * 监听订单取消消息（延迟队列超时自动取消）
     */
    @RabbitListener(queues = MQConstants.ORDER_CANCEL_QUEUE)
    public void handleOrderCancel(String orderId) {
        logger.info("收到订单取消消息，订单ID: {}", orderId);
        try {
            Order order = orderMapper.selectById(Long.parseLong(orderId));
            if (order == null) {
                logger.warn("订单不存在，订单ID: {}", orderId);
                return;
            }

            // 只有待付款状态的订单才能自动取消
            if (order.getStatus() != OrderConstants.STATUS_PENDING_PAYMENT) {
                logger.info("订单状态不是待付款，不能自动取消，订单ID: {}, 当前状态: {}", 
                        orderId, order.getStatus());
                return;
            }

            // 更新订单状态为已取消
            Order updateOrder = new Order();
            updateOrder.setId(order.getId());
            updateOrder.setStatus(OrderConstants.STATUS_CANCELLED);
            updateOrder.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(updateOrder);

            logger.info("订单自动取消成功，订单ID: {}", orderId);

            // 恢复库存（发送库存回滚消息）
            List<OrderItem> orderItems = orderItemMapper.selectByOrderId(order.getId());
            if (orderItems != null && !orderItems.isEmpty()) {
                for (OrderItem item : orderItems) {
                    Map<String, Object> message = new HashMap<>();
                    message.put("productId", item.getProductId());
                    message.put("quantity", item.getQuantity());

                    rabbitTemplate.convertAndSend(
                            MQConstants.STOCK_EXCHANGE,
                            MQConstants.STOCK_ROLLBACK_ROUTING_KEY,
                            message
                    );
                    logger.info("发送库存回滚消息成功，商品ID: {}, 数量: {}", item.getProductId(), item.getQuantity());
                }
            }

        } catch (Exception e) {
            logger.error("处理订单取消消息失败，订单ID: {}", orderId, e);
        }
    }

    /**
     * 监听订单支付成功消息
     */
    @RabbitListener(queues = MQConstants.ORDER_PAY_SUCCESS_QUEUE)
    public void handleOrderPaySuccess(String orderNo) {
        logger.info("收到订单支付成功消息，订单号: {}", orderNo);
        try {
            Order order = orderMapper.selectByOrderNo(orderNo);
            if (order == null) {
                logger.warn("订单不存在，订单号: {}", orderNo);
                return;
            }

            // 可以在这里添加支付成功后的异步处理逻辑
            // 如：发送支付成功通知、更新销量统计等
            logger.info("订单 {} 支付成功，金额: {}", orderNo, order.getPayAmount());

        } catch (Exception e) {
            logger.error("处理订单支付成功消息失败，订单号: {}", orderNo, e);
        }
    }
}
