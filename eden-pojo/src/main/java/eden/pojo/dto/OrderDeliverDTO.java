package eden.pojo.dto;

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
    private Long orderId;

    /**
     * 物流公司名称
     */
    private String deliveryCompany;

    /**
     * 物流单号
     */
    private String deliverySn;
}
