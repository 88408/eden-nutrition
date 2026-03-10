package eden.web.controller;

import eden.common.result.Result;
import eden.pojo.SeckillProduct;
import eden.pojo.dto.SeckillDTO;
import eden.service.SeckillService;
import eden.web.annotation.CurrentUser;
import eden.web.annotation.RequireLogin;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 秒杀控制器
 */
@Api(tags = "秒杀活动")
@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @ApiOperation("获取秒杀活动列表")
    @GetMapping("/list")
    public Result<List<SeckillProduct>> getSeckillList() {
        // 返回所有进行中的秒杀活动
        List<SeckillProduct> list = seckillService.getOngoingSeckills();
        return Result.success(list);
    }

    @ApiOperation("获取进行中的秒杀活动")
    @GetMapping("/ongoing")
    public Result<List<SeckillProduct>> getOngoingSeckills() {
        List<SeckillProduct> list = seckillService.getOngoingSeckills();
        return Result.success(list);
    }

    @ApiOperation("获取即将开始的秒杀活动")
    @GetMapping("/upcoming")
    public Result<List<SeckillProduct>> getUpcomingSeckills() {
        List<SeckillProduct> list = seckillService.getUpcomingSeckills();
        return Result.success(list);
    }

    @ApiOperation("获取秒杀商品详情")
    @GetMapping("/{seckillId}")
    public Result<SeckillProduct> getSeckillDetail(@PathVariable Long seckillId) {
        SeckillProduct seckillProduct = seckillService.getSeckillDetail(seckillId);
        return Result.success(seckillProduct);
    }

    @ApiOperation("执行秒杀")
    @RequireLogin
    @PostMapping("/do")
    public Result<String> doSeckill(@CurrentUser Long userId, @Validated @RequestBody SeckillDTO seckillDTO) {
        String orderNo = seckillService.doSeckill(userId, seckillDTO);
        return Result.success("秒杀成功", orderNo);
    }

    @ApiOperation("检查是否已秒杀")
    @RequireLogin
    @GetMapping("/check/{seckillId}")
    public Result<Boolean> checkKilled(@CurrentUser Long userId, @PathVariable Long seckillId) {
        boolean killed = seckillService.hasKilled(userId, seckillId);
        return Result.success(killed);
    }
}
