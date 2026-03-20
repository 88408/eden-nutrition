package eden.pojo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀订单实体类（防止重复秒杀）
 */
@Data
public class SeckillOrder implements Serializable {
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 订单编号 */
    private String orderNo;

    /** 订单ID */
    private Long orderId;

    /** 秒杀ID */
    private Long seckillId;

    /** 商品ID */
    private Long productId;

    /** 秒杀金额 */
    private BigDecimal amount;

    /** 状态：0-未支付，1-已支付，2-已取消 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;
}
