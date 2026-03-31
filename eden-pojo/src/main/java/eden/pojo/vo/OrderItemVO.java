package eden.pojo.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单项明细 VO
 */
@Data
public class OrderItemVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long orderId;
    private Long productId;
    private String productSkuId;
    private String productName;
    private String productPic;
    private BigDecimal productPrice;
    private Integer quantity;
    private BigDecimal totalAmount;
}
