package eden.pojo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀商品实体类
 */
@Data
public class SeckillProduct implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 秒杀ID */
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 秒杀价格 */
    private BigDecimal seckillPrice;

    /** 秒杀库存 */
    private Integer stockCount;

    /** 库存（兼容字段） */
    private Integer stock;

    /** 秒杀开始时间 */
    private LocalDateTime startTime;

    /** 秒杀结束时间 */
    private LocalDateTime endTime;

    /** 状态：0-未开始 1-进行中 2-已结束 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // ========== 状态常量 ==========
    public static final int STATUS_NOT_STARTED = 0;  // 未开始
    public static final int STATUS_ONGOING = 1;      // 进行中
    public static final int STATUS_ENDED = 2;        // 已结束
}
