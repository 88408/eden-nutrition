package eden.service.impl;

import eden.common.exception.BusinessException;
import eden.common.result.ResultCode;
import eden.mapper.ProductMapper;
import eden.mapper.ProductReviewMapper;
import eden.pojo.Product;
import eden.pojo.ProductReview;
import eden.pojo.vo.PageVO;
import eden.pojo.vo.ProductReviewVO;
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

    @Autowired
    private ProductMapper productMapper;

    @Override
    public PageVO<ProductReview> getProductReviews(Long productId, int pageNum, int pageSize) {
        int offset = (pageNum - 1) * pageSize;
        List<ProductReview> reviews = productReviewMapper.selectByProductId(productId, offset, pageSize);
        long total = productReviewMapper.countByProductId(productId);
        return PageVO.of(reviews, total, pageNum, pageSize);
    }

    @Override
    public PageVO<ProductReviewVO> getMyReviews(Long userId, int pageNum, int pageSize) {
        // pageNum/pageSize 来自前端分页参数，统一在服务层换算 offset，避免 Controller 暴露数据库分页细节。
        int offset = (pageNum - 1) * pageSize;
        List<ProductReviewVO> reviews = productReviewMapper.selectByUserId(userId, offset, pageSize);
        long total = productReviewMapper.countByUserId(userId);
        return PageVO.of(reviews, total, pageNum, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addReview(Long userId, ProductReview review) {
        if (review == null || review.getProductId() == null) {
            throw new BusinessException("商品不能为空");
        }

        Product product = productMapper.selectById(review.getProductId());
        if (product == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "商品不存在");
        }

        // 验证评分范围
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new BusinessException("评分必须在1-5之间");
        }

        if (review.getContent() == null || review.getContent().trim().isEmpty()) {
            throw new BusinessException("评价内容不能为空");
        }

        // 评价写入先保留后端接口，但商品详情页不再开放自由发表；后续恢复时应增加已完成订单归属校验。
        review.setUserId(userId);
        review.setContent(review.getContent().trim());
        review.setStatus(1); // 默认审核通过（可改为0需要审核）

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
