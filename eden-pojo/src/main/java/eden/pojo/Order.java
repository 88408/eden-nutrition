package eden.pojo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 订单实体类
 */
@Data
public class Order implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 订单ID */
    private Long id;

    /** 订单编号 */
    private String orderNo;

    /** 用户ID */
    private Long userId;

    /** 订单总金额 */
    private BigDecimal totalAmount;

    /** 实付金额 */
    private BigDecimal payAmount;

    /** 运费 */
    private BigDecimal freightAmount;

    /** 优惠金额 */
    private BigDecimal discountAmount;

    /** 使用的优惠券ID */
    private Long couponId;

    /** 订单状态：0-待支付 1-已支付 2-已发货 3-已完成 4-已取消 5-已退款 */
    private Integer status;

    /** 支付方式：1-支付宝 2-微信 */
    private Integer payType;

    /** 支付时间 */
    private LocalDateTime payTime;

    /** 发货时间 */
    private LocalDateTime shipTime;

    /** 获取时间 */
    private LocalDateTime receiveTime;

    /** 完成时间 */
    private LocalDateTime completeTime;

    /** 物流公司 */
    private String deliveryCompany;

    /** 物流单号 */
    private String deliverySn;

    /** 收货人姓名 */
    private String receiverName;

    /** 收货人电话 */
    private String receiverPhone;

    /** 收货地址 */
    private String receiverAddress;

    /** 订单备注 */
    private String remark;

    /** 订单项列表 */
    private List<OrderItem> orderItems;

    /** 支付方式 */
    private String paymentMethod;

    /** 支付宝交易号，用于异步通知幂等校验和后续对账 */
    private String paymentTradeNo;

    /** 订单类型：0-普通订单 1-秒杀订单 2-团购订单 */
    private Integer orderType;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    // ========== 订单状态常量 ==========
    public static final int STATUS_UNPAID = 0;      // 待支付
    public static final int STATUS_PAID = 1;        // 已支付
    public static final int STATUS_SHIPPED = 2;     // 已发货
    public static final int STATUS_COMPLETED = 3;   // 已完成
    public static final int STATUS_CANCELLED = 4;   // 已取消
    public static final int STATUS_REFUNDED = 5;    // 已退款
}
