package eden.service.impl;

import eden.mapper.ReportMapper;
import eden.pojo.dto.ReportQueryDTO;
import eden.pojo.vo.ReportMetricVO;
import eden.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

/**
 * 后台真实报表服务实现。
 * <p>CSV 导出返回 base64 文本，前端可直接生成下载文件，也避免响应头兼容问题。</p>
 */
@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Override
    public List<ReportMetricVO> salesByCategory(ReportQueryDTO query) {
        normalize(query);
        return reportMapper.selectSalesByCategory(query);
    }

    @Override
    public List<ReportMetricVO> salesTrend(ReportQueryDTO query) {
        normalize(query);
        return reportMapper.selectSalesTrend(query);
    }

    @Override
    public List<ReportMetricVO> orderTrend(ReportQueryDTO query) {
        normalize(query);
        return reportMapper.selectOrderTrend(query);
    }

    @Override
    public List<ReportMetricVO> topProducts(ReportQueryDTO query) {
        normalize(query);
        return reportMapper.selectTopProducts(query);
    }

    @Override
    public String exportCsv(ReportQueryDTO query) {
        normalize(query);
        StringBuilder csv = new StringBuilder("类型,名称,销售额,订单数,销量,退款额,客单价\n");
        appendRows(csv, "分类销售", reportMapper.selectSalesByCategory(query));
        appendRows(csv, "销售趋势", reportMapper.selectSalesTrend(query));
        appendRows(csv, "热销商品", reportMapper.selectTopProducts(query));
        return Base64.getEncoder().encodeToString(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void appendRows(StringBuilder csv, String type, List<ReportMetricVO> rows) {
        for (ReportMetricVO row : rows) {
            csv.append(type).append(',')
                    .append(safe(row.getLabel())).append(',')
                    .append(amount(row.getSalesAmount())).append(',')
                    .append(row.getOrderCount() == null ? 0 : row.getOrderCount()).append(',')
                    .append(row.getQuantity() == null ? 0 : row.getQuantity()).append(',')
                    .append(amount(row.getRefundAmount())).append(',')
                    .append(amount(row.getAverageOrderAmount())).append('\n');
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.replace(",", "，");
    }

    private BigDecimal amount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    /**
     * 统一兜底报表查询参数，避免前端漏传 grain/limit 时 SQL 聚合异常。
     */
    private void normalize(ReportQueryDTO query) {
        if (query.getGrain() == null || (!"day".equals(query.getGrain()) && !"month".equals(query.getGrain()))) {
            query.setGrain("day");
        }
        if (query.getLimit() == null || query.getLimit() <= 0) {
            query.setLimit(10);
        }
    }
}
