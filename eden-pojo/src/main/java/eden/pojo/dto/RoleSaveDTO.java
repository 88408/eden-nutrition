package eden.pojo.dto;

import lombok.Data;

import java.util.List;

/**
 * 角色保存请求。
 * <p>permissionIds 允许在创建或修改角色时同步分配权限，便于演示 RBAC 闭环。</p>
 */
@Data
public class RoleSaveDTO {
    private Long id;
    private String name;
    private String code;
    private String description;
    private Integer status;
    private List<Long> permissionIds;
}
