package eden.service.impl;

import eden.common.constant.RedisConstants;
import eden.common.exception.BusinessException;
import eden.common.result.ResultCode;
import eden.mapper.CouponMapper;
import eden.mapper.UserCouponMapper;
import eden.pojo.Coupon;
import eden.pojo.UserCoupon;
import eden.service.CouponService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 优惠券服务实现类
 */
@Service
public class CouponServiceImpl implements CouponService {

    @Autowired
    private CouponMapper couponMapper;

    @Autowired
    private UserCouponMapper userCouponMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public List<Coupon> getAvailableCoupons() {
        return couponMapper.selectAvailable();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void receiveCoupon(Long userId, Long couponId) {
        // 分布式锁，防止并发领取
        String lockKey = RedisConstants.LOCK_COUPON + couponId + ":" + userId;
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(lockKey, "1", 
                RedisConstants.EXPIRE_LOCK, TimeUnit.SECONDS);
        if (locked == null || !locked) {
            throw new BusinessException("领取中，请勿重复操作");
        }

        try {
            // 检查优惠券是否存在
            Coupon coupon = couponMapper.selectById(couponId);
            if (coupon == null) {
                throw new BusinessException(ResultCode.COUPON_NOT_FOUND);
            }

            // 检查优惠券状态
            if (coupon.getStatus() != 1) {
                throw new BusinessException(ResultCode.COUPON_NOT_AVAILABLE);
            }

            // 检查是否在有效期内
            LocalDateTime now = LocalDateTime.now();
            if (now.isBefore(coupon.getStartTime()) || now.isAfter(coupon.getEndTime())) {
                throw new BusinessException(ResultCode.COUPON_EXPIRED);
            }

            // 检查剩余数量
            if (coupon.getRemainCount() <= 0) {
                throw new BusinessException("优惠券已被领完");
            }

            // 检查用户是否已领取过
            UserCoupon existing = userCouponMapper.selectByUserAndCoupon(userId, couponId);
            if (existing != null) {
                throw new BusinessException(ResultCode.COUPON_ALREADY_RECEIVED);
            }

            // 扣减优惠券数量
            int rows = couponMapper.decreaseRemainCount(couponId);
            if (rows == 0) {
                throw new BusinessException("优惠券已被领完");
            }

            // 创建用户优惠券记录
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setUserId(userId);
            userCoupon.setCouponId(couponId);
            userCoupon.setStatus(0); // 未使用
            // 过期时间从 coupon 表关联查询，不单独存储

            userCouponMapper.insert(userCoupon);
        } finally {
            redisTemplate.delete(lockKey);
        }
    }

    @Override
    public List<UserCoupon> getUserCoupons(Long userId, Integer status) {
        return userCouponMapper.selectByUserId(userId, status);
    }

    @Override
    public List<UserCoupon> getUsableCoupons(Long userId) {
        return userCouponMapper.selectAvailableByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void useCoupon(Long userCouponId, Long orderId) {
        int rows = userCouponMapper.use(userCouponId, orderId);
        if (rows == 0) {
            throw new BusinessException(ResultCode.COUPON_NOT_AVAILABLE);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void returnCoupon(Long userCouponId) {
        UserCoupon userCoupon = userCouponMapper.selectById(userCouponId);
        if (userCoupon == null) {
            return;
        }

        // 检查是否过期
        if (userCoupon.getExpireTime() != null && 
            LocalDateTime.now().isAfter(userCoupon.getExpireTime())) {
            // 已过期，不返还
            userCouponMapper.updateStatus(userCouponId, 2);
            return;
        }

        // 返还优惠券（状态改为未使用）
        userCouponMapper.updateStatus(userCouponId, 0);

        // 增加优惠券剩余数量
        couponMapper.increaseRemainCount(userCoupon.getCouponId());
    }

    @Override
    public Coupon getById(Long id) {
        return couponMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Coupon coupon) {
        if (coupon.getRemainCount() == null) {
            coupon.setRemainCount(coupon.getTotalCount());
        }
        if (coupon.getStatus() == null) {
            coupon.setStatus(1);
        }
        couponMapper.insert(coupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Coupon coupon) {
        couponMapper.update(coupon);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateExpiredCoupons() {
        userCouponMapper.updateExpiredStatus();
    }
}
