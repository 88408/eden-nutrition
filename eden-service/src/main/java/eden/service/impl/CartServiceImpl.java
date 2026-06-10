package eden.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eden.common.constant.RedisConstants;
import eden.common.exception.BusinessException;
import eden.common.result.ResultCode;
import eden.mapper.ProductMapper;
import eden.pojo.CartItem;
import eden.pojo.Product;
import eden.pojo.ProductSku;
import eden.pojo.dto.CartDTO;
import eden.pojo.vo.CartItemVO;
import eden.pojo.vo.CartVO;
import eden.service.CartService;
import eden.service.ProductService;
import eden.service.ProductSkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 购物车服务实现类
 * 购物车数据存储在Redis中，使用Hash结构
 * Key: eden:cart:{userId}
 * Field: productId 或 productId:skuId；带 SKU 的商品必须用组合键，避免不同规格互相覆盖
 * Value: CartItem JSON字符串
 */
@Service
public class CartServiceImpl implements CartService {

    /** 购物车最大商品种类数 */
    private static final int MAX_CART_ITEMS = 50;

    /** 单个商品最大数量 */
    private static final int MAX_ITEM_QUANTITY = 99;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductSkuService productSkuService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 将 CartItem 序列化为 JSON 字符串
     */
    private String toJson(CartItem item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (JsonProcessingException e) {
            throw new BusinessException("序列化购物车项失败");
        }
    }

    /**
     * 将 JSON 字符串反序列化为 CartItem
     */
    private CartItem fromJson(String json) {
        if (json == null || json.isEmpty()) {
            System.out.println("[CartService] fromJson: json is null or empty");
            return null;
        }
        try {
            System.out.println("[CartService] fromJson: parsing json = " + json);
            CartItem item = objectMapper.readValue(json, CartItem.class);
            System.out.println("[CartService] fromJson: parsed item productId = " + (item != null ? item.getProductId() : "null"));
            return item;
        } catch (JsonProcessingException e) {
            System.out.println("[CartService] fromJson error: " + e.getMessage());
            return null;
        }
    }

    /**
     * 生成 Redis Hash 字段名。
     * <p>旧购物车数据只有商品维度，继续使用 productId；新 SKU 购物车项使用 productId:skuId 保证同一商品不同规格可共存。</p>
     */
    private String buildFieldKey(Long productId, Long skuId) {
        return skuId == null ? String.valueOf(productId) : productId + ":" + skuId;
    }

    /**
     * 拼接规格展示名，下单和购物车展示共用同一份快照规则，避免前后端文案不一致。
     */
    private String buildSkuSpecName(ProductSku sku) {
        if (sku == null) {
            return null;
        }
        return String.join(" / ",
                java.util.stream.Stream.of(sku.getSpecName(), sku.getFlavor(), sku.getPackageSize())
                        .filter(value -> value != null && !value.isBlank())
                        .toArray(String[]::new));
    }

