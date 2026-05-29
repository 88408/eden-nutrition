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
     * 查询已到开始时间但尚未开始的秒杀活动
     */
    List<SeckillProduct> selectStartableSeckills(@Param("now") LocalDateTime now);

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
     * 更新已到开始时间的秒杀活动状态
     */
    int updateStartedSeckills(@Param("now") LocalDateTime now);

    /**
     * 更新已结束的秒杀活动状态
     */
    int updateEndedSeckills(@Param("now") LocalDateTime now);

    /**
     * B端: 管理员分页查询秒杀活动列表 (关联商品表)
     */
    List<eden.pojo.vo.AdminSeckillVO> selectAdminPage(@Param("productId") Long productId,
                                                      @Param("status") Integer status,
                                                      @Param("offset") int offset,
                                                      @Param("pageSize") int pageSize);

    /**
     * B端: 管理员分页查询秒杀活动总数
     */
    long countAdminPage(@Param("productId") Long productId, @Param("status") Integer status);

    /**
     * B端: 查询商品在指定时间段内是否有冲突的活动
     */
    int countOverlappingSeckill(@Param("productId") Long productId,
                                @Param("startTime") LocalDateTime startTime,
                                @Param("endTime") LocalDateTime endTime,
                                @Param("excludeId") Long excludeId);
}
