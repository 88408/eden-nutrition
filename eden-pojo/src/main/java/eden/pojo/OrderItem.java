package eden.pojo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单明细实体类
 */
@Data
public class OrderItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 订单编号 */
    private String orderNo;

    /** 商品ID */
    private Long productId;

    /** 商品 SKU ID，下单时为空表示使用主商品库存 */
    private Long skuId;

    /** SKU 规格快照，避免后续修改 SKU 后影响历史订单展示 */
    private String skuSpecName;

    /** 商品名称（快照） */
    private String productName;

    /** 商品图片（快照） */
    private String productImage;

    /** 商品单价（快照） */
    private BigDecimal price;

    /** 购买数量 */
    private Integer quantity;

    /** 小计金额 */
    private BigDecimal totalPrice;

    /** 创建时间 */
    private LocalDateTime createTime;
}
