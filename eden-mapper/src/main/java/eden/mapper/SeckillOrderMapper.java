package eden.mapper;

import eden.pojo.SeckillOrder;
import org.apache.ibatis.annotations.Param;

/**
 * 秒杀订单 Mapper 接口
 */
public interface SeckillOrderMapper {

    /**
     * 根据用户ID和秒杀ID查询（检查是否重复秒杀）
     */
    SeckillOrder selectByUserAndSeckill(@Param("userId") Long userId, @Param("seckillId") Long seckillId);

    /**
     * 插入秒杀订单
     */
    int insert(SeckillOrder seckillOrder);

    /**
     * 根据订单ID删除
     */
    int deleteByOrderId(@Param("orderId") Long orderId);
}
