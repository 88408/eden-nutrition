package eden.service.listener;

import eden.common.constant.MQConstants;
import eden.mapper.ProductMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 库存消息监听器
 */
@Component
public class StockMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(StockMessageListener.class);

    @Autowired
    private ProductMapper productMapper;

    /**
     * 监听库存扣减消息
     */
    @SuppressWarnings("unchecked")
    @RabbitListener(queues = MQConstants.STOCK_DEDUCT_QUEUE)
    public void handleStockDeduct(Map<String, Object> message) {
        Long productId = Long.parseLong(message.get("productId").toString());
        Integer quantity = Integer.parseInt(message.get("quantity").toString());

        logger.info("收到库存扣减消息，商品ID: {}, 数量: {}", productId, quantity);

        try {
            int rows = productMapper.deductStock(productId, quantity);
            if (rows > 0) {
                logger.info("库存扣减成功，商品ID: {}, 数量: {}", productId, quantity);
            } else {
                logger.warn("库存扣减失败，商品ID: {}, 可能库存不足", productId);
                // 可以发送库存不足警告通知
            }
        } catch (Exception e) {
            logger.error("处理库存扣减消息失败，商品ID: {}", productId, e);
        }
    }

    /**
     * 监听库存回滚消息
     */
    @SuppressWarnings("unchecked")
    @RabbitListener(queues = MQConstants.STOCK_ROLLBACK_QUEUE)
    public void handleStockRollback(Map<String, Object> message) {
        Long productId = Long.parseLong(message.get("productId").toString());
        Integer quantity = Integer.parseInt(message.get("quantity").toString());

        logger.info("收到库存回滚消息，商品ID: {}, 数量: {}", productId, quantity);

        try {
            int rows = productMapper.addStock(productId, quantity);
            if (rows > 0) {
                logger.info("库存回滚成功，商品ID: {}, 数量: {}", productId, quantity);
            } else {
                logger.warn("库存回滚失败，商品ID: {}", productId);
            }
        } catch (Exception e) {
            logger.error("处理库存回滚消息失败，商品ID: {}", productId, e);
        }
    }
}
