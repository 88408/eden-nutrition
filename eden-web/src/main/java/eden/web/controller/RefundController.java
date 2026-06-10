package eden.web.controller;

import eden.common.result.Result;
import eden.pojo.RefundApply;
import eden.pojo.dto.RefundApplyDTO;
import eden.pojo.vo.PageVO;
import eden.service.RefundService;
import eden.web.annotation.CurrentUser;
import eden.web.annotation.RequireLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户端售后退款接口。
 * <p>用户可提交退款申请并查看自己的售后记录。</p>
 */
@RestController
@RequestMapping("/refund")
@RequireLogin
public class RefundController {

    @Autowired
    private RefundService refundService;

    @PostMapping("/apply")
    public Result<RefundApply> apply(@CurrentUser Long userId, @RequestBody RefundApplyDTO dto) {
        return Result.success(refundService.apply(userId, dto));
    }

    @GetMapping("/my")
    public Result<PageVO<RefundApply>> my(@CurrentUser Long userId,
                                          @RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(refundService.listMy(userId, pageNum, pageSize));
    }
}
