package eden.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀提交结果。
 */
@Data
public class SeckillSubmitVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 订单号，异步消费成功后可用于订单详情查询 */
    private String orderNo;

    /** 当前处理状态 */
    private String status;
}
