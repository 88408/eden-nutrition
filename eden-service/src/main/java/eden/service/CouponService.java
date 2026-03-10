package eden.service;

import eden.pojo.Coupon;
import eden.pojo.UserCoupon;
import java.util.List;

/**
 * 优惠券服务接口
 */
public interface CouponService {

    /**
     * 获取可领取的优惠券列表
     */
    List<Coupon> getAvailableCoupons();

    /**
     * 领取优惠券
     */
    void receiveCoupon(Long userId, Long couponId);

    /**
     * 获取用户的优惠券列表
     */
    List<UserCoupon> getUserCoupons(Long userId, Integer status);

    /**
     * 获取用户可用的优惠券（下单时）
     */
    List<UserCoupon> getUsableCoupons(Long userId);

    /**
     * 使用优惠券
     */
    void useCoupon(Long userCouponId, Long orderId);

    /**
     * 返还优惠券（订单取消）
     */
    void returnCoupon(Long userCouponId);

    /**
     * 根据ID获取优惠券
     */
    Coupon getById(Long id);

    /**
     * 添加优惠券
     */
    void add(Coupon coupon);

    /**
     * 更新优惠券
     */
    void update(Coupon coupon);

    /**
     * 更新过期的优惠券状态
     */
    void updateExpiredCoupons();
}
