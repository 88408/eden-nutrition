package eden.mapper;

import eden.pojo.Category;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * 商品分类 Mapper 接口
 */
public interface CategoryMapper {

    /**
     * 根据ID查询分类
     */
    Category selectById(@Param("id") Long id);

    /**
     * 查询所有分类
     */
    List<Category> selectAll();

    /**
     * 根据父ID查询子分类
     */
    List<Category> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 查询一级分类
     */
    List<Category> selectFirstLevel();

    /**
     * 插入分类
     */
    int insert(Category category);

    /**
     * 更新分类
     */
    int update(Category category);

    /**
     * 删除分类
     */
    int deleteById(@Param("id") Long id);

    /**
     * 更新分类状态
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
