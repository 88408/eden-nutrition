package eden.web.controller;

import eden.common.result.Result;
import eden.pojo.Product;
import eden.pojo.dto.ProductQueryDTO;
import eden.pojo.vo.PageVO;
import eden.pojo.vo.ProductVO;
import eden.service.ProductService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品控制器
 */
@Api(tags = "商品管理")
@RestController
@RequestMapping("/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @ApiOperation("获取商品详情")
    @GetMapping("/{id}")
    public Result<ProductVO> getById(@PathVariable Long id) {
        Product product = productService.getById(id);
        return Result.success(ProductVO.fromProduct(product));
    }

    @ApiOperation("商品列表查询")
    @GetMapping("/list")
    public Result<PageVO<ProductVO>> list(
            @ApiParam("分类ID") @RequestParam(required = false) Long categoryId,
            @ApiParam("搜索关键词") @RequestParam(required = false) String keyword,
            @ApiParam("最低价格") @RequestParam(required = false) BigDecimal minPrice,
            @ApiParam("最高价格") @RequestParam(required = false) BigDecimal maxPrice,
            @ApiParam("排序字段:price/sales/new") @RequestParam(required = false) String sortField,
            @ApiParam("排序方式:asc/desc") @RequestParam(required = false) String sortOrder,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        
        ProductQueryDTO queryDTO = new ProductQueryDTO();
        queryDTO.setCategoryId(categoryId);
        queryDTO.setKeyword(keyword);
        queryDTO.setMinPrice(minPrice);
        queryDTO.setMaxPrice(maxPrice);
        queryDTO.setSortField(sortField);
        queryDTO.setSortOrder(sortOrder);
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);

        PageVO<Product> page = productService.queryProducts(queryDTO);
        
        List<ProductVO> voList = page.getList().stream()
                .map(ProductVO::fromProduct)
                .collect(Collectors.toList());
        
        PageVO<ProductVO> pageVO = PageVO.of(voList, page.getTotal(), page.getPageNum(), page.getPageSize());
        
        return Result.success(pageVO);
    }

    @ApiOperation("获取热门商品")
    @GetMapping("/hot")
    public Result<List<ProductVO>> getHotProducts(
            @ApiParam("数量") @RequestParam(defaultValue = "8") Integer limit) {
        List<Product> products = productService.getHotProducts(limit);
        List<ProductVO> productVOS = products.stream()
                .map(ProductVO::fromProduct)
                .collect(Collectors.toList());
        return Result.success(productVOS);
    }

    @ApiOperation("获取推荐商品")
    @GetMapping("/recommend")
    public Result<List<ProductVO>> getRecommendProducts(
            @ApiParam("数量") @RequestParam(defaultValue = "10") Integer limit) {
        // 推荐商品暂时使用热门商品逻辑
        List<Product> products = productService.getHotProducts(limit);
        List<ProductVO> productVOS = products.stream()
                .map(ProductVO::fromProduct)
                .collect(Collectors.toList());
        return Result.success(productVOS);
    }

    @ApiOperation("获取新品列表")
    @GetMapping("/new")
    public Result<List<ProductVO>> getNewProducts(
            @ApiParam("数量") @RequestParam(defaultValue = "8") Integer limit) {
        List<Product> products = productService.getNewProducts(limit);
        List<ProductVO> productVOS = products.stream()
                .map(ProductVO::fromProduct)
                .collect(Collectors.toList());
        return Result.success(productVOS);
    }

    @ApiOperation("根据分类获取商品")
    @GetMapping("/category/{categoryId}")
    public Result<List<ProductVO>> getByCategory(@PathVariable Long categoryId) {
        List<Product> products = productService.getByCategory(categoryId);
        List<ProductVO> productVOS = products.stream()
                .map(ProductVO::fromProduct)
                .collect(Collectors.toList());
        return Result.success(productVOS);
    }

    @ApiOperation("新增商品")
    @PostMapping("")
    public Result<Void> create(@RequestBody Product product) {
        productService.add(product);
        return Result.success();
    }

    @ApiOperation("修改商品")
    @PutMapping("")
    public Result<Void> update(@RequestBody Product product) {
        productService.update(product);
        return Result.success();
    }

    @ApiOperation("删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return Result.success();
    }

    @ApiOperation("修改商品状态")
    @PatchMapping("/{id}/{status}")
    public Result<Void> updateStatus(@PathVariable Long id, @PathVariable Integer status) {
        productService.updateStatus(id, status);
        return Result.success();
    }
}
