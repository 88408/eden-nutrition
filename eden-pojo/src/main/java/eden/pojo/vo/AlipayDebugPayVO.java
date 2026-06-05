package eden.pojo.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 微信开发者工具内调试支付宝沙箱支付的桥接地址。
 */
@Data
public class AlipayDebugPayVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 后端桥接页地址，微信小程序 web-view 会加载该地址并自动跳转支付宝沙箱 */
    private String bridgeUrl;

    /** 外部手机浏览器调试用的支付宝手机网站支付地址，用于 web-view 空白时复制兜底 */
    private String externalPayUrl;

    /** 桥接 token 有效期，单位秒 */
    private Long expireSeconds;

    /** 订单编号 */
    private String orderNo;
}
