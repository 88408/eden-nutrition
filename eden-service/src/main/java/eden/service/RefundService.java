package eden.service;

import eden.pojo.RefundApply;
import eden.pojo.dto.RefundApplyDTO;
import eden.pojo.dto.RefundAuditDTO;
import eden.pojo.vo.PageVO;

/**
 * 售后退款服务。
 * <p>串联用户申请、后台审核、退款执行和通知写入。</p>
 */
public interface RefundService {
    RefundApply apply(Long userId, RefundApplyDTO dto);

    PageVO<RefundApply> listMy(Long userId, int pageNum, int pageSize);

    PageVO<RefundApply> adminPage(Integer status, String keyword, int pageNum, int pageSize);

    RefundApply getById(Long id);

    void audit(Long auditorId, RefundAuditDTO dto);

    void executeRefund(Long auditorId, Long refundId);
}
