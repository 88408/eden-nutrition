package eden.web.controller;

import eden.common.result.Result;
import eden.pojo.dto.LoginDTO;
import eden.pojo.dto.PasswordResetCodeDTO;
import eden.pojo.dto.PasswordResetDTO;
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

    @ApiOperation("发送找回密码验证码")
    @PostMapping("/password/reset-code")
    public Result<Void> sendPasswordResetCode(@Validated @RequestBody PasswordResetCodeDTO codeDTO) {
        // 验证码发送前先校验手机号是否属于注册用户，避免无效手机号占用缓存资源。
        userService.sendPasswordResetCode(codeDTO.getPhone());
        return Result.success();
    }

    @ApiOperation("通过验证码重置密码")
    @PostMapping("/password/reset")
    public Result<Void> resetPassword(@Validated @RequestBody PasswordResetDTO resetDTO) {
        // 重置密码接口不依赖登录态，身份凭证来自手机号与一次性验证码。
        userService.resetPassword(resetDTO);
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

    @ApiOperation("用户签到")
    @RequireLogin
    @PostMapping("/sign")
    public Result<Void> sign(@CurrentUser Long userId) {
        userService.sign(userId);
        return Result.success();
    }

    @ApiOperation("检查今日是否已签到")
    @RequireLogin
    @GetMapping("/sign/check")
    public Result<Boolean> checkSign(@CurrentUser Long userId) {
        boolean isSigned = userService.checkSign(userId);
        return Result.success(isSigned);
    }

    @ApiOperation("获取当前用户积分")
    @RequireLogin
    @GetMapping("/points")
    public Result<Integer> getUserPoints(@CurrentUser Long userId) {
        UserVO userVO = userService.getUserById(userId);
        return Result.success(userVO.getPoints());
    }
}
