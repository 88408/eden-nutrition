package eden.admin.controller;

import eden.admin.annotation.RequireAdminLogin;
import eden.common.result.Result;
import eden.pojo.dto.AdminSeckillQueryDTO;
import eden.pojo.dto.AdminSeckillSaveDTO;
import eden.pojo.vo.AdminSeckillVO;
import eden.pojo.vo.PageVO;
import eden.service.SeckillService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 管理后台：秒杀活动管理 API
 */
@Api(tags = "管理端-秒杀活动管理")
@RestController
@RequestMapping("/admin/seckill")
@RequireAdminLogin
public class AdminSeckillController {

    @Autowired
    private SeckillService seckillService;

    @ApiOperation("获取管理端秒杀活动分页列表")
    @GetMapping("/page")
    public Result<PageVO<AdminSeckillVO>> getAdminPage(AdminSeckillQueryDTO queryDTO) {
        PageVO<AdminSeckillVO> pageVO = seckillService.getAdminPage(queryDTO);
        return Result.success(pageVO);
    }

    @ApiOperation("获取管理端秒杀活动详情")
    @GetMapping("/{id}")
    public Result<AdminSeckillVO> getAdminDetail(@PathVariable Long id) {
        AdminSeckillVO detail = seckillService.getAdminDetail(id);
        return Result.success(detail);
    }

    @ApiOperation("新增秒杀活动")
    @PostMapping
    public Result<Void> addAdminSeckill(@RequestBody AdminSeckillSaveDTO dto) {
        seckillService.addAdminSeckill(dto);
        return Result.success();
    }

    @ApiOperation("修改秒杀活动")
    @PutMapping
    public Result<Void> updateAdminSeckill(@RequestBody AdminSeckillSaveDTO dto) {
        seckillService.updateAdminSeckill(dto);
        return Result.success();
    }

    @ApiOperation("删除秒杀活动")
    @DeleteMapping("/{id}")
    public Result<Void> deleteAdminSeckill(@PathVariable Long id) {
        seckillService.deleteAdminSeckill(id);
        return Result.success();
    }

    @ApiOperation("强制结束秒杀活动")
    @PutMapping("/finish/{id}")
    public Result<Void> finishAdminSeckill(@PathVariable Long id) {
        seckillService.finishAdminSeckill(id);
        return Result.success();
    }
}
