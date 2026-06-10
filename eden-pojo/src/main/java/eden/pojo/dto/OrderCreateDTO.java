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
    private List<Long> productIds;

    /** 直接下单或多规格购物车下单时使用的 SKU 明细；为空时沿用旧购物车选中项逻辑 */
    private List<OrderSkuItemDTO> skuItems;

    /** 优惠券ID（可选） */
    private Long couponId;

    /** 用户优惠券ID */
    private Long userCouponId;

    /** 订单备注 */
    private String remark;

    /**
     * SKU 下单项。
     * <p>productId 兼容前端快速构造订单；skuId 为空时使用主商品价格和库存。</p>
     */
    @Data
    public static class OrderSkuItemDTO {
        private Long productId;
        private Long skuId;
        private Integer quantity;
    }
}
