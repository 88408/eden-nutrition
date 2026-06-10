package eden.pojo.vo;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 通用报表指标行。
 * <p>用于分类销售、趋势、热销商品等场景，减少前端对多套返回结构的适配成本。</p>
 */
@Data
public class ReportMetricVO {
    private String label;
    private Long categoryId;
    private Long productId;
    private BigDecimal salesAmount;
    private BigDecimal refundAmount;
    private Long orderCount;
    private Long quantity;
    private BigDecimal averageOrderAmount;
}
