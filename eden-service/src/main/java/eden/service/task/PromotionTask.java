package eden.service.task;

import eden.common.constant.RedisConstants;
import eden.mapper.CouponMapper;
import eden.mapper.SeckillMapper;
import eden.pojo.Coupon;
import eden.pojo.SeckillProduct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 促销活动定时任务
 */
@Component
public class PromotionTask {

    private static final Logger logger = LoggerFactory.getLogger(PromotionTask.class);

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private SeckillMapper seckillMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 预热秒杀商品库存到Redis
     * 每天凌晨1点执行，预热当天的秒杀活动
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void warmUpSeckillStock() {
        logger.info("开始预热秒杀库存到Redis...");

        try {
            LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
            LocalDateTime tomorrow = today.plusDays(1);

            // 查询今天的秒杀活动
            List<SeckillProduct> seckills = seckillMapper.selectByTimeRange(today, tomorrow);

            for (SeckillProduct seckill : seckills) {
                preheatSeckillStock(seckill);
            }

            logger.info("秒杀库存预热完成，共预热 {} 个活动", seckills.size());

        } catch (Exception e) {
            logger.error("秒杀库存预热失败", e);
        }
    }

    /**
     * 更新过期优惠券状态
     * 每天凌晨3点执行
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void updateExpiredCoupons() {
        logger.info("开始更新过期优惠券状态...");

        try {
            // 查询已过期但状态仍为有效的优惠券
            List<Coupon> expiredCoupons = couponMapper.selectExpiredCoupons(LocalDateTime.now());

            if (expiredCoupons.isEmpty()) {
                logger.info("没有需要更新的过期优惠券");
                return;
            }

            int updatedCount = 0;
            for (Coupon coupon : expiredCoupons) {
                try {
                    Coupon updateCoupon = new Coupon();
                    updateCoupon.setId(coupon.getId());
                    updateCoupon.setStatus(0); // 设置为无效
                    updateCoupon.setUpdateTime(LocalDateTime.now());
                    couponMapper.updateById(updateCoupon);
                    updatedCount++;
                } catch (Exception e) {
                    logger.error("更新优惠券 {} 状态失败", coupon.getId(), e);
                }
            }

            logger.info("过期优惠券状态更新完成，共更新 {} 个", updatedCount);

        } catch (Exception e) {
            logger.error("更新过期优惠券状态失败", e);
        }
    }

    /**
     * 刷新秒杀活动生命周期状态
     * 每分钟执行一次
     */
    @Scheduled(cron = "0 * * * * ?")
    public void refreshSeckillLifecycle() {
        logger.info("开始刷新秒杀活动生命周期状态...");

        try {
            LocalDateTime now = LocalDateTime.now();
            // 先结束过期活动，避免过期活动在同一轮任务中又被误判为可启动。
            int endedCount = seckillMapper.updateEndedSeckills(now);
            if (endedCount > 0) {
                logger.info("更新了 {} 个已结束的秒杀活动", endedCount);
            }

            // 启动前先查出活动明细，用于状态更新成功后按活动结束时间设置 Redis 库存过期时间。
            List<SeckillProduct> startableSeckills = seckillMapper.selectStartableSeckills(now);
            int startedCount = seckillMapper.updateStartedSeckills(now);
            if (startedCount > 0) {
                for (SeckillProduct seckill : startableSeckills) {
                    preheatSeckillStock(seckill);
                }
                logger.info("更新了 {} 个已开始的秒杀活动", startedCount);
            }
        } catch (Exception e) {
            logger.error("刷新秒杀活动生命周期状态失败", e);
        }
    }

    /**
     * 将秒杀活动库存预热到 Redis。
     * Redis key 的生命周期比活动结束时间多保留一小时，用于覆盖异步订单消息处理的延迟窗口。
     */
    private void preheatSeckillStock(SeckillProduct seckill) {
        if (seckill == null || seckill.getId() == null || seckill.getEndTime() == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        if (!now.isBefore(seckill.getEndTime())) {
            // 已过期活动不应再保留可扣减库存，避免前端或旧请求误触发秒杀扣减。
            redisTemplate.delete(RedisConstants.SECKILL_STOCK + seckill.getId());
            return;
        }

        Integer stock = seckill.getStock() != null ? seckill.getStock() : seckill.getStockCount();
        if (stock == null) {
            return;
        }

        long expireSeconds = Duration.between(now, seckill.getEndTime().plusHours(1)).getSeconds();
        if (expireSeconds > 0) {
            redisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK + seckill.getId(), stock, expireSeconds, TimeUnit.SECONDS);
            logger.info("秒杀活动 {} 库存预热成功，库存: {}", seckill.getId(), stock);
        }
    }
}
