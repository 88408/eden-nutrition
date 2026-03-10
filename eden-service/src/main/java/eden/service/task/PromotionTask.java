package eden.service.task;

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
                String stockKey = "seckill:stock:" + seckill.getId();
                // 将库存预热到Redis，过期时间设为活动结束时间后1小时
                long expireSeconds = java.time.Duration.between(
                        LocalDateTime.now(), seckill.getEndTime().plusHours(1)).getSeconds();
                
                if (expireSeconds > 0) {
                    redisTemplate.opsForValue().set(stockKey, seckill.getStock(), 
                            expireSeconds, TimeUnit.SECONDS);
                    logger.info("秒杀活动 {} 库存预热成功，库存: {}", seckill.getId(), seckill.getStock());
                }
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
     * 更新已结束的秒杀活动状态
     * 每5分钟执行一次
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void updateEndedSeckills() {
        logger.info("开始更新已结束的秒杀活动状态...");

        try {
            int updatedCount = seckillMapper.updateEndedSeckills(LocalDateTime.now());
            if (updatedCount > 0) {
                logger.info("更新了 {} 个已结束的秒杀活动", updatedCount);
            }
        } catch (Exception e) {
            logger.error("更新已结束秒杀活动状态失败", e);
        }
    }
}
