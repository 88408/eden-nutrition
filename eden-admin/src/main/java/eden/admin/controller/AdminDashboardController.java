package eden.admin.controller;

import eden.admin.annotation.RequireAdminLogin;
import eden.common.result.Result;
import eden.pojo.vo.DashboardStatItemVO;
import eden.pojo.vo.SalesRevenueVO;
import eden.service.DashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/dashboard")
@Tag(name = "后台业务-仪表盘")
@RequireAdminLogin
public class AdminDashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    public Result<List<DashboardStatItemVO>> getDashboardStats() {
        return Result.success(dashboardService.getDashboardStats());
    }

    @GetMapping("/sales")
    public Result<List<SalesRevenueVO>> getSalesRevenue(@RequestParam(defaultValue = "7") Integer days) {
        return Result.success(dashboardService.getSalesRevenue(days));
    }
}
