package eden.service;

import eden.pojo.Order;
import eden.pojo.dto.OrderCreateDTO;
import eden.pojo.vo.PageVO;
import java.util.List;

/**
 * 订单服务接口
 */
public interface OrderService {

    /**
     * 创建订单
     */
    Order createOrder(Long userId, OrderCreateDTO createDTO);

    /**
     * 根据订单号获取订单
     */
    Order getByOrderNo(String orderNo);

    /**
     * 根据ID获取订单
     */
    Order getById(Long id);

    /**
     * 获取用户订单列表
     */
    PageVO<Order> getUserOrders(Long userId, Integer status, int pageNum, int pageSize);

    /**
     * 取消订单
     */
    void cancelOrder(Long userId, String orderNo);

    /**
     * 支付订单
     */
    void payOrder(String orderNo, Integer payType);

    /**
     * 确认收货
     */
    void confirmReceive(Long userId, String orderNo);

    /**
     * 订单超时处理
     */
    void handleTimeoutOrders();

    /**
     * 获取订单详情（含订单项）
     */
    Order getOrderDetail(Long userId, String orderNo);

    /**
     * 删除订单（软删除）
     */
    void deleteOrder(Long userId, String orderNo);
}
