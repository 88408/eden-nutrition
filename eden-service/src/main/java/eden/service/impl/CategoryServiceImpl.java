package eden.service.impl;

import eden.common.constant.RedisConstants;
import eden.common.exception.BusinessException;
import eden.common.result.ResultCode;
import eden.mapper.CategoryMapper;
import eden.pojo.Category;
import eden.pojo.vo.CategoryTreeVO;
import eden.service.CategoryService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 分类服务实现类
 */
@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public List<CategoryTreeVO> getCategoryTree() {
        // 先从缓存获取
        List<CategoryTreeVO> cached = (List<CategoryTreeVO>) redisTemplate.opsForValue()
                .get(RedisConstants.CATEGORY_TREE);
        if (cached != null) {
            return cached;
        }

        // 获取所有分类
        List<Category> allCategories = categoryMapper.selectAll();
        
        // 构建分类树
        List<CategoryTreeVO> tree = buildTree(allCategories);

        // 缓存分类树
        redisTemplate.opsForValue().set(RedisConstants.CATEGORY_TREE, tree, 
                RedisConstants.EXPIRE_CATEGORY, TimeUnit.SECONDS);

        return tree;
    }

    /**
     * 构建分类树
     */
    private List<CategoryTreeVO> buildTree(List<Category> categories) {
        // 转换为VO
        List<CategoryTreeVO> voList = categories.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        // 按父ID分组
        Map<Long, List<CategoryTreeVO>> parentMap = voList.stream()
                .collect(Collectors.groupingBy(CategoryTreeVO::getParentId));

        // 设置子分类
        voList.forEach(vo -> {
            List<CategoryTreeVO> children = parentMap.get(vo.getId());
            if (children != null) {
                vo.setChildren(children);
            }
        });

        // 返回顶级分类
        return voList.stream()
                .filter(vo -> vo.getParentId() == 0)
                .collect(Collectors.toList());
    }

    private CategoryTreeVO convertToVO(Category category) {
        CategoryTreeVO vo = new CategoryTreeVO();
        BeanUtils.copyProperties(category, vo);
        vo.setChildren(new ArrayList<>());
        return vo;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Category> getFirstLevelCategories() {
        // 先从缓存获取
        String cacheKey = RedisConstants.CATEGORY_LIST + ":first";
        List<Category> cached = (List<Category>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return cached;
        }

        List<Category> categories = categoryMapper.selectFirstLevel();

        // 缓存
        redisTemplate.opsForValue().set(cacheKey, categories, 
                RedisConstants.EXPIRE_CATEGORY, TimeUnit.SECONDS);

        return categories;
    }

    @Override
    public List<Category> getChildren(Long parentId) {
        return categoryMapper.selectByParentId(parentId);
    }

    @Override
    public Category getById(Long id) {
        Category category = categoryMapper.selectById(id);
        if (category == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类不存在");
        }
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void add(Category category) {
        // 设置层级
        if (category.getParentId() == null || category.getParentId() == 0) {
            category.setParentId(0L);
            category.setLevel(1);
        } else {
            Category parent = categoryMapper.selectById(category.getParentId());
            if (parent == null) {
                throw new BusinessException("父分类不存在");
            }
            category.setLevel(parent.getLevel() + 1);
        }

        if (category.getSortOrder() == null) {
            category.setSortOrder(0);
        }
        if (category.getStatus() == null) {
            category.setStatus(1);
        }

        categoryMapper.insert(category);

        // 清除缓存
        clearCategoryCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Category category) {
        Category existing = categoryMapper.selectById(category.getId());
        if (existing == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "分类不存在");
        }

        categoryMapper.update(category);

        // 清除缓存
        clearCategoryCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查是否有子分类
        List<Category> children = categoryMapper.selectByParentId(id);
        if (children != null && !children.isEmpty()) {
            throw new BusinessException("存在子分类，无法删除");
        }

        categoryMapper.deleteById(id);

        // 清除缓存
        clearCategoryCache();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(Long id, Integer status) {
        categoryMapper.updateStatus(id, status);

        // 清除缓存
        clearCategoryCache();
    }

    /**
     * 清除分类缓存
     */
    private void clearCategoryCache() {
        redisTemplate.delete(RedisConstants.CATEGORY_TREE);
        redisTemplate.delete(RedisConstants.CATEGORY_LIST + ":first");
    }
}
