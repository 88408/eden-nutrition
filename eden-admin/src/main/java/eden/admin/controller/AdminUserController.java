package eden.admin.controller;

import eden.admin.annotation.RequireAdminLogin;
import eden.admin.annotation.RequirePermission;
import eden.common.result.Result;
import eden.pojo.dto.LoginDTO;
import eden.pojo.vo.LoginVO;
import eden.pojo.vo.UserVO;
import eden.service.RbacService;
import eden.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "管理后台-管理员接口")
@RestController
@RequestMapping("/admin/user")
public class AdminUserController {

    @Autowired
    private UserService userService;

    @Autowired
    private RbacService rbacService;

    @ApiOperation("管理员登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO) {
        return Result.success(userService.adminLogin(loginDTO));
    }

    @ApiOperation("获取当前管理员信息")
    @GetMapping("/info")
    @RequireAdminLogin
    public Result<UserVO> info(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        if (adminId == null) {
            return Result.fail(eden.common.result.ResultCode.UNAUTHORIZED);
        }
        return Result.success(userService.getUserById(adminId));
    }

    @ApiOperation("管理员注销")
    @PostMapping("/logout")
    @RequireAdminLogin
    public Result<Void> logout(HttpServletRequest request) {
        Long adminId = (Long) request.getAttribute("adminId");
        if (adminId != null) {
            userService.logout(adminId);
        }
        return Result.success();
    }

    /**
     * 为后台用户分配角色，演示 RBAC 时可用于快速切换管理员权限范围。
     */
    @PutMapping("/{userId}/roles")
    @RequireAdminLogin
    @RequirePermission("rbac:manage")
    public Result<Void> assignUserRoles(@PathVariable Long userId, @RequestBody List<Long> roleIds) {
        rbacService.assignUserRoles(userId, roleIds);
        return Result.success();
    }
}
