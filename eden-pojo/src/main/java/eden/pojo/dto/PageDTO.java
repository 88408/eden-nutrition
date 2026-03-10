package eden.pojo.dto;

import lombok.Data;

/**
 * 分页查询 DTO
 */
@Data
public class PageDTO {

    /** 当前页码，默认1 */
    private Integer pageNum = 1;

    /** 每页数量，默认10 */
    private Integer pageSize = 10;

    /** 排序字段 */
    private String orderBy;

    /** 排序方式：asc/desc */
    private String orderDir = "desc";

    /**
     * 获取偏移量
     */
    public int getOffset() {
        return (pageNum - 1) * pageSize;
    }
}
