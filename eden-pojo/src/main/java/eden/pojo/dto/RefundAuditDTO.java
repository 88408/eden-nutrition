package eden.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 后台退款审核请求。
 * <p>approved=true 表示审核通过进入待执行退款，false 表示拒绝并保留拒绝原因。</p>
 */
@Data
public class RefundAuditDTO {
    @NotNull(message = "退款申请ID不能为空")
    private Long refundId;
    @NotNull(message = "审核结果不能为空")
    private Boolean approved;
    private String auditRemark;
}
