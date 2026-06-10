package eden.admin.controller;

import eden.admin.annotation.RequireAdminLogin;
import eden.admin.annotation.RequirePermission;
import eden.common.result.Result;
import eden.pojo.Permission;
import eden.pojo.Role;
import eden.pojo.dto.RoleSaveDTO;
import eden.pojo.vo.PageVO;
import eden.service.RbacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台角色管理接口。
 * <p>角色和权限分配是 RBAC 的核心演示入口。</p>
 */
@RestController
@RequestMapping("/admin/role")
@RequireAdminLogin
@RequirePermission("rbac:manage")
public class AdminRoleController {

    @Autowired
    private RbacService rbacService;

    @GetMapping("/page")
    public Result<PageVO<Role>> page(@RequestParam(required = false) String keyword,
                                     @RequestParam(defaultValue = "1") Integer pageNum,
                                     @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(rbacService.getRolePage(keyword, pageNum, pageSize));
    }

    @PostMapping
    public Result<Void> save(@RequestBody RoleSaveDTO dto) {
        rbacService.saveRole(dto);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody RoleSaveDTO dto) {
        rbacService.saveRole(dto);
        return Result.success();
    }

    @GetMapping("/{roleId}/permissions")
    public Result<List<Permission>> rolePermissions(@PathVariable Long roleId) {
        return Result.success(rbacService.getRolePermissions(roleId));
    }

    @PutMapping("/{roleId}/permissions")
    public Result<Void> assignPermissions(@PathVariable Long roleId, @RequestBody List<Long> permissionIds) {
        rbacService.assignRolePermissions(roleId, permissionIds);
        return Result.success();
    }
}
