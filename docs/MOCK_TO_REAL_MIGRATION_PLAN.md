# Eden Nutrition - 管理后台前端 Mock 数据替换真实接口全流程方案

**文档状态**: 已初步规划完成  
**目标工程**: `eden-admin-vue/eden-nutrition-admin-front` (React + Vite)

---

## 一、 背景与总体思路

当前前端工程中，各业务页面（如产品列表、订单列表、秒杀列表等）均大量依赖于静态的本地模拟数据（主要集中在 `src/data/mock.ts` 中）。在完成了**跨域代理配置**和 **Axios 请求库拦截器封装**（均已就绪）后，我们现需将所有静态 Mock 数据全面切换至 Spring Boot 后端提供的真实 API（参照 `ADMIN_API_DOCUMENTATION.md`）。

**总体替换工作流如下**：
1. **API 层扩建**：根据业务模块，在 `src/api` 目录下创建对应的接口文件（TypeScript），定义入参和出参。
2. **页面数据源注入**：在 React 组件中移除由于引入 `mock.ts` 而产生的硬编码，替换为真实的异步 Hook 请求（如 `useEffect` 获取数据）。
3. **联调与清理**：打通增删改查闭环后，彻底删除前端工程中的 Mock 数据文件。

---

## 二、 模块级替换详细计划

### 1. 秒杀模块 (Seckill Management) - 优先替换模块
*前期在方案四中已将 `src/api/seckill.ts` 搭建完毕。*
- **文件**: `src/pages/SeckillList.tsx`
- **替换动作**:
  1. 移除对 `mockSeckills` 静态数组的引用。
  2. 引入 `import { getSeckillPage, addSeckill, updateSeckill, finishSeckill, deleteSeckill } from '@/api/seckill';`。
  3. 定义 `const [data, setData] = useState<SeckillVO[]>([])` 和 `const [total, setTotal] = useState(0)`。
  4. 使用 `useEffect` 在组件挂载及分页参数（`pageNum`）变化时，调用 `getSeckillPage({ page: pageNum, pageSize: 10 })`，并将结果回填至 `setData(res.list)`。
  5. 替换现有的强行结束与删除按钮 onClick 事件，走真实的 `finishSeckill` 和 `deleteSeckill` 请求，请求完成后调用重新获取列表数据的函数（如 `fetchData()`）。

### 2. 用户与认证模块 (Auth / Login)
- **文件**: `src/pages/Login.tsx` / `src/store/useAuthStore.ts`
- **替换动作**:
  1. 创建 `src/api/auth.ts`，定义 `login(username, password)` 方法，请求后端的真实登录接口。
  2. 将返回出的真实 JWT Token 存入全局 Store（zustand）或 `localStorage`中。
  3. 移除 `mock.ts` 中伪造的登录校验（如比对 admin/123456 的硬代码）。

### 3. 商品模块 (Product Management)
- **文件**: `src/pages/ProductList.tsx`
- **替换动作**:
  1. 创建 `src/api/product.ts`。定义对齐 `ADMIN_API_DOCUMENTATION.md` 的接口。
     - `getProductList(params)` -> `GET /admin/product/list`
     - `addProduct(data)` -> `POST /admin/product`
     - `updateProduct(data)` -> `PUT /admin/product`
     - `updateProductStatus(id, status)` -> `PUT /admin/product/status/{id}/{status}`
     - `deleteProduct(id)` -> `DELETE /admin/product/{id}`
  2. 移除 `ProductList.tsx` 对 `mockProducts` 的导入，改为真实分页请求。
  3. 修复表格开关（Switch）：当触发上下架操作时，调用 `updateProductStatus`。

### 4. 订单模块 (Order Management)
- **文件**: `src/pages/OrderList.tsx`
- **替换动作**:
  1. 创建 `src/api/order.ts`。
     - `getOrderList(params)` -> `GET /admin/order/list`
     - `getOrderDetail(id)` -> `GET /admin/order/{id}`
     - `deliverOrder(orderId, company, sn)` -> `POST /admin/order/deliver`
  2. 替换掉针对 `mockOrders` 的使用。
  3. 订单的“发货”操作之前是由本地直接变更状态，现接上后端的 `deliverOrder` 接口，必须收集承运商与物流单号并提交。

### 5. 分类模块 (Category Management)
- **文件**: `src/pages/CategoryList.tsx`
- **替换动作**:
  1. 创建 `src/api/category.ts`，打通 `GET /category/tree` (可以复用客户端或提供单独的 admin 类型获取接口)。
  2. 移除硬编码树状菜单数据。

### 6. 仪表盘模块 (Dashboard)
- **文件**: `src/pages/Dashboard.tsx`
- **说明**: 
  - 如果后端暂未提供完整的总销售额、订单核心折线图等数据接口，可以**暂时保留该页面的 Mock 数据**以作界面展示，亦或在后端加急开发一组专门的聚合基础统计接口。

---

## 三、 数据渲染时的兼容注意点

1. **分页数据适配**:
   大部分真实的后端返回包裹层类似如下：
   ```json
   {
       "total": 50,
       "pages": 5,
       "list": [...]
   }
   ```
   前端的 `Table` 组件应在外部接收这个完整的 `total` 并将其传递给底部的 Pagination 翻页器，实现真正的服务端分页，而非目前的将全体纯数组在单页切片。
   
2. **时间格式化**:
   Mock 中的可能写死了 `"2026-03-18 10:00"`，而真实后端可能抛出 ISO时间例如 `"2026-03-18T10:00:00"`，此时应在 TS 层统一使用 `dayjs` 或前端公共格式化函数处理，防止表格样式撑穿。

3. **图片资源加载**:
   从 `productMainImage` 替换后，注意图片的相对路径或 URL 开头（后端可能反出的是对象存储 OSS 强URL），无需前端自行作拼接。

---

## 四、 实施步骤建议 (执行落准)
1. 开一个**全新 Git 分支**（如 `feature/real-data-integration`）。
2. 在 `src/api` 下**一口气生成所有的 `.ts` 文件**与数据类型，确保底层定义齐备。
3. **逐个路由（/seckill -> /product -> /order）** 替换组件的 `useEffect` 和方法。
4. 去除 `import * from '../data/mock'`，并直接通过 `npm run dev` 看页面是否白屏或提示错误。
5. 最终验证无误后，**全盘删除 `src/data/mock.ts` 文件**。
