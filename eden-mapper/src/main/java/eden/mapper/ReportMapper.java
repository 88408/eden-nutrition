package eden.mapper;

import eden.pojo.dto.ReportQueryDTO;
import eden.pojo.vo.ReportMetricVO;

import java.util.List;

/**
 * 后台报表 Mapper。
 * <p>报表 SQL 统一放在该 Mapper，避免 Dashboard 继续依赖静态或模拟数据。</p>
 */
public interface ReportMapper {
    List<ReportMetricVO> selectSalesByCategory(ReportQueryDTO query);

    List<ReportMetricVO> selectSalesTrend(ReportQueryDTO query);

    List<ReportMetricVO> selectOrderTrend(ReportQueryDTO query);

    List<ReportMetricVO> selectTopProducts(ReportQueryDTO query);
}