    @Override
    public CartVO getCart(Long userId) {
        String cartKey = RedisConstants.CART + userId;

        Map<Object, Object> rawItems = stringRedisTemplate.opsForHash().entries(cartKey);

        CartVO cartVO = new CartVO();
        List<CartItemVO> items = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
        int selectedCount = 0;

        for (Map.Entry<Object, Object> entry : rawItems.entrySet()) {
            try {
                System.out.println("[CartService] Processing entry key: " + entry.getKey() + ", value type: " + (entry.getValue() != null ? entry.getValue().getClass().getName() : "null"));
                CartItem item = fromJson((String) entry.getValue());
                if (item == null || item.getProductId() == null) {
                    System.out.println("[CartService] item is null or productId is null, skipping");
                    continue;
                }

                // 直接从数据库获取商品信息（避免缓存序列化问题）
                System.out.println("[CartService] Getting product: " + item.getProductId());
                Product product = productMapper.selectById(item.getProductId());
                System.out.println("[CartService] Product result: " + (product != null ? "id=" + product.getId() + ", status=" + product.getStatus() : "null"));
                if (product == null || product.getStatus() != 1) {
                    // 商品不存在或已下架，从购物车移除
                    stringRedisTemplate.opsForHash().delete(cartKey, entry.getKey());
                    continue;
                }

                CartItemVO itemVO = new CartItemVO();
                itemVO.setProductId(product.getId());
                ProductSku sku = null;
                if (item.getSkuId() != null) {
                    sku = productSkuService.getById(item.getSkuId());
                    if (sku == null || !product.getId().equals(sku.getProductId()) || sku.getStatus() == null || sku.getStatus() != 1) {
                        // SKU 已删除、停用或不属于该商品时移除购物车项，避免结算时出现不可用规格。
                        stringRedisTemplate.opsForHash().delete(cartKey, entry.getKey());
                        continue;
                    }
                }

                BigDecimal price = sku == null ? product.getPrice() : sku.getPrice();
                Integer stock = sku == null ? product.getStock() : Math.min(product.getStock(), sku.getStock());
                String imageUrl = sku != null && sku.getImageUrl() != null && !sku.getImageUrl().isBlank()
                        ? sku.getImageUrl()
                        : product.getMainImage();

                itemVO.setProductId(product.getId());
                itemVO.setSkuId(sku == null ? null : sku.getId());
                itemVO.setSkuSpecName(sku == null ? item.getSkuSpecName() : buildSkuSpecName(sku));
                itemVO.setProductName(product.getName());
                itemVO.setProductImage(imageUrl);
                itemVO.setPrice(price);
                itemVO.setQuantity(item.getQuantity());
                itemVO.setSelected(item.getSelected());
                itemVO.setStock(stock);

                // 计算小计
                BigDecimal subtotal = price.multiply(BigDecimal.valueOf(item.getQuantity()));
                itemVO.setSubtotal(subtotal);

                // 检查库存
                itemVO.setStockEnough(stock >= item.getQuantity());

                items.add(itemVO);

                totalQuantity += item.getQuantity();

                // 只计算选中的商品
                if (item.getSelected() != null && item.getSelected()) {
                    totalAmount = totalAmount.add(subtotal);
                    selectedCount++;
                }
            } catch (Exception e) {
                // 记录日志但不删除，保留购物车项
                e.printStackTrace();
            }
        }

        cartVO.setItems(items);
        cartVO.setTotalAmount(totalAmount);
        cartVO.setSelectedAmount(totalAmount);
        cartVO.setTotalCount(items.size());
        cartVO.setTotalQuantity(totalQuantity);
        cartVO.setSelectedCount(selectedCount);
        cartVO.setAllSelected(selectedCount == items.size() && !items.isEmpty());

        return cartVO;
    }

    @Override
    public void addToCart(Long userId, CartDTO cartDTO) {
        String cartKey = RedisConstants.CART + userId;

        // 验证商品
        Product product = productService.getById(cartDTO.getProductId());

        ProductSku sku = null;
        List<ProductSku> enabledSkus = productSkuService.listByProductId(cartDTO.getProductId()).stream()
                .filter(item -> item.getStatus() != null && item.getStatus() == 1)
                .collect(Collectors.toList());
        if (!enabledSkus.isEmpty() && cartDTO.getSkuId() == null) {
            throw new BusinessException("请选择商品规格后再加入购物车");
        }
        if (cartDTO.getSkuId() != null) {
            sku = productSkuService.getById(cartDTO.getSkuId());
            if (sku == null || !cartDTO.getProductId().equals(sku.getProductId()) || sku.getStatus() == null || sku.getStatus() != 1) {
                throw new BusinessException("所选商品规格不可用");
            }
        }

        String fieldKey = buildFieldKey(cartDTO.getProductId(), cartDTO.getSkuId());
        int stock = sku == null ? product.getStock() : Math.min(product.getStock(), sku.getStock());

        // 检查购物车中是否已存在该商品
        String existingJson = (String) stringRedisTemplate.opsForHash().get(cartKey, fieldKey);
        CartItem existingItem = fromJson(existingJson);

        if (existingItem != null) {
            // 更新数量
            int newQuantity = existingItem.getQuantity() + cartDTO.getQuantity();
            if (newQuantity > MAX_ITEM_QUANTITY) {
                newQuantity = MAX_ITEM_QUANTITY;
            }
            if (newQuantity > stock) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
            }
            existingItem.setQuantity(newQuantity);
            stringRedisTemplate.opsForHash().put(cartKey, fieldKey, toJson(existingItem));
        } else {
            // 检查购物车商品种类数量
            Long size = stringRedisTemplate.opsForHash().size(cartKey);
            if (size != null && size >= MAX_CART_ITEMS) {
                throw new BusinessException("购物车商品数量已达上限");
            }

            // 检查库存
            if (cartDTO.getQuantity() > stock) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
            }

