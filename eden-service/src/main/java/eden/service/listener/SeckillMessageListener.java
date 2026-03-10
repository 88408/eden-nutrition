package eden.service.listener;

import eden.common.constant.MQConstants;
import eden.common.constant.OrderConstants;
import eden.common.utils.IdGenerator;
import eden.mapper.OrderMapper;
import eden.mapper.SeckillMapper;
import eden.pojo.Order;
import eden.pojo.SeckillProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 秒杀消息监听器
 */
@Component
public class SeckillMessageListener {

    private static final Logger logger = LoggerFactory.getLogger(SeckillMessageListener.class);

    @Autowired
    private SeckillMapper seckillMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String SECKILL_RESULT_KEY = "seckill:result:";

    /**
     * 监听秒杀订单消息，异步创建订单
     */
    @SuppressWarnings("unchecked")
    @RabbitListener(queues = MQConstants.SECKILL_ORDER_QUEUE)
    public void handleSeckillOrder(Map<String, Object> message) {
        Long userId = Long.parseLong(message.get("userId").toString());
        Long seckillId = Long.parseLong(message.get("seckillId").toString());
        Integer quantity = Integer.parseInt(message.get("quantity").toString());

        String resultKey = SECKILL_RESULT_KEY + seckillId + ":" + userId;

        logger.info("收到秒杀订单消息，用户ID: {}, 秒杀ID: {}, 数量: {}", userId, seckillId, quantity);

        try {
            // 查询秒杀活动
            SeckillProduct seckill = seckillMapper.selectById(seckillId);
            if (seckill == null) {
                logger.error("秒杀活动不存在，秒杀ID: {}", seckillId);
                redisTemplate.opsForValue().set(resultKey, "FAILED:秒杀活动不存在");
                return;
            }

            // 扣减数据库库存
            int rows = seckillMapper.deductStock(seckillId, quantity);
            if (rows <= 0) {
                logger.warn("秒杀库存扣减失败，秒杀ID: {}", seckillId);
                redisTemplate.opsForValue().set(resultKey, "FAILED:库存不足");
                return;
            }

            // 创建秒杀订单
            Order order = new Order();
            order.setId(IdGenerator.nextId());
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setTotalAmount(seckill.getSeckillPrice().multiply(new BigDecimal(quantity)));
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setPayAmount(order.getTotalAmount());
            order.setStatus(OrderConstants.STATUS_PENDING_PAYMENT);
            order.setPaymentMethod(""); // 未选择
            order.setOrderType(OrderConstants.TYPE_SECKILL);
            order.setRemark("秒杀订单");
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());

            orderMapper.insert(order);

            // 设置秒杀结果
            redisTemplate.opsForValue().set(resultKey, "SUCCESS:" + order.getId());

            logger.info("秒杀订单创建成功，订单ID: {}, 用户ID: {}", order.getId(), userId);

        } catch (Exception e) {
            logger.error("处理秒杀订单消息失败，用户ID: {}, 秒杀ID: {}", userId, seckillId, e);
            redisTemplate.opsForValue().set(resultKey, "FAILED:系统错误");
            
            // 回滚Redis库存
            String stockKey = "seckill:stock:" + seckillId;
            redisTemplate.opsForValue().increment(stockKey, quantity);
        }
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo() {
        return "SK" + System.currentTimeMillis() + 
                String.format("%04d", (int) (Math.random() * 10000));
    }
}
