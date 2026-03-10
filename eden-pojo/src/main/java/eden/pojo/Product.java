package eden.pojo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品实体类
 */
@Data
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 商品ID */
    private Long id;

    /** 商品名称 */
    private String name;

    /** 商品副标题 */
    private String subtitle;

    /** 分类ID */
    private Long categoryId;

    /** 主图URL */
    private String mainImage;

    /** 商品图片列表（JSON数组） */
    private String subImages;

    /** 商品详情（富文本） */
    private String detail;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 销售价格 */
    private BigDecimal price;

    /** 库存 */
    private Integer stock;

    /** 销量 */
    private Integer sales;

    /** 状态：0-下架 1-上架 */
    private Integer status;

    /** 是否热门：0-否 1-是 */
    private Integer isHot;

    /** 是否新品：0-否 1-是 */
    private Integer isNew;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