            // 添加新商品
            CartItem newItem = new CartItem();
            newItem.setUserId(userId);
            newItem.setProductId(cartDTO.getProductId());
            newItem.setSkuId(cartDTO.getSkuId());
            newItem.setSkuSpecName(buildSkuSpecName(sku));
            newItem.setQuantity(cartDTO.getQuantity());
            newItem.setSelected(true);

            stringRedisTemplate.opsForHash().put(cartKey, fieldKey, toJson(newItem));
        }

        // 设置过期时间
        stringRedisTemplate.expire(cartKey, RedisConstants.EXPIRE_CART, TimeUnit.SECONDS);
    }

    @Override
    public void updateQuantity(Long userId, Long productId, Long skuId, Integer quantity) {
        if (quantity <= 0) {
            removeFromCart(userId, productId, skuId);
            return;
        }

        if (quantity > MAX_ITEM_QUANTITY) {
            quantity = MAX_ITEM_QUANTITY;
        }

        String cartKey = RedisConstants.CART + userId;
        String fieldKey = buildFieldKey(productId, skuId);

        String json = (String) stringRedisTemplate.opsForHash().get(cartKey, fieldKey);
        CartItem item = fromJson(json);
        if (item == null) {
            throw new BusinessException(ResultCode.CART_ITEM_NOT_FOUND);
        }

        // 检查库存
        Product product = productService.getById(productId);
        int stock = product.getStock();
        if (skuId != null) {
            ProductSku sku = productSkuService.getById(skuId);
            if (sku == null || !productId.equals(sku.getProductId()) || sku.getStatus() == null || sku.getStatus() != 1) {
                throw new BusinessException("所选商品规格不可用");
            }
            stock = Math.min(stock, sku.getStock());
        }
        if (quantity > stock) {
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        }

        item.setQuantity(quantity);
        stringRedisTemplate.opsForHash().put(cartKey, fieldKey, toJson(item));
    }

    @Override
    public void removeFromCart(Long userId, Long productId, Long skuId) {
        String cartKey = RedisConstants.CART + userId;
        String fieldKey = buildFieldKey(productId, skuId);
        stringRedisTemplate.opsForHash().delete(cartKey, fieldKey);
    }

    @Override
    public void clearCart(Long userId) {
        String cartKey = RedisConstants.CART + userId;
        stringRedisTemplate.delete(cartKey);
    }

    @Override
    public int getCartItemCount(Long userId) {
        String cartKey = RedisConstants.CART + userId;
        Long size = stringRedisTemplate.opsForHash().size(cartKey);
        return size != null ? size.intValue() : 0;
    }

    @Override
    public void selectItem(Long userId, Long productId, Long skuId, boolean selected) {
        String cartKey = RedisConstants.CART + userId;
        String fieldKey = buildFieldKey(productId, skuId);

        String json = (String) stringRedisTemplate.opsForHash().get(cartKey, fieldKey);
        CartItem item = fromJson(json);
        if (item == null) {
            throw new BusinessException(ResultCode.CART_ITEM_NOT_FOUND);
        }

        item.setSelected(selected);
        stringRedisTemplate.opsForHash().put(cartKey, fieldKey, toJson(item));
    }

    @Override
    public void selectAll(Long userId, boolean selected) {
        String cartKey = RedisConstants.CART + userId;

        Map<Object, Object> rawItems = stringRedisTemplate.opsForHash().entries(cartKey);
        for (Map.Entry<Object, Object> entry : rawItems.entrySet()) {
            CartItem item = fromJson((String) entry.getValue());
            if (item != null) {
                item.setSelected(selected);
                stringRedisTemplate.opsForHash().put(cartKey, entry.getKey().toString(), toJson(item));
            }
        }
    }
}
