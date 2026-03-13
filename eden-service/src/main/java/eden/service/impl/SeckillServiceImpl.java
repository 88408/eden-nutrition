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
import eden.pojo.dto.SeckillSessionDTO;
import eden.service.SeckillService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
    public List<SeckillSessionDTO> getSeckillSessions() {
        // Fetch all relevant seckill products
        List<SeckillProduct> ongoing = getOngoingSeckills();
        List<SeckillProduct> upcoming = getUpcomingSeckills();
        
        List<SeckillProduct> all = new ArrayList<>();
        if (ongoing != null) all.addAll(ongoing);
        if (upcoming != null) all.addAll(upcoming);
        
        if (all.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Group by startTime
        Map<LocalDateTime, List<SeckillProduct>> grouped = all.stream()
            .collect(Collectors.groupingBy(SeckillProduct::getStartTime));
            
        LocalDateTime now = LocalDateTime.now();
        
        // Convert to DTOs
        List<SeckillSessionDTO> sessions = grouped.entrySet().stream()
            .map(entry -> {
                SeckillSessionDTO dto = new SeckillSessionDTO();
                dto.setStartTime(entry.getKey());
                dto.setProducts(entry.getValue());
                
                // Determine end time (max of products in this slot, usually same)
                if (!entry.getValue().isEmpty()) {
                    dto.setEndTime(entry.getValue().get(0).getEndTime());
                }
                
                // Determine status
                if (now.isAfter(dto.getEndTime())) {
                    dto.setStatus(2); // Ended
                } else if (now.isBefore(dto.getStartTime())) {
                    dto.setStatus(0); // Upcoming
                } else {
                    dto.setStatus(1); // Ongoing
                }
                
                return dto;
            })
            .sorted(Comparator.comparing(SeckillSessionDTO::getStartTime))
            .collect(Collectors.toList());
            
        return sessions;
    }

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
            throw new BusinessException(ResultCode.SECKILL_NO_STOCK);
        }
        if (result == -2) {
            throw new BusinessException(ResultCode.SECKILL_REPEAT);
        }

        // 4. 创建订单消息，发送MQ
        String orderNo = IdGenerator.generateOrderNo();
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setUserId(userId);
        seckillOrder.setSeckillId(seckillId);
        seckillOrder.setOrderNo(orderNo);
        seckillOrder.setAmount(seckillProduct.getSeckillPrice());
        seckillOrder.setStatus(0); // 未支付
        seckillOrder.setCreateTime(LocalDateTime.now());

        if (rabbitTemplate != null) {
            rabbitTemplate.convertAndSend(MQConstants.SECKILL_EXCHANGE, MQConstants.SECKILL_ROUTING_KEY, seckillOrder);
        } else {
            // Fallback if MQ not available (just correct logic placeholder)
             seckillOrderMapper.insert(seckillOrder);
        }
        
        return orderNo;
    }

    @Override
    public SeckillProduct getSeckillDetail(Long seckillId) {
        return seckillProductMapper.selectById(seckillId);
    }

    @Override
    public boolean hasKilled(Long userId, Long seckillId) {
        // Check redis set first
        String userKey = RedisConstants.SECKILL_USER + seckillId;
        Boolean member = redisTemplate.opsForSet().isMember(userKey, userId.toString());
        if (Boolean.TRUE.equals(member)) {
            return true;
        }
        // Double check DB?
        return false;
    }

    @Override
    public void add(SeckillProduct seckillProduct) {
        seckillProduct.setCreateTime(LocalDateTime.now());
        seckillProduct.setUpdateTime(LocalDateTime.now());
        seckillProduct.setStatus(0); // Default Not Started
        seckillProductMapper.insert(seckillProduct);
    }

    @Override
    public void update(SeckillProduct seckillProduct) {
        seckillProduct.setUpdateTime(LocalDateTime.now());
        seckillProductMapper.update(seckillProduct);
    }

    @Override
    public void initSeckillStock() {
        // Logic to preheat stock to Redis
        // For simplicity, just load all products
        List<SeckillProduct> products = seckillProductMapper.selectUpcoming(); // Or query all valid
        for (SeckillProduct p : products) {
            redisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK + p.getId(), p.getStockCount());
        }
    }
}
