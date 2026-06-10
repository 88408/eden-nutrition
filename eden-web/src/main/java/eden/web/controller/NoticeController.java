package eden.web.controller;

import eden.common.result.Result;
import eden.pojo.Notice;
import eden.pojo.vo.PageVO;
import eden.service.NoticeService;
import eden.web.annotation.CurrentUser;
import eden.web.annotation.RequireLogin;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 站内通知控制器，承载首页铃铛与通知中心页面。
 */
@Api(tags = "站内通知")
@RestController
@RequestMapping("/notice")
@RequireLogin
public class NoticeController {

    @Autowired
    private NoticeService noticeService;

    /**
     * 分页查询当前用户通知。
     */
    @ApiOperation("通知列表")
    @GetMapping("/list")
    public Result<PageVO<Notice>> list(
            @CurrentUser Long userId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(noticeService.list(userId, pageNum, pageSize));
    }

    /**
     * 查询未读数量，用于首页铃铛角标。
     */
    @ApiOperation("未读通知数量")
    @GetMapping("/unread-count")
    public Result<Long> countUnread(@CurrentUser Long userId) {
        return Result.success(noticeService.countUnread(userId));
    }

    /**
     * 标记单条通知已读。
     */
    @ApiOperation("标记通知已读")
    @PutMapping("/read/{id}")
    public Result<Void> markRead(@CurrentUser Long userId, @PathVariable Long id) {
        noticeService.markRead(userId, id);
        return Result.success();
    }

    /**
     * 标记全部通知已读。
     */
    @ApiOperation("全部通知已读")
    @PutMapping("/read-all")
    public Result<Void> markAllRead(@CurrentUser Long userId) {
        noticeService.markAllRead(userId);
        return Result.success();
    }
}
