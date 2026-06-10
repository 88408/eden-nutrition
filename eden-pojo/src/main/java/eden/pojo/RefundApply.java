package eden.pojo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 售后退款申请实体。
 * <p>记录用户申请、后台审核、退款执行和第三方/模拟退款流水，保证售后流程可追溯。</p>
 */
@Data
public class RefundApply implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String refundNo;
    private Long orderId;
    private String orderNo;
    private Long orderItemId;
    private Long userId;
    private BigDecimal refundAmount;
    /** 申请退款前的订单状态，审核拒绝时用于恢复业务流转状态 */
    private Integer originalOrderStatus;
    private String reason;
    private String images;
    /** 0-待审核 1-审核通过 2-审核拒绝 3-退款成功 4-退款失败 */
    private Integer status;
    private String auditRemark;
    private Long auditorId;
    private LocalDateTime auditTime;
    private String refundTradeNo;
    private Integer simulated;
    private LocalDateTime refundTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
