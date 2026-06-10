package eden.pojo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 后台权限实体。
 * <p>权限同时承载菜单和按钮/接口权限，type=1 表示菜单，type=2 表示按钮或接口动作。</p>
 */
@Data
public class Permission implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long parentId;
    private String name;
    private String code;
    private String path;
    private String component;
    private String icon;
    private Integer type;
    private Integer sortOrder;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
