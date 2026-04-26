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

    /** 分页偏移量 */
    private Integer offset;

    /** 分页大小（limit） */
    private Integer limit;

    /**
     * 获取偏移量
     */
    public int getOffset() {
        if (offset != null) {
            return offset;
        }
        return (pageNum - 1) * pageSize;
    }

    /**
     * 获取分页大小
     */
    public int getLimit() {
        if (limit != null) {
            return limit;
        }
        return pageSize;
    }
}
