package eden.mapper;

import eden.pojo.SeckillProduct;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 秒杀商品 Mapper 接口
 */
public interface SeckillProductMapper {

    /**
     * 根据ID查询秒杀商品
     */
    SeckillProduct selectById(@Param("id") Long id);

    /**
     * 查询进行中的秒杀活动
     */
    List<SeckillProduct> selectOngoing();

    /**
     * 查询即将开始的秒杀活动
     */
    List<SeckillProduct> selectUpcoming();

    /**
     * 查询所有秒杀活动（管理端）
     */
    List<SeckillProduct> selectAll(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 插入秒杀商品
     */
    int insert(SeckillProduct seckillProduct);

    /**
     * 更新秒杀商品
     */
    int update(SeckillProduct seckillProduct);

    /**
     * 扣减秒杀库存
     */
    int decreaseStock(@Param("id") Long id);

    /**
     * 增加秒杀库存（回滚）
     */
    int increaseStock(@Param("id") Long id);

    /**
     * 更新秒杀状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
