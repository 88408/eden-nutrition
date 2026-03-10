package eden.mapper;

import eden.pojo.UserCoupon;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 用户优惠券 Mapper 接口
 */
public interface UserCouponMapper {

    /**
     * 根据ID查询
     */
    UserCoupon selectById(@Param("id") Long id);

    /**
     * 查询用户的优惠券列表
     */
    List<UserCoupon> selectByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 查询用户可用的优惠券
     */
    List<UserCoupon> selectAvailableByUserId(@Param("userId") Long userId);

    /**
     * 检查用户是否已领取某优惠券
     */
    UserCoupon selectByUserAndCoupon(@Param("userId") Long userId, @Param("couponId") Long couponId);

    /**
     * 插入用户优惠券
     */
    int insert(UserCoupon userCoupon);

    /**
     * 使用优惠券
     */
    int use(@Param("id") Long id, @Param("orderId") Long orderId);

    /**
     * 更新状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 批量更新过期的优惠券状态
     */
    int updateExpiredStatus();
}
