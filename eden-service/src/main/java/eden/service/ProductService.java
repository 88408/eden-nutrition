package eden.service;

import eden.pojo.Product;
import eden.pojo.dto.ProductQueryDTO;
import eden.pojo.vo.PageVO;
import java.util.List;

/**
 * 商品服务接口
 */
public interface ProductService {

    /**
     * 根据ID获取商品详情
     */
    Product getById(Long id);

    /**
     * 条件查询商品列表（分页）
     */
    PageVO<Product> queryProducts(ProductQueryDTO queryDTO);

    /**
     * 获取热门商品
     */
    List<Product> getHotProducts(int limit);

    /**
     * 获取新品列表
     */
    List<Product> getNewProducts(int limit);

    /**
     * 根据分类获取商品
     */
    List<Product> getByCategory(Long categoryId);

    /**
     * 添加商品
     */
    void add(Product product);

    /**
     * 更新商品
     */
    void update(Product product);

    /**
     * 删除商品
     */
    void delete(Long id);

    /**
     * 上架/下架商品
     */
    void updateStatus(Long id, Integer status);

    /**
     * 扣减库存
     */
    boolean decreaseStock(Long productId, Integer quantity);

    /**
     * 增加库存（回滚）
     */
    void increaseStock(Long productId, Integer quantity);

    /**
     * 增加销量
     */
    void increaseSales(Long productId, Integer quantity);
}
