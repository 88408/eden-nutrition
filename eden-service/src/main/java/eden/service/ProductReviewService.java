package eden.service;

import eden.pojo.ProductReview;
import eden.pojo.vo.PageVO;
import eden.pojo.vo.ProductReviewVO;

/**
 * 商品评价服务接口
 */
public interface ProductReviewService {

    /**
     * 获取商品评价列表
     */
    PageVO<ProductReview> getProductReviews(Long productId, int pageNum, int pageSize);

    /**
     * 获取当前用户的评价列表，返回商品快照字段以支撑个人中心“我的评价”。
     */
    PageVO<ProductReviewVO> getMyReviews(Long userId, int pageNum, int pageSize);

    /**
     * 添加评价
     */
    void addReview(Long userId, ProductReview review);

    /**
     * 删除评价
     */
    void deleteReview(Long userId, Long reviewId);

    /**
     * 获取商品平均评分
     */
    Double getAverageRating(Long productId);

    /**
     * 获取商品评价数量
     */
    long getReviewCount(Long productId);

    /**
     * 审核评价（管理端）
     */
    void auditReview(Long reviewId, Integer status);
}
