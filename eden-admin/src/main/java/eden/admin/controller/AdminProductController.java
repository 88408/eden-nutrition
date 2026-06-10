package eden.admin.controller;

import eden.admin.annotation.RequireAdminLogin;
import eden.admin.annotation.RequirePermission;
import eden.common.result.Result;
import eden.pojo.Product;
import eden.pojo.ProductSku;
import eden.pojo.dto.AdminProductQueryDTO;
import eden.pojo.dto.ProductSaveDTO;
import eden.pojo.vo.PageVO;
import eden.service.ProductService;
import eden.service.ProductSkuService;
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

    @Autowired
    private ProductSkuService productSkuService;

    /**
     * 获取全量商品分页列表（包含下架商品）
     */
    @GetMapping("/page")
    @RequirePermission("product:view")
    public Result<PageVO<Product>> getAdminProductPage(AdminProductQueryDTO queryDTO) {
        PageVO<Product> pageInfo = productService.getAdminProductPage(queryDTO);
        return Result.success(pageInfo);
    }

    /**
     * 获取商品详情
     */
    @GetMapping("/{id}")
    @RequirePermission("product:view")
    public Result<Product> getProductDetail(@PathVariable Long id) {
        Product product = productService.getAdminById(id);
        return Result.success(product);
    }

    /**
     * 查询商品规格列表，后台编辑商品时用于维护口味、包装和规格库存。
     */
    @GetMapping("/{id}/skus")
    @RequirePermission("product:view")
    public Result<java.util.List<ProductSku>> getProductSkus(@PathVariable Long id) {
        return Result.success(productSkuService.listByProductId(id));
    }

    /**
     * 保存商品规格列表；采用全量替换，避免前端维护新增/删除差异。
     */
    @PostMapping("/{id}/skus")
    @RequirePermission("product:update")
    public Result<Void> saveProductSkus(@PathVariable Long id, @RequestBody java.util.List<ProductSku> skuList) {
        productSkuService.saveProductSkus(id, skuList);
        return Result.success();
    }

    /**
     * 新增商品
     */
    @PostMapping
    @RequirePermission("product:create")
    public Result<Void> saveProduct(@RequestBody ProductSaveDTO dto) {
        productService.saveProduct(dto);
        return Result.success();
    }

    /**
     * 修改商品
     */
    @PutMapping
    @RequirePermission("product:update")
    public Result<Void> updateProduct(@RequestBody ProductSaveDTO dto) {
        productService.updateProduct(dto);
        return Result.success();
    }

    /**
     * 修改商品上下架状态
     */
    @PutMapping("/status/{id}/{status}")
    @RequirePermission("product:update")
    public Result<Void> updateProductStatus(@PathVariable Long id, @PathVariable Integer status) {
        productService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    @RequirePermission("product:delete")
    public Result<Void> deleteProduct(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }
}
