package eden.web.controller;

import eden.common.result.Result;
import eden.pojo.Category;
import eden.pojo.vo.CategoryTreeVO;
import eden.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 分类控制器
 */
@Api(tags = "商品分类")
@RestController
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @ApiOperation("获取分类树")
    @GetMapping("/tree")
    public Result<List<CategoryTreeVO>> getCategoryTree() {
        List<CategoryTreeVO> tree = categoryService.getCategoryTree();
        return Result.success(tree);
    }

    @ApiOperation("获取一级分类")
    @GetMapping("/first")
    public Result<List<Category>> getFirstLevel() {
        List<Category> categories = categoryService.getFirstLevelCategories();
        return Result.success(categories);
    }

    @ApiOperation("获取子分类")
    @GetMapping("/children/{parentId}")
    public Result<List<Category>> getChildren(@PathVariable Long parentId) {
        List<Category> categories = categoryService.getChildren(parentId);
        return Result.success(categories);
    }

    @ApiOperation("获取分类详情")
    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        Category category = categoryService.getById(id);
        return Result.success(category);
    }
}
