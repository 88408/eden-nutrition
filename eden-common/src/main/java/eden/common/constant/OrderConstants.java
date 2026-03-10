package eden.common.constant;

/**
 * 订单常量
 */
public class OrderConstants {

    // ============================= 订单状态 =============================

    /** 待支付 */
    public static final int STATUS_UNPAID = 0;

    /** 已支付/待发货 */
    public static final int STATUS_PAID = 1;

    /** 已发货/待收货 */
    public static final int STATUS_SHIPPED = 2;

    /** 已收货/待评价 */
    public static final int STATUS_RECEIVED = 3;

    /** 已完成 */
    public static final int STATUS_COMPLETED = 4;

    /** 已取消 */
    public static final int STATUS_CANCELLED = 5;

    /** 退款中 */
    public static final int STATUS_REFUNDING = 6;

    /** 已退款 */
    public static final int STATUS_REFUNDED = 7;

    // ============================= 支付方式 =============================

    /** 支付宝 */
    public static final int PAY_TYPE_ALIPAY = 1;

    /** 微信支付 */
    public static final int PAY_TYPE_WECHAT = 2;

    /** 银行卡 */
    public static final int PAY_TYPE_BANK = 3;

    // ============================= 订单配置 =============================

    /** 订单超时时间（分钟） */
    public static final int ORDER_TIMEOUT_MINUTES = 30;

    /** 自动确认收货时间（天） */
    public static final int AUTO_CONFIRM_DAYS = 7;

    /** 售后申请期限（天） */
    public static final int AFTER_SALE_DAYS = 15;

    // ============================= 运费 =============================

    /** 免运费门槛 */
    public static final double FREE_SHIPPING_THRESHOLD = 99.0;

    /** 基础运费 */
    public static final double BASE_SHIPPING_FEE = 10.0;

    // ============================= 别名（兼容） =============================

    /** 待支付（别名） */
    public static final int STATUS_PENDING_PAYMENT = STATUS_UNPAID;

    // ============================= 订单类型 =============================

    /** 普通订单 */
    public static final int TYPE_NORMAL = 0;

    /** 秒杀订单 */
    public static final int TYPE_SECKILL = 1;

    /** 团购订单 */
    public static final int TYPE_GROUP = 2;
}
