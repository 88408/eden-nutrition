package eden.web.controller;

import eden.common.result.Result;
import eden.pojo.Coupon;
import eden.pojo.UserCoupon;
import eden.service.CouponService;
import eden.web.annotation.CurrentUser;
import eden.web.annotation.RequireLogin;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 优惠券控制器
 */
@Api(tags = "优惠券")
@RestController
@RequestMapping("/coupon")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @ApiOperation("获取可领取的优惠券列表")
    @GetMapping("/available")
    public Result<List<Coupon>> getAvailableCoupons() {
        List<Coupon> coupons = couponService.getAvailableCoupons();
        return Result.success(coupons);
    }

    @ApiOperation("领取优惠券")
    @RequireLogin
    @PostMapping("/receive/{couponId}")
    public Result<Void> receiveCoupon(@CurrentUser Long userId, @PathVariable Long couponId) {
        couponService.receiveCoupon(userId, couponId);
        return Result.success();
    }

    @ApiOperation("获取我的优惠券列表")
    @RequireLogin
    @GetMapping("/my")
    public Result<List<UserCoupon>> getMyCoupons(
            @CurrentUser Long userId,
            @ApiParam("状态:0未使用 1已使用 2已过期") @RequestParam(required = false) Integer status) {
        List<UserCoupon> coupons = couponService.getUserCoupons(userId, status);
        return Result.success(coupons);
    }

    @ApiOperation("获取可用的优惠券(下单时)")
    @RequireLogin
    @GetMapping("/usable")
    public Result<List<UserCoupon>> getUsableCoupons(@CurrentUser Long userId) {
        List<UserCoupon> coupons = couponService.getUsableCoupons(userId);
        return Result.success(coupons);
    }
}
