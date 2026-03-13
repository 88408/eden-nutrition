package eden.pojo.dto;

import lombok.Data;

/**
 * 订单查询参数
 */
@Data
public class OrderQueryDTO {

    /** 订单号 */
    private String orderNo;

    /** 订单状态 */
    private Integer status;

    /** 用户ID */
    private Long userId;

    /** 偏移量 */
    private Integer offset;

    /** 页码 */
    private Integer pageNum;

    /** 每页数量 */
    private Integer pageSize;
}