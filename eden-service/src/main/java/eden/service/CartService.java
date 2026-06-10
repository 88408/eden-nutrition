package eden.service;

import eden.pojo.dto.CartDTO;
import eden.pojo.vo.CartVO;

/**
 * 购物车服务接口
 */
public interface CartService {

    /**
     * 获取用户购物车
     */
    CartVO getCart(Long userId);

    /**
     * 添加商品到购物车
     */
    void addToCart(Long userId, CartDTO cartDTO);

    /**
     * 更新购物车商品数量
     */
    void updateQuantity(Long userId, Long productId, Long skuId, Integer quantity);

    /**
     * 删除购物车商品
     */
    void removeFromCart(Long userId, Long productId, Long skuId);

    /**
     * 清空购物车
     */
    void clearCart(Long userId);

    /**
     * 获取购物车商品数量
     */
    int getCartItemCount(Long userId);

    /**
     * 选中/取消选中商品
     */
    void selectItem(Long userId, Long productId, Long skuId, boolean selected);

    /**
     * 全选/取消全选
     */
    void selectAll(Long userId, boolean selected);
}
