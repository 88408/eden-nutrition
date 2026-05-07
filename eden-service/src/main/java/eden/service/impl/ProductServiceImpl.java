package eden.service.impl;

import eden.common.constant.RedisConstants;
import eden.common.exception.BusinessException;
import eden.common.result.ResultCode;
import eden.mapper.ProductMapper;
import eden.pojo.Product;
import eden.pojo.dto.ProductQueryDTO;
import eden.pojo.vo.PageVO;
import eden.service.ProductService;
import eden.pojo.dto.ProductSaveDTO;
import eden.pojo.dto.AdminProductQueryDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 商品服务实现类
 */
@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public Product getById(Long id) {
        // 先从缓存获取
        String cacheKey = RedisConstants.PRODUCT_DETAIL + id;
        Product cached = (Product) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        // 从数据库获取
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        // 检查商品状态
        if (product.getStatus() != 1) {
            throw new BusinessException(ResultCode.PRODUCT_OFF_SHELF);
        }

        // 缓存商品
        redisTemplate.opsForValue().set(cacheKey, product, 
                RedisConstants.EXPIRE_PRODUCT, TimeUnit.SECONDS);

        return product;
    }

    @Override
    public PageVO<Product> queryProducts(ProductQueryDTO queryDTO) {
        // 设置默认分页参数
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }

        // 计算偏移量
        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        queryDTO.setOffset(offset);

        // 查询商品列表
        List<Product> products = productMapper.selectByCondition(queryDTO);

        // 查询总数
        long total = productMapper.countByCondition(queryDTO);

        // 构建分页结果
        return PageVO.of(products, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Product> getHotProducts(int limit) {
        // 先从缓存获取
        List<Product> cached = (List<Product>) redisTemplate.opsForValue()
                .get(RedisConstants.PRODUCT_HOT);
        if (cached != null) {
            return cached.size() > limit ? cached.subList(0, limit) : cached;
        }

        // 从数据库获取
        List<Product> products = productMapper.selectHotProducts(limit);

        // 缓存
        redisTemplate.opsForValue().set(RedisConstants.PRODUCT_HOT, products, 
                RedisConstants.EXPIRE_HOT_NEW, TimeUnit.SECONDS);

        return products;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Product> getNewProducts(int limit) {
        // 先从缓存获取
        List<Product> cached = (List<Product>) redisTemplate.opsForValue()
                .get(RedisConstants.PRODUCT_NEW);
        if (cached != null) {
            return cached.size() > limit ? cached.subList(0, limit) : cached;
        }

        // 从数据库获取
        List<Product> products = productMapper.selectNewProducts(limit);

        // 缓存
        redisTemplate.opsForValue().set(RedisConstants.PRODUCT_NEW, products, 
                RedisConstants.EXPIRE_HOT_NEW, TimeUnit.SECONDS);

        return products;
    }

    @Override
    public List<Product> getByCategory(Long categoryId) {
        return productMapper.selectByCategoryId(categoryId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Product product) {
        // 设置默认值
        if (product.getSales() == null) {
            product.setSales(0);
        }
        if (product.getStatus() == null) {
            product.setStatus(1);
        }
        if (product.getIsHot() == null) {
            product.setIsHot(0);
        }
        if (product.getIsNew() == null) {
            product.setIsNew(0);
        }

        productMapper.insert(product);

        // 初始化库存到Redis
        String stockKey = RedisConstants.PRODUCT_STOCK + product.getId();
        redisTemplate.opsForValue().set(stockKey, product.getStock());

        // 清除热门/新品缓存
        clearProductCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Product product) {
        Product existing = productMapper.selectById(product.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        productMapper.update(product);

        // 清除商品详情缓存
        redisTemplate.delete(RedisConstants.PRODUCT_DETAIL + product.getId());

        // 如果更新了库存，同步到Redis
        if (product.getStock() != null) {
            String stockKey = RedisConstants.PRODUCT_STOCK + product.getId();
            redisTemplate.opsForValue().set(stockKey, product.getStock());
        }

        // 清除热门/新品缓存
        clearProductCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        productMapper.deleteById(id);

        // 清除缓存
        redisTemplate.delete(RedisConstants.PRODUCT_DETAIL + id);
        redisTemplate.delete(RedisConstants.PRODUCT_STOCK + id);
        clearProductCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        productMapper.updateStatus(id, status);

        // 清除缓存
        redisTemplate.delete(RedisConstants.PRODUCT_DETAIL + id);
        clearProductCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean decreaseStock(Long productId, Integer quantity) {
        // 先检查Redis中的库存
        String stockKey = RedisConstants.PRODUCT_STOCK + productId;
        Long remainStock = redisTemplate.opsForValue().decrement(stockKey, quantity);
        
        if (remainStock == null || remainStock < 0) {
            // 库存不足，回滚Redis
            if (remainStock != null) {
                redisTemplate.opsForValue().increment(stockKey, quantity);
            }
            return false;
        }

        // 更新数据库库存
        int rows = productMapper.decreaseStock(productId, quantity);
        if (rows == 0) {
            // 数据库扣减失败，回滚Redis
            redisTemplate.opsForValue().increment(stockKey, quantity);
            return false;
        }

        // 清除商品详情缓存
        redisTemplate.delete(RedisConstants.PRODUCT_DETAIL + productId);

        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseStock(Long productId, Integer quantity) {
        // 更新数据库
        productMapper.increaseStock(productId, quantity);

        // 更新Redis
        String stockKey = RedisConstants.PRODUCT_STOCK + productId;
        redisTemplate.opsForValue().increment(stockKey, quantity);

        // 清除商品详情缓存
        redisTemplate.delete(RedisConstants.PRODUCT_DETAIL + productId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void increaseSales(Long productId, Integer quantity) {
        productMapper.increaseSales(productId, quantity);

        // 清除缓存
        redisTemplate.delete(RedisConstants.PRODUCT_DETAIL + productId);
        clearProductCache();
    }

    /**
     * 清除商品相关缓存
     */
    private void clearProductCache() {
        redisTemplate.delete(RedisConstants.PRODUCT_HOT);
        redisTemplate.delete(RedisConstants.PRODUCT_NEW);
    }

    @Override
    public PageVO<Product> getAdminProductPage(AdminProductQueryDTO queryDTO) {
        if (queryDTO.getPageNum() == null || queryDTO.getPageNum() < 1) {
            queryDTO.setPageNum(1);
        }
        if (queryDTO.getPageSize() == null || queryDTO.getPageSize() < 1) {
            queryDTO.setPageSize(10);
        }

        int offset = (queryDTO.getPageNum() - 1) * queryDTO.getPageSize();
        queryDTO.setOffset(offset);

        List<Product> products = productMapper.selectAdminList(queryDTO);
        long total = productMapper.countAdminList(queryDTO);

        return PageVO.of(products, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public Product getAdminById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProduct(ProductSaveDTO dto) {
        Product product = new Product();
        product.setName(dto.getName());
        product.setSubtitle(dto.getSubtitle());
        product.setCategoryId(dto.getCategoryId());
        product.setMainImage(dto.getMainImage());
        product.setSubImages(dto.getSubImages());
        product.setDetail(dto.getDetail());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock() != null ? dto.getStock() : 0);
        product.setIsHot(dto.getIsHot() != null ? dto.getIsHot() : 0);
        product.setIsNew(dto.getIsNew() != null ? dto.getIsNew() : 0);
        product.setStatus(dto.getStatus() != null ? dto.getStatus() : 0);
        product.setSales(0);

        this.add(product);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(ProductSaveDTO dto) {
        if (dto.getId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR);
        }

        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getName());
        product.setSubtitle(dto.getSubtitle());
        product.setCategoryId(dto.getCategoryId());
        product.setMainImage(dto.getMainImage());
        product.setSubImages(dto.getSubImages());
        product.setDetail(dto.getDetail());
        product.setOriginalPrice(dto.getOriginalPrice());
        product.setPrice(dto.getPrice());
        product.setStock(dto.getStock());
        product.setIsHot(dto.getIsHot());
        product.setIsNew(dto.getIsNew());
        product.setStatus(dto.getStatus());

        this.update(product);
    }

    @Override
    public boolean toggleFavorite(Long userId, Long productId) {
        // 先检查商品是否存在
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }

        String key = RedisConstants.USER_FAVORITES + userId;
        Boolean isMember = redisTemplate.opsForSet().isMember(key, productId);
        if (Boolean.TRUE.equals(isMember)) {
            // 已收藏，执行取消收藏
            redisTemplate.opsForSet().remove(key, productId);
            return false;
        } else {
            // 未收藏，执行收藏
            redisTemplate.opsForSet().add(key, productId);
            return true;
        }
    }

    @Override
    public boolean isFavorited(Long userId, Long productId) {
        String key = RedisConstants.USER_FAVORITES + userId;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, productId));
    }
}
