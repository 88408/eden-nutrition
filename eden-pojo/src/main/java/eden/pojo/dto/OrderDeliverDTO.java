package eden.pojo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import java.io.Serializable;

/**
 * B端订单发货 DTO
 */
@Data
public class OrderDeliverDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID
     */
    @NotNull(message = "订单ID不能为空")
    private Long orderId;

    /**
     * 物流公司名称
     */
    @NotBlank(message = "物流公司不能为空")
    private String deliveryCompany;

    /**
     * 物流单号（前端传入 trackingNo）
     */
    @NotBlank(message = "物流单号不能为空")
    @JsonAlias("trackingNo")
    private String deliverySn;
}
