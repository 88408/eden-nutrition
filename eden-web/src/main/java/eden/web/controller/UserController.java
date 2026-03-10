package eden.web.controller;

import eden.common.result.Result;
import eden.pojo.dto.LoginDTO;
import eden.pojo.dto.RegisterDTO;
import eden.pojo.vo.LoginVO;
import eden.pojo.vo.UserVO;
import eden.service.UserService;
import eden.web.annotation.CurrentUser;
import eden.web.annotation.RequireLogin;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Api(tags = "用户管理")
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation("用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Validated @RequestBody RegisterDTO registerDTO) {
        userService.register(registerDTO);
        return Result.success();
    }

    @ApiOperation("用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO) {
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success(loginVO);
    }

    @ApiOperation("用户登出")
    @RequireLogin
    @PostMapping("/logout")
    public Result<Void> logout(@CurrentUser Long userId) {
        userService.logout(userId);
        return Result.success();
    }

    @ApiOperation("获取当前用户信息")
    @RequireLogin
    @GetMapping("/info")
    public Result<UserVO> getUserInfo(@CurrentUser Long userId) {
        UserVO userVO = userService.getUserById(userId);
        return Result.success(userVO);
    }

    @ApiOperation("更新用户信息")
    @RequireLogin
    @PutMapping("/info")
    public Result<Void> updateUserInfo(@CurrentUser Long userId, @RequestBody UserVO userVO) {
        userService.updateUser(userId, userVO);
        return Result.success();
    }

    @ApiOperation("修改密码")
    @RequireLogin
    @PutMapping("/password")
    public Result<Void> changePassword(@CurrentUser Long userId,
                                       @RequestParam String oldPassword,
                                       @RequestParam String newPassword) {
        userService.changePassword(userId, oldPassword, newPassword);
        return Result.success();
    }

    @ApiOperation("检查用户名是否存在")
    @GetMapping("/check/username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.existsByUsername(username);
        return Result.success(exists);
    }

    @ApiOperation("检查手机号是否存在")
    @GetMapping("/check/phone")
    public Result<Boolean> checkPhone(@RequestParam String phone) {
        boolean exists = userService.existsByPhone(phone);
        return Result.success(exists);
    }
}
