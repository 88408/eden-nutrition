package eden.pojo.dto;

import lombok.Data;

/**
 * 报表查询条件。
 * <p>grain 支持 day/month，用于控制销售和订单趋势的聚合粒度。</p>
 */
@Data
public class ReportQueryDTO {
    private String startTime;
    private String endTime;
    private String grain = "day";
    private Long categoryId;
    private Integer limit = 10;
}
