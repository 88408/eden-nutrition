package eden.service.impl;

import eden.common.exception.BusinessException;
import eden.mapper.ProductSkuMapper;
import eden.pojo.ProductSku;
import eden.service.ProductSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 商品 SKU 服务实现。
 * <p>保存 SKU 时采用“先删后插”的全量替换策略，适合后台表单一次性维护规格列表。</p>
 */
@Service
public class ProductSkuServiceImpl implements ProductSkuService {

    @Autowired
    private ProductSkuMapper productSkuMapper;

    @Override
    public ProductSku getById(Long id) {
        return id == null ? null : productSkuMapper.selectById(id);
    }

    @Override
    public List<ProductSku> listByProductId(Long productId) {
        List<ProductSku> skuList = productSkuMapper.selectByProductId(productId);
        return skuList == null ? Collections.emptyList() : skuList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProductSkus(Long productId, List<ProductSku> skuList) {
        productSkuMapper.deleteByProductId(productId);
        if (skuList == null || skuList.isEmpty()) {
            return;
        }
        for (ProductSku sku : skuList) {
            sku.setProductId(productId);
            if (sku.getStatus() == null) {
                sku.setStatus(1);
            }
            if (sku.getStock() == null || sku.getStock() < 0 || sku.getPrice() == null) {
                throw new BusinessException("SKU 价格和库存不能为空，库存不能为负数");
            }
            productSkuMapper.insert(sku);
        }
    }

    @Override
    public boolean decreaseStock(Long skuId, Integer quantity) {
        if (skuId == null) {
            return false;
        }
        return productSkuMapper.decreaseStock(skuId, quantity) > 0;
    }

    @Override
    public void increaseStock(Long skuId, Integer quantity) {
        if (skuId != null && quantity != null && quantity > 0) {
            productSkuMapper.increaseStock(skuId, quantity);
        }
    }
}
