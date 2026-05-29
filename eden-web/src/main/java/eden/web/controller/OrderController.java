package eden.web.controller;

import eden.common.result.Result;
import eden.pojo.Order;
import eden.pojo.dto.OrderCreateDTO;
import eden.pojo.vo.AlipayDebugPayVO;
import eden.pojo.vo.PageVO;
import eden.service.config.AlipayProperties;
import eden.service.OrderService;
import eden.web.annotation.CurrentUser;
import eden.web.annotation.RequireLogin;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * 订单控制器
 */
@Api(tags = "订单管理")
@RestController
@RequestMapping("/order")
@RequireLogin
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AlipayProperties alipayProperties;

    @ApiOperation("创建订单")
    @PostMapping("/create")
    public Result<Order> createOrder(@CurrentUser Long userId, 
                                     @Validated @RequestBody OrderCreateDTO createDTO) {
        Order order = orderService.createOrder(userId, createDTO);
        return Result.success(order);
    }

    @ApiOperation("获取订单列表")
    @GetMapping("/list")
    public Result<PageVO<Order>> getOrderList(
            @CurrentUser Long userId,
            @ApiParam("订单状态") @RequestParam(required = false) Integer status,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageVO<Order> page = orderService.getUserOrders(userId, status, pageNum, pageSize);
        return Result.success(page);
    }

    @ApiOperation("获取订单详情")
    @GetMapping("/{orderNo}")
    public Result<Order> getOrderDetail(@CurrentUser Long userId, @PathVariable String orderNo) {
        Order order = orderService.getOrderDetail(userId, orderNo);
        return Result.success(order);
    }

    @ApiOperation("取消订单")
    @PostMapping("/cancel/{orderNo}")
    public Result<Void> cancelOrder(@CurrentUser Long userId, @PathVariable String orderNo) {
        orderService.cancelOrder(userId, orderNo);
        return Result.success();
    }

    @ApiOperation("支付宝沙箱支付")
    @PostMapping("/pay/alipay/{orderNo}")
    public Result<String> createAlipayPayment(@CurrentUser Long userId, @PathVariable String orderNo) {
        String formHtml = orderService.createAlipayPayment(userId, orderNo);
        return Result.success(formHtml);
    }

    @ApiOperation("支付宝沙箱支付（微信开发者工具调试）")
    @PostMapping("/pay/alipay/weapp-debug/{orderNo}")
    public Result<AlipayDebugPayVO> createWeappDebugAlipayPayment(@CurrentUser Long userId, @PathVariable String orderNo) {
        String bridgeUrl = resolveWeappDebugBridgeUrl();
        AlipayDebugPayVO vo = orderService.createWeappDebugAlipayPayment(userId, orderNo, bridgeUrl);
        return Result.success(vo);
    }

    @ApiOperation("支付订单（兼容旧入口）")
    @PostMapping("/pay/{orderNo}")
    public Result<String> payOrder(@CurrentUser Long userId,
                                   @PathVariable String orderNo,
                                   @ApiParam("支付方式:1支付宝 2微信") @RequestParam Integer payType) {
        String formHtml = orderService.payOrder(userId, orderNo, payType);
        return Result.success(formHtml);
    }

    @ApiOperation("确认收货")
    @PostMapping("/confirm/{orderNo}")
    public Result<Void> confirmReceive(@CurrentUser Long userId, @PathVariable String orderNo) {
        orderService.confirmReceive(userId, orderNo);
        return Result.success();
    }

    @ApiOperation("删除订单")
    @DeleteMapping("/{orderNo}")
    public Result<Void> deleteOrder(@CurrentUser Long userId, @PathVariable String orderNo) {
        orderService.deleteOrder(userId, orderNo);
        return Result.success();
    }

    @ApiOperation("管理员查询订单")
    @GetMapping("/admin/list")
    public Result<PageVO<Order>> queryOrders(
            @ApiParam("订单号") @RequestParam(required = false) String orderNo,
            @ApiParam("订单状态") @RequestParam(required = false) Integer status,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        eden.pojo.dto.OrderQueryDTO queryDTO = new eden.pojo.dto.OrderQueryDTO();
        queryDTO.setOrderNo(orderNo);
        queryDTO.setStatus(status);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);

        PageVO<Order> page = orderService.queryOrders(queryDTO);
        return Result.success(page);
    }

    @ApiOperation("订单发货")
    @PostMapping("/admin/ship/{orderNo}")
    public Result<Void> shipOrder(@PathVariable String orderNo) {
        orderService.shipOrder(orderNo);
        return Result.success();
    }

    /**
     * 微信开发者工具 web-view 对 localhost、代理地址和 context-path 很敏感，优先使用显式配置生成稳定桥接地址。
     */
    private String resolveWeappDebugBridgeUrl() {
        String configuredBaseUrl = alipayProperties.getWeappDebugBridgeBaseUrl();
        if (StringUtils.hasText(configuredBaseUrl)) {
            return configuredBaseUrl.replaceAll("/+$", "") + "/order/pay/alipay/bridge";
        }
        return ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/order/pay/alipay/bridge")
                .toUriString();
    }
}
