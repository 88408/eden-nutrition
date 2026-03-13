# Mock 数据迁移至真实后端数据方案 (Mock to Real Data Migration Plan)

本方案旨在打通 **客户端 (`eden-vue`)**、**管理端 (`eden-admin-vue`)** 与 **后端 (`eden-service`)** 之间的数据链路，替换目前前端硬编码的 Mock 数据。

覆盖核心业务模块：
1.  **商品 (Product)** - 浏览、详情、搜索
2.  **购物车与订单 (Cart & Order)** - 加购、下单、支付、历史订单
3.  **秒杀 (Flash Sale)** - 限时抢购活动

---

## 1. 总体准备 (Common Infrastructure)

### 1.1 接口代理配置
确保前端项目 (`eden-vue` 和 `eden-admin-vue`) 的 `vite.config.ts` 正确配置了 API 代理，解决跨域问题。
```typescript
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    rewrite: (path) => path.replace(/^\/api/, '')
  }
}
```

### 1.2 认证状态同步
后端多数交易接口（购物车、订单、秒杀）均带有 `@RequireLogin` 注解。
*   **前端动作**: 确保 `axios` 拦截器在请求头中携带 `Authorization: Bearer <token>`。
*   **登录流**: 商品浏览可匿名，但加入购物车或下单前需校验登录状态，若未登录跳转至 `/login`。

---

## 2. 商品模块迁移 (Product Migration)

后端接口已就绪 (`ProductController`)，管理端已打通。重点在于客户端 `eden-vue` 的改造。

### 2.1 涉及页面
*   `src/pages/ProductList.tsx` (商品列表)
*   `src/pages/ProductDetail.tsx` (商品详情)
*   `src/components/FeaturedProducts.tsx` (首页推荐)

### 2.2 改造方案
1.  **数据源替换**:
    *   移除页面内的 `const products = [...]` 硬编码数组。
    *   调用 `src/api/product.ts` 中的 `getProductList` 和 `getProductDetail`。
    *   注意字段映射：前端 `imageUrl` 对应后端 `mainImage`，需在 API 层或组件层做适配。
2.  **分类联动**:
    *   列表页增加分类筛选，调用 `GET /category/list` 获取真实分类数据。

---

## 3. 购物车与订单模块迁移 (Cart & Order Migration)

### 3.1 购物车 (`src/pages/Cart.tsx`)
*   **现状**: 本地 State 管理，刷新丢失。
*   **目标**: 服务端存储购物车。
*   **后端接口**: `CartController` (`POST /cart/add`, `GET /cart/list`, `PUT /cart/update`, `DELETE /cart/delete`).
*   **改造点**:
    *   `ProductDetail` 点击"加入购物车" -> 调用 `POST /cart/add`。
    *   进入购物车页面 -> 调用 `GET /cart/list`。
    *   修改数量 (+/-) -> 这里的更新需防抖 (Debounce) 后调用 `PUT /cart/update`。

### 3.2 结算与下单 (`src/pages/Checkout.tsx`)
*   **现状**: 纯 UI 展示，点击支付仅弹窗。
*   **后端接口**: `OrderController` (`POST /order/create`).
*   **改造点**:
    *   提交订单时，收集收货地址 ID 和支付方式。
    *   调用创建订单接口，获取 `orderNo`。
    *   跳转至支付页或订单详情页。

### 3.3 个人中心订单 (`src/pages/UserCenter.tsx`)
*   **现状**: Mock 两条固定订单。
*   **后端接口**: `OrderController` (`GET /order/list`).
*   **改造点**:
    *   使用 `useEffect` 获取真实订单列表。
    *   状态映射：后端状态 (0:待支付, 1:已支付...) 映射为前端展示文本。

---

## 4. 秒杀模块迁移 (Flash Sale Migration)

### 4.1 后端改造 (`eden-web`)
当前 `SeckillController` 缺少部分管理功能，需补充。

#### A. 管理端接口 (Admin API)
*   `POST /seckill/create`: 创建秒杀活动（入参：商品ID、价格、库存、时间段）。
*   `PUT /seckill/update`: 更新活动。
*   `GET /seckill/list`: 分页查询活动。
*   `POST /seckill/publish/{id}`: 上架活动（预热库存至 Redis）。

#### B. 客户端接口 (Client API)
*   `GET /seckill/sessions`: **[新增]** 获取当日秒杀时间段列表 (如 10:00, 14:00)，包含每个时间段的状态 (已结束/进行中/即将开始)。
*   `GET /seckill/list`: 根据时间段获取商品列表。

### 4.2 管理端前端 (`eden-admin-vue`)
*   新增 `src/api/seckill.ts`。
*   开发 `Marketing/FlashSale` 页面：
    *   展示秒杀场次表。
    *   提供商品选择弹窗，从现有商品库中选择商品加入秒杀。

### 4.3 客户端前端 (`eden-vue`)
*   `src/pages/FlashSale.tsx`:
    *   **移除 Mock**: 删除 `flashSaleItems` 和 `timeSlots` 常量。
    *   **时间轴渲染**: 请求 `/seckill/sessions` 动态生成时间轴 Tabs。
    *   **商品列表**: 切换 Tab 时请求该时间段的秒杀商品。
    *   **倒计时**: 基于服务器时间 (Response Header `Date` 或接口返回的时间戳) 计算，避免客户端时间不准。

---

## 5. 执行步骤 (Step-by-Step)

1.  **[后端]** 完善 `SeckillController` 管理接口，确认 `Product/Cart/Order` 接口可用性。
2.  **[Admin]** 开发秒杀管理页，录入真实秒杀数据。
3.  **[Client - Product]** 改造 `ProductList` 和 `Detail`，对接真实商品库。
4.  **[Client - Cart/Order]** 改造购物车和结算流程，联调下单逻辑。
5.  **[Client - FlashSale]** 改造秒杀页，完成活动展示与抢购链路。
6.  **[全链路测试]** 注册新用户 -> 浏览 -> 加购 -> 下单 -> 支付 -> 查看订单。

## 6. 注意事项
*   **图片资源**: 确保管理端录入商品时图片 URL 可访问（建议使用现有静态资源路径或公网图床）。
*   **数据一致性**: 秒杀价格不能高于原价；下单扣减库存需保证事务性。
*   **异常处理**: 接口报错（如库存不足、活动结束）需在前端给出友好提示 (`Toast`)。
