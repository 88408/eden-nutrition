package eden.web.controller;

import eden.common.result.Result;
import eden.pojo.SupportMessage;
import eden.pojo.SupportSession;
import eden.pojo.dto.SupportMessageDTO;
import eden.service.SupportService;
import eden.web.annotation.CurrentUser;
import eden.web.annotation.RequireLogin;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户侧客服控制器，提供留言式客服会话能力。
 */
@Api(tags = "用户客服")
@RestController
@RequestMapping("/support")
@RequireLogin
public class SupportController {

    @Autowired
    private SupportService supportService;

    /**
     * 获取或创建客服会话，商品详情页可传 productId，个人中心入口不传。
     */
    @ApiOperation("获取或创建客服会话")
    @PostMapping("/session")
    public Result<SupportSession> getOrCreateSession(
            @CurrentUser Long userId,
            @ApiParam("来源商品ID") @RequestParam(required = false) Long productId) {
        SupportSession session = supportService.getOrCreateSession(userId, productId);
        return Result.success(session);
    }

    /**
     * 发送客服留言，服务层会校验会话归属。
     */
    @ApiOperation("发送客服消息")
    @PostMapping("/message")
    public Result<SupportMessage> sendMessage(@CurrentUser Long userId,
                                              @Validated @RequestBody SupportMessageDTO messageDTO) {
        SupportMessage message = supportService.sendMessage(userId, messageDTO);
        return Result.success(message);
    }

    /**
     * 查询客服消息，服务层会校验会话归属。
     */
    @ApiOperation("查询客服消息")
    @GetMapping("/messages")
    public Result<List<SupportMessage>> listMessages(@CurrentUser Long userId,
                                                     @RequestParam Long sessionId) {
        List<SupportMessage> messages = supportService.listMessages(userId, sessionId);
        return Result.success(messages);
    }
}
