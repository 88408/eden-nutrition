# C端前端交互 API 完整文档

## 模块 Address (API Base: `/address`)

### `GET` `/address/list`
- **说明**: 
- **签名**: `public Result<List<UserAddress>> list(@CurrentUser Long userId)`

### `GET` `/address/default`
- **说明**: 
- **签名**: `public Result<UserAddress> getDefault(@CurrentUser Long userId)`

### `GET` `/address/{id}`
- **说明**: 
- **签名**: `public Result<UserAddress> getById(@PathVariable Long id)`

### `POST` `/address`
- **说明**: 
- **签名**: `public Result<Void> add(@CurrentUser Long userId, @RequestBody UserAddress address)`

### `PUT` `/address`
- **说明**: 
- **签名**: `public Result<Void> update(@CurrentUser Long userId, @RequestBody UserAddress address)`

### `DELETE` `/address/{id}`
- **说明**: 
- **签名**: `public Result<Void> delete(@CurrentUser Long userId, @PathVariable Long id)`

### `PUT` `/address/default/{id}`
- **说明**: 
- **签名**: `public Result<Void> setDefault(@CurrentUser Long userId, @PathVariable Long id)`

## 模块 Cart (API Base: `/cart`)

### `GET` `/cart`
- **说明**: 
- **签名**: `public Result<CartVO> getCart(@CurrentUser Long userId)`

### `POST` `/cart/add`
- **说明**: 
- **签名**: `public Result<Void> addToCart(@CurrentUser Long userId, @Validated @RequestBody CartDTO cartDTO)`

### `PUT` `/cart/quantity`
- **说明**: 
- **签名**: `public Result<Void> updateQuantity(@CurrentUser Long userId,
                                       @RequestParam Long productId,
                                       @RequestParam Integer quantity)`

### `DELETE` `/cart/{productId}`
- **说明**: 
- **签名**: `public Result<Void> removeFromCart(@CurrentUser Long userId, @PathVariable Long productId)`

### `DELETE` `/cart/clear`
- **说明**: 
- **签名**: `public Result<Void> clearCart(@CurrentUser Long userId)`

### `GET` `/cart/count`
- **说明**: 
- **签名**: `public Result<Integer> getCartItemCount(@CurrentUser Long userId)`

### `PUT` `/cart/select`
- **说明**: 
- **签名**: `public Result<Void> selectItem(@CurrentUser Long userId,
                                   @RequestParam Long productId,
                                   @RequestParam Boolean selected)`

### `PUT` `/cart/selectAll`
- **说明**: 
- **签名**: `public Result<Void> selectAll(@CurrentUser Long userId, @RequestParam Boolean selected)`

## 模块 Category (API Base: `/category`)

### `GET` `/category/tree`
- **说明**: 
- **签名**: `public Result<List<CategoryTreeVO>> getCategoryTree()`

### `GET` `/category/first`
- **说明**: 
- **签名**: `public Result<List<Category>> getFirstLevel()`

### `GET` `/category/children/{parentId}`
- **说明**: 
- **签名**: `public Result<List<Category>> getChildren(@PathVariable Long parentId)`

### `GET` `/category/{id}`
- **说明**: 
- **签名**: `public Result<Category> getById(@PathVariable Long id)`

### `POST` `/category/add`
- **说明**: 
- **签名**: `public Result<String> add(@RequestBody Category category)`

## 模块 Coupon (API Base: `/coupon`)

### `GET` `/coupon/available`
- **说明**: 
- **签名**: `public Result<List<Coupon>> getAvailableCoupons()`

### `POST` `/coupon/receive/{couponId}`
- **说明**: 
- **签名**: `public Result<Void> receiveCoupon(@CurrentUser Long userId, @PathVariable Long couponId)`

### `GET` `/coupon/my`
- **说明**: 
- **签名**: `public Result<List<UserCoupon>> getMyCoupons(
            @CurrentUser Long userId,
            @ApiParam("状态:0未使用 1已使用 2已过期") @RequestParam(required = false) Integer status)`

### `GET` `/coupon/usable`
- **说明**: 
- **签名**: `public Result<List<UserCoupon>> getUsableCoupons(@CurrentUser Long userId)`

