package eden.service.impl;

import eden.common.constant.MQConstants;
import eden.common.constant.RedisConstants;
import eden.common.exception.BusinessException;
import eden.common.result.ResultCode;
import eden.common.utils.IdGenerator;
import eden.mapper.SeckillOrderMapper;
import eden.mapper.SeckillProductMapper;
import eden.pojo.SeckillOrder;
import eden.pojo.SeckillProduct;
import eden.pojo.dto.SeckillDTO;
import eden.service.SeckillService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 秒杀服务实现类
 */
@Service
public class SeckillServiceImpl implements SeckillService {

    @Autowired
    private SeckillProductMapper seckillProductMapper;

    @Autowired
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    /** Lua脚本：原子性扣减库存并标记用户 */
    private static final String SECKILL_SCRIPT = 
            "local stock = redis.call('get', KEYS[1]) " +
            "if stock == false or tonumber(stock) <= 0 then " +
            "   return -1 " +
            "end " +
            "local userKey = KEYS[2] " +
            "if redis.call('sismember', userKey, ARGV[1]) == 1 then " +
            "   return -2 " +
            "end " +
            "redis.call('decr', KEYS[1]) " +
            "redis.call('sadd', userKey, ARGV[1]) " +
            "return 1";

    @Override
    public List<SeckillProduct> getOngoingSeckills() {
        return seckillProductMapper.selectOngoing();
    }

    @Override
    public List<SeckillProduct> getUpcomingSeckills() {
        return seckillProductMapper.selectUpcoming();
    }

    @Override
    public String doSeckill(Long userId, SeckillDTO seckillDTO) {
        Long seckillId = seckillDTO.getSeckillId();

        // 1. 获取秒杀商品信息
        SeckillProduct seckillProduct = getSeckillDetail(seckillId);
        
        // 2. 检查秒杀时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(seckillProduct.getStartTime())) {
            throw new BusinessException(ResultCode.SECKILL_NOT_START);
        }
        if (now.isAfter(seckillProduct.getEndTime())) {
            throw new BusinessException(ResultCode.SECKILL_ENDED);
        }

        // 3. 使用Lua脚本原子性操作
        String stockKey = RedisConstants.SECKILL_STOCK + seckillId;
        String userKey = RedisConstants.SECKILL_USER + seckillId;

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(SECKILL_SCRIPT);
        script.setResultType(Long.class);

        Long result = redisTemplate.execute(script, 
                List.of(stockKey, userKey), 
                userId.toString());

        if (result == null) {
            throw new BusinessException("秒杀失败，请重试");
        }

        if (result == -1) {
            throw new BusinessException(ResultCode.SECKILL_SOLD_OUT);
        }

        if (result == -2) {
            throw new BusinessException(ResultCode.SECKILL_REPEATED);
        }

        // 4. 秒杀成功，生成订单号
        String orderNo = IdGenerator.generateOrderNo();

        // 5. 发送消息到MQ，异步创建订单
        if (rabbitTemplate != null) {
            SeckillMessage message = new SeckillMessage();
            message.setUserId(userId);
            message.setSeckillId(seckillId);
            message.setProductId(seckillProduct.getProductId());
            message.setSeckillPrice(seckillProduct.getSeckillPrice());
            message.setOrderNo(orderNo);

            rabbitTemplate.convertAndSend(MQConstants.SECKILL_EXCHANGE, 
                    MQConstants.SECKILL_ORDER_KEY, message);
        } else {
            // 没有MQ，同步创建秒杀订单记录
            createSeckillOrder(userId, seckillId, null);
        }

        return orderNo;
    }

    @Override
    public SeckillProduct getSeckillDetail(Long seckillId) {
        SeckillProduct seckillProduct = seckillProductMapper.selectById(seckillId);
        if (seckillProduct == null) {
            throw new BusinessException("秒杀活动不存在");
        }
        if (seckillProduct.getStatus() != 1) {
            throw new BusinessException("秒杀活动未开启");
        }
        return seckillProduct;
    }

    @Override
    public boolean hasKilled(Long userId, Long seckillId) {
        // 先检查Redis
        String userKey = RedisConstants.SECKILL_USER + seckillId;
        Boolean isMember = redisTemplate.opsForSet().isMember(userKey, userId.toString());
        if (isMember != null && isMember) {
            return true;
        }

        // 再检查数据库
        SeckillOrder seckillOrder = seckillOrderMapper.selectByUserAndSeckill(userId, seckillId);
        return seckillOrder != null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(SeckillProduct seckillProduct) {
        if (seckillProduct.getStatus() == null) {
            seckillProduct.setStatus(0); // 默认未开启
        }
        seckillProductMapper.insert(seckillProduct);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(SeckillProduct seckillProduct) {
        seckillProductMapper.update(seckillProduct);

        // 如果更新了库存，同步到Redis
        if (seckillProduct.getStock() != null) {
            String stockKey = RedisConstants.SECKILL_STOCK + seckillProduct.getId();
            redisTemplate.opsForValue().set(stockKey, seckillProduct.getStock());
        }
    }

    @Override
    public void initSeckillStock() {
        // 获取所有进行中和即将开始的秒杀活动
        List<SeckillProduct> ongoingList = seckillProductMapper.selectOngoing();
        List<SeckillProduct> upcomingList = seckillProductMapper.selectUpcoming();

        // 初始化库存到Redis
        for (SeckillProduct sp : ongoingList) {
            initStock(sp);
        }
        for (SeckillProduct sp : upcomingList) {
            initStock(sp);
        }
    }

    private void initStock(SeckillProduct seckillProduct) {
        String stockKey = RedisConstants.SECKILL_STOCK + seckillProduct.getId();
        // 只有不存在时才初始化
        if (Boolean.FALSE.equals(redisTemplate.hasKey(stockKey))) {
            redisTemplate.opsForValue().set(stockKey, seckillProduct.getStock());
            
            // 设置过期时间为活动结束时间+1小时
            long seconds = java.time.Duration.between(LocalDateTime.now(), 
                    seckillProduct.getEndTime().plusHours(1)).getSeconds();
            if (seconds > 0) {
                redisTemplate.expire(stockKey, seconds, TimeUnit.SECONDS);
            }
        }
    }

    /**
     * 创建秒杀订单记录（内部方法）
     */
    @Transactional(rollbackFor = Exception.class)
    public void createSeckillOrder(Long userId, Long seckillId, Long orderId) {
        // 数据库扣减库存
        int rows = seckillProductMapper.decreaseStock(seckillId);
        if (rows == 0) {
            throw new BusinessException(ResultCode.SECKILL_SOLD_OUT);
        }

        // 创建秒杀订单记录
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(userId);
        seckillOrder.setSeckillId(seckillId);
        seckillOrder.setOrderId(orderId);
        seckillOrderMapper.insert(seckillOrder);
    }

    /**
     * 秒杀消息对象
     */
    public static class SeckillMessage implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        
        private Long userId;
        private Long seckillId;
        private Long productId;
        private java.math.BigDecimal seckillPrice;
        private String orderNo;

        // Getters and Setters
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public Long getSeckillId() { return seckillId; }
        public void setSeckillId(Long seckillId) { this.seckillId = seckillId; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public java.math.BigDecimal getSeckillPrice() { return seckillPrice; }
        public void setSeckillPrice(java.math.BigDecimal seckillPrice) { this.seckillPrice = seckillPrice; }
        public String getOrderNo() { return orderNo; }
        public void setOrderNo(String orderNo) { this.orderNo = orderNo; }
    }
}
