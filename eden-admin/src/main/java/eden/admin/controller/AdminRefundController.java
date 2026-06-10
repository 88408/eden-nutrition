package eden.admin.controller;

import eden.admin.annotation.RequireAdminLogin;
import eden.admin.annotation.RequirePermission;
import eden.common.result.Result;
import eden.pojo.RefundApply;
import eden.pojo.dto.RefundAuditDTO;
import eden.pojo.vo.PageVO;
import eden.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 后台退款审核接口。
 * <p>覆盖退款分页、详情、审核和执行退款四个售后关键步骤。</p>
 */
@RestController
@RequestMapping("/admin/refund")
@RequireAdminLogin
public class AdminRefundController {

    @Autowired
    private RefundService refundService;

    @GetMapping("/page")
    @RequirePermission("refund:view")
    public Result<PageVO<RefundApply>> page(@RequestParam(required = false) Integer status,
                                            @RequestParam(required = false) String keyword,
                                            @RequestParam(defaultValue = "1") Integer pageNum,
                                            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(refundService.adminPage(status, keyword, pageNum, pageSize));
    }

    @GetMapping("/{id}")
    @RequirePermission("refund:view")
    public Result<RefundApply> detail(@PathVariable Long id) {
        return Result.success(refundService.getById(id));
    }

    @PostMapping("/audit")
    @RequirePermission("refund:audit")
    public Result<Void> audit(HttpServletRequest request, @RequestBody RefundAuditDTO dto) {
        refundService.audit((Long) request.getAttribute("adminId"), dto);
        return Result.success();
    }

    @PostMapping("/execute/{id}")
    @RequirePermission("refund:execute")
    public Result<Void> execute(HttpServletRequest request, @PathVariable Long id) {
        refundService.executeRefund((Long) request.getAttribute("adminId"), id);
        return Result.success();
    }
}
