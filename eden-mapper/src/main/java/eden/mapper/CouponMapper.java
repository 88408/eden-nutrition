package eden.mapper;

import eden.pojo.Coupon;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 优惠券 Mapper 接口
 */
public interface CouponMapper {

    /**
     * 根据ID查询优惠券
     */
    Coupon selectById(@Param("id") Long id);

    /**
     * 查询可领取的优惠券列表
     */
    List<Coupon> selectAvailable();

    /**
     * 查询已过期但仍有效的优惠券
     */
    List<Coupon> selectExpiredCoupons(@Param("now") LocalDateTime now);

    /**
     * 根据ID更新优惠券
     */
    int updateById(Coupon coupon);

    /**
     * 查询所有优惠券（管理端）
     */
    List<Coupon> selectAll(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计总数
     */
    long count();

    /**
     * 插入优惠券
     */
    int insert(Coupon coupon);

    /**
     * 更新优惠券
     */
    int update(Coupon coupon);

    /**
     * 扣减优惠券数量
     */
    int decreaseRemainCount(@Param("id") Long id);

    /**
     * 增加优惠券数量（回滚）
     */
    int increaseRemainCount(@Param("id") Long id);

    /**
     * 更新状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
