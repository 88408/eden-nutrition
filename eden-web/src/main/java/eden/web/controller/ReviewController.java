package eden.web.controller;

import eden.common.result.Result;
import eden.pojo.ProductReview;
import eden.pojo.vo.PageVO;
import eden.service.ProductReviewService;
import eden.web.annotation.CurrentUser;
import eden.web.annotation.RequireLogin;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 商品评价控制器
 */
@Api(tags = "商品评价")
@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ProductReviewService productReviewService;

    @ApiOperation("获取商品评价列表")
    @GetMapping("/product/{productId}")
    public Result<PageVO<ProductReview>> getProductReviews(
            @PathVariable Long productId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageVO<ProductReview> page = productReviewService.getProductReviews(productId, pageNum, pageSize);
        return Result.success(page);
    }

    @ApiOperation("获取商品评价统计")
    @GetMapping("/product/{productId}/stats")
    public Result<Map<String, Object>> getReviewStats(@PathVariable Long productId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("avgRating", productReviewService.getAverageRating(productId));
        stats.put("totalCount", productReviewService.getReviewCount(productId));
        return Result.success(stats);
    }

    @ApiOperation("添加评价")
    @RequireLogin
    @PostMapping
    public Result<Void> addReview(@CurrentUser Long userId, @RequestBody ProductReview review) {
        productReviewService.addReview(userId, review);
        return Result.success();
    }

    @ApiOperation("删除评价")
    @RequireLogin
    @DeleteMapping("/{reviewId}")
    public Result<Void> deleteReview(@CurrentUser Long userId, @PathVariable Long reviewId) {
        productReviewService.deleteReview(userId, reviewId);
        return Result.success();
    }
}
