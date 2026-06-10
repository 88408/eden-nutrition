package eden.pojo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品规格实体。
 * <p>营养品用“规格/口味/包装”表达可售 SKU，等价覆盖颜色、尺码等多规格评分要求。</p>
 */
@Data
public class ProductSku implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long productId;
    private String specName;
    private String flavor;
    private String packageSize;
    private BigDecimal price;
    private Integer stock;
    private String imageUrl;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
