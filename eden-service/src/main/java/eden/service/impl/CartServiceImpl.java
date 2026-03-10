package eden.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eden.common.constant.RedisConstants;
import eden.common.exception.BusinessException;
import eden.common.result.ResultCode;
import eden.mapper.ProductMapper;
import eden.pojo.CartItem;
import eden.pojo.Product;
import eden.pojo.dto.CartDTO;
import eden.pojo.vo.CartItemVO;
import eden.pojo.vo.CartVO;
import eden.service.CartService;
import eden.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 购物车服务实现类
 * 购物车数据存储在Redis中，使用Hash结构
 * Key: eden:cart:{userId}
 * Field: productId
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
                itemVO.setProductName(product.getName());
                itemVO.setProductImage(product.getMainImage());
                itemVO.setPrice(product.getPrice());
                itemVO.setQuantity(item.getQuantity());
                itemVO.setSelected(item.getSelected());
                itemVO.setStock(product.getStock());

                // 计算小计
                BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                itemVO.setSubtotal(subtotal);

                // 检查库存
                itemVO.setStockEnough(product.getStock() >= item.getQuantity());

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

        String fieldKey = String.valueOf(cartDTO.getProductId());

        // 检查购物车中是否已存在该商品
        String existingJson = (String) stringRedisTemplate.opsForHash().get(cartKey, fieldKey);
        CartItem existingItem = fromJson(existingJson);

        if (existingItem != null) {
            // 更新数量
            int newQuantity = existingItem.getQuantity() + cartDTO.getQuantity();
            if (newQuantity > MAX_ITEM_QUANTITY) {
                newQuantity = MAX_ITEM_QUANTITY;
            }
            if (newQuantity > product.getStock()) {
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
            if (cartDTO.getQuantity() > product.getStock()) {
                throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
            }

            // 添加新商品
            CartItem newItem = new CartItem();
            newItem.setUserId(userId);
            newItem.setProductId(cartDTO.getProductId());
            newItem.setQuantity(cartDTO.getQuantity());
            newItem.setSelected(true);

            stringRedisTemplate.opsForHash().put(cartKey, fieldKey, toJson(newItem));
        }

        // 设置过期时间
        stringRedisTemplate.expire(cartKey, RedisConstants.EXPIRE_CART, TimeUnit.SECONDS);
    }

    @Override
    public void updateQuantity(Long userId, Long productId, Integer quantity) {
        if (quantity <= 0) {
            removeFromCart(userId, productId);
            return;
        }

        if (quantity > MAX_ITEM_QUANTITY) {
            quantity = MAX_ITEM_QUANTITY;
        }

        String cartKey = RedisConstants.CART + userId;
        String fieldKey = String.valueOf(productId);

        String json = (String) stringRedisTemplate.opsForHash().get(cartKey, fieldKey);
        CartItem item = fromJson(json);
        if (item == null) {
            throw new BusinessException(ResultCode.CART_ITEM_NOT_FOUND);
        }

        // 检查库存
        Product product = productService.getById(productId);
        if (quantity > product.getStock()) {
            throw new BusinessException(ResultCode.STOCK_NOT_ENOUGH);
        }

        item.setQuantity(quantity);
        stringRedisTemplate.opsForHash().put(cartKey, fieldKey, toJson(item));
    }

    @Override
    public void removeFromCart(Long userId, Long productId) {
        String cartKey = RedisConstants.CART + userId;
        String fieldKey = String.valueOf(productId);
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
    public void selectItem(Long userId, Long productId, boolean selected) {
        String cartKey = RedisConstants.CART + userId;
        String fieldKey = String.valueOf(productId);

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
