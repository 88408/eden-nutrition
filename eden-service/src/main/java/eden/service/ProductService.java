package eden.service;

import eden.pojo.Product;
import eden.pojo.dto.ProductQueryDTO;
import eden.pojo.dto.AdminProductQueryDTO;
import eden.pojo.dto.ProductSaveDTO;
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

    /**
     * B端管理后台专用：获取商品分页列表
     */
    PageVO<Product> getAdminProductPage(AdminProductQueryDTO queryDTO);

    /**
     * B端管理后台专用：根据ID获取商品（无视上下架状态，不走缓存）
     */
    Product getAdminById(Long id);

    /**
     * B端管理后台专用：新增商品
     */
    void saveProduct(ProductSaveDTO dto);

    /**
     * B端管理后台专用：修改全量商品信息
     */
    void updateProduct(ProductSaveDTO dto);

    /**
     * 切换商品收藏状态（收藏/取消收藏）
     * @param userId 用户ID
     * @param productId 商品ID
     * @return true: 已收藏, false: 已取消收藏
     */
    boolean toggleFavorite(Long userId, Long productId);

    /**
     * 检查商品是否被用户收藏
     * @param userId 用户ID
     * @param productId 商品ID
     * @return true: 已收藏, false: 未收藏
     */
    boolean isFavorited(Long userId, Long productId);
}
