package eden.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 购物车项 VO
 */
@Data
public class CartItemVO {

    private Long productId;
    private String productName;
    private String productImage;
    private BigDecimal price;
    private Integer quantity;
    private Boolean selected;
    private Integer stock;
    private BigDecimal totalPrice;

    /** 小计金额 */
    private BigDecimal subtotal;

    /** 库存是否充足 */
    private Boolean stockEnough;

    /** 商品状态：1-正常 0-已下架 */
    private Integer productStatus;
}
