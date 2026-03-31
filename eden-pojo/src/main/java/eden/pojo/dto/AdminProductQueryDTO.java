package eden.pojo.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理后台商品查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class AdminProductQueryDTO extends PageDTO {

    /** 关键词搜索 (name 或 subtitle) */
    private String keyword;

    /** 分类ID */
    private Long categoryId;

    /** 状态: 0-下架, 1-上架 */
    private Integer status;

    /** 分页偏移量 (由于 PageDTO 没有暴露设值，我们需要自己计算) */
    private int offset;

    public void setOffset(int offset) {
        this.offset = offset;
    }
}
