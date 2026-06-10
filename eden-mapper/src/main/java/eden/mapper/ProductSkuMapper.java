package eden.mapper;

import eden.pojo.ProductSku;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 商品 SKU Mapper。
 * <p>SKU 库存单独维护，避免多规格商品只扣减主商品库存导致演示数据失真。</p>
 */
public interface ProductSkuMapper {
    ProductSku selectById(@Param("id") Long id);

    List<ProductSku> selectByProductId(@Param("productId") Long productId);

    int insert(ProductSku sku);

    int update(ProductSku sku);

    int deleteByProductId(@Param("productId") Long productId);

    int decreaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);

    int increaseStock(@Param("id") Long id, @Param("quantity") Integer quantity);
}
