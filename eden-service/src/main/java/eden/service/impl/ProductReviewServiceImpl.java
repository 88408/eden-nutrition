package eden.service.impl;

import eden.common.exception.BusinessException;
import eden.common.result.ResultCode;
import eden.mapper.ProductReviewMapper;
import eden.pojo.ProductReview;
import eden.pojo.vo.PageVO;
import eden.service.ProductReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品评价服务实现类
 */
@Service
public class ProductReviewServiceImpl implements ProductReviewService {

    @Autowired
    private ProductReviewMapper productReviewMapper;

    @Override
    public PageVO<ProductReview> getProductReviews(Long productId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<ProductReview> reviews = productReviewMapper.selectByProductId(productId, offset, pageSize);
        long total = productReviewMapper.countByProductId(productId);
        return PageVO.of(reviews, total, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addReview(Long userId, ProductReview review) {
        review.setUserId(userId);
        review.setStatus(1); // 默认审核通过（可改为0需要审核）
        
        // 验证评分范围
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new BusinessException("评分必须在1-5之间");
        }

        productReviewMapper.insert(review);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteReview(Long userId, Long reviewId) {
        ProductReview review = productReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评价不存在");
        }
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        productReviewMapper.deleteById(reviewId);
    }

    @Override
    public Double getAverageRating(Long productId) {
        Double avg = productReviewMapper.avgRatingByProductId(productId);
        return avg != null ? Math.round(avg * 10) / 10.0 : 5.0;
    }

    @Override
    public long getReviewCount(Long productId) {
        return productReviewMapper.countByProductId(productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditReview(Long reviewId, Integer status) {
        ProductReview review = productReviewMapper.selectById(reviewId);
        if (review == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "评价不存在");
        }
        productReviewMapper.updateStatus(reviewId, status);
    }
}
