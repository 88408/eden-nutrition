# 订单管理接口文档

## 概述
订单管理分为两个部分：
- **后台管理接口**：管理员查看订单列表、订单详情、发货管理
- **前台用户接口**：用户创建订单、查询订单、支付、收货、取消等

---

## 公共说明

### 全局请求头
```
Authorization: Bearer {token}    // JWT token（大部分接口需要）
Content-Type: application/json
```

### 统一响应格式
```json
{
  "code": 200,
  "message": "成功",
  "data": {},
  "timestamp": 1704067200000
}
```

### 订单状态码
| 状态码 | 含义 |
|--------|------|
| 0 | 待支付 |
| 1 | 已支付 |
| 2 | 已发货 |
| 3 | 已完成 |
| 4 | 已取消 |
| 5 | 已退款 |

### 支付方式
| 代码 | 含义 |
|------|------|
| 1 | 支付宝 |
| 2 | 微信 |

### 订单类型
| 代码 | 含义 |
|------|------|
| 0 | 普通订单 |
| 1 | 秒杀订单 |
| 2 | 团购订单 |

---

## 后台订单接口（/admin/order）

### 1. 分页查询订单
- **路径与方法**：`GET /admin/order/list`
- **功能描述**：分页查询所有订单，支持按订单号、状态、时间范围筛选
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| page | Query | Integer | ✗ | 页码，默认1 | 1 |
| size | Query | Integer | ✗ | 每页数量，默认10 | 10 |
| orderNo | Query | String | ✗ | 订单号，支持模糊查询 | "2024011500001" |
| status | Query | Integer | ✗ | 订单状态 0-5 | 1 |
| startTime | Query | String | ✗ | 下单起始时间，ISO 8601格式 | "2024-01-15T00:00:00" |
| endTime | Query | String | ✗ | 下单结束时间，ISO 8601格式 | "2024-01-16T00:00:00" |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "list": [
      {
        "id": 1,
        "orderNo": "2024011500001",
        "userId": 123,
        "totalAmount": 15998.00,
        "payAmount": 14998.00,
        "status": 2,
        "createTime": "2024-01-15T10:30:00",
        "receiverName": "张三",
        "receiverPhone": "13800138000",
        "receiverDetailAddress": "北京市朝阳区某街道1号"
      }
    ],
    "total": 156,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 16,
    "hasNext": true,
    "hasPrev": false
  },
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `分页参数错误` - page 或 size 不合法

---

