package eden.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AdminSeckillQueryDTO extends PageDTO {
    /**
     * 商品ID
     */
    private Long productId;

    /**
     * 状态: 0-未开始, 1-进行中, 2-已结束
     */
    private Integer status;
}