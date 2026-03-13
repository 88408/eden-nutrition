package eden.pojo.dto;

import eden.pojo.SeckillProduct;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀场次DTO
 */
@Data
public class SeckillSessionDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 场次开始时间 */
    private LocalDateTime startTime;

    /** 场次结束时间 */
    private LocalDateTime endTime;

    /** 状态：0-即将开始 1-进行中 2-已结束 */
    private Integer status;
    
    /** 该场次下的商品列表 */
    private List<SeckillProduct> products;
}