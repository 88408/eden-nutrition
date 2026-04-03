import sys

path = r"d:\project\eden-nutrition\eden-service\src\main\java\eden\service\impl\SeckillServiceImpl.java"

with open(path, "r", encoding="utf-8") as f:
    content = f.read()

imports = """
import eden.mapper.SeckillMapper;
import eden.pojo.dto.AdminSeckillQueryDTO;
import eden.pojo.dto.AdminSeckillSaveDTO;
import eden.pojo.vo.AdminSeckillVO;
import eden.pojo.vo.PageVO;
import org.springframework.beans.BeanUtils;
import java.util.stream.Collectors;
"""

methods = """

    @Autowired
    private SeckillMapper seckillMapper;

    // ==================== B端后台管理方法 ====================

    @Override
    public PageVO<AdminSeckillVO> getAdminPage(AdminSeckillQueryDTO queryDTO) {
        List<AdminSeckillVO> list = seckillMapper.selectAdminPage(queryDTO.getProductId(), queryDTO.getStatus(), queryDTO.getOffset(), queryDTO.getPageSize());
        long total = seckillMapper.countAdminPage(queryDTO.getProductId(), queryDTO.getStatus());
        return PageVO.of(list, total, queryDTO.getPageNum(), queryDTO.getPageSize());
    }

    @Override
    public AdminSeckillVO getAdminDetail(Long id) {
        SeckillProduct product = seckillProductMapper.selectById(id);
        if (product == null) {
            return null;
        }
        AdminSeckillVO vo = new AdminSeckillVO();
        BeanUtils.copyProperties(product, vo);
        return vo;
    }

    @Override
    public void addAdminSeckill(AdminSeckillSaveDTO dto) {
        // 校验排期重叠
        int count = seckillMapper.countOverlappingSeckill(
                dto.getProductId(),
                dto.getStartTime(),
                dto.getEndTime(),
                null
        );
        if (count > 0) {
            throw new BusinessException("该商品在所选时间段内已有秒杀活动，排期重叠");
        }

        SeckillProduct sp = new SeckillProduct();
        BeanUtils.copyProperties(dto, sp);
        sp.setCreateTime(LocalDateTime.now());
        sp.setUpdateTime(LocalDateTime.now());
        sp.setStatus(0); // 默认未开始
        seckillProductMapper.insert(sp);
    }

    @Override
    public void updateAdminSeckill(AdminSeckillSaveDTO dto) {
        // 校验排期重叠（排除自身）
        int count = seckillMapper.countOverlappingSeckill(
                dto.getProductId(),
                dto.getStartTime(),
                dto.getEndTime(),
                dto.getId()
        );
        if (count > 0) {
            throw new BusinessException("该商品在所选时间段内已有秒杀活动，排期重叠");
        }

        SeckillProduct sp = new SeckillProduct();
        BeanUtils.copyProperties(dto, sp);
        sp.setUpdateTime(LocalDateTime.now());
        seckillProductMapper.update(sp);

        redisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK + sp.getId(), sp.getStockCount());
    }

    @Override
    public void deleteAdminSeckill(Long id) {
        seckillProductMapper.updateStatus(id, 2); // mark as ended/deleted
        redisTemplate.delete(RedisConstants.SECKILL_STOCK + id);
        redisTemplate.delete(RedisConstants.SECKILL_USER + id);
    }

    @Override
    public void finishAdminSeckill(Long id) {
        SeckillProduct product = seckillProductMapper.selectById(id);
        if (product != null) {
            product.setEndTime(LocalDateTime.now());
            product.setStatus(2); // 结束
            product.setUpdateTime(LocalDateTime.now());
            seckillProductMapper.update(product);

            // 清理缓存
            redisTemplate.delete(RedisConstants.SECKILL_STOCK + id);
        }
    }
}
"""

content = content.replace("import org.springframework.stereotype.Service;", "import org.springframework.stereotype.Service;" + imports)

content = content.rsplit("}", 1)[0] + methods

with open(path, "w", encoding="utf-8") as f:
    f.write(content)
print("done")