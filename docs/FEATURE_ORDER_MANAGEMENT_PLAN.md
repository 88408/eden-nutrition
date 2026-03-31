# Eden Nutrition - B端订单管理模块开发方案 (Order Management Plan)

本文档旨在为 `eden-admin` (后端管理端) 和 `eden-admin-vue` (前端管理端) 的**订单管理(Order Management)** 功能提供详细、完善的开发与联调指南。

当前整体进度约为 60%，主要阻碍在于后端 `eden-admin` 专属接口的缺失。本方案将指导如何打通最后 40% 的开发工作。

---

## 1. 业务需求与目标

管理员通过 B端后台需要完成以下核心操作：
1. **全量订单查询 (Order List)**：支持多条件、分页查询（按订单号、用户ID、订单状态、时间范围）。
2. **订单详情查看 (Order Detail)**：查看订单基本信息、收货地址、支付金额明细、包含的商品明细 (`OrderItem`)。
3. **订单发货 (Order Delivery)**：针对“已支付、待发货”的订单，录入物流公司和物流单号，并将状态流转为“已发货”。
4. **后台取消/关闭订单 (Cancel Order) [可选/扩展]**：应对特殊客诉情况，强制关闭未完成或问题订单。

---

## 2. 后端接口设计 (RESTful API in `eden-admin`)

所有 B 端订单管理的接口应统一前缀为 `/admin/order`，并在 `eden-admin` 模块中实现，必须经过后台权限校验。

### 2.1 分页条件查询订单
* **接口路径**: `GET /admin/order/list`
* **请求参数 (Query)**:
  * `pageNum` (int, default: 1)
  * `pageSize` (int, default: 10)
  * `orderNo` (String, 订单号模糊或精确查询)
  * `status` (Integer, 订单状态：0-待支付 1-已支付待发货 2-已发货 3-已完成 4-已取消)
  * `startTime` / `endTime` (String, 下单时间范围)
* **响应数据**:
  * `Result<PageVO<OrderAdminVO>>` (包含订单基础信息、收货人信息、总金额等)

### 2.2 获取订单详情
* **接口路径**: `GET /admin/order/{orderId}`
* **请求参数 (Path)**:
  * `orderId` (Long)
* **响应数据**:
  * `Result<OrderDetailAdminVO>` (除了包含 `OrderAdminVO` 的信息，还必须包含 `List<OrderItemVO>` 商品明细列表)

### 2.3 订单发货
* **接口路径**: `POST /admin/order/deliver`
* **请求体 (JSON)**:
  ```json
  {
    "orderId": 123456789,
    "deliveryCompany": "顺丰速运",
    "deliverySn": "SF1234567890"
  }
  ```
* **响应数据**: `Result<Void>` (成功/失败)

---

## 3. 后端代码实现步骤 (`eden-admin` & `eden-service`)

### 步骤 1: 补充 DTO 和 VO (`eden-pojo`)
在 `eden-pojo/src/main/java/eden/pojo/` 路径下：
1. **创建请求 DTO**: `dto/AdminOrderQueryDTO.java` 和 `dto/OrderDeliverDTO.java`。
2. **创建响应 VO**: `vo/OrderAdminVO.java` 和 `vo/OrderDetailAdminVO.java`。

### 步骤 2: 数据访问层 (`eden-mapper`)
1. 检查 `OrderMapper.java` 和 `OrderMapper.xml`。
2. 编写多条件分页查询的 XML SQL:
   ```xml
   <select id="selectAdminOrderList" resultType="eden.pojo.Order">
       SELECT * FROM `order`
       <where>
           <if test="query.orderNo != null and query.orderNo != ''"> AND order_no LIKE CONCAT('%', #{query.orderNo}, '%') </if>
           <if test="query.status != null"> AND status = #{query.status} </if>
           <!-- 其他时间范围等条件 -->
       </where>
       ORDER BY create_time DESC
   </select>
   ```

