package eden.pojo.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.io.Serializable;
import java.util.List;

/**
 * B端订单详情 VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderDetailAdminVO extends OrderAdminVO {
    private static final long serialVersionUID = 1L;

    /**
     * 该订单包含的商品明细
     */
    private List<OrderItemVO> orderItemList;
    
    // 以下为物流信息 (如果有的话)
    private String deliveryCompany;
    private String deliverySn;
    private java.util.Date deliveryTime;
}
