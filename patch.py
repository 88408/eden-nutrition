import sys

path = r"d:\project\eden-nutrition\eden-service\src\main\java\eden\service\impl\SeckillServiceImpl.java"

with open(path, "r", encoding="utf-8") as f:
    content = f.read()

imports = """
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import eden.common.result.PageResult;
import eden.pojo.dto.SeckillQueryDTO;
import eden.pojo.vo.SeckillProductVO;
import org.springframework.beans.BeanUtils;
import java.util.stream.Collectors;
"""

methods = """
    // ==================== B端后台管理方法 ====================

    @Override
    public PageResult<SeckillProductVO> getAdminPage(SeckillQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        List<SeckillProduct> list = seckillProductMapper.selectAdminPage(queryDTO.getProductName(), queryDTO.getStatus());
        PageInfo<SeckillProduct> pageInfo = new PageInfo<>(list);

        List<SeckillProductVO> voList = list.stream().map(product -> {
            SeckillProductVO vo = new SeckillProductVO();
            BeanUtils.copyProperties(product, vo);
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(pageInfo.getTotal(), voList);
    }

    @Override
    public SeckillProduct getAdminDetail(Long id) {
        return seckillProductMapper.selectById(id);
    }

    @Override
    public void addAdminSeckill(SeckillProduct seckillProduct) {
        // 校验排期重叠
        int count = seckillProductMapper.countOverlappingSeckill(
                seckillProduct.getProductId(),
                seckillProduct.getStartTime(),
                seckillProduct.getEndTime(),
                null
        );
        if (count > 0) {
            throw new BusinessException("该商品在所选时间段内已有秒杀活动，排期重叠");
        }

        seckillProduct.setCreateTime(LocalDateTime.now());
        seckillProduct.setUpdateTime(LocalDateTime.now());
        seckillProduct.setStatus(0); // 默认未开始
        seckillProductMapper.insert(seckillProduct);
    }

    @Override
    public void updateAdminSeckill(SeckillProduct seckillProduct) {
        // 校验排期重叠（排除自身）
        int count = seckillProductMapper.countOverlappingSeckill(
                seckillProduct.getProductId(),
                seckillProduct.getStartTime(),
                seckillProduct.getEndTime(),
                seckillProduct.getId()
        );
        if (count > 0) {
            throw new BusinessException("该商品在所选时间段内已有秒杀活动，排期重叠");
        }

        seckillProduct.setUpdateTime(LocalDateTime.now());
        seckillProductMapper.update(seckillProduct);

        redisTemplate.opsForValue().set(RedisConstants.SECKILL_STOCK + seckillProduct.getId(), seckillProduct.getStockCount());
    }

    @Override
    public void deleteAdminSeckill(Long id) {
        seckillProductMapper.deleteById(id);
        redisTemplate.delete(RedisConstants.SECKILL_STOCK + id);
        redisTemplate.delete(RedisConstants.SECKILL_USER + id);
    }

    @Override
    public void finishAdminSeckill(Long id) {
        SeckillProduct product = seckillProductMapper.selectById(id);
        if (product != null) {
            // 将结束时间设置为当前时间，表示强制结束
            product.setEndTime(LocalDateTime.now());
            product.setUpdateTime(LocalDateTime.now());
            seckillProductMapper.update(product);

            // 清理缓存，防止继续购买
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