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
     * 更新订单状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);

    /**
     * 取消订单
     */
    int cancel(@Param("id") Long id);

    /**
     * 支付订单
     */
    int pay(@Param("id") Long id, @Param("payType") Integer payType);

    /**
     * 统计销售额（按日期范围）
     */
    java.math.BigDecimal sumPayAmount(@Param("startDate") String startDate, @Param("endDate") String endDate);
}
