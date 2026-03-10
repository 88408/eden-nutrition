package eden.pojo.dto;

import lombok.Data;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 创建订单请求 DTO
 */
@Data
public class OrderCreateDTO {

    /** 收货地址ID */
    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    /** 选中的购物车商品ID列表 */
    @NotEmpty(message = "请选择要购买的商品")
    private List<Long> productIds;

    /** 优惠券ID（可选） */
    private Long couponId;

    /** 用户优惠券ID */
    private Long userCouponId;

    /** 订单备注 */
    private String remark;
}
