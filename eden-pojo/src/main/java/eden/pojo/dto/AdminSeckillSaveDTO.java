package eden.pojo.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminSeckillSaveDTO {
    /**
     * 秒杀ID (修改时传入)
     */
    private Long id;

    /**
     * 关联商品ID
     */
    private Long productId;

    /**
     * 秒杀价格
     */
    private BigDecimal seckillPrice;

    /**
     * 秒杀库存量
     */
    private Integer stockCount;

    /**
     * 每人限购数量
     */
    private Integer limitPerUser;

    /**
     * 秒杀开始时间
     */
    private LocalDateTime startTime;

    /**
     * 秒杀结束时间
     */
    private LocalDateTime endTime;
}
