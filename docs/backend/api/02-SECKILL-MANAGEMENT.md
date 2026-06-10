# 秒杀管理接口文档

## 概述
秒杀管理分为两个部分：
- **后台管理接口**：管理员创建、编辑、删除秒杀活动和查看秒杀数据
- **前台用户接口**：用户查看秒杀活动和参与秒杀

---

## 公共说明

### 全局请求头
```
Authorization: Bearer {token}    // JWT token（涉及用户信息的接口需要）
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

### 秒杀状态码
| 状态码 | 含义 |
|--------|------|
| 0 | 未开始 |
| 1 | 进行中 |
| 2 | 已结束 |

---

## 后台秒杀接口（/admin/seckill）

### 1. 获取秒杀分页列表
- **路径与方法**：`GET /admin/seckill/page`
- **功能描述**：分页查询所有秒杀活动，包括未开始、进行中、已结束的活动
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| pageNum | Query | Integer | ✗ | 页码，默认1 | 1 |
| pageSize | Query | Integer | ✗ | 每页数量，默认10 | 10 |
| productId | Query | Long | ✗ | 商品ID，用于筛选 | 123 |
| status | Query | Integer | ✗ | 秒杀状态：0-未开始 1-进行中 2-已结束 | 1 |

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
        "productId": 100,
        "productName": "苹果 iPhone 15",
        "productMainImage": "https://example.com/iphone15.jpg",
        "originalPrice": 7999.00,
        "seckillPrice": 5999.00,
        "stockCount": 100,
        "stock": 45,
        "limitPerUser": 1,
        "startTime": "2024-01-15T10:00:00",
        "endTime": "2024-01-15T12:00:00",
        "status": 1,
        "createTime": "2024-01-01T00:00:00",
        "updateTime": "2024-01-15T10:30:00"
      }
    ],
    "total": 50,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 5,
    "hasNext": true,
    "hasPrev": false
  },
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `分页参数错误` - pageNum 或 pageSize 不合法

---

### 2. 获取秒杀详情
- **路径与方法**：`GET /admin/seckill/{id}`
- **功能描述**：获取指定秒杀活动的完整详情
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Path | Long | ✓ | 秒杀活动ID | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "productId": 100,
    "productName": "苹果 iPhone 15",
    "productMainImage": "https://example.com/iphone15.jpg",
    "originalPrice": 7999.00,
    "seckillPrice": 5999.00,
    "stockCount": 100,
    "stock": 45,
    "limitPerUser": 1,
    "startTime": "2024-01-15T10:00:00",
    "endTime": "2024-01-15T12:00:00",
    "status": 1,
    "createTime": "2024-01-01T00:00:00",
    "updateTime": "2024-01-15T10:30:00"
  },
  "timestamp": 1704067200000
}
```

**失败示例（404 Not Found）：**
```json
{
  "code": 404,
  "message": "秒杀活动不存在",
  "data": null,
  "timestamp": 1704067200000
}
```

---

### 3. 新增秒杀活动
- **路径与方法**：`POST /admin/seckill`
- **功能描述**：创建新的秒杀活动
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| productId | Body | Long | ✓ | 商品ID | 100 |
| seckillPrice | Body | BigDecimal | ✓ | 秒杀价格 | 5999.00 |
| stock | Body | Integer | ✓ | 秒杀库存数量，最小1 | 100 |
| limitPerUser | Body | Integer | ✓ | 每人限购数量，最小1 | 1 |
| startTime | Body | LocalDateTime | ✓ | 秒杀开始时间 | "2024-01-15T10:00:00" |
| endTime | Body | LocalDateTime | ✓ | 秒杀结束时间 | "2024-01-15T12:00:00" |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "新增成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "秒杀库存不能为空",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `秒杀库存不能为空` - stock 未传入或为 null
  - `库存必须大于 0` - stock < 1
  - `每人限购数量不能为空` - limitPerUser 未传入或为 null
  - `每人限购数量必须大于 0` - limitPerUser < 1
  - `商品不存在` - productId 对应的商品不存在
  - `秒杀时间范围不合法` - startTime >= endTime

---