### 2. 获取订单详情
- **路径与方法**：`GET /admin/order/{orderId}`
- **功能描述**：获取指定订单的完整详情，包含订单项、收货信息等
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| orderId | Path | Long | ✓ | 订单ID | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "orderNo": "2024011500001",
    "userId": 123,
    "totalAmount": 15998.00,
    "payAmount": 14998.00,
    "freightAmount": 10.00,
    "discountAmount": 1000.00,
    "couponId": 5,
    "status": 2,
    "payType": 1,
    "payTime": "2024-01-15T10:35:00",
    "shipTime": "2024-01-15T15:00:00",
    "receiveTime": null,
    "deliveryCompany": "圆通快递",
    "deliverySn": "YT123456789",
    "receiverName": "张三",
    "receiverPhone": "13800138000",
    "receiverAddress": "北京市朝阳区某街道1号",
    "remark": "请在下午3点后送达",
    "orderType": 0,
    "createTime": "2024-01-15T10:30:00",
    "updateTime": "2024-01-15T15:00:00",
    "orderItems": [
      {
        "id": 1,
        "orderId": 1,
        "productId": 100,
        "productName": "苹果 iPhone 15",
        "productImage": "https://example.com/iphone15.jpg",
        "purchasePrice": 5999.00,
        "quantity": 2,
        "totalPrice": 11998.00
      },
      {
        "id": 2,
        "orderId": 1,
        "productId": 101,
        "productName": "AirPods Pro",
        "productImage": "https://example.com/airpodspro.jpg",
        "purchasePrice": 1999.00,
        "quantity": 1,
        "totalPrice": 1999.00
      }
    ]
  },
  "timestamp": 1704067200000
}
```

**失败示例（404 Not Found）：**
```json
{
  "code": 404,
  "message": "订单不存在",
  "data": null,
  "timestamp": 1704067200000
}
```

---

### 3. 订单发货
- **路径与方法**：`POST /admin/order/deliver`
- **功能描述**：标记订单已发货，记录物流信息
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| orderId | Body | Long | ✓ | 订单ID | 1 |
| deliveryCompany | Body | String | ✓ | 物流公司名称 | "圆通快递" |
| deliverySn | Body | String | ✓ | 物流单号 | "YT123456789" |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "发货成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "只能对已支付的订单进行发货操作",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `订单不存在` - orderId 对应的订单不存在
  - `只能对已支付的订单进行发货操作` - 订单状态不是已支付（状态不为 1）
  - `物流公司和物流单号不能为空` - deliveryCompany 或 deliverySn 为空

---

## 前台订单接口（/order）

### 1. 创建订单
- **路径与方法**：`POST /order/create`
- **功能描述**：根据购物车商品创建订单
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| addressId | Body | Long | ✓ | 收货地址ID | 10 |
| productIds | Body | List<Long> | ✓ | 要购买的商品ID列表 | [100, 101] |
| couponId | Body | Long | ✗ | 使用的优惠券ID | 5 |
| userCouponId | Body | Long | ✗ | 用户优惠券ID | 25 |
| remark | Body | String | ✗ | 订单备注 | "请在下午3点后送达" |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "订单创建成功",
  "data": {
    "id": 1,
    "orderNo": "2024011500001",
    "userId": 123,
    "totalAmount": 15998.00,
    "payAmount": 14998.00,
    "freightAmount": 10.00,
    "discountAmount": 1000.00,
    "status": 0,
    "createTime": "2024-01-15T10:30:00"
  },
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "请选择要购买的商品",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `收货地址不能为空` - addressId 未传入
  - `请选择要购买的商品` - productIds 列表为空
  - `收货地址不存在` - addressId 对应的地址不存在或不属于当前用户
  - `商品不存在` - productIds 中的某个商品不存在
  - `商品库存不足` - 某个商品库存不足
  - `优惠券不存在或已过期` - couponId 或 userCouponId 无效

---

### 2. 获取订单列表
- **路径与方法**：`GET /order/list`
- **功能描述**：获取当前用户的订单列表，支持按状态筛选
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| status | Query | Integer | ✗ | 订单状态 0-5 | 1 |
| pageNum | Query | Integer | ✗ | 页码，默认1 | 1 |
| pageSize | Query | Integer | ✗ | 每页数量，默认10 | 10 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "list": [
      {
        "id": 1,
        "orderNo": "2024011500001",
        "totalAmount": 15998.00,
        "payAmount": 14998.00,
        "status": 1,
        "createTime": "2024-01-15T10:30:00",
        "orderItems": [
          {
            "productId": 100,
            "productName": "苹果 iPhone 15",
            "quantity": 2,
            "totalPrice": 11998.00
          }
        ]
      }
    ],
    "total": 12,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 2,
    "hasNext": false,
    "hasPrev": false
  },
  "timestamp": 1704067200000
}
```

---

