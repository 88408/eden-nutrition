package eden.pojo.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 评价展示 VO，在评价基础信息上补充商品快照字段，供“我的评价”和评价列表展示使用。
 */
@Data
public class ProductReviewVO {

    /** 评价ID。 */
    private Long id;

    /** 商品ID。 */
    private Long productId;

    /** 用户ID。 */
    private Long userId;

    /** 订单ID。 */
    private Long orderId;

    /** 评分，范围 1-5。 */
    private Integer rating;

    /** 评价内容。 */
    private String content;

    /** 评价图片 JSON。 */
    private String images;

    /** 是否匿名：0-否，1-是。 */
    private Integer isAnonymous;

    /** 评价状态。 */
    private Integer status;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 商品名称，用于“我的评价”列表展示。 */
    private String productName;

    /** 商品主图，用于“我的评价”列表展示。 */
    private String productImage;
}
