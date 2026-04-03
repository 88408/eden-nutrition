package eden.admin.controller;

import eden.admin.annotation.RequireAdminLogin;
import eden.common.result.Result;
import eden.pojo.Category;
import eden.pojo.vo.CategoryTreeVO;
import eden.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/category")
@RequireAdminLogin
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/tree")
    public Result<List<CategoryTreeVO>> getCategoryTree() {
        return Result.success(categoryService.getCategoryTree());
    }

    @PostMapping
    public Result<Void> addCategory(@RequestBody Category category) {
        categoryService.add(category);
        return Result.success();
    }

    @PutMapping
    public Result<Void> updateCategory(@RequestBody Category category) {
        categoryService.update(category);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
        return Result.success();
    }

    @PutMapping("/status/{id}/{status}")
    public Result<Void> updateCategoryStatus(@PathVariable Long id, @PathVariable Integer status) {
        categoryService.updateStatus(id, status);
        return Result.success();
    }
}
