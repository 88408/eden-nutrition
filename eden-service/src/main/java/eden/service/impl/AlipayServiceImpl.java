package eden.service.impl;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import eden.common.exception.BusinessException;
import eden.pojo.Order;
import eden.service.AlipayService;
import eden.service.config.AlipayProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.RoundingMode;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 支付宝沙箱支付实现。
 */
@Service
public class AlipayServiceImpl implements AlipayService {

    /** 支付宝沙箱支付有效期：60分钟，避免调试环境跳转、登录或扫码耗时导致订单侧提前关闭。 */
    private static final String PAGE_PAY_TIMEOUT_EXPRESS = "60m";

    /** 电脑网站支付产品码，保留给普通 H5/桌面调试入口使用。 */
    private static final String PAGE_PAY_PRODUCT_CODE = "FAST_INSTANT_TRADE_PAY";

    /** 手机网站支付产品码；微信开发者工具不默认使用，避免触发 alipay:// 原生协议。 */
    private static final String WAP_PAY_PRODUCT_CODE = "QUICK_WAP_WAY";

    @Autowired
    private AlipayProperties alipayProperties;

    @Override
    public String createPagePayForm(Order order) {
        return createPagePayForm(order, alipayProperties.getReturnUrl());
    }

    @Override
    public String createPagePayForm(Order order, String returnUrl) {
        try {
            AlipayTradePagePayRequest request = buildPagePayRequest(order, returnUrl);
            return createClient().pageExecute(request).getBody();
        } catch (AlipayApiException | IllegalStateException e) {
            throw new BusinessException("创建支付宝沙箱支付表单失败：" + e.getMessage(), e);
        }
    }

    @Override
    public String createPagePayRedirectUrl(Order order, String returnUrl) {
        try {
            AlipayTradePagePayRequest request = buildPagePayRequest(order, returnUrl);
            // 支付宝沙箱 PagePay POST 表单在微信开发者工具链路中可能 504，GET 跳转可直接进入收银台分配页。
            return createClient().pageExecute(request, "GET").getBody();
        } catch (AlipayApiException | IllegalStateException e) {
            throw new BusinessException("创建支付宝沙箱支付跳转地址失败：" + e.getMessage(), e);
        }
    }

    @Override
    public String createWapPayForm(Order order, String returnUrl) {
        try {
            AlipayTradeWapPayRequest request = buildWapPayRequest(order, returnUrl);
            return createClient().pageExecute(request).getBody();
        } catch (AlipayApiException | IllegalStateException e) {
            throw new BusinessException("创建支付宝沙箱手机网站支付表单失败：" + e.getMessage(), e);
        }
    }

    @Override
    public String createWapPayRedirectUrl(Order order, String returnUrl) {
        try {
            AlipayTradeWapPayRequest request = buildWapPayRequest(order, returnUrl);
            // 外部手机浏览器兜底链路使用 GET 地址，便于从微信开发者工具复制后直接打开。
            return createClient().pageExecute(request, "GET").getBody();
        } catch (AlipayApiException | IllegalStateException e) {
            throw new BusinessException("创建支付宝沙箱手机网站支付跳转地址失败：" + e.getMessage(), e);
        }
    }

