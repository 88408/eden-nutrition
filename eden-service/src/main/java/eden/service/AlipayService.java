package eden.service;

import eden.pojo.Order;

import java.util.Map;

/**
 * 支付宝沙箱支付服务。
 */
public interface AlipayService {

    /**
     * 为待支付订单生成支付宝 PagePay 表单 HTML。
     */
    String createPagePayForm(Order order);

    /**
     * 为待支付订单生成指定同步返回地址的支付宝 PagePay 表单 HTML。
     */
    String createPagePayForm(Order order, String returnUrl);

    /**
     * 为调试桥接页生成支付宝 PagePay GET 跳转地址，规避沙箱 POST 表单偶发 504。
     */
    String createPagePayRedirectUrl(Order order, String returnUrl);

    /**
     * 生成支付宝 WapPay 表单 HTML，仅保留给真机或外部浏览器实验链路使用。
     */
    String createWapPayForm(Order order, String returnUrl);

    /**
     * 为外部手机调试生成支付宝 WapPay GET 跳转地址，供微信开发者工具复制链接兜底。
     */
    String createWapPayRedirectUrl(Order order, String returnUrl);

    /**
     * 验证支付宝异步通知签名。
     */
    boolean verifyNotify(Map<String, String> params);

    /**
     * 判断异步通知中的交易状态是否代表支付成功。
     */
    boolean isPaidTrade(Map<String, String> params);

    /**
     * 根据支付宝同步返回参数构造前端订单详情页地址。
     */
    String buildReturnRedirectUrl(String orderNo);
}
