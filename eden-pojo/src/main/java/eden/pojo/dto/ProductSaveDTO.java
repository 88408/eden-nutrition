package eden.pojo.dto;

import lombok.Data;
import java.math.BigDecimal;

/**
 * 商品新增于修改 DTO
 */
@Data
public class ProductSaveDTO {

    /** 商品ID (新增为空，修改非空) */
    private Long id;

    /** 商品名称 */
    private String name;

    /** 商品副标题 */
    private String subtitle;

    /** 分类ID */
    private Long categoryId;

    /** 主图URL */
    private String mainImage;

    /** 副图urls，逗号分隔 */
    private String subImages;

    /** 商品详情/富文本 */
    private String detail;

    /** 划线价/原价 */
    private BigDecimal originalPrice;

    /** 实际售价 */
    private BigDecimal price;

    /** 库存数量 */
    private Integer stock;

    /** 是否热门：0-否，1-是 */
    private Integer isHot;

    /** 是否新品：0-否，1-是 */
    private Integer isNew;

    /** 状态：0-下架，1-上架 */
    private Integer status;
}
