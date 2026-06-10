package eden.mapper;

import eden.pojo.ProductReview;
import eden.pojo.vo.ProductReviewVO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 商品评价 Mapper 接口
 */
public interface ProductReviewMapper {

    /**
     * 根据ID查询评价
     */
    ProductReview selectById(@Param("id") Long id);

    /**
     * 查询商品的评价列表
     */
    List<ProductReview> selectByProductId(@Param("productId") Long productId,
                                          @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 查询当前用户发表过的评价，并补充商品名称和主图，供“我的评价”页面展示。
     */
    List<ProductReviewVO> selectByUserId(@Param("userId") Long userId,
                                         @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计商品评价数
     */
    long countByProductId(@Param("productId") Long productId);

    /**
     * 统计当前用户评价数量，用于“我的评价”分页。
     */
    long countByUserId(@Param("userId") Long userId);

    /**
     * 计算商品平均评分
     */
    Double avgRatingByProductId(@Param("productId") Long productId);

    /**
     * 插入评价
     */
    int insert(ProductReview review);

    /**
     * 更新评价状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 删除评价
     */
    int deleteById(@Param("id") Long id);
}
