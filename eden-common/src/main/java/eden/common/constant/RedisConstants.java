package eden.common.constant;

/**
 * Redis Key 常量
 */
public class RedisConstants {

    /** Key 前缀 */
    public static final String PREFIX = "eden:";

    // ============================= 用户相关 =============================

    /** 用户收藏商品 Set前缀 */
    public static final String USER_FAVORITES = PREFIX + "user:favorites:";

    /** 用户Token前缀 */
    public static final String USER_TOKEN = PREFIX + "user:token:";

    /** 用户信息缓存 */
    public static final String USER_INFO = PREFIX + "user:info:";

    /** 验证码前缀 */
    public static final String VERIFY_CODE = PREFIX + "verify:code:";

    /** 用户登录失败次数 */
    public static final String USER_LOGIN_FAIL = PREFIX + "user:login:fail:";

    // ============================= 购物车相关 =============================

    /** 购物车Key前缀 */
    public static final String CART = PREFIX + "cart:";

    // ============================= 商品相关 =============================

    /** 商品详情缓存 */
    public static final String PRODUCT_DETAIL = PREFIX + "product:detail:";

    /** 商品库存 */
    public static final String PRODUCT_STOCK = PREFIX + "product:stock:";

    /** 热门商品列表 */
    public static final String PRODUCT_HOT = PREFIX + "product:hot";

    /** 新品列表 */
    public static final String PRODUCT_NEW = PREFIX + "product:new";

    /** 分类列表 */
    public static final String CATEGORY_LIST = PREFIX + "category:list";

    /** 分类树 */
    public static final String CATEGORY_TREE = PREFIX + "category:tree";

    // ============================= 秒杀相关 =============================

    /** 秒杀商品库存 */
    public static final String SECKILL_STOCK = PREFIX + "seckill:stock:";

    /** 秒杀用户记录（防止重复秒杀） */
    public static final String SECKILL_USER = PREFIX + "seckill:user:";

    /** 秒杀商品列表 */
    public static final String SECKILL_PRODUCTS = PREFIX + "seckill:products";

    /** 秒杀异步处理结果 */
    public static final String SECKILL_RESULT = PREFIX + "seckill:result:";

    /** 秒杀提交中的订单归属 */
    public static final String SECKILL_PENDING = PREFIX + "seckill:pending:";

    // ============================= 订单相关 =============================

    /** 订单超时队列 */
    public static final String ORDER_TIMEOUT = PREFIX + "order:timeout";

    /** 订单创建锁 */
    public static final String ORDER_CREATE_LOCK = PREFIX + "order:create:lock:";

    /** 支付宝微信开发者工具调试桥接 token */
    public static final String ALIPAY_BRIDGE_TOKEN = PREFIX + "alipay:bridge:";

    // ============================= 分布式锁 =============================

    /** 锁前缀 */
    public static final String LOCK = PREFIX + "lock:";

    /** 库存扣减锁 */
    public static final String LOCK_STOCK = LOCK + "stock:";

    /** 秒杀锁 */
    public static final String LOCK_SECKILL = LOCK + "seckill:";

    /** 优惠券领取锁 */
    public static final String LOCK_COUPON = LOCK + "coupon:";

    // ============================= 过期时间（秒） =============================

    /** Token 过期时间：24小时 */
    public static final long EXPIRE_TOKEN = 24 * 60 * 60;

    /** 验证码过期时间：5分钟 */
    public static final long EXPIRE_VERIFY_CODE = 5 * 60;

    /** 用户信息缓存时间：1小时 */
    public static final long EXPIRE_USER_INFO = 60 * 60;

    /** 商品详情缓存时间：30分钟 */
    public static final long EXPIRE_PRODUCT = 30 * 60;

    /** 分类缓存时间：1天 */
    public static final long EXPIRE_CATEGORY = 24 * 60 * 60;

    /** 热门/新品缓存时间：10分钟 */
    public static final long EXPIRE_HOT_NEW = 10 * 60;

    /** 购物车过期时间：7天 */
    public static final long EXPIRE_CART = 7 * 24 * 60 * 60;

    /** 锁默认过期时间：30秒 */
    public static final long EXPIRE_LOCK = 30;

    /** 登录失败记录过期时间：30分钟 */
    public static final long EXPIRE_LOGIN_FAIL = 30 * 60;

    /** 支付宝调试桥接 token 过期时间：10分钟 */
    public static final long EXPIRE_ALIPAY_BRIDGE = 10 * 60;

    /** 秒杀异步结果过期时间：30分钟 */
    public static final long EXPIRE_SECKILL_RESULT = 30 * 60;
}
