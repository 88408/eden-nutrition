package eden.service;

import eden.pojo.SeckillProduct;
import eden.pojo.dto.SeckillDTO;
import java.util.List;

/**
 * 秒杀服务接口
 */
public interface SeckillService {

    /**
     * 获取进行中的秒杀活动
     */
    List<SeckillProduct> getOngoingSeckills();

    /**
     * 获取即将开始的秒杀活动
     */
    List<SeckillProduct> getUpcomingSeckills();

    /**
     * 执行秒杀
     */
    String doSeckill(Long userId, SeckillDTO seckillDTO);

    /**
     * 获取秒杀商品详情
     */
    SeckillProduct getSeckillDetail(Long seckillId);

    /**
     * 检查用户是否已秒杀过
     */
    boolean hasKilled(Long userId, Long seckillId);

    /**
     * 添加秒杀活动
     */
    void add(SeckillProduct seckillProduct);

    /**
     * 更新秒杀活动
     */
    void update(SeckillProduct seckillProduct);

    /**
     * 初始化秒杀库存到Redis
     */
    void initSeckillStock();

    /**
     * 获取秒杀场次列表
     */
    List<eden.pojo.dto.SeckillSessionDTO> getSeckillSessions();
}

