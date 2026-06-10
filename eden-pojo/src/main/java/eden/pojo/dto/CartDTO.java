package eden.pojo.dto;

import lombok.Data;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * 购物车操作 DTO
 */
@Data
public class CartDTO {

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 商品 SKU ID；商品存在启用 SKU 时必须传入，避免规格选择只停留在前端展示层 */
    private Long skuId;

    @NotNull(message = "数量不能为空")
    @Min(value = 1, message = "数量至少为1")
    private Integer quantity;

    /** 是否选中 */
    private Boolean selected = true;
}
