package eden.pojo.vo;

import lombok.Data;
import java.util.List;

/**
 * 分页结果 VO
 */
@Data
public class PageVO<T> {

    /** 数据列表 */
    private List<T> list;

    /** 总记录数 */
    private Long total;

    /** 当前页码 */
    private Integer pageNum;

    /** 每页数量 */
    private Integer pageSize;

    /** 总页数 */
    private Integer pages;

    /** 是否有下一页 */
    private Boolean hasNext;

    /** 是否有上一页 */
    private Boolean hasPrev;

    public static <T> PageVO<T> of(List<T> list, Long total, Integer pageNum, Integer pageSize) {
        PageVO<T> pageVO = new PageVO<>();
        pageVO.setList(list);
        pageVO.setTotal(total);
        pageVO.setPageNum(pageNum);
        pageVO.setPageSize(pageSize);
        pageVO.setPages((int) Math.ceil((double) total / pageSize));
        pageVO.setHasNext(pageNum < pageVO.getPages());
        pageVO.setHasPrev(pageNum > 1);
        return pageVO;
    }
}
