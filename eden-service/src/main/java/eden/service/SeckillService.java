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

    // ================== B端管理接口 ==================

    /**
     * B端分页查询活动
     */
    eden.pojo.vo.PageVO<eden.pojo.vo.AdminSeckillVO> getAdminPage(eden.pojo.dto.AdminSeckillQueryDTO queryDTO);

    /**
     * B端获取详情
     */
    eden.pojo.vo.AdminSeckillVO getAdminDetail(Long id);

    /**
     * B端新增秒杀活动
     */
    void addAdminSeckill(eden.pojo.dto.AdminSeckillSaveDTO dto);

    /**
     * B端修改秒杀活动
     */
    void updateAdminSeckill(eden.pojo.dto.AdminSeckillSaveDTO dto);

    /**
     * B端删除秒杀活动
     */
    void deleteAdminSeckill(Long id);

    /**
     * B端强行结束活动
     */
    void finishAdminSeckill(Long id);
}
