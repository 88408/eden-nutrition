package eden.service.impl;

import eden.common.constant.OrderConstants;
import eden.common.exception.BusinessException;
import eden.common.result.ResultCode;
import eden.mapper.NoticeMapper;
import eden.mapper.OrderItemMapper;
import eden.mapper.OrderMapper;
import eden.mapper.RefundApplyMapper;
import eden.pojo.Notice;
import eden.pojo.Order;
import eden.pojo.OrderItem;
import eden.pojo.RefundApply;
import eden.pojo.dto.RefundApplyDTO;
import eden.pojo.dto.RefundAuditDTO;
import eden.pojo.vo.PageVO;
import eden.service.AlipayService;
import eden.service.ProductService;
import eden.service.ProductSkuService;
import eden.service.RefundService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 售后退款服务实现。
 * <p>退款执行优先调用支付宝沙箱；若沙箱配置或网络不可用，则落地模拟退款流水，保证演示和审计链路完整。</p>
 */
@Service
public class RefundServiceImpl implements RefundService {

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_REJECTED = 2;
    private static final int STATUS_SUCCESS = 3;

    @Autowired
    private RefundApplyMapper refundApplyMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductSkuService productSkuService;

    @Autowired
    private AlipayService alipayService;

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public RefundApply apply(Long userId, RefundApplyDTO dto) {
        Order order = orderMapper.selectByOrderNo(dto.getOrderNo());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
        if (order.getStatus() != OrderConstants.STATUS_PAID
                && order.getStatus() != OrderConstants.STATUS_SHIPPED
                && order.getStatus() != OrderConstants.STATUS_RECEIVED
                && order.getStatus() != OrderConstants.STATUS_COMPLETED) {
            throw new BusinessException("当前订单状态不支持申请退款");
        }
        if (refundApplyMapper.selectActiveByOrderNo(order.getOrderNo()) != null) {
            throw new BusinessException("该订单已有进行中的退款申请，请勿重复提交");
        }
        if (dto.getOrderItemId() != null) {
            throw new BusinessException("当前版本仅支持整单退款，请勿选择单个订单项");
        }

        BigDecimal refundAmount = dto.getRefundAmount() == null ? order.getPayAmount() : dto.getRefundAmount();
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0 || refundAmount.compareTo(order.getPayAmount()) > 0) {
            throw new BusinessException("退款金额必须大于0且不能超过订单实付金额");
        }
        if (refundAmount.compareTo(order.getPayAmount()) != 0) {
            throw new BusinessException("当前版本仅支持整单退款，退款金额必须等于订单实付金额");
        }

        RefundApply refund = new RefundApply();
        refund.setRefundNo("RA" + System.currentTimeMillis() + userId);
        refund.setOrderId(order.getId());
        refund.setOrderNo(order.getOrderNo());
        refund.setOrderItemId(dto.getOrderItemId());
        refund.setUserId(userId);
        refund.setRefundAmount(refundAmount);
        // 申请时订单会进入退款中，必须保存原状态，审核拒绝时才能恢复到正确的业务阶段。
        refund.setOriginalOrderStatus(order.getStatus());
        refund.setReason(dto.getReason());
        refund.setImages(dto.getImages());
        refund.setStatus(STATUS_PENDING);
        refundApplyMapper.insert(refund);

