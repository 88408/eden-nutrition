package eden.pojo.vo;

import eden.pojo.Product;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    /** 主图URL，保留给用户端小程序等历史前端字段使用。 */
    private String mainImage;

    /** 主图URL，保留给管理端或旧接口文档中的 imageUrl 字段使用。 */
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

    /** 商品规格列表，用户端详情页用于选择口味、规格和包装 */
    private List<eden.pojo.ProductSku> skuList = new ArrayList<>();

    /**
     * 从 Product 实体转换
     */
    public static ProductVO fromProduct(Product product) {
        if (product == null) {
            return null;
        }
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        // 商品图片同时输出 mainImage 和 imageUrl，避免不同前端因字段名差异读不到同一套后端图片。
        vo.setImageUrl(product.getMainImage());
        return vo;
    }
}
