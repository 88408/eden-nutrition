package eden.pojo.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * B端订单列表基础 VO
 */
@Data
public class OrderAdminVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private BigDecimal payAmount;
    private Integer status;
    private Date createTime;
    
    // 收货人精简信息
    private String receiverName;
    private String receiverPhone;
    private String receiverDetailAddress;
}
