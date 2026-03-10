package eden.service;

import eden.pojo.Category;
import eden.pojo.vo.CategoryTreeVO;
import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {

    /**
     * 获取分类树
     */
    List<CategoryTreeVO> getCategoryTree();

    /**
     * 获取一级分类
     */
    List<Category> getFirstLevelCategories();

    /**
     * 获取子分类
     */
    List<Category> getChildren(Long parentId);

    /**
     * 根据ID获取分类
     */
    Category getById(Long id);

    /**
     * 添加分类
     */
    void add(Category category);

    /**
     * 更新分类
     */
    void update(Category category);

    /**
     * 删除分类
     */
    void delete(Long id);

    /**
     * 更新分类状态
     */
    void updateStatus(Long id, Integer status);
}
