package eden.web.controller;

import eden.common.result.Result;
import eden.pojo.UserAddress;
import eden.service.UserAddressService;
import eden.web.annotation.CurrentUser;
import eden.web.annotation.RequireLogin;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收货地址控制器
 */
@Api(tags = "收货地址")
@RestController
@RequestMapping("/address")
@RequireLogin
public class AddressController {

    @Autowired
    private UserAddressService userAddressService;

    @ApiOperation("获取收货地址列表")
    @GetMapping("/list")
    public Result<List<UserAddress>> list(@CurrentUser Long userId) {
        List<UserAddress> addresses = userAddressService.listByUserId(userId);
        return Result.success(addresses);
    }

    @ApiOperation("获取默认地址")
    @GetMapping("/default")
    public Result<UserAddress> getDefault(@CurrentUser Long userId) {
        UserAddress address = userAddressService.getDefaultAddress(userId);
        return Result.success(address);
    }

    @ApiOperation("获取地址详情")
    @GetMapping("/{id}")
    public Result<UserAddress> getById(@PathVariable Long id) {
        UserAddress address = userAddressService.getById(id);
        return Result.success(address);
    }

    @ApiOperation("添加收货地址")
    @PostMapping
    public Result<Void> add(@CurrentUser Long userId, @RequestBody UserAddress address) {
        userAddressService.add(userId, address);
        return Result.success();
    }

    @ApiOperation("更新收货地址")
    @PutMapping
    public Result<Void> update(@CurrentUser Long userId, @RequestBody UserAddress address) {
        userAddressService.update(userId, address);
        return Result.success();
    }

    @ApiOperation("删除收货地址")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@CurrentUser Long userId, @PathVariable Long id) {
        userAddressService.delete(userId, id);
        return Result.success();
    }

    @ApiOperation("设置默认地址")
    @PutMapping("/default/{id}")
    public Result<Void> setDefault(@CurrentUser Long userId, @PathVariable Long id) {
        userAddressService.setDefault(userId, id);
        return Result.success();
    }
}
