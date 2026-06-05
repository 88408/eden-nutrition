package eden.web.controller;

import eden.service.OrderService;
import eden.common.exception.BusinessException;
import eden.service.config.AlipayProperties;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * 支付宝沙箱支付回调控制器。
 *
 * <p>该控制器不加登录注解，异步通知依靠支付宝签名验签确认来源。</p>
 */
@Api(tags = "支付宝支付回调")
@RestController
@RequestMapping("/order/pay/alipay")
public class AlipayCallbackController {

    private static final Logger log = LoggerFactory.getLogger(AlipayCallbackController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private AlipayProperties alipayProperties;

    @ApiOperation("支付宝异步通知")
    @PostMapping(value = "/notify", produces = MediaType.TEXT_PLAIN_VALUE)
    public String notify(HttpServletRequest request) {
        Map<String, String> params = extractSingleValueParams(request);
        boolean success = orderService.handleAlipayNotify(params);
        // 支付宝要求商户成功处理后返回纯文本 success，否则会按策略重试通知。
        return success ? "success" : "failure";
    }

    @ApiOperation("支付宝同步返回")
    @GetMapping("/return")
    public ResponseEntity<Void> returnUrl(HttpServletRequest request) {
        String orderNo = request.getParameter("out_trade_no");
        String redirectUrl = orderService.buildAlipayReturnRedirectUrl(orderNo);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(URI.create(redirectUrl));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @ApiOperation("支付宝沙箱支付桥接页（微信开发者工具调试）")
    @GetMapping(value = "/bridge", produces = MediaType.TEXT_HTML_VALUE)
    public String bridge(@RequestParam String token) {
        try {
            String debugReturnUrl = resolveWeappDebugReturnUrl();
            return orderService.createAlipayBridgeHtml(token, debugReturnUrl);
        } catch (BusinessException e) {
            return buildReadableErrorHtml("支付宝沙箱支付无法继续", e.getMessage());
        } catch (Exception e) {
            log.error("生成支付宝沙箱调试桥接页失败", e);
            return buildReadableErrorHtml("支付宝沙箱支付异常", "请返回小程序订单页重新发起支付：" + e.getMessage());
        }
    }

    @ApiOperation("支付宝沙箱支付调试返回页")
    @GetMapping(value = "/weapp-debug-return", produces = MediaType.TEXT_HTML_VALUE)
    public String weappDebugReturn(@RequestParam(value = "out_trade_no", required = false) String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            return buildReadableErrorHtml("订单号缺失", "支付宝返回参数中没有订单号，请返回小程序订单页刷新查看支付状态。");
        }
        return buildWeappDebugReturnHtml(orderNo);
    }

    /**
     * 支付完成页在微信 web-view 内使用 JSSDK 主动回到小程序订单详情，失败时保留手动按钮兜底。
     */
    private String buildWeappDebugReturnHtml(String orderNo) {
        String miniProgramUrl = "/pages/OrderDetail/index?orderNo=" + escapeUrlParam(orderNo);
        String escapedMiniProgramUrl = escapeHtml(miniProgramUrl);
        String jsMiniProgramUrl = escapeJavaScriptString(miniProgramUrl);
        return "<!doctype html><html>"
                + buildMobileHead("支付调试完成")
                + "<body style=\"margin:0;min-height:100vh;background:#f7f8fa;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;color:#111827;\">"
                + "<main style=\"min-height:100vh;box-sizing:border-box;display:flex;flex-direction:column;align-items:center;justify-content:center;padding:calc(24px + env(safe-area-inset-top)) 20px calc(24px + env(safe-area-inset-bottom));text-align:center;\">"
                + "<h3 style=\"margin:0 0 12px;font-size:20px;line-height:1.4;\">支付宝沙箱支付已完成</h3>"
                + "<p style=\"margin:0 0 18px;max-width:320px;color:#6b7280;font-size:14px;line-height:1.7;\">正在返回小程序订单详情并刷新支付状态。若没有自动跳转，请点击下方按钮。</p>"
                + "<button type=\"button\" onclick=\"returnToMiniProgram()\" style=\"box-sizing:border-box;width:100%;max-width:280px;height:44px;border:0;border-radius:999px;background:#1677ff;color:#fff;font-size:15px;font-weight:600;\">返回订单详情</button>"
                + "<p style=\"margin:14px 0 0;max-width:320px;color:#9ca3af;font-size:12px;line-height:1.6;\">外部浏览器打开时无法自动返回微信开发者工具，请手动回到小程序刷新。</p>"
                + "</main>"
                + "<script src=\"https://res.wx.qq.com/open/js/jweixin-1.3.2.js\"></script>"
                + "<script>var targetUrl='" + jsMiniProgramUrl + "';"
                + "var retryCount=0;"
                + "function returnToMiniProgram(){if(window.wx&&window.wx.miniProgram){window.wx.miniProgram.redirectTo({url:targetUrl});return;}if(retryCount++<10){setTimeout(returnToMiniProgram,300);}}"
                + "document.addEventListener('DOMContentLoaded',function(){setTimeout(returnToMiniProgram,300);});</script>"
                + "<noscript><a href=\"" + escapedMiniProgramUrl + "\">返回订单详情</a></noscript>"
                + "</body></html>";
    }

    /**
     * web-view 调试页不能只返回 404/空白页，明确展示错误原因能减少联调成本。
     */
    private String buildReadableErrorHtml(String title, String message) {
        return "<!doctype html><html>"
                + buildMobileHead(title)
                + "<body style=\"margin:0;min-height:100vh;background:#f7f8fa;font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',sans-serif;color:#111827;\">"
                + "<main style=\"min-height:100vh;box-sizing:border-box;display:flex;flex-direction:column;align-items:center;justify-content:center;padding:calc(24px + env(safe-area-inset-top)) 20px calc(24px + env(safe-area-inset-bottom));text-align:center;\">"
                + "<h3 style=\"margin:0 0 12px;font-size:20px;line-height:1.4;\">" + escapeHtml(title) + "</h3>"
                + "<p style=\"margin:0;max-width:320px;color:#374151;font-size:14px;line-height:1.7;\">" + escapeHtml(message) + "</p>"
                + "<p style=\"margin:16px 0 0;max-width:320px;color:#888;font-size:13px;line-height:1.6;\">请返回微信开发者工具中的小程序订单页重试。</p>"
                + "</main>"
                + "</body></html>";
    }

    /**
     * 本地调试 HTML 统一声明标准移动端 viewport，防止微信 web-view 按 PC 页面比例缩放。
     */
    private String buildMobileHead(String title) {
        return "<head><meta charset=\"utf-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, viewport-fit=cover\">"
                + "<title>" + escapeHtml(title) + "</title></head>";
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String escapeJavaScriptString(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\r", "")
                .replace("\n", "");
    }

    private String escapeUrlParam(String value) {
        if (value == null) {
            return "";
        }
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }

    /**
     * 支付宝同步返回地址必须保持公网可访问，避免从 cpolar 代理请求中误推断出 localhost 或异常地址。
     */
    private String resolveWeappDebugReturnUrl() {
        String configuredBaseUrl = alipayProperties.getWeappDebugBridgeBaseUrl();
        if (StringUtils.hasText(configuredBaseUrl)) {
            return configuredBaseUrl.replaceAll("/+$", "") + "/order/pay/alipay/weapp-debug-return";
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/order/pay/alipay/weapp-debug-return")
                .toUriString();
    }

    /**
     * 支付宝通知参数是表单键值对，验签 SDK 需要 Map<String, String>。
     */
    private Map<String, String> extractSingleValueParams(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((key, values) -> {
            if (values != null && values.length > 0) {
                params.put(key, values[0]);
            }
        });
        return params;
    }
}
