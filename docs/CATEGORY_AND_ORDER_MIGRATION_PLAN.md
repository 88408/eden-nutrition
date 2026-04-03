# 后台订单与分类模块 Mock 迁移与开发实施方案 (Category & Order Module Migration Plan)

本方案用于指导 `eden-nutrition-admin-front` 中**订单管理 (OrderList)** 和 **分类管理 (CategoryList)** 模块剥离本地假数据 (Mock)，全面接入基于 `eden-admin` 的 SpringBoot 后端真实 API 的详细过程。

---

## 一、 订单模块 (Order Module) 改造方案

目前订单模块在前端依赖于静态的 `mockOrders` 数据。经过审计，后端 `eden-admin` 工程的 `AdminOrderController` 已经提供了对应完备的管理端订单处理接口，只需前端完成网络层与组件状态逻辑层的对接。

### 1. 后端接口现状分析 (已就绪)
在 `eden-admin` 下的 `AdminOrderController.java` 中已由 `@RequireAdminLogin` 保护了下列可用接口：
* `GET /admin/order/list`：分页条件查询所有买家订单 (`AdminOrderQueryDTO`)，返回 `PageVO<OrderAdminVO>`。
* `GET /admin/order/{orderId}`：根据 ID 获取订单详细信息（含商品项目内容）。
* `POST /admin/order/deliver`：操作发货，接收 `OrderDeliverDTO`（含订单ID与快递单号）。

*(注：后端订单状态流转、分页逻辑均已实现，无需对后端进行额外调整。)*

### 2. 前端 API 层补充 (`src/api/order.ts`)
新建 `src/api/order.ts`，利用现成的 `request` 拦截器连接上述后端接口：
```typescript
import request from './request';

export interface OrderQueryDTO {
  page: number;
  pageSize: number;
  orderNo?: string;
  status?: number;
}

export const getOrderPage = (params: OrderQueryDTO) => {
  return request.get<any>('/admin/order/list', { params });
};

export const getOrderDetail = (id: number) => {
  return request.get<any>(`/admin/order/${id}`);
};

export const deliverOrder = (data: { orderId: number; trackingNo: string; trackingCompany?: string }) => {
  return request.post<void>('/admin/order/deliver', data);
};
```

### 3. 前端 UI 组件改造 (`src/pages/OrderList.tsx`)
1. **清理 Mock 持久化**：移除内部硬编码的 `mockOrders` 及其相关的本地 `map` 操作。
2. **初始化分页拉取**：在 `useEffect` 中挂载 `fetchOrderData()` 方法，根据当前的 `pagination` 与筛选条件去执行 `getOrderPage`。
3. **对接“发货”逻辑**：
   * 将界面上的“发货”按钮点击事件交由真实的 API：`await deliverOrder({ orderId, trackingNo })` 来执行。
   * 操作后触发 `fetchOrderData()` 刷新界面重新渲染单据状态。
   * 对任何服务端失败（如库存异常、非法状态）捕获异常并抛出通用的 `toast.error()`。

---

## 二、 分类模块 (Category Module) 改造方案

分类模块主要用于管理商品的层级类目，它的特别之处在于其包含层级关系的展示（分类树）。

### 1. 后端接口补全规划 (`AdminCategoryController.java`)
**目前现状**：`eden-service/CategoryService.java` 中已拥有完善的实现（如 `getCategoryTree()`, `add()`, `update()`, `delete()`等），但这些方法并没有在 `eden-admin` 端暴露出配套的 Controller，导致管理员目前并无操作入口。
**开发计划**：
我们需要在 `eden-admin` 下的主工程内创建 `AdminCategoryController.java` 以暴露业务：
* `GET /admin/category/tree`：获取完整树形类目。
* `POST /admin/category`：后台新增分类（要求携带父级树ID或名字等信息 `CategorySaveDTO`）。
* `PUT /admin/category`：后台更新分类数据。
* `DELETE /admin/category/{id}`：后台删除分类（服务端需要校验是否存在子节点或者挂载商品）。

```java
// 范例结构 (AdminCategoryController.java)
@RestController
@RequestMapping("/admin/category")
@RequireAdminLogin
public class AdminCategoryController {
    // 依赖注入 CategoryService
    // 暴露出对应操作如 getTree, add, update, delete
}
```

### 2. 前端 API 层补充 (`src/api/category.ts`)
扩充目前的 `category.ts`，加入针对管理后台的修改接口：
```typescript
import request from './request';

export const getCategoryTree = () => {
    return request.get<any>('/admin/category/tree');
}
export const addCategory = (data: any) => request.post('/admin/category', data);
export const updateCategory = (data: any) => request.put('/admin/category', data);
export const deleteCategory = (id: number) => request.delete(`/admin/category/${id}`);
export const updateCategoryStatus = (id: number, status: number) => request.put(`/admin/category/status/${id}/${status}`);
```

### 3. 前端 UI 组件改造 (`src/pages/CategoryList.tsx`)
1. **数据拉取**：界面加载调用 `getCategoryTree` 渲染 Ant Design 或自有分类表格树组件，抛弃原 `mockCategories`。
2. **事件替换**：
   * **新增子分类/一级分类**：弹出 Modal 层填入字段，保存事件替换为 `addCategory`。
   * **修 / 删除**：点击按钮并成功通过拦截层之后触发对应的 `updateCategory` 或 `deleteCategory`，执行后调用 `fetchTreeData()` 进行数据与视图的同步。

---

## 三、 推进顺序建议

依据业务实现依赖关系，建议采取以下执行顺序进行后续开发：
1. **第一步（先做分类）**：处理 `eden-admin` 中缺失的 `AdminCategoryController` 的编码并与前端 `CategoryList.tsx` 整合完毕。（*因商品管理严格依赖于正确的分类数据，优先落实后台类目可以巩固系统业务逻辑的连贯性*）。
2. **第二步（后做订单）**：前端建立 `src/api/order.ts` 并在 `OrderList.tsx` 中把表格循环、分页器对接。并在页面中集成简单的发货弹窗来消费真实的 `deliverOrder` 接口。

文档至此梳理完毕，各阶段可独立进入开发执行实施。
