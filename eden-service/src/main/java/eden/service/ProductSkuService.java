package eden.service;

import eden.pojo.ProductSku;

import java.util.List;

/**
 * 商品 SKU 服务。
 * <p>负责规格维护和 SKU 库存扣减/回滚。</p>
 */
public interface ProductSkuService {
    ProductSku getById(Long id);

    List<ProductSku> listByProductId(Long productId);

    void saveProductSkus(Long productId, List<ProductSku> skuList);

    boolean decreaseStock(Long skuId, Integer quantity);

    void increaseStock(Long skuId, Integer quantity);
}
