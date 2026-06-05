package eden.service.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 支付宝沙箱支付配置。
 *
 * <p>应用私钥和支付宝公钥只从运行环境读取，避免把敏感配置写入仓库。</p>
 */
@Data
@Component
@ConfigurationProperties(prefix = "eden.alipay")
public class AlipayProperties {

    /** 沙箱应用 ID */
    private String appId;

    /** 应用私钥 */
    private String privateKey;

    /** 支付宝公钥，用于异步通知验签 */
    private String alipayPublicKey;

    /** 沙箱商家账号 PID，用于 PagePay 明确指定收款方 */
    private String sellerId;

    /** 支付宝沙箱网关 */
    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";

    /** 支付宝异步通知地址，必须是公网可访问地址 */
    private String notifyUrl;

    /** 支付宝同步返回地址，只用于页面跳转，不作为支付成功依据 */
    private String returnUrl;

    /** H5 前端基础地址，用于同步返回后跳回订单详情页 */
    private String frontendReturnBaseUrl;

    /** 微信开发者工具调试时可访问的后端 /api 根地址，用于生成稳定的 web-view bridgeUrl */
    private String weappDebugBridgeBaseUrl;

    /** 请求与验签字符集 */
    private String charset = "UTF-8";

    /** 签名算法 */
    private String signType = "RSA2";

    /**
     * 校验生成支付表单所需的必填配置。
     */
    public void validateForPagePay() {
        if (!StringUtils.hasText(appId)
                || !StringUtils.hasText(privateKey)
                || !StringUtils.hasText(gatewayUrl)
                || !StringUtils.hasText(notifyUrl)
                || !StringUtils.hasText(returnUrl)) {
            throw new IllegalStateException("支付宝沙箱支付配置不完整，请检查 ALIPAY_APP_ID/ALIPAY_PRIVATE_KEY/ALIPAY_NOTIFY_URL/ALIPAY_RETURN_URL");
        }
    }

    /**
     * 校验异步通知验签所需的必填配置。
     */
    public void validateForNotifyVerify() {
        if (!StringUtils.hasText(alipayPublicKey)) {
            throw new IllegalStateException("支付宝公钥未配置，请检查 ALIPAY_PUBLIC_KEY");
        }
    }
}