        orderMapper.updateStatus(order.getId(), OrderConstants.STATUS_REFUNDING);
        createNotice(userId, "ORDER", "退款申请已提交", "订单 " + order.getOrderNo() + " 的退款申请已提交，等待后台审核。", order.getOrderNo());
        return refund;
    }

    @Override
    public PageVO<RefundApply> listMy(Long userId, int pageNum, int pageSize) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.max(pageSize, 1);
        int offset = (safePageNum - 1) * safePageSize;
        List<RefundApply> list = refundApplyMapper.selectByUserId(userId, offset, safePageSize);
        long total = refundApplyMapper.countByUserId(userId);
        return PageVO.of(list, total, safePageNum, safePageSize);
    }

    @Override
    public PageVO<RefundApply> adminPage(Integer status, String keyword, int pageNum, int pageSize) {
        int safePageNum = Math.max(pageNum, 1);
        int safePageSize = Math.max(pageSize, 1);
        int offset = (safePageNum - 1) * safePageSize;
        List<RefundApply> list = refundApplyMapper.selectAdminPage(status, keyword, offset, safePageSize);
        long total = refundApplyMapper.countAdminPage(status, keyword);
        return PageVO.of(list, total, safePageNum, safePageSize);
    }

    @Override
    public RefundApply getById(Long id) {
        RefundApply refund = refundApplyMapper.selectById(id);
        if (refund == null) {
            throw new BusinessException("退款申请不存在");
        }
        return refund;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void audit(Long auditorId, RefundAuditDTO dto) {
        RefundApply refund = getById(dto.getRefundId());
        if (refund.getStatus() != STATUS_PENDING) {
            throw new BusinessException("只有待审核退款申请可以审核");
        }

        RefundApply update = new RefundApply();
        update.setId(refund.getId());
        update.setAuditorId(auditorId);
        update.setAuditRemark(dto.getAuditRemark());
        update.setAuditTime(LocalDateTime.now());
        if (Boolean.TRUE.equals(dto.getApproved())) {
            update.setStatus(STATUS_APPROVED);
            createNotice(refund.getUserId(), "ORDER", "退款审核通过", "订单 " + refund.getOrderNo() + " 的退款申请已通过，请等待退款到账。", refund.getOrderNo());
        } else {
            update.setStatus(STATUS_REJECTED);
            // 拒绝退款只改变退款单状态，订单恢复到申请前状态，避免售后失败后订单生命周期被永久卡住。
            orderMapper.updateStatus(refund.getOrderId(), resolveOriginalOrderStatus(refund));
            createNotice(refund.getUserId(), "ORDER", "退款审核拒绝", "订单 " + refund.getOrderNo() + " 的退款申请被拒绝：" + nullToEmpty(dto.getAuditRemark()), refund.getOrderNo());
        }
        refundApplyMapper.update(update);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void executeRefund(Long auditorId, Long refundId) {
        RefundApply refund = getById(refundId);
        if (refund.getStatus() != STATUS_APPROVED) {
            throw new BusinessException("只有审核通过的退款申请可以执行退款");
        }
        Order order = orderMapper.selectById(refund.getOrderId());
        if (order == null) {
            throw new BusinessException(ResultCode.ORDER_NOT_FOUND);
        }

        String refundTradeNo;
        int simulated = 0;
        String simulatedReason = null;
        try {
            refundTradeNo = alipayService.refund(order, refund.getRefundAmount());
        } catch (Exception ex) {
            // 沙箱退款依赖密钥、公网和支付宝沙箱稳定性；失败时生成模拟流水，保证售后流程可演示且可审计。
            simulated = 1;
            simulatedReason = ex.getMessage();
            refundTradeNo = "SIM-REFUND-" + refund.getRefundNo();
        }

        RefundApply update = new RefundApply();
        update.setId(refund.getId());
        update.setStatus(STATUS_SUCCESS);
        update.setAuditorId(auditorId);
        update.setRefundTradeNo(refundTradeNo);
        update.setSimulated(simulated);
        if (simulated == 1) {
            update.setAuditRemark("模拟退款降级原因：" + nullToEmpty(simulatedReason));
        }
        update.setRefundTime(LocalDateTime.now());
        refundApplyMapper.update(update);

        orderMapper.updateStatus(order.getId(), OrderConstants.STATUS_REFUNDED);
        returnStock(order.getId());
        createNotice(refund.getUserId(), "ORDER", simulated == 1 ? "模拟退款成功" : "退款成功",
                "订单 " + refund.getOrderNo() + " 已完成退款，退款流水：" + refundTradeNo, refund.getOrderNo());
    }

    /**
     * 退款成功后回滚商品和 SKU 库存，确保售后流程能真实影响库存数据。
     */
    private void returnStock(Long orderId) {
        List<OrderItem> items = orderItemMapper.selectByOrderId(orderId);
        for (OrderItem item : items) {
            productService.increaseStock(item.getProductId(), item.getQuantity());
            productSkuService.increaseStock(item.getSkuId(), item.getQuantity());
        }
    }

    private void createNotice(Long userId, String type, String title, String content, String target) {
        Notice notice = new Notice();
        notice.setUserId(userId);
        notice.setType(type);
        notice.setTitle(title);
        notice.setContent(content);
        notice.setTarget(target);
        notice.setIsRead(0);
        noticeMapper.insert(notice);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    /**
     * 解析退款申请前的订单状态。
     * <p>历史退款单可能没有 originalOrderStatus，兜底恢复为已支付，避免审核拒绝后继续停留在“退款中”。</p>
     */
    private Integer resolveOriginalOrderStatus(RefundApply refund) {
        Integer originalStatus = refund.getOriginalOrderStatus();
        if (originalStatus == null || originalStatus == OrderConstants.STATUS_REFUNDING
                || originalStatus == OrderConstants.STATUS_REFUNDED
                || originalStatus == OrderConstants.STATUS_REFUND_REJECTED) {
            return OrderConstants.STATUS_PAID;
        }
        return originalStatus;
    }
}
