package eden.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 秒杀异步处理结果。
 */
@Data
public class SeckillResultVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /** PROCESSING、SUCCESS、FAILED */
    private String status;

    /** 订单号 */
    private String orderNo;

    /** 展示给前端的处理说明 */
    private String message;
}
