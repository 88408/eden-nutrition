package eden.mapper;

import eden.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 订单明细 Mapper 接口
 */
public interface OrderItemMapper {

    /**
     * 根据订单ID查询订单明细
     */
    List<OrderItem> selectByOrderId(@Param("orderId") Long orderId);

    /**
     * 根据订单号查询订单明细
     */
    List<OrderItem> selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 批量插入订单明细
     */
    int batchInsert(@Param("list") List<OrderItem> items);

    /**
     * 插入单条订单明细
     */
    int insert(OrderItem item);

    /**
     * 删除订单明细（按订单ID）
     */
    int deleteByOrderId(@Param("orderId") Long orderId);
}