## 模块 Order (API Base: `/order`)

### `POST` `/order/create`
- **说明**: 
- **签名**: `public Result<Order> createOrder(@CurrentUser Long userId, 
                                     @Validated @RequestBody OrderCreateDTO createDTO)`

### `GET` `/order/list`
- **说明**: 
- **签名**: `public Result<PageVO<Order>> getOrderList(
            @CurrentUser Long userId,
            @ApiParam("订单状态") @RequestParam(required = false) Integer status,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize)`

### `GET` `/order/{orderNo}`
- **说明**: 
- **签名**: `public Result<Order> getOrderDetail(@CurrentUser Long userId, @PathVariable String orderNo)`

### `POST` `/order/cancel/{orderNo}`
- **说明**: 
- **签名**: `public Result<Void> cancelOrder(@CurrentUser Long userId, @PathVariable String orderNo)`

### `POST` `/order/pay/{orderNo}`
- **说明**: 
- **签名**: `public Result<Void> payOrder(@PathVariable String orderNo,
                                 @ApiParam("支付方式:1支付宝 2微信") @RequestParam Integer payType)`

### `POST` `/order/confirm/{orderNo}`
- **说明**: 
- **签名**: `public Result<Void> confirmReceive(@CurrentUser Long userId, @PathVariable String orderNo)`

### `DELETE` `/order/{orderNo}`
- **说明**: 
- **签名**: `public Result<Void> deleteOrder(@CurrentUser Long userId, @PathVariable String orderNo)`

### `GET` `/order/admin/list`
- **说明**: 
- **签名**: `public Result<PageVO<Order>> queryOrders(
            @ApiParam("订单号") @RequestParam(required = false) String orderNo,
            @ApiParam("订单状态") @RequestParam(required = false) Integer status,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize)`

### `POST` `/order/admin/ship/{orderNo}`
- **说明**: 
- **签名**: `public Result<Void> shipOrder(@PathVariable String orderNo)`

## 模块 Product (API Base: `/product`)

### `GET` `/product/{id}`
- **说明**: 
- **签名**: `public Result<ProductVO> getById(@PathVariable Long id)`

### `GET` `/product/list`
- **说明**: 
- **签名**: `public Result<PageVO<ProductVO>> list(
            @ApiParam("分类ID") @RequestParam(required = false) Long categoryId,
            @ApiParam("搜索关键词") @RequestParam(required = false) String keyword,
            @ApiParam("最低价格") @RequestParam(required = false) BigDecimal minPrice,
            @ApiParam("最高价格") @RequestParam(required = false) BigDecimal maxPrice,
            @ApiParam("排序字段:price/sales/new") @RequestParam(required = false) String sortField,
            @ApiParam("排序方式:asc/desc") @RequestParam(required = false) String sortOrder,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize)`

### `GET` `/product/hot`
- **说明**: 
- **签名**: `public Result<List<ProductVO>> getHotProducts(
            @ApiParam("数量") @RequestParam(defaultValue = "8") Integer limit)`

### `GET` `/product/recommend`
- **说明**: 
- **签名**: `public Result<List<ProductVO>> getRecommendProducts(
            @ApiParam("数量") @RequestParam(defaultValue = "10") Integer limit)`

### `GET` `/product/new`
- **说明**: 
- **签名**: `public Result<List<ProductVO>> getNewProducts(
            @ApiParam("数量") @RequestParam(defaultValue = "8") Integer limit)`

### `GET` `/product/category/{categoryId}`
- **说明**: 
- **签名**: `public Result<List<ProductVO>> getByCategory(@PathVariable Long categoryId)`

### `POST` `/product`
- **说明**: 
- **签名**: `public Result<Void> create(@RequestBody Product product)`

### `PUT` `/product`
- **说明**: 
- **签名**: `public Result<Void> update(@RequestBody Product product)`

### `DELETE` `/product/{id}`
- **说明**: 
- **签名**: `public Result<Void> delete(@PathVariable Long id)`

## 模块 Review (API Base: `/review`)

