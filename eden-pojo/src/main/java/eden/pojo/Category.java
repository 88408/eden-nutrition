package eden.pojo;

import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品分类实体类
 */
@Data
public class Category implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 分类ID */
    private Long id;

    /** 分类名称 */
    private String name;

    /** 父分类ID，0表示一级分类 */
    private Long parentId;

    /** 分类层级：1-一级 2-二级 */
    private Integer level;

    /** 排序值 */
    private Integer sortOrder;

    /** 分类图标 */
    private String icon;

    /** 状态：0-禁用 1-启用 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}
