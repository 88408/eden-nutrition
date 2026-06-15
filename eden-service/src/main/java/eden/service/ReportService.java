package eden.service;

import eden.pojo.dto.ReportQueryDTO;
import eden.pojo.vo.ReportMetricVO;

import java.io.OutputStream;
import java.util.List;

/**
 * 后台真实报表服务。
 * <p>所有指标均来自订单、订单明细、分类和退款数据，不再依赖前端静态 mock。</p>
 */
public interface ReportService {
    List<ReportMetricVO> salesByCategory(ReportQueryDTO query);

    List<ReportMetricVO> salesTrend(ReportQueryDTO query);

    List<ReportMetricVO> orderTrend(ReportQueryDTO query);

    List<ReportMetricVO> topProducts(ReportQueryDTO query);

    void exportCsv(ReportQueryDTO query, OutputStream out);
}
