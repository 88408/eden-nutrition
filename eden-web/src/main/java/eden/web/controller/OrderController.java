package eden.web.controller;

import eden.common.result.Result;
import eden.pojo.Order;
import eden.pojo.dto.OrderCreateDTO;
import eden.pojo.vo.PageVO;
import eden.service.OrderService;
import eden.web.annotation.CurrentUser;
import eden.web.annotation.RequireLogin;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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

    @ApiOperation("支付订单")
    @PostMapping("/pay/{orderNo}")
    public Result<Void> payOrder(@PathVariable String orderNo,
                                 @ApiParam("支付方式:1支付宝 2微信") @RequestParam Integer payType) {
        orderService.payOrder(orderNo, payType);
        return Result.success();
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
}
