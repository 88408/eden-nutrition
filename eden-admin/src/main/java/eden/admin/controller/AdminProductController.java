package eden.admin.controller;

import eden.admin.annotation.RequireAdminLogin;
import eden.common.result.Result;
import eden.pojo.Product;
import eden.pojo.dto.AdminProductQueryDTO;
import eden.pojo.dto.ProductSaveDTO;
import eden.pojo.vo.PageVO;
import eden.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理后台：商品管理 API
 */
@RestController
@RequestMapping("/admin/product")
@RequireAdminLogin
public class AdminProductController {

    @Autowired
    private ProductService productService;

    /**
     * 获取全量商品分页列表（包含下架商品）
     */
    @GetMapping("/page")
    public Result<PageVO<Product>> getAdminProductPage(AdminProductQueryDTO queryDTO) {
        PageVO<Product> pageInfo = productService.getAdminProductPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        Product product = productService.getAdminById(id);
        return Result.success(product);
    }

    /**
     * 新增商品
     */
    @PostMapping
    public Result<Void> saveProduct(@RequestBody ProductSaveDTO dto) {
        productService.saveProduct(dto);
        return Result.success();
    }

    /**
     * 修改商品
     */
    @PutMapping
    public Result<Void> updateProduct(@RequestBody ProductSaveDTO dto) {
        productService.updateProduct(dto);
        return Result.success();
    }

    /**
     * 修改商品上下架状态
     */
    @PutMapping("/status/{id}/{status}")
    public Result<Void> updateProductStatus(@PathVariable Long id, @PathVariable Integer status) {
        productService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }
}