### 4. 修改秒杀活动
- **路径与方法**：`PUT /admin/seckill`
- **功能描述**：编辑秒杀活动信息（仅限未开始或进行中的活动）
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Body | Long | ✓ | 秒杀活动ID（必须提供） | 1 |
| productId | Body | Long | ✓ | 商品ID | 100 |
| seckillPrice | Body | BigDecimal | ✓ | 秒杀价格 | 5999.00 |
| stock | Body | Integer | ✓ | 秒杀库存数量 | 100 |
| limitPerUser | Body | Integer | ✓ | 每人限购数量 | 1 |
| startTime | Body | LocalDateTime | ✓ | 秒杀开始时间 | "2024-01-15T10:00:00" |
| endTime | Body | LocalDateTime | ✓ | 秒杀结束时间 | "2024-01-15T12:00:00" |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "修改成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "已结束的秒杀活动不能修改",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `秒杀活动不存在` - id 对应的秒杀活动不存在
  - `已结束的秒杀活动不能修改` - 状态已为 2（已结束）
  - 同新增接口的验证错误码

---

### 5. 删除秒杀活动
- **路径与方法**：`DELETE /admin/seckill/{id}`
- **功能描述**：删除指定的秒杀活动（仅限已结束的活动）
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Path | Long | ✓ | 秒杀活动ID | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "只能删除已结束的秒杀活动",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `秒杀活动不存在` - id 对应的秒杀活动不存在
  - `只能删除已结束的秒杀活动` - 活动状态不是已结束（状态不为 2）

---

### 6. 强制结束秒杀活动
- **路径与方法**：`PUT /admin/seckill/finish/{id}`
- **功能描述**：管理员强制结束进行中的秒杀活动
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Path | Long | ✓ | 秒杀活动ID | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "秒杀活动已强制结束",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "只能结束进行中的秒杀活动",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `秒杀活动不存在` - id 对应的秒杀活动不存在
  - `只能结束进行中的秒杀活动` - 活动状态不是进行中（状态不为 1）

---

## 前台秒杀接口（/seckill）

### 1. 获取秒杀场次列表
- **路径与方法**：`GET /seckill/sessions`
- **功能描述**：获取所有秒杀场次（按时间分组）
- **权限要求**：无需登录
- **请求参数**：无

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "sessionId": 1,
      "sessionName": "早场秒杀 10:00",
      "startTime": "2024-01-15T10:00:00",
      "endTime": "2024-01-15T12:00:00",
      "description": "每日早场秒杀"
    },
    {
      "sessionId": 2,
      "sessionName": "午场秒杀 14:00",
      "startTime": "2024-01-15T14:00:00",
      "endTime": "2024-01-15T16:00:00",
      "description": "每日午场秒杀"
    }
  ],
  "timestamp": 1704067200000
}
```

---

### 2. 获取秒杀活动列表
- **路径与方法**：`GET /seckill/list`
- **功能描述**：获取所有进行中的秒杀活动列表
- **权限要求**：无需登录
- **请求参数**：无

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 1,
      "productId": 100,
      "seckillPrice": 5999.00,
      "stock": 45,
      "limitPerUser": 1,
      "status": 1,
      "startTime": "2024-01-15T10:00:00",
      "endTime": "2024-01-15T12:00:00",
      "createTime": "2024-01-01T00:00:00",
      "updateTime": "2024-01-15T10:30:00"
    }
  ],
  "timestamp": 1704067200000
}
```

---

### 3. 获取进行中的秒杀活动
- **路径与方法**：`GET /seckill/ongoing`
- **功能描述**：获取状态为"进行中"的秒杀活动列表（同 /seckill/list）
- **权限要求**：无需登录
- **请求参数**：无

- **响应结构**：与上一接口相同

---

### 4. 获取即将开始的秒杀活动
- **路径与方法**：`GET /seckill/upcoming`
- **功能描述**：获取状态为"未开始"且距现在最近的秒杀活动
- **权限要求**：无需登录
- **请求参数**：无

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 2,
      "productId": 101,
      "seckillPrice": 1999.00,
      "stock": 200,
      "limitPerUser": 2,
      "status": 0,
      "startTime": "2024-01-15T14:00:00",
      "endTime": "2024-01-15T16:00:00",
      "createTime": "2024-01-01T00:00:00",
      "updateTime": "2024-01-15T09:00:00"
    }
  ],
  "timestamp": 1704067200000
}
```

---

### 5. 获取秒杀商品详情
- **路径与方法**：`GET /seckill/{seckillId}`
- **功能描述**：获取指定秒杀活动的详细信息，包含关联商品信息
- **权限要求**：无需登录
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| seckillId | Path | Long | ✓ | 秒杀活动ID | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "productId": 100,
    "seckillPrice": 5999.00,
    "stock": 45,
    "limitPerUser": 1,
    "status": 1,
    "startTime": "2024-01-15T10:00:00",
    "endTime": "2024-01-15T12:00:00",
    "createTime": "2024-01-01T00:00:00",
    "updateTime": "2024-01-15T10:30:00"
  },
  "timestamp": 1704067200000
}
```

