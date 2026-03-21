package eden.web.controller;

import eden.common.result.Result;
import eden.service.SubscribeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 订阅控制器
 */
@Api(tags = "订阅管理")
@RestController
public class SubscribeController {

    @Autowired
    private SubscribeService subscribeService;

    @ApiOperation("订阅邮箱")
    @PostMapping("/subscribe")
    public Result<Void> subscribe(@RequestParam String email) {
        subscribeService.subscribe(email);
        return Result.success();
    }

    @ApiOperation("订阅邮箱（GET 用于浏览器调试）")
    @GetMapping("/subscribe")
    public Result<Void> subscribeGet(@RequestParam String email) {
        // 与 POST 处理逻辑一致，便于在浏览器地址栏直接测试
        subscribeService.subscribe(email);
        return Result.success();
    }
}

