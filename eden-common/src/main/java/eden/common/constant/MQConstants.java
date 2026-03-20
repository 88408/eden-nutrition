package eden.common.constant;

/**
 * MQ 消息队列常量
 */
public class MQConstants {

    // ============================= Exchange 交换机 =============================

    /** 订单交换机 */
    public static final String ORDER_EXCHANGE = "eden.order.exchange";

    /** 延迟交换机（订单超时） */
    public static final String DELAY_EXCHANGE = "eden.delay.exchange";

    /** 订单延迟交换机 */
    public static final String ORDER_DELAY_EXCHANGE = "eden.order.delay.exchange";

    /** 库存交换机 */
    public static final String STOCK_EXCHANGE = "eden.stock.exchange";

    /** 秒杀交换机 */
    public static final String SECKILL_EXCHANGE = "eden.seckill.exchange";

    // ============================= Routing Key 路由键 =============================

    /** 秒杀路由键 */
    public static final String SECKILL_ROUTING_KEY = "eden.seckill.key";

    // ============================= Queue 队列 =============================

    /** 订单创建队列 */
    public static final String ORDER_CREATE_QUEUE = "eden.order.create.queue";

    /** 订单取消队列 */
    public static final String ORDER_CANCEL_QUEUE = "eden.order.cancel.queue";

    /** 订单超时队列 */
    public static final String ORDER_TIMEOUT_QUEUE = "eden.order.timeout.queue";

    /** 订单支付成功队列 */
    public static final String ORDER_PAY_SUCCESS_QUEUE = "eden.order.pay.success.queue";

    /** 秒杀订单队列 */
    public static final String SECKILL_ORDER_QUEUE = "eden.seckill.order.queue";

    /** 库存扣减队列 */
    public static final String STOCK_DEDUCT_QUEUE = "eden.stock.deduct.queue";

    /** 库存回滚队列 */
    public static final String STOCK_ROLLBACK_QUEUE = "eden.stock.rollback.queue";

    /** 订单延迟队列 */
    public static final String ORDER_DELAY_QUEUE = "eden.order.delay.queue";

    // ============================= Routing Key 路由键 =============================

    /** 订单创建路由键 */
    public static final String ORDER_CREATE_KEY = "order.create";

    /** 订单取消路由键 */
    public static final String ORDER_CANCEL_KEY = "order.cancel";

    /** 订单超时路由键 */
    public static final String ORDER_TIMEOUT_KEY = "order.timeout";

    /** 订单支付成功路由键 */
    public static final String ORDER_PAY_SUCCESS_KEY = "order.pay.success";

    /** 秒杀订单路由键 */
    public static final String SECKILL_ORDER_KEY = "seckill.order";

    /** 库存回滚路由键 */
    public static final String STOCK_ROLLBACK_KEY = "stock.rollback";

    /** 订单创建路由键（别名） */
    public static final String ORDER_CREATE_ROUTING_KEY = ORDER_CREATE_KEY;

    /** 订单取消路由键（别名） */
    public static final String ORDER_CANCEL_ROUTING_KEY = ORDER_CANCEL_KEY;

    /** 订单支付成功路由键（别名） */
    public static final String ORDER_PAY_SUCCESS_ROUTING_KEY = ORDER_PAY_SUCCESS_KEY;

    /** 订单延迟路由键 */
    public static final String ORDER_DELAY_ROUTING_KEY = "order.delay";

    /** 秒杀订单路由键（别名） */
    public static final String SECKILL_ORDER_ROUTING_KEY = SECKILL_ORDER_KEY;

    /** 库存扣减路由键 */
    public static final String STOCK_DEDUCT_ROUTING_KEY = "stock.deduct";

    /** 库存回滚路由键（别名） */
    public static final String STOCK_ROLLBACK_ROUTING_KEY = STOCK_ROLLBACK_KEY;

    // ============================= 延迟时间（毫秒） =============================

    /** 订单超时延迟时间：30分钟 */
    public static final int ORDER_TIMEOUT_DELAY = 30 * 60 * 1000;
}