    @Override
    public boolean verifyNotify(Map<String, String> params) {
        try {
            alipayProperties.validateForNotifyVerify();
            // 支付宝异步通知必须先验签，后续订单号、金额和状态判断才可信。
            return AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    alipayProperties.getCharset(),
                    alipayProperties.getSignType());
        } catch (AlipayApiException | IllegalStateException e) {
            return false;
        }
    }

    @Override
    public boolean isPaidTrade(Map<String, String> params) {
        String tradeStatus = params.get("trade_status");
        return "TRADE_SUCCESS".equals(tradeStatus) || "TRADE_FINISHED".equals(tradeStatus);
    }

    @Override
    public String buildReturnRedirectUrl(String orderNo) {
        String baseUrl = alipayProperties.getFrontendReturnBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = "http://localhost:10086";
        }
        return UriComponentsBuilder.fromHttpUrl(baseUrl)
                .path("/pages/OrderDetail/index")
                .queryParam("orderNo", orderNo)
                .build()
                .toUriString();
    }

    @Override
    public String refund(Order order, BigDecimal refundAmount) {
        try {
            String outRequestNo = "RF" + order.getOrderNo() + System.currentTimeMillis();
            String amount = refundAmount.setScale(2, RoundingMode.HALF_UP).toPlainString();
            String tradeNoParam = StringUtils.hasText(order.getPaymentTradeNo())
                    ? "\"trade_no\":\"" + order.getPaymentTradeNo() + "\","
                    : "";

            AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
            request.setBizContent("{"
                    + "\"out_trade_no\":\"" + order.getOrderNo() + "\","
                    + tradeNoParam
                    + "\"refund_amount\":\"" + amount + "\","
                    + "\"out_request_no\":\"" + outRequestNo + "\","
                    + "\"refund_reason\":\"Eden Nutrition 售后退款\""
                    + "}");
            AlipayTradeRefundResponse response = createClient().execute(request);
            // 支付宝 SDK 可能正常返回业务失败响应，必须显式检查结果，避免把第三方失败记录成真实退款成功。
            if (response == null || !response.isSuccess()) {
                String message = response == null ? "支付宝无退款响应" : response.getSubMsg();
                if (message == null || message.isBlank()) {
                    message = response == null ? "支付宝无退款响应" : response.getMsg();
                }
                throw new BusinessException("支付宝沙箱退款失败：" + message);
            }
            return outRequestNo;
        } catch (AlipayApiException | IllegalStateException e) {
            throw new BusinessException("支付宝沙箱退款失败：" + e.getMessage(), e);
        }
    }

    /**
     * 创建支付宝 SDK 客户端前先校验必填配置，保证 PagePay/WapPay 使用同一套沙箱密钥和回调地址。
     */
    private AlipayClient createClient() {
        alipayProperties.validateForPagePay();
        return new DefaultAlipayClient(
                alipayProperties.getGatewayUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getPrivateKey(),
                "json",
                alipayProperties.getCharset(),
                alipayProperties.getAlipayPublicKey(),
                alipayProperties.getSignType());
    }

    /**
     * 电脑网站支付请求在普通 H5 和微信开发者工具调试中复用同一组业务参数，避免两条链路签名字段不一致。
     */
    private AlipayTradePagePayRequest buildPagePayRequest(Order order, String returnUrl) {
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        request.setReturnUrl(returnUrl);
        request.setBizContent(buildBizContent(order, PAGE_PAY_PRODUCT_CODE));
        return request;
    }

    /**
     * 手机网站支付仅作为外部手机浏览器兜底，不在微信开发者工具 web-view 中自动使用，避免 alipay:// 协议白屏。
     */
    private AlipayTradeWapPayRequest buildWapPayRequest(Order order, String returnUrl) {
        AlipayTradeWapPayRequest request = new AlipayTradeWapPayRequest();
        request.setNotifyUrl(alipayProperties.getNotifyUrl());
        request.setReturnUrl(returnUrl);
        request.setBizContent(buildBizContent(order, WAP_PAY_PRODUCT_CODE));
        return request;
    }

    /**
     * 支付请求只需要订单号、金额、标题、产品码和支付有效期，金额固定保留两位，避免回调金额校验出现精度差异。
     *
     * <p>沙箱环境显式传入 seller_id，避免收银台登录后无法识别当前沙箱应用绑定的收款商户。</p>
     */
    private String buildBizContent(Order order, String productCode) {
        String amount = order.getPayAmount().setScale(2, RoundingMode.HALF_UP).toPlainString();
        String sellerId = alipayProperties.getSellerId();
        String sellerIdParam = StringUtils.hasText(sellerId)
                ? ",\"seller_id\":\"" + sellerId + "\""
                : "";
        return "{"
                + "\"out_trade_no\":\"" + order.getOrderNo() + "\","
                + "\"total_amount\":\"" + amount + "\","
                + "\"subject\":\"Eden Nutrition 订单 " + order.getOrderNo() + "\","
                + "\"product_code\":\"" + productCode + "\","
                + "\"timeout_express\":\"" + PAGE_PAY_TIMEOUT_EXPRESS + "\""
                + sellerIdParam
                + "}";
    }
}
