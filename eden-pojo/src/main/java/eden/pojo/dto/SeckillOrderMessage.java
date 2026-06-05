package eden.pojo.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀异步下单消息。
 */
@Data
public class SeckillOrderMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 前端后续轮询和订单详情使用的订单号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 秒杀活动ID */
    private Long seckillId;

    /** 收货地址ID */
    private Long addressId;

    /** 秒杀数量，当前固定为 1 */
    private Integer quantity;
}
