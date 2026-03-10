package eden.service.task;

import eden.common.constant.OrderConstants;
import eden.mapper.OrderMapper;
import eden.pojo.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单定时任务
 */
@Component
public class OrderTask {

    private static final Logger logger = LoggerFactory.getLogger(OrderTask.class);

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 定时关闭超时未支付订单
     * 每分钟执行一次，关闭超过30分钟未支付的订单
     */
    @Scheduled(cron = "0 * * * * ?")
    public void closeTimeoutOrders() {
        logger.info("开始执行订单超时关闭任务...");

        try {
            // 查询30分钟前创建的待付款订单
            LocalDateTime timeoutTime = LocalDateTime.now().minusMinutes(30);
            List<Order> timeoutOrders = orderMapper.selectTimeoutOrders(
                    OrderConstants.STATUS_PENDING_PAYMENT, timeoutTime);

            if (timeoutOrders.isEmpty()) {
                logger.info("没有超时订单需要关闭");
                return;
            }

            int closedCount = 0;
            for (Order order : timeoutOrders) {
                try {
                    Order updateOrder = new Order();
                    updateOrder.setId(order.getId());
                    updateOrder.setStatus(OrderConstants.STATUS_CANCELLED);
                    updateOrder.setUpdateTime(LocalDateTime.now());
                    orderMapper.updateById(updateOrder);

                    closedCount++;

                    // TODO: 恢复库存

                    logger.info("订单 {} 超时关闭成功", order.getOrderNo());
                } catch (Exception e) {
                    logger.error("关闭订单 {} 失败", order.getOrderNo(), e);
                }
            }

            logger.info("订单超时关闭任务执行完成，共关闭 {} 个订单", closedCount);

        } catch (Exception e) {
            logger.error("订单超时关闭任务执行失败", e);
        }
    }

    /**
     * 自动确认收货
     * 每天凌晨2点执行，自动确认发货超过14天的订单
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoConfirmOrders() {
        logger.info("开始执行自动确认收货任务...");

        try {
            // 查询发货超过14天的订单
            LocalDateTime autoConfirmTime = LocalDateTime.now().minusDays(14);
            List<Order> orders = orderMapper.selectByStatusAndShippedBefore(
                    OrderConstants.STATUS_SHIPPED, autoConfirmTime);

            if (orders.isEmpty()) {
                logger.info("没有需要自动确认的订单");
                return;
            }

            int confirmedCount = 0;
            for (Order order : orders) {
                try {
                    Order updateOrder = new Order();
                    updateOrder.setId(order.getId());
                    updateOrder.setStatus(OrderConstants.STATUS_COMPLETED);
                    updateOrder.setUpdateTime(LocalDateTime.now());
                    orderMapper.updateById(updateOrder);

                    confirmedCount++;
                    logger.info("订单 {} 自动确认收货成功", order.getOrderNo());
                } catch (Exception e) {
                    logger.error("自动确认订单 {} 失败", order.getOrderNo(), e);
                }
            }

            logger.info("自动确认收货任务执行完成，共确认 {} 个订单", confirmedCount);

        } catch (Exception e) {
            logger.error("自动确认收货任务执行失败", e);
        }
    }
}
