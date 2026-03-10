package eden.mapper;

import eden.pojo.SeckillProduct;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 秒杀活动 Mapper 接口
 */
public interface SeckillMapper {

    /**
     * 根据ID查询秒杀活动
     */
    SeckillProduct selectById(@Param("id") Long id);

    /**
     * 查询进行中的秒杀活动
     */
    List<SeckillProduct> selectOngoing(@Param("now") LocalDateTime now);

    /**
     * 查询即将开始的秒杀活动
     */
    List<SeckillProduct> selectUpcoming(@Param("now") LocalDateTime now, @Param("hours") int hours);

    /**
     * 查询时间范围内的秒杀活动
     */
    List<SeckillProduct> selectByTimeRange(@Param("startTime") LocalDateTime startTime, 
                                     @Param("endTime") LocalDateTime endTime);

    /**
     * 插入秒杀活动
     */
    int insert(SeckillProduct seckill);

    /**
     * 更新秒杀活动
     */
    int update(SeckillProduct seckill);

    /**
     * 扣减库存
     */
    int deductStock(@Param("id") Long id, @Param("quantity") int quantity);

    /**
     * 更新已结束的秒杀活动状态
     */
    int updateEndedSeckills(@Param("now") LocalDateTime now);
}
