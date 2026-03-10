package eden.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 商品查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductQueryDTO extends PageDTO {

    /** 分类ID */
    private Long categoryId;

    /** 关键词搜索 */
    private String keyword;

    /** 品牌 */
    private String brand;

    /** 最低价格 */
    private BigDecimal minPrice;

    /** 最高价格 */
    private BigDecimal maxPrice;

    /** 是否热门 */
    private Integer isHot;

    /** 是否新品 */
    private Integer isNew;

    /** 状态 */
    private Integer status;

    /** 排序字段 */
    private String sortField;

    /** 排序方式：asc/desc */
    private String sortOrder;

    /** 分页偏移量 */
    private int offset;

    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public int getOffset() {
        return this.offset;
    }
}
