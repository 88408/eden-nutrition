# 前端 Mock 数据替换真实 API 进度报告

该文档记录将 `eden-nutrition-admin-front` 中的本地假数据（Mock）彻底转用 Axios 接入 SpringBoot API 的详细工作进展。每一页（或者模块）在执行完毕后都会在此更新。

## 替换总览进度
| 模块 | 文件 | 替换状态 | 说明 |
|---|---|---|---|
| 秒杀模块 | `src/pages/SeckillList.tsx` | ✅ 已完成 | 移除了 `mockSeckills` 静态数据。全面接入了真实 Axios 请求。 |
| 认证模块 | `src/pages/Login.tsx` | ✅ 已完成 | 已在 `eden-admin` 创建 `AdminUserController` 与 `AdminAuthInterceptor`，并在前端对接真实 `/admin/user/login` 接口，移除了 Mock JWT。 |
| 商品模块 | `src/pages/ProductList.tsx` | ✅ 已完成 | 已创建 `src/api/product.ts` 和 `src/api/category.ts`，彻底重写了 React 文件移除 mockProducts，实现真实的商品增删改查、状态切换及批量操作 |
| 订单模块 | `src/pages/OrderList.tsx` | ⏳ 待处理 | 需要创建 `src/api/order.ts` 接入真实订单分页、发货接口。 |
| 分类模块 | `src/pages/CategoryList.tsx` | ⏳ 待处理 | 后端已有 `/category/tree` 接口，待后续重构 React 组件并剔除 `mockCategories`。 |
| 数据看板 | `src/pages/Dashboard.tsx` | ⏳ 待处理 | 暂无全局聚合接口，建议后期再做或最后保留部分 mock。 |

---

## 详细实施记录

### 1. 秒杀模块 (`SeckillList.tsx`) - ✅ 完成
- **数据源获取**：组件挂载时调用 `getSeckillPage` 获取服务器分页数据，彻底移除了内部数组。
- **动态方法改写**：将新增、编辑和状态开关全部代理到真实后端的 `addSeckill`, `updateSeckill`, `deleteSeckill` 网络请求中。
- **状态同步刷新**：所有动作在 `try-catch` 后均自动 `fetchData()` 更新列表。

### 2. 商品模块 (`ProductList.tsx`) - ✅ 完成
- **API 层已搭建**：`src/api/product.ts` 接口包装器（含增删改查分页和 `status` 上下架切换）已经构建完成。
- **分类层已搭建**：`src/api/category.ts` 获取所有分类树。
- **重构完成**：彻底重写了 `ProductList.tsx`，移除了 `mockProducts` 静态数据，接入了真实的分页查询，同时处理了表单中的分类级联选择和商品状态操作。

### 3. 认证模块 (`Login.tsx`) - ✅ 完成
- **后端权限隔离**：在 `eden-admin` 中创建了 `AdminAuthInterceptor` 与 `@RequireAdminLogin` 拦截器体系，在 `WebMvcConfig` 中配置。
- **登录控制层**：在 `eden-admin` 新增了 `AdminUserController.java` 下发真实 JWT token。
- **服务层校验**：`UserServiceImpl` 中增加了专用于 Admin 的登录密码/角色校验。
- **前端调用真实接口**：修改 `Login.tsx` 中的登录事件，通过 `await adminLogin(...)` 对接服务端，替换了硬编码假 Token 响应，并将返回信息更新到全局 Store。

### 📝 Troubleshooting / 遇到的问题
1. **TS 类型报错**: 原本定义的 `request.get<PageResult<ProductVO>>` 期望返回的是 `AxiosResponse`，但在 axios 拦截器中因为强行剥离了 `data` 层级，导致直接返回了原始数据（没有 `data` 或者 `list` 属性对应的强类型校验）。我在调用时加上了 `any` 强转并做了兼容取值修复了 `res.list` 报错。
2. **UI 组件参数报错**: `ConfirmModal.tsx` 组件并未开放 `description` 与 `isDanger` 这两个属性，它实际使用的是 `message`。我替换对应的属性名后已成功消除类型报警。
