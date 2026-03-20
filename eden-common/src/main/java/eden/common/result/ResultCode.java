package eden.common.result;

/**
 * 响应状态码枚举
 */
public enum ResultCode {

    // 成功
    SUCCESS(200, "操作成功"),

    // 客户端错误 4xx
    FAIL(400, "操作失败"),
    PARAM_ERROR(400, "参数错误"),
    UNAUTHORIZED(401, "未登录或登录已过期"),
    FORBIDDEN(403, "没有访问权限"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),

    // 业务错误 5xx
    INTERNAL_ERROR(500, "服务器内部错误"),
    SERVICE_UNAVAILABLE(503, "服务暂时不可用"),

    // 用户相关 1xxx
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USERNAME_OR_PASSWORD_ERROR(1003, "用户名或密码错误"),
    USER_DISABLED(1004, "用户已被禁用"),
    PASSWORD_ERROR(1005, "密码错误"),
    PHONE_ALREADY_EXISTS(1006, "手机号已被注册"),
    VERIFY_CODE_ERROR(1007, "验证码错误"),

    // 商品相关 2xxx
    PRODUCT_NOT_FOUND(2001, "商品不存在"),
    PRODUCT_OFF_SHELF(2002, "商品已下架"),
    STOCK_NOT_ENOUGH(2003, "库存不足"),

    // 订单相关 3xxx
    ORDER_NOT_FOUND(3001, "订单不存在"),
    ORDER_STATUS_ERROR(3002, "订单状态异常"),
    ORDER_ALREADY_PAID(3003, "订单已支付"),
    ORDER_TIMEOUT(3004, "订单已超时"),
    ORDER_CANNOT_CANCEL(3005, "订单不可取消"),

    // 购物车相关 4xxx
    CART_EMPTY(4001, "购物车为空"),
    CART_ITEM_NOT_FOUND(4002, "购物车商品不存在"),

    // 优惠券相关 5xxx
    COUPON_NOT_FOUND(5001, "优惠券不存在"),
    COUPON_ALREADY_RECEIVED(5002, "优惠券已领取"),
    COUPON_EXPIRED(5003, "优惠券已过期"),
    COUPON_NOT_AVAILABLE(5004, "优惠券不可用"),
    COUPON_AMOUNT_NOT_ENOUGH(5005, "订单金额不满足优惠券使用条件"),

    // 秒杀相关 6xxx
    SECKILL_NOT_START(6001, "秒杀活动未开始"),
    SECKILL_ENDED(6002, "秒杀活动已结束"),
    SECKILL_NO_STOCK(6003, "库存不足"),
    SECKILL_REPEAT(6004, "您已经参与过该秒杀"),
    SECKILL_LIMIT(6005, "超过秒杀限购数量"),

    // 地址相关 7xxx
    ADDRESS_NOT_FOUND(7001, "收货地址不存在"),
    ADDRESS_LIMIT(7002, "收货地址数量已达上限");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
