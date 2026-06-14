package eden.admin.controller;

import eden.admin.annotation.RequireAdminLogin;
import eden.admin.annotation.RequirePermission;
import eden.common.result.Result;
import eden.pojo.dto.AdminOrderQueryDTO;
import eden.pojo.dto.OrderDeliverDTO;
import eden.pojo.vo.OrderAdminVO;
import eden.pojo.vo.OrderDetailAdminVO;
import eden.pojo.vo.PageVO;
import eden.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 后台订单管理接口
 */
@RestController
@RequestMapping("/admin/order")
@Tag(name = "后台业务-订单管理")
@RequireAdminLogin
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping("/list")
    @Operation(summary = "分页查询订单")
    @RequirePermission("order:view")
    public Result<PageVO<OrderAdminVO>> list(AdminOrderQueryDTO queryDTO) {
        PageVO<OrderAdminVO> pageInfo = orderService.getAdminOrderPage(queryDTO);
        return Result.success(pageInfo);
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "获取订单详情")
    @RequirePermission("order:view")
    public Result<OrderDetailAdminVO> detail(@PathVariable("orderId") Long orderId) {
        OrderDetailAdminVO detail = orderService.getAdminOrderDetail(orderId);
        return Result.success(detail);
    }

    @PostMapping("/deliver")
    @Operation(summary = "订单发货")
    @RequirePermission("order:deliver")
    public Result<Void> deliver(@Validated @RequestBody OrderDeliverDTO deliverDTO) {
        orderService.deliverOrder(deliverDTO);
        return Result.success(null);
    }
}
