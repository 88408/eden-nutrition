package eden.pojo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户站内通知实体，承载订单、优惠券和系统类消息中心数据。
 */
@Data
public class Notice implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 通知ID。 */
    private Long id;

    /** 接收用户ID。 */
    private Long userId;

    /** 通知类型：ORDER-订单，COUPON-优惠券，SYSTEM-系统。 */
    private String type;

    /** 通知标题。 */
    private String title;

    /** 通知内容。 */
    private String content;

    /** 业务跳转目标，例如订单号或页面路径。 */
    private String target;

    /** 是否已读：0-未读，1-已读。 */
    private Integer isRead;

    /** 创建时间。 */
    private LocalDateTime createTime;

    /** 已读时间。 */
    private LocalDateTime readTime;
}
