package eden.mapper;

import eden.pojo.RefundApply;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 退款申请 Mapper。
 * <p>支持用户侧售后列表和后台审核分页查询。</p>
 */
public interface RefundApplyMapper {
    RefundApply selectById(@Param("id") Long id);

    RefundApply selectByRefundNo(@Param("refundNo") String refundNo);

    RefundApply selectActiveByOrderNo(@Param("orderNo") String orderNo);

    List<RefundApply> selectByUserId(@Param("userId") Long userId, @Param("offset") Integer offset, @Param("limit") Integer limit);

    long countByUserId(@Param("userId") Long userId);

    List<RefundApply> selectAdminPage(@Param("status") Integer status, @Param("keyword") String keyword,
                                      @Param("offset") Integer offset, @Param("limit") Integer limit);

    long countAdminPage(@Param("status") Integer status, @Param("keyword") String keyword);

    int insert(RefundApply refundApply);

    int update(RefundApply refundApply);
}