### 3. 获取订单详情
- **路径与方法**：`GET /order/{orderNo}`
- **功能描述**：获取指定订单的详细信息（仅限订单所有者）
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| orderNo | Path | String | ✓ | 订单号 | "2024011500001" |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "orderNo": "2024011500001",
    "userId": 123,
    "totalAmount": 15998.00,
    "payAmount": 14998.00,
    "freightAmount": 10.00,
    "discountAmount": 1000.00,
    "status": 1,
    "payType": 1,
    "payTime": "2024-01-15T10:35:00",
    "shipTime": null,
    "receiveTime": null,
    "deliveryCompany": null,
    "deliverySn": null,
    "receiverName": "张三",
    "receiverPhone": "13800138000",
    "receiverAddress": "北京市朝阳区某街道1号",
    "remark": "请在下午3点后送达",
    "createTime": "2024-01-15T10:30:00",
    "updateTime": "2024-01-15T10:35:00",
    "orderItems": [
      {
        "productId": 100,
        "productName": "苹果 iPhone 15",
        "productImage": "https://example.com/iphone15.jpg",
        "purchasePrice": 5999.00,
        "quantity": 2,
        "totalPrice": 11998.00
      }
    ]
  },
  "timestamp": 1704067200000
}
```

**失败示例（403 Forbidden）：**
```json
{
  "code": 403,
  "message": "无权限访问此订单",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `订单不存在` - orderNo 对应的订单不存在
  - `无权限访问此订单` - 当前用户不是该订单的所有者

---

### 4. 取消订单
- **路径与方法**：`POST /order/cancel/{orderNo}`
- **功能描述**：取消未支付的订单
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| orderNo | Path | String | ✓ | 订单号 | "2024011500001" |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "订单已取消",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "只能取消待支付的订单",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `订单不存在` - orderNo 对应的订单不存在
  - `只能取消待支付的订单` - 订单状态不是待支付（状态不为 0）

---

### 5. 支付订单
- **路径与方法**：`POST /order/pay/{orderNo}`
- **功能描述**：对订单进行支付
- **权限要求**：无需登录（代码未强制要求，但应需要）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| orderNo | Path | String | ✓ | 订单号 | "2024011500001" |
| payType | Query | Integer | ✓ | 支付方式：1-支付宝 2-微信 | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "支付成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "只能对待支付的订单进行支付",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `订单不存在` - orderNo 对应的订单不存在
  - `只能对待支付的订单进行支付` - 订单状态不是待支付（状态不为 0）
  - `支付方式不合法` - payType 不是 1 或 2

---

### 6. 确认收货
- **路径与方法**：`POST /order/confirm/{orderNo}`
- **功能描述**：确认收货，订单变为已完成
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| orderNo | Path | String | ✓ | 订单号 | "2024011500001" |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "已确认收货",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "只能对已发货的订单确认收货",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `订单不存在` - orderNo 对应的订单不存在
  - `只能对已发货的订单确认收货` - 订单状态不是已发货（状态不为 2）

---

### 7. 删除订单
- **路径与方法**：`DELETE /order/{orderNo}`
- **功能描述**：从订单列表删除已取消或已完成的订单
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| orderNo | Path | String | ✓ | 订单号 | "2024011500001" |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "订单已删除",
  "data": null,
  "timestamp": 1704067200000
}
```

---

### 8. 管理员查询订单 [推断]
- **路径与方法**：`GET /order/admin/list`
- **功能描述**：[推断] 支持管理员按订单号、状态查询订单（权限检查推断）
- **权限要求**：无需登录（代码未强制要求）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| orderNo | Query | String | ✗ | 订单号 | "2024011500001" |
| status | Query | Integer | ✗ | 订单状态 | 1 |
| pageNum | Query | Integer | ✗ | 页码，默认1 | 1 |
| pageSize | Query | Integer | ✗ | 每页数量，默认10 | 10 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "list": [
      {
        "id": 1,
        "orderNo": "2024011500001",
        "userId": 123,
        "totalAmount": 15998.00,
        "payAmount": 14998.00,
        "status": 1,
        "createTime": "2024-01-15T10:30:00"
      }
    ],
    "total": 156,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 16,
    "hasNext": true,
    "hasPrev": false
  },
  "timestamp": 1704067200000
}
```

---

### 9. 订单发货 [推断]
- **路径与方法**：`POST /order/admin/ship/{orderNo}`
- **功能描述**：[推断] 管理员对订单进行发货操作
- **权限要求**：无需登录（代码未强制要求）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| orderNo | Path | String | ✓ | 订单号 | "2024011500001" |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "发货成功",
  "data": null,
  "timestamp": 1704067200000
}
```

---

## 附注
- 本文档由 AI 基于当前代码自动生成，上线前需人工核对
- [推断] 标记的接口需人工验证权限控制
- 订单号采用时间戳 + 序列号的格式生成
- 所有时间参数采用 ISO 8601 格式：`yyyy-MM-ddTHH:mm:ss`
- 支付接口实际对接第三方支付平台（支付宝/微信），此文档仅记录接口定义
- 订单创建后默认状态为待支付（0），支付成功后变为已支付（1）
