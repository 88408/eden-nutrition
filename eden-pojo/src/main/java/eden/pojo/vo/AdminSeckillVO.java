package eden.pojo.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class AdminSeckillVO {
    /** 秒杀ID */
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 秒杀价格 */
    private BigDecimal seckillPrice;

    /** 秒杀库存配置数 */
    private Integer stockCount;

    /** 当前剩余库存 */
    private Integer stock;

    /** 每位用户限购数量 */
    private Integer limitPerUser;

    /** 开始时间 */
    private LocalDateTime startTime;

    /** 结束时间 */
    private LocalDateTime endTime;

    /** 状态: 0-未开始, 1-进行中, 2-已结束 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // --- 关联商品信息 ---

    /** 商品名称 */
    private String productName;

    /** 商品主图 */
    private String productMainImage;

    /** 商品原价 */
    private BigDecimal originalPrice;
}
