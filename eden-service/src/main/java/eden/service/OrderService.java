package eden.service;

import eden.pojo.Order;
import eden.pojo.dto.OrderCreateDTO;
import eden.pojo.vo.PageVO;
import eden.pojo.vo.AlipayDebugPayVO;
import java.util.Map;
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
     * 条件查询订单列表（管理员）
     */
    PageVO<Order> queryOrders(eden.pojo.dto.OrderQueryDTO queryDTO);

    /**
     * 订单发货
     */
    void shipOrder(String orderNo);

    /**
     * 取消订单
     */
    void cancelOrder(Long userId, String orderNo);

    /**
     * 兼容旧支付入口，支付宝支付返回沙箱支付表单，不再直接模拟支付成功
     */
    String payOrder(Long userId, String orderNo, Integer payType);

    /**
     * 创建支付宝沙箱支付表单
     */
    String createAlipayPayment(Long userId, String orderNo);

    /**
     * 创建微信开发者工具调试用支付宝支付桥接地址
     */
    AlipayDebugPayVO createWeappDebugAlipayPayment(Long userId, String orderNo, String bridgeUrl, String debugReturnUrl);

    /**
     * 消费微信调试支付桥接 token，并生成自动提交支付宝沙箱的 HTML
     */
    String createAlipayBridgeHtml(String token, String debugReturnUrl);

    /**
     * 处理支付宝异步通知，验签通过且金额匹配后才更新订单支付状态
     */
    boolean handleAlipayNotify(Map<String, String> notifyParams);

    /**
     * 构造支付宝同步返回后的前端跳转地址
     */
    String buildAlipayReturnRedirectUrl(String orderNo);

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

    // --- 以下为 B端管理接口 ---
    PageVO<eden.pojo.vo.OrderAdminVO> getAdminOrderPage(eden.pojo.dto.AdminOrderQueryDTO queryDTO);
    eden.pojo.vo.OrderDetailAdminVO getAdminOrderDetail(Long orderId);
    void deliverOrder(eden.pojo.dto.OrderDeliverDTO deliverDTO);
}
