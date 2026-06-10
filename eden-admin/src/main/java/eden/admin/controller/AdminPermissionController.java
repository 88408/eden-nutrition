package eden.admin.controller;

import eden.admin.annotation.RequireAdminLogin;
import eden.admin.annotation.RequirePermission;
import eden.common.result.Result;
import eden.pojo.vo.PermissionTreeVO;
import eden.service.RbacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 后台权限管理接口。
 * <p>提供权限树给角色授权页面使用。</p>
 */
@RestController
@RequestMapping("/admin/permission")
@RequireAdminLogin
public class AdminPermissionController {

    @Autowired
    private RbacService rbacService;

    @GetMapping("/tree")
    @RequirePermission("rbac:manage")
    public Result<List<PermissionTreeVO>> tree() {
        return Result.success(rbacService.getPermissionTree());
    }
}
