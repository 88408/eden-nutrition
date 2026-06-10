package eden.web.controller;

import eden.common.result.Result;
import eden.pojo.ProductReview;
import eden.pojo.vo.PageVO;
import eden.pojo.vo.ProductReviewVO;
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
 * 商品评价控制器，提供商品维度评价、当前用户评价和评价维护能力。
 */
@Api(tags = "商品评价")
@RestController
@RequestMapping("/review")
public class ReviewController {

    @Autowired
    private ProductReviewService productReviewService;

    /**
     * 商品详情页评价列表，按商品维度公开展示已审核评价。
     */
    @ApiOperation("获取商品评价列表")
    @GetMapping("/product/{productId}")
    public Result<PageVO<ProductReview>> getProductReviews(
            @PathVariable Long productId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageVO<ProductReview> page = productReviewService.getProductReviews(productId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 个人中心“我的评价”列表，只使用登录态解析出的 userId，避免用户传参越权。
     */
    @ApiOperation("获取我的评价列表")
    @RequireLogin
    @GetMapping("/my")
    public Result<PageVO<ProductReviewVO>> getMyReviews(
            @CurrentUser Long userId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        PageVO<ProductReviewVO> page = productReviewService.getMyReviews(userId, pageNum, pageSize);
        return Result.success(page);
    }

    /**
     * 商品评分统计，用于评价全部页头部展示评分概览。
     */
    @ApiOperation("获取商品评价统计")
    @GetMapping("/product/{productId}/stats")
    public Result<Map<String, Object>> getReviewStats(@PathVariable Long productId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("avgRating", productReviewService.getAverageRating(productId));
        stats.put("totalCount", productReviewService.getReviewCount(productId));
        return Result.success(stats);
    }

    /**
     * 新增评价，用户身份从登录态获取，禁止前端指定评价归属用户。
     */
    @ApiOperation("添加评价")
    @RequireLogin
    @PostMapping
    public Result<Void> addReview(@CurrentUser Long userId, @RequestBody ProductReview review) {
        productReviewService.addReview(userId, review);
        return Result.success();
    }

    /**
     * 删除评价，服务层会再次校验评价是否属于当前用户。
     */
    @ApiOperation("删除评价")
    @RequireLogin
    @DeleteMapping("/{reviewId}")
    public Result<Void> deleteReview(@CurrentUser Long userId, @PathVariable Long reviewId) {
        productReviewService.deleteReview(userId, reviewId);
        return Result.success();
    }
}