### 步骤 3: 业务逻辑层 (`eden-service`)
1. 在 `OrderService.java` 中增加后台专用方法：
   ```java
   PageVO<OrderAdminVO> getAdminOrderPage(AdminOrderQueryDTO queryDTO);
   OrderDetailAdminVO getAdminOrderDetail(Long orderId);
   void deliverOrder(OrderDeliverDTO deliverDTO);
   ```
2. 在 `OrderServiceImpl.java` 中实现逻辑。
   * **发货逻辑重点**：
     * 必须校验当前订单状态是否为 **1 (已支付，待发货)**。
     * 更新状态为 **2 (已发货)**。
     * 写入 `delivery_company`, `delivery_sn` 和 `delivery_time`。
     * 记录操作日志 (`OperationLogService.saveLog(...)`)。

### 步骤 4: 控制层 (`eden-admin`)
在 `eden-admin/src/main/java/eden/admin/controller/` 新建 `AdminOrderController.java`:
```java
@RestController
@RequestMapping("/admin/order")
@Tag(name = "管理端-订单管理")
public class AdminOrderController {
    
    @Autowired
    private OrderService orderService;

    @GetMapping("/list")
    @Operation(summary = "分页查询订单")
    public Result<PageVO<OrderAdminVO>> list(AdminOrderQueryDTO queryDTO) {
        return Result.success(orderService.getAdminOrderPage(queryDTO));
    }
    
    // ... detail 和 deliver 接口
}
```

---

## 4. 前端联调指南 (`eden-admin-vue`)

当前 `OrderManagement` 页面已完成初步 UI 构建，需按以下步骤对接后端接口：

### 4.1 API 层对接 (`api/order.ts`)
修改或新增 `eden-admin-vue/src/api/order.ts` 文件：
```typescript
import client from './client';

export const fetchOrderList = (params: any) => {
  return client.get('/admin/order/list', { params });
};

export const fetchOrderDetail = (id: number) => {
  return client.get(`/admin/order/${id}`);
};

export const deliverOrder = (data: { orderId: number; deliveryCompany: string; deliverySn: string }) => {
  return client.post('/admin/order/deliver', data);
};
```

### 4.2 页面状态管理 (`store/slices/orderSlice.ts`) [可选]
若项目使用 Redux Toolkit，可在此处定义异步 Thunk 以方便全局状态派发。如果是 Zustand 或 React Query，则略过此步，直接在组件中调用 API。

### 4.3 界面逻辑绑定 (`pages/OrderManagement/index.tsx`)
1. **列表数据加载**: 在 `useEffect` 中调用 `fetchOrderList`，将数据绑定到 Table 组件，并处理 Pagination 变更。
2. **搜索防抖**: 搜索框输入触发列表刷新，建议添加防抖（Debounce）处理。
3. **发货弹窗 (Modal)**: 
   * 点击"发货"按钮打开 Modal。
   * 表单验证要求必填物流公司和单号。
   * 提交表单时调用 `deliverOrder` 并显示 Toast 成功提示，随后自动触发列表状态刷新。
4. **详情Drawer/Modal**: 点击“查看详情”展示对应的商品列表及买家地址信息。

---

## 5. 测试与注意事项 (Testing & Cautions)

1. **状态流转安全性**: 后端在 `deliverOrder` 中**必须**防范越权发货或重复发货（例如利用数据库乐观锁 `UPDATE order SET status = 2 WHERE id = x AND status = 1`）。
2. **读写分离与缓存**: 后台由于多条件复杂查询较多，建议不走 Redis 缓存，直接打向 MySQL 从库 (如果有读写分离架构)。
3. **数据假脱敏**: 视后台系统权限要求而定，部分低权限客服角色查看订单详情时，收货人手机号等敏感信息需要脱敏返回 (如 `138****1234`)。这也是未来的拓展点之一。