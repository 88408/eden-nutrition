package eden.pojo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 后台角色实体。
 * <p>角色用于聚合权限，后台用户可以绑定多个角色以实现细粒度 RBAC。</p>
 */
@Data
public class Role implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
