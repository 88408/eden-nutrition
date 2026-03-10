package eden.mapper;

import eden.pojo.ProductReview;
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
     * 统计商品评价数
     */
    long countByProductId(@Param("productId") Long productId);

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
