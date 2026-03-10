package eden.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 购物车项实体类（存储在 Redis 中）
 */
@Data
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 商品ID */
    private Long productId;

    /** 商品名称 */
    private String productName;

    /** 商品图片 */
    private String productImage;

    /** 商品单价 */
    private BigDecimal price;

    /** 购买数量 */
    private Integer quantity;

    /** 是否选中 */
    private Boolean selected;

    /** 商品库存（用于前端校验） */
    private Integer stock;

    /**
     * 计算小计金额（不参与序列化）
     */
    @JsonIgnore
    public BigDecimal getTotalPrice() {
        if (price == null || quantity == null) {
            return BigDecimal.ZERO;
        }
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
