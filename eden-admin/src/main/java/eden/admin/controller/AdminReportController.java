package eden.admin.controller;

import eden.admin.annotation.RequireAdminLogin;
import eden.admin.annotation.RequirePermission;
import eden.common.result.Result;
import eden.pojo.dto.ReportQueryDTO;
import eden.pojo.vo.ReportMetricVO;
import eden.service.ReportService;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 后台报表接口。
 * <p>所有指标均来自真实订单数据，支持时间范围和聚合粒度筛选。</p>
 */
@RestController
@RequestMapping("/admin/report")
@RequireAdminLogin
@RequirePermission("dashboard:view")
public class AdminReportController {

    @Autowired
    private ReportService reportService;

    @GetMapping("/sales/category")
    public Result<List<ReportMetricVO>> salesByCategory(ReportQueryDTO query) {
        return Result.success(reportService.salesByCategory(query));
    }

    @GetMapping("/sales/trend")
    public Result<List<ReportMetricVO>> salesTrend(ReportQueryDTO query) {
        return Result.success(reportService.salesTrend(query));
    }

    @GetMapping("/orders/trend")
    public Result<List<ReportMetricVO>> orderTrend(ReportQueryDTO query) {
        return Result.success(reportService.orderTrend(query));
    }

    @GetMapping("/top-products")
    public Result<List<ReportMetricVO>> topProducts(ReportQueryDTO query) {
        return Result.success(reportService.topProducts(query));
    }

    @GetMapping("/export")
    public void export(ReportQueryDTO query, HttpServletResponse response) throws IOException {
        response.setContentType("text/csv; charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode("报表导出.csv", StandardCharsets.UTF_8));
        reportService.exportCsv(query, response.getOutputStream());
    }
}
