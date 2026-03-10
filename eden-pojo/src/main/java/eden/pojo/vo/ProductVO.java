package eden.pojo.vo;

import eden.pojo.Product;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品视图对象
 */
@Data
public class ProductVO implements Serializable {
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
    private String imageUrl;

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

    /** 评分 */
    private Double rating = 5.0;

    /** 评论数 */
    private Integer reviewCount = 0;

    /**
     * 从 Product 实体转换
     */
    public static ProductVO fromProduct(Product product) {
        if (product == null) {
            return null;
        }
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        // Map mainImage -> imageUrl
        vo.setImageUrl(product.getMainImage());
        return vo;
    }
}
