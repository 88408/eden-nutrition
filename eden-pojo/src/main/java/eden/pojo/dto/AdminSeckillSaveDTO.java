package eden.pojo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
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
    @JsonAlias("stockCount")
    @NotNull(message = "秒杀库存不能为空")
    @Min(value = 1, message = "库存必须大于 0")
    private Integer stock = 100;

    /**
     * 每人限购数量
     */
    @NotNull(message = "每人限购数量不能为空")
    @Min(value = 1, message = "每人限购数量必须大于 0")
    private Integer limitPerUser = 1;

    /**
     * 秒杀开始时间
     */
    private LocalDateTime startTime;

    /**
     * 秒杀结束时间
     */
    private LocalDateTime endTime;
}