### `GET` `/review/product/{productId}`
- **说明**: 
- **签名**: `public Result<PageVO<ProductReview>> getProductReviews(
            @PathVariable Long productId,
            @ApiParam("页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @ApiParam("每页数量") @RequestParam(defaultValue = "10") Integer pageSize)`

### `GET` `/review/product/{productId}/stats`
- **说明**: 
- **签名**: `public Result<Map<String, Object>> getReviewStats(@PathVariable Long productId)`

### `POST` `/review`
- **说明**: 
- **签名**: `public Result<Void> addReview(@CurrentUser Long userId, @RequestBody ProductReview review)`

### `DELETE` `/review/{reviewId}`
- **说明**: 
- **签名**: `public Result<Void> deleteReview(@CurrentUser Long userId, @PathVariable Long reviewId)`

## 模块 Seckill (API Base: `/seckill`)

### `GET` `/seckill/sessions`
- **说明**: 
- **签名**: `public Result<List<SeckillSessionDTO>> getSeckillSessions()`

### `GET` `/seckill/list`
- **说明**: 
- **签名**: `public Result<List<SeckillProduct>> getSeckillList()`

### `GET` `/seckill/ongoing`
- **说明**: 
- **签名**: `public Result<List<SeckillProduct>> getOngoingSeckills()`

### `GET` `/seckill/upcoming`
- **说明**: 
- **签名**: `public Result<List<SeckillProduct>> getUpcomingSeckills()`

### `GET` `/seckill/{seckillId}`
- **说明**: 
- **签名**: `public Result<SeckillProduct> getSeckillDetail(@PathVariable Long seckillId)`

### `POST` `/seckill/do`
- **说明**: 
- **签名**: `public Result<String> doSeckill(@CurrentUser Long userId, @Validated @RequestBody SeckillDTO seckillDTO)`

### `GET` `/seckill/check/{seckillId}`
- **说明**: 
- **签名**: `public Result<Boolean> checkKilled(@CurrentUser Long userId, @PathVariable Long seckillId)`

### `POST` `/seckill/create`
- **说明**: 
- **签名**: `public Result<Void> createSeckill(@RequestBody SeckillProduct seckillProduct)`

### `PUT` `/seckill/update`
- **说明**: 
- **签名**: `public Result<Void> updateSeckill(@RequestBody SeckillProduct seckillProduct)`

### `POST` `/seckill/publish/{id}`
- **说明**: 
- **签名**: `public Result<Void> publishSeckill(@PathVariable Long id)`

## 模块 Subscribe (API Base: ``)

### `POST` `/subscribe`
- **说明**: 
- **签名**: `public Result<Void> subscribe(@RequestParam String email)`

### `GET` `/subscribe`
- **说明**: 
- **签名**: `public Result<Void> subscribeGet(@RequestParam String email)`

## 模块 User (API Base: `/user`)

### `POST` `/user/register`
- **说明**: 
- **签名**: `public Result<Void> register(@Validated @RequestBody RegisterDTO registerDTO)`

### `POST` `/user/login`
- **说明**: 
- **签名**: `public Result<LoginVO> login(@Validated @RequestBody LoginDTO loginDTO)`

### `POST` `/user/logout`
- **说明**: 
- **签名**: `public Result<Void> logout(@CurrentUser Long userId)`

### `GET` `/user/info`
- **说明**: 
- **签名**: `public Result<UserVO> getUserInfo(@CurrentUser Long userId)`

### `PUT` `/user/info`
- **说明**: 
- **签名**: `public Result<Void> updateUserInfo(@CurrentUser Long userId, @RequestBody UserVO userVO)`

### `PUT` `/user/password`
- **说明**: 
- **签名**: `public Result<Void> changePassword(@CurrentUser Long userId,
                                       @RequestParam String oldPassword,
                                       @RequestParam String newPassword)`

### `GET` `/user/check/username`
- **说明**: 
- **签名**: `public Result<Boolean> checkUsername(@RequestParam String username)`

### `GET` `/user/check/phone`
- **说明**: 
- **签名**: `public Result<Boolean> checkPhone(@RequestParam String phone)`

