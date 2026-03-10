package eden.pojo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品评价实体类
 */
@Data
public class ProductReview implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 评价ID */
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 用户ID */
    private Long userId;

    /** 订单ID */
    private Long orderId;

    /** 评分：1-5星 */
    private Integer rating;

    /** 评价内容 */
    private String content;

    /** 评价图片（JSON数组） */
    private String images;

    /** 是否匿名：0-否 1-是 */
    private Integer isAnonymous;

    /** 状态：0-隐藏 1-显示 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}