**失败示例（404 Not Found）：**
```json
{
  "code": 404,
  "message": "秒杀活动不存在",
  "data": null,
  "timestamp": 1704067200000
}
```

---

### 6. 执行秒杀（下单）
- **路径与方法**：`POST /seckill/do`
- **功能描述**：用户参与秒杀，创建秒杀订单
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| seckillId | Body | Long | ✓ | 秒杀活动ID | 1 |
| addressId | Body | Long | ✓ | 收货地址ID | 10 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "秒杀成功",
  "data": "SK202401150001",
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "秒杀库存不足",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `秒杀活动不存在` - seckillId 对应的秒杀活动不存在
  - `秒杀活动未开始` - 秒杀活动状态为未开始（状态为 0）
  - `秒杀活动已结束` - 秒杀活动状态为已结束（状态为 2）
  - `秒杀库存不足` - 当前剩余库存为 0
  - `您已参与过此秒杀活动` - 用户已秒杀过该商品，超过 limitPerUser 限制
  - `收货地址不存在` - addressId 对应的地址不存在或不属于当前用户

---

### 7. 检查是否已秒杀
- **路径与方法**：`GET /seckill/check/{seckillId}`
- **功能描述**：检查当前用户是否已参与过指定的秒杀活动
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| seckillId | Path | Long | ✓ | 秒杀活动ID | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": true,
  "timestamp": 1704067200000
}
```

**说明**：`data: true` 表示已秒杀；`data: false` 表示未秒杀

---

### 8. 创建秒杀活动 [推断]
- **路径与方法**：`POST /seckill/create`
- **功能描述**：[推断] 创建新的秒杀活动（可能是后台功能但放在前台路由）
- **权限要求**：无需登录（代码未强制要求）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Body | Long | ✗ | 秒杀ID | 1 |
| productId | Body | Long | ✓ | 商品ID | 100 |
| seckillPrice | Body | BigDecimal | ✓ | 秒杀价格 | 5999.00 |
| stock | Body | Integer | ✓ | 库存 | 100 |
| limitPerUser | Body | Integer | ✓ | 限购数 | 1 |
| startTime | Body | LocalDateTime | ✓ | 开始时间 | "2024-01-15T10:00:00" |
| endTime | Body | LocalDateTime | ✓ | 结束时间 | "2024-01-15T12:00:00" |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "创建成功",
  "data": null,
  "timestamp": 1704067200000
}
```

---

### 9. 更新秒杀活动 [推断]
- **路径与方法**：`PUT /seckill/update`
- **功能描述**：[推断] 更新秒杀活动信息（可能是后台功能但放在前台路由）
- **权限要求**：无需登录（代码未强制要求）
- **请求参数**：同创建接口

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "更新成功",
  "data": null,
  "timestamp": 1704067200000
}
```

---

### 10. 发布秒杀活动 [推断]
- **路径与方法**：`POST /seckill/publish/{id}`
- **功能描述**：[推断] 发布秒杀活动，初始化库存到 Redis
- **权限要求**：无需登录（代码未强制要求）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Path | Long | ✓ | 秒杀活动ID | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "发布成功",
  "data": null,
  "timestamp": 1704067200000
}
```

---

## 附注
- 本文档由 AI 基于当前代码自动生成，上线前需人工核对
- [推断] 标记的接口表示根据代码逻辑推断，建议人工验证是否需要身份验证
- 秒杀库存同步采用 Redis + MySQL 双层架构，前台秒杀实时消耗 Redis 库存
- 时间戳采用毫秒级 Unix 时间戳
- 所有时间参数采用 ISO 8601 格式：`yyyy-MM-ddTHH:mm:ss`
