package eden.web.controller;

import eden.common.result.Result;
import eden.pojo.dto.CartDTO;
import eden.pojo.vo.CartVO;
import eden.service.CartService;
import eden.web.annotation.CurrentUser;
import eden.web.annotation.RequireLogin;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 购物车控制器
 */
@Api(tags = "购物车")
@RestController
@RequestMapping("/cart")
@RequireLogin
public class CartController {

    @Autowired
    private CartService cartService;

    @ApiOperation("获取购物车")
    @GetMapping
    public Result<CartVO> getCart(@CurrentUser Long userId) {
        CartVO cart = cartService.getCart(userId);
        return Result.success(cart);
    }

    @ApiOperation("添加商品到购物车")
    @PostMapping("/add")
    public Result<Void> addToCart(@CurrentUser Long userId, @Validated @RequestBody CartDTO cartDTO) {
        cartService.addToCart(userId, cartDTO);
        return Result.success();
    }

    @ApiOperation("更新商品数量")
    @PutMapping("/quantity")
    public Result<Void> updateQuantity(@CurrentUser Long userId,
                                       @RequestParam Long productId,
                                       @RequestParam(required = false) Long skuId,
                                       @RequestParam Integer quantity) {
        cartService.updateQuantity(userId, productId, skuId, quantity);
        return Result.success();
    }

    @ApiOperation("删除购物车商品")
    @DeleteMapping("/{productId}")
    public Result<Void> removeFromCart(@CurrentUser Long userId,
                                       @PathVariable Long productId,
                                       @RequestParam(required = false) Long skuId) {
        cartService.removeFromCart(userId, productId, skuId);
        return Result.success();
    }

    @ApiOperation("清空购物车")
    @DeleteMapping("/clear")
    public Result<Void> clearCart(@CurrentUser Long userId) {
        cartService.clearCart(userId);
        return Result.success();
    }

    @ApiOperation("获取购物车商品数量")
    @GetMapping("/count")
    public Result<Integer> getCartItemCount(@CurrentUser Long userId) {
        int count = cartService.getCartItemCount(userId);
        return Result.success(count);
    }

    @ApiOperation("选中/取消选中商品")
    @PutMapping("/select")
    public Result<Void> selectItem(@CurrentUser Long userId,
                                   @RequestParam Long productId,
                                   @RequestParam(required = false) Long skuId,
                                   @RequestParam Boolean selected) {
        cartService.selectItem(userId, productId, skuId, selected);
        return Result.success();
    }

    @ApiOperation("全选/取消全选")
    @PutMapping("/selectAll")
    public Result<Void> selectAll(@CurrentUser Long userId, @RequestParam Boolean selected) {
        cartService.selectAll(userId, selected);
        return Result.success();
    }
}
