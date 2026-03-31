package eden.mapper;

import eden.pojo.Product;
import eden.pojo.dto.ProductQueryDTO;
import eden.pojo.dto.AdminProductQueryDTO;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 商品 Mapper 接口
 */
public interface ProductMapper {

    /**
     * 根据ID查询商品
     */
    Product selectById(@Param("id") Long id);

    /**
     * 条件查询商品列表
     */
    List<Product> selectByCondition(ProductQueryDTO query);

    /**
     * 统计条件查询总数
     */
    long countByCondition(ProductQueryDTO query);

    /**
     * 查询热门商品
     */
    List<Product> selectHotProducts(@Param("limit") int limit);

    /**
     * 查询新品
     */
    List<Product> selectNewProducts(@Param("limit") int limit);

    /**
     * 根据分类ID查询商品
     */
    List<Product> selectByCategoryId(@Param("categoryId") Long categoryId);

    /**
     * 后台：全量查询商品列表（不受状态=1限制）
     */
    List<Product> selectAdminList(AdminProductQueryDTO query);

    /**
     * 后台：统计全量查询商品总数
     */
    long countAdminList(AdminProductQueryDTO query);

    /**
     * 插入商品
     */
    int insert(Product product);

    /**
     * 更新商品
     */
    int update(Product product);

    /**
     * 删除商品
     */
    int deleteById(@Param("id") Long id);

    /**
     * 更新商品状态（上下架）
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 扣减库存
     */
    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 增加库存（回滚）
     */
    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 增加销量
     */
    int increaseSales(@Param("id") Long id, @Param("quantity") Integer quantity);

    /**
     * 扣减库存（别名）
     */
    default int deductStock(Long id, Integer quantity) {
        return decreaseStock(id, quantity);
    }

    /**
     * 增加库存（别名）
     */
    default int addStock(Long id, Integer quantity) {
        return increaseStock(id, quantity);
    }
}
