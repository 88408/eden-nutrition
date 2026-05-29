package eden.mapper;

import eden.pojo.Order;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单 Mapper 接口
 */
public interface OrderMapper {

    /**
     * 根据ID查询订单
     */
    Order selectById(@Param("id") Long id);

    /**
     * 根据订单号查询订单
     */
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询用户的订单列表
     */
    List<Order> selectByUserId(@Param("userId") Long userId, @Param("status") Integer status,
                               @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计用户订单数
     */
    long countByUserId(@Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 查询超时未支付订单
     */
    List<Order> selectUnpaidTimeout(@Param("minutes") int minutes);

    /**
     * 查询超时未支付订单（用于定时任务）
     */
    List<Order> selectTimeoutOrders(@Param("status") Integer status, @Param("timeoutTime") LocalDateTime timeoutTime);

    /**
     * 根据状态和发货时间查询订单（用于自动确认收货）
     */
    List<Order> selectByStatusAndShippedBefore(@Param("status") Integer status, @Param("shippedBefore") LocalDateTime shippedBefore);

    /**
     * 条件查询订单列表
     */
    List<Order> selectByCondition(eden.pojo.dto.OrderQueryDTO queryDTO);

    /**
     * 条件统计订单数
     */
    long countByCondition(eden.pojo.dto.OrderQueryDTO queryDTO);

    /**
     * 根据ID更新订单
     */
    int updateById(Order order);

    /**
     * 插入订单
     */
    int insert(Order order);

    /**
     * 更新订单
     */
    int update(Order order);

    /**
     * 更新通用字段
     * @param order
     * @return
     */
    int updateBasic(Order order);

    /**
     * 更新订单状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 取消订单
     */
    int cancel(@Param("id") Long id);

    /**
     * 支付订单；限定 status=0，确保支付宝重复通知不会重复触发销量和积分副作用
     */
    int pay(@Param("id") Long id,
            @Param("payType") Integer payType,
            @Param("paymentTradeNo") String paymentTradeNo);

    /**
     * 统计销售额（按日期范围）
     */
    java.math.BigDecimal sumPayAmount(@Param("startDate") String startDate, @Param("endDate") String endDate);

    // --- 以下为 B端管理接口 ---
    List<Order> selectAdminOrderList(@Param("query") eden.pojo.dto.AdminOrderQueryDTO queryDTO);
    long countAdminOrderList(@Param("query") eden.pojo.dto.AdminOrderQueryDTO queryDTO);
}
