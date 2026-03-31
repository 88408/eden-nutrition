package eden.pojo.dto;

import lombok.Data;
import java.io.Serializable;

/**
 * B端订单查询 DTO
 */
@Data
public class AdminOrderQueryDTO extends PageDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 订单号 (模糊或精确查询)
     */
    private String orderNo;

    /**
     * 订单状态：0-待支付 1-已支付待发货 2-已发货 3-已完成 4-已取消
     */
    private Integer status;

    /**
     * 下单时间范围起始
     */
    private String startTime;

    /**
     * 下单时间范围结束
     */
    private String endTime;
}
