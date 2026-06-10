package eden.pojo.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 用户申请退款请求。
 * <p>orderItemId 可为空，表示整单退款；refundAmount 为空时默认按订单实付金额退款。</p>
 */
@Data
public class RefundApplyDTO {
    @NotBlank(message = "订单号不能为空")
    private String orderNo;
    private Long orderItemId;
    private BigDecimal refundAmount;
    @NotBlank(message = "退款原因不能为空")
    private String reason;
    private String images;
}
