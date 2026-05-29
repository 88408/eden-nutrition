package eden.service.impl;

import eden.common.constant.MQConstants;
import eden.common.constant.RedisConstants;
import eden.common.exception.BusinessException;
import eden.common.result.ResultCode;
import eden.common.utils.IdGenerator;
import eden.mapper.OrderMapper;
import eden.mapper.SeckillOrderMapper;
import eden.pojo.Order;
import eden.pojo.SeckillProduct;
import eden.pojo.UserAddress;
import eden.pojo.dto.SeckillDTO;
import eden.pojo.dto.SeckillOrderMessage;
import eden.pojo.dto.SeckillSessionDTO;
import eden.service.SeckillService;
import eden.service.UserAddressService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import eden.mapper.SeckillMapper;
import eden.pojo.dto.AdminSeckillQueryDTO;
import eden.pojo.dto.AdminSeckillSaveDTO;
import eden.pojo.vo.AdminSeckillVO;
import eden.pojo.vo.PageVO;
import eden.pojo.vo.SeckillResultVO;
import eden.pojo.vo.SeckillSubmitVO;
import org.springframework.beans.BeanUtils;
import java.time.Duration;
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
    private SeckillOrderMapper seckillOrderMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SeckillMapper seckillMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired(required = false)
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UserAddressService userAddressService;

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
        return seckillMapper.selectOngoing(LocalDateTime.now());
    }

    @Override
    public List<SeckillProduct> getUpcomingSeckills() {
        return seckillMapper.selectUpcoming(LocalDateTime.now(), 24);
    }

    @Override
    public SeckillSubmitVO doSeckill(Long userId, SeckillDTO seckillDTO) {
        Long seckillId = seckillDTO.getSeckillId();
        validateSeckillAddress(userId, seckillDTO.getAddressId());

        // 1. 获取秒杀商品信息
        SeckillProduct seckillProduct = getSeckillDetail(seckillId);
        if (seckillProduct == null) {
            throw new BusinessException("秒杀活动不存在");
        }
        
        // 2. 检查秒杀时间
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(seckillProduct.getStartTime())) {
            throw new BusinessException(ResultCode.SECKILL_NOT_START);
        }
        if (now.isAfter(seckillProduct.getEndTime())) {
            throw new BusinessException(ResultCode.SECKILL_ENDED);
        }
        if (seckillProduct.getStatus() != null && seckillProduct.getStatus() == SeckillProduct.STATUS_ENDED) {
            throw new BusinessException(ResultCode.SECKILL_ENDED);
        }
        if (seckillProduct.getStatus() != null && seckillProduct.getStatus() == SeckillProduct.STATUS_NOT_STARTED) {
            // 时间窗口已经生效但定时任务尚未刷新状态时，在下单入口做一次轻量兜底，避免用户到点后仍无法秒杀。
            seckillMapper.updateStartedSeckills(now);
            seckillProduct.setStatus(SeckillProduct.STATUS_ONGOING);
            preheatSeckillStockCache(seckillProduct);
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

        // 4. 发送异步下单消息，真实订单由 MQ 消费成功后创建。
        String orderNo = IdGenerator.generateOrderNo();
        SeckillOrderMessage message = new SeckillOrderMessage();
        message.setOrderNo(orderNo);
        message.setUserId(userId);
        message.setSeckillId(seckillId);
        message.setAddressId(seckillDTO.getAddressId());
        message.setQuantity(1);

        String pendingKey = RedisConstants.SECKILL_PENDING + orderNo;
        String resultKey = RedisConstants.SECKILL_RESULT + orderNo;
        redisTemplate.delete(resultKey);
        redisTemplate.opsForValue().set(pendingKey, userId.toString(), RedisConstants.EXPIRE_SECKILL_RESULT, TimeUnit.SECONDS);

        try {
            if (rabbitTemplate == null) {
                throw new BusinessException("秒杀队列不可用");
            }
            rabbitTemplate.convertAndSend(MQConstants.SECKILL_EXCHANGE, MQConstants.SECKILL_ORDER_ROUTING_KEY, message);
        } catch (Exception e) {
            rollbackRedisSeckillMark(stockKey, userKey, userId);
            redisTemplate.delete(pendingKey);
            throw new BusinessException("秒杀请求提交失败，请重试");
        }

        SeckillSubmitVO vo = new SeckillSubmitVO();
        vo.setOrderNo(orderNo);
        vo.setStatus("PROCESSING");
        return vo;
    }

    @Override
    public SeckillResultVO getSeckillResult(Long userId, String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order != null) {
            if (!userId.equals(order.getUserId())) {
                throw new BusinessException(ResultCode.FORBIDDEN);
            }
            return buildSeckillResult("SUCCESS", orderNo, "秒杀订单已创建");
        }

        String pendingKey = RedisConstants.SECKILL_PENDING + orderNo;
        Object pendingUser = redisTemplate.opsForValue().get(pendingKey);
        if (pendingUser == null || !userId.toString().equals(pendingUser.toString())) {
            return buildSeckillResult("FAILED", orderNo, "秒杀请求不存在或已过期");
        }

        Object rawResult = redisTemplate.opsForValue().get(RedisConstants.SECKILL_RESULT + orderNo);
        if (rawResult == null) {
            return buildSeckillResult("PROCESSING", orderNo, "秒杀订单处理中");
        }

        String result = rawResult.toString();
        if (result.startsWith("SUCCESS:")) {
            return buildSeckillResult("SUCCESS", orderNo, "秒杀订单已创建");
        }
        if (result.startsWith("FAILED:")) {
            return buildSeckillResult("FAILED", orderNo, result.substring("FAILED:".length()));
        }
        return buildSeckillResult("PROCESSING", orderNo, "秒杀订单处理中");
    }

    private SeckillResultVO buildSeckillResult(String status, String orderNo, String message) {
        SeckillResultVO vo = new SeckillResultVO();
        vo.setStatus(status);
        vo.setOrderNo(orderNo);
        vo.setMessage(message);
        return vo;
    }

    private void rollbackRedisSeckillMark(String stockKey, String userKey, Long userId) {
        redisTemplate.opsForValue().increment(stockKey);
        redisTemplate.opsForSet().remove(userKey, userId.toString());
    }

    /**
     * 秒杀订单会直接进入下单链路，必须在服务端确认收货地址属于当前用户。
     */
    private void validateSeckillAddress(Long userId, Long addressId) {
        UserAddress address = userAddressService.getById(addressId);
        if (address == null || !userId.equals(address.getUserId())) {
            throw new BusinessException(ResultCode.ADDRESS_NOT_FOUND);
        }
    }

    @Override
    public SeckillProduct getSeckillDetail(Long seckillId) {
        return seckillMapper.selectById(seckillId);
    }

    @Override
    public boolean hasKilled(Long userId, Long seckillId) {
        // Check redis set first
        String userKey = RedisConstants.SECKILL_USER + seckillId;
        Boolean member = redisTemplate.opsForSet().isMember(userKey, userId.toString());
        if (Boolean.TRUE.equals(member)) {
            return true;
        }
        return seckillOrderMapper.selectByUserAndSeckill(userId, seckillId) != null;
    }

    @Override
    public void add(SeckillProduct seckillProduct) {
        prepareSeckillForSave(seckillProduct);
        seckillMapper.insert(seckillProduct);
        syncSeckillStockCache(seckillProduct);
    }

    @Override
    public void update(SeckillProduct seckillProduct) {
        prepareSeckillForUpdate(seckillProduct);
        seckillMapper.update(seckillProduct);
        syncSeckillStockCache(seckillProduct);
    }

    @Override
    public void initSeckillStock() {
        LocalDateTime now = LocalDateTime.now();
        // 兼容旧的全量发布/预热入口：只预热当天仍可能参与秒杀的活动，过期活动不再写入 Redis。
        List<SeckillProduct> products = seckillMapper.selectByTimeRange(
                now.withHour(0).withMinute(0).withSecond(0).withNano(0),
                now.plusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0));
        for (SeckillProduct product : products) {
            preheatSeckillStockCache(product);
        }
    }

    @Override
    public void initSeckillStock(Long id) {
        SeckillProduct product = seckillMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("秒杀活动不存在");
        }
        preheatSeckillStockCache(product);
    }

    /**
     * 新增秒杀活动前统一校验时间、补齐库存兼容字段，并按当前时间确定初始状态。
     * 如果活动已经进入时间窗口，会直接保存为进行中，随后由调用方同步 Redis 库存。
     */
    private void prepareSeckillForSave(SeckillProduct seckillProduct) {
        validateSeckillTime(seckillProduct.getStartTime(), seckillProduct.getEndTime());
        LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(seckillProduct.getEndTime())) {
            throw new BusinessException("秒杀活动结束时间必须晚于当前时间");
        }
        seckillProduct.setCreateTime(now);
        seckillProduct.setUpdateTime(now);
        syncStockFields(seckillProduct);
        seckillProduct.setStatus(resolveSeckillStatus(seckillProduct.getStartTime(), seckillProduct.getEndTime(), now));
    }

    /**
     * 修改秒杀活动前合并原记录与本次入参，确保局部更新时仍能重新计算完整生命周期状态。
     * 这样活动被改到未来时间会清理缓存，被改到当前时间窗口会立即预热库存。
     */
    private void prepareSeckillForUpdate(SeckillProduct seckillProduct) {
        if (seckillProduct.getId() == null) {
            throw new BusinessException("秒杀活动ID不能为空");
        }
        SeckillProduct existing = seckillMapper.selectById(seckillProduct.getId());
        if (existing == null) {
            throw new BusinessException("秒杀活动不存在");
        }

        LocalDateTime startTime = seckillProduct.getStartTime() != null ? seckillProduct.getStartTime() : existing.getStartTime();
        LocalDateTime endTime = seckillProduct.getEndTime() != null ? seckillProduct.getEndTime() : existing.getEndTime();
        validateSeckillTime(startTime, endTime);

        LocalDateTime now = LocalDateTime.now();
        seckillProduct.setStartTime(startTime);
        seckillProduct.setEndTime(endTime);
        if (seckillProduct.getStock() == null && seckillProduct.getStockCount() == null) {
            seckillProduct.setStock(existing.getStock());
        }
        syncStockFields(seckillProduct);
        seckillProduct.setStatus(resolveSeckillStatus(startTime, endTime, now));
        seckillProduct.setUpdateTime(now);
    }

    /**
     * 校验秒杀活动时间边界，保证开始和结束时间完整且结束时间晚于开始时间。
     */
    private void validateSeckillTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null || endTime == null) {
            throw new BusinessException("秒杀活动开始时间和结束时间不能为空");
        }
        if (!endTime.isAfter(startTime)) {
            throw new BusinessException("秒杀活动结束时间必须晚于开始时间");
        }
    }

    /**
     * 根据当前时间计算活动状态：未开始、进行中或已结束，统一服务层与定时任务的状态语义。
     */
    private int resolveSeckillStatus(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime now) {
        if (!now.isBefore(endTime)) {
            return SeckillProduct.STATUS_ENDED;
        }
        if (now.isBefore(startTime)) {
            return SeckillProduct.STATUS_NOT_STARTED;
        }
        return SeckillProduct.STATUS_ONGOING;
    }

    /**
     * 兼容前后端历史字段命名差异，将 stock 与 stockCount 双向补齐后再写库或写缓存。
     */
    private void syncStockFields(SeckillProduct seckillProduct) {
        if (seckillProduct.getStock() == null && seckillProduct.getStockCount() != null) {
            seckillProduct.setStock(seckillProduct.getStockCount());
        }
        if (seckillProduct.getStockCount() == null && seckillProduct.getStock() != null) {
            seckillProduct.setStockCount(seckillProduct.getStock());
        }
    }

    /**
     * 根据活动状态同步 Redis 秒杀库存：进行中活动预热库存，非进行中活动清理库存 key。
     */
    private void syncSeckillStockCache(SeckillProduct seckillProduct) {
        if (seckillProduct.getStatus() != null && seckillProduct.getStatus() == SeckillProduct.STATUS_ONGOING) {
            preheatSeckillStockCache(seckillProduct);
            return;
        }
        if (seckillProduct.getId() != null) {
            redisTemplate.delete(RedisConstants.SECKILL_STOCK + seckillProduct.getId());
        }
    }

    /**
     * 将秒杀库存写入 Redis，并把过期时间设置到活动结束后一小时，给异步订单处理保留缓冲窗口。
     */
    private void preheatSeckillStockCache(SeckillProduct seckillProduct) {
        if (seckillProduct == null || seckillProduct.getId() == null || seckillProduct.getEndTime() == null) {
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(seckillProduct.getEndTime())) {
            redisTemplate.delete(RedisConstants.SECKILL_STOCK + seckillProduct.getId());
            return;
        }
        syncStockFields(seckillProduct);
        Integer stock = seckillProduct.getStock();
        if (stock == null) {
            return;
        }
        long expireSeconds = Duration.between(now, seckillProduct.getEndTime().plusHours(1)).getSeconds();
        if (expireSeconds > 0) {
            redisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK + seckillProduct.getId(), stock, expireSeconds, java.util.concurrent.TimeUnit.SECONDS);
        }
    }

    // ==================== B端后台管理方法 ====================

    @Override
    public PageVO<AdminSeckillVO> getAdminPage(AdminSeckillQueryDTO queryDTO) {
        List<AdminSeckillVO> list = seckillMapper.selectAdminPage(queryDTO.getProductId(), queryDTO.getStatus(), queryDTO.getOffset(), queryDTO.getPageSize());
        long total = seckillMapper.countAdminPage(queryDTO.getProductId(), queryDTO.getStatus());
        return PageVO.of(list, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public AdminSeckillVO getAdminDetail(Long id) {
        SeckillProduct product = seckillMapper.selectById(id);
        if (product == null) {
            return null;
        }
        AdminSeckillVO vo = new AdminSeckillVO();
        BeanUtils.copyProperties(product, vo);
        return vo;
    }

    @Override
    public void addAdminSeckill(AdminSeckillSaveDTO dto) {
        // 校验排期重叠
        int count = seckillMapper.countOverlappingSeckill(
                dto.getProductId(),
                dto.getStartTime(),
                dto.getEndTime(),
                null
        );
        if (count > 0) {
            throw new BusinessException("该商品在所选时间段内已有秒杀活动，排期重叠");
        }

        SeckillProduct sp = new SeckillProduct();
        BeanUtils.copyProperties(dto, sp);
        sp.setStock(dto.getStockCount());
        prepareSeckillForSave(sp);
        seckillMapper.insert(sp);
        syncSeckillStockCache(sp);
    }

    @Override
    public void updateAdminSeckill(AdminSeckillSaveDTO dto) {
        // 校验排期重叠（排除自身）
        int count = seckillMapper.countOverlappingSeckill(
                dto.getProductId(),
                dto.getStartTime(),
                dto.getEndTime(),
                dto.getId()
        );
        if (count > 0) {
            throw new BusinessException("该商品在所选时间段内已有秒杀活动，排期重叠");
        }

        SeckillProduct sp = new SeckillProduct();
        BeanUtils.copyProperties(dto, sp);
        if (dto.getStockCount() != null) {
            sp.setStock(dto.getStockCount());
        }
        prepareSeckillForUpdate(sp);
        seckillMapper.update(sp);
        syncSeckillStockCache(sp);
    }

    @Override
    public void deleteAdminSeckill(Long id) {
        SeckillProduct tempSp = new SeckillProduct();
        tempSp.setId(id);
        tempSp.setStatus(2);
        seckillMapper.update(tempSp); // mark as ended/deleted
        redisTemplate.delete(RedisConstants.SECKILL_STOCK + id);
        redisTemplate.delete(RedisConstants.SECKILL_USER + id);
    }

    @Override
    public void finishAdminSeckill(Long id) {
        SeckillProduct product = seckillMapper.selectById(id);
        if (product != null) {
            product.setEndTime(LocalDateTime.now());
            product.setStatus(2); // 结束
            product.setUpdateTime(LocalDateTime.now());
            seckillMapper.update(product);

            // 清理缓存
            redisTemplate.delete(RedisConstants.SECKILL_STOCK + id);
        }
    }
}
