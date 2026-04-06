# C端前端生成提示词 (Prompt)

## 1. 背景要求
我是基于 Spring Boot 开发的伊甸园营养品商城，这部分接口是面向 C 端普通用户。我需要一个非常现代化的 React / Vue3 前端应用（支持移动端或响应式浏览器界面）。请依据下方的完整后端 API 文档，为我生成所有的 `src/api/*.ts` 封装请求服务（建议使用 Axios）以及所有的关键交互页面组件（如：**首页推荐及商品列表、分类导航、商品详情页与加购物车、秒杀频道、购物车页面、下单结算页面、个人中心地址管理、订单支付流水追踪**）。

## 2. Axios 请求配置要求
在全局请求拦截器中携带来自本地存储的 JWT Token（Storage Key 一般为 `token`），并在响应拦截器处理未登录或无权限的系统导航逻辑。

## 3. 后端可用完整 API 列表

### Address 模块 (`/address`)
- `GET` `/address/list` - <List<UserAddress>> list
- `GET` `/address/default` - <UserAddress> getDefault
- `GET` `/address/{id}` - <UserAddress> getById
- `POST` `/address` - <Void> add
- `PUT` `/address` - <Void> update
- `DELETE` `/address/{id}` - <Void> delete
- `PUT` `/address/default/{id}` - <Void> setDefault
### Cart 模块 (`/cart`)
- `GET` `/cart` - <CartVO> getCart
- `POST` `/cart/add` - <Void> addToCart
- `PUT` `/cart/quantity` - <Void> updateQuantity
- `DELETE` `/cart/{productId}` - <Void> removeFromCart
- `DELETE` `/cart/clear` - <Void> clearCart
- `GET` `/cart/count` - <Integer> getCartItemCount
- `PUT` `/cart/select` - <Void> selectItem
- `PUT` `/cart/selectAll` - <Void> selectAll
### Category 模块 (`/category`)
- `GET` `/category/tree` - <List<CategoryTreeVO>> getCategoryTree
- `GET` `/category/first` - <List<Category>> getFirstLevel
- `GET` `/category/children/{parentId}` - <List<Category>> getChildren
- `GET` `/category/{id}` - <Category> getById
- `POST` `/category/add` - <String> add
### Coupon 模块 (`/coupon`)
- `GET` `/coupon/available` - <List<Coupon>> getAvailableCoupons
- `POST` `/coupon/receive/{couponId}` - <Void> receiveCoupon
- `GET` `/coupon/my` - <List<UserCoupon>> getMyCoupons
- `GET` `/coupon/usable` - <List<UserCoupon>> getUsableCoupons
### Order 模块 (`/order`)
- `POST` `/order/create` - <Order> createOrder
- `GET` `/order/list` - <PageVO<Order>> getOrderList
- `GET` `/order/{orderNo}` - <Order> getOrderDetail
- `POST` `/order/cancel/{orderNo}` - <Void> cancelOrder
- `POST` `/order/pay/{orderNo}` - <Void> payOrder
- `POST` `/order/confirm/{orderNo}` - <Void> confirmReceive
- `DELETE` `/order/{orderNo}` - <Void> deleteOrder
- `GET` `/order/admin/list` - <PageVO<Order>> queryOrders
- `POST` `/order/admin/ship/{orderNo}` - <Void> shipOrder
### Product 模块 (`/product`)
- `GET` `/product/{id}` - <ProductVO> getById
- `GET` `/product/list` - <PageVO<ProductVO>> list
- `GET` `/product/hot` - <List<ProductVO>> getHotProducts
- `GET` `/product/recommend` - <List<ProductVO>> getRecommendProducts
- `GET` `/product/new` - <List<ProductVO>> getNewProducts
- `GET` `/product/category/{categoryId}` - <List<ProductVO>> getByCategory
- `POST` `/product` - <Void> create
- `PUT` `/product` - <Void> update
- `DELETE` `/product/{id}` - <Void> delete
### Review 模块 (`/review`)
- `GET` `/review/product/{productId}` - <PageVO<ProductReview>> getProductReviews
- `GET` `/review/product/{productId}/stats` - <Map<String, Object>> getReviewStats
- `POST` `/review` - <Void> addReview
- `DELETE` `/review/{reviewId}` - <Void> deleteReview
### Seckill 模块 (`/seckill`)
- `GET` `/seckill/sessions` - <List<SeckillSessionDTO>> getSeckillSessions
- `GET` `/seckill/list` - <List<SeckillProduct>> getSeckillList
- `GET` `/seckill/ongoing` - <List<SeckillProduct>> getOngoingSeckills
- `GET` `/seckill/upcoming` - <List<SeckillProduct>> getUpcomingSeckills
- `GET` `/seckill/{seckillId}` - <SeckillProduct> getSeckillDetail
- `POST` `/seckill/do` - <String> doSeckill
- `GET` `/seckill/check/{seckillId}` - <Boolean> checkKilled
- `POST` `/seckill/create` - <Void> createSeckill
- `PUT` `/seckill/update` - <Void> updateSeckill
- `POST` `/seckill/publish/{id}` - <Void> publishSeckill
### Subscribe 模块 (``)
- `POST` `/subscribe` - <Void> subscribe
- `GET` `/subscribe` - <Void> subscribeGet
### User 模块 (`/user`)
- `POST` `/user/register` - <Void> register
- `POST` `/user/login` - <LoginVO> login
- `POST` `/user/logout` - <Void> logout
- `GET` `/user/info` - <UserVO> getUserInfo
- `PUT` `/user/info` - <Void> updateUserInfo
- `PUT` `/user/password` - <Void> changePassword
- `GET` `/user/check/username` - <Boolean> checkUsername
- `GET` `/user/check/phone` - <Boolean> checkPhone


## 4. 特别说明与指引
请按照以上的后端接口，组织出完整结构的项目代码。重点包括：
1. `request.ts` 的配置方案代码。
2. 在 `src/api` 目录下将每个模块细分的文件及对应的类型接口声明 (interface/type)。
3. 具体页面的路由守卫逻辑，并写出能真实调用业务的主逻辑骨架代码。


请不要生成假数据，所有数据与操作均需和该 API 一对一贴合联动。