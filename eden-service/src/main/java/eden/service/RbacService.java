package eden.service;

import eden.pojo.Permission;
import eden.pojo.Role;
import eden.pojo.dto.RoleSaveDTO;
import eden.pojo.vo.PageVO;
import eden.pojo.vo.PermissionTreeVO;

import java.util.List;

/**
 * 后台 RBAC 服务。
 * <p>统一提供角色、权限树、权限码和用户角色分配能力。</p>
 */
public interface RbacService {
    List<PermissionTreeVO> getPermissionTree();

    List<PermissionTreeVO> getMenuTreeByUserId(Long userId);

    List<String> getPermissionCodes(Long userId);

    List<Role> getUserRoles(Long userId);

    PageVO<Role> getRolePage(String keyword, int pageNum, int pageSize);

    void saveRole(RoleSaveDTO dto);

    void assignRolePermissions(Long roleId, List<Long> permissionIds);

    void assignUserRoles(Long userId, List<Long> roleIds);

    List<Permission> getRolePermissions(Long roleId);
}
