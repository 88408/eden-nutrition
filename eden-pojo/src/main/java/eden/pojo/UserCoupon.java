package eden.pojo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户优惠券实体类
 */
@Data
public class UserCoupon implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 优惠券ID */
    private Long couponId;

    /** 状态：0-未使用 1-已使用 2-已过期 */
    private Integer status;

    /** 使用时间 */
    private LocalDateTime usedTime;

    /** 使用的订单ID */
    private Long orderId;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 过期时间 */
    private LocalDateTime expireTime;

    // ========== 状态常量 ==========
    public static final int STATUS_UNUSED = 0;   // 未使用
    public static final int STATUS_USED = 1;     // 已使用
    public static final int STATUS_EXPIRED = 2;  // 已过期
}
