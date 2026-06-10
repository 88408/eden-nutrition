# 购物车接口文档

## 概述
购物车模块提供用户的购物车管理功能，包括添加商品、更新数量、删除商品、选中商品、清空购物车等操作。

---

## 公共说明

### 全局请求头
```
Authorization: Bearer {token}    // JWT token（所有接口都需要）
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

---

## 接口列表（/cart）

所有购物车接口均需用户登录（@RequireLogin）。

### 1. 获取购物车
- **路径与方法**：`GET /cart`
- **功能描述**：获取当前用户的购物车信息
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：无

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "items": [
      {
        "productId": 100,
        "productName": "苹果 iPhone 15",
        "productImage": "https://example.com/iphone15.jpg",
        "price": 5999.00,
        "quantity": 2,
        "selected": true,
        "totalPrice": 11998.00
      },
      {
        "productId": 101,
        "productName": "AirPods Pro",
        "productImage": "https://example.com/airpodspro.jpg",
        "price": 1999.00,
        "quantity": 1,
        "selected": false,
        "totalPrice": 1999.00
      }
    ],
    "selectedCount": 1,
    "selectedAmount": 11998.00,
    "totalCount": 2,
    "totalAmount": 13997.00,
    "totalQuantity": 3,
    "allSelected": false
  },
  "timestamp": 1704067200000
}
```

**字段说明：**
- `items`：购物车商品列表
- `selectedCount`：选中的商品个数（已选中的 item 数）
- `selectedAmount`：选中商品的总金额
- `totalCount`：购物车商品总个数（不同的商品数）
- `totalAmount`：购物车商品总金额
- `totalQuantity`：购物车商品总数量（所有商品的 quantity 之和）
- `allSelected`：是否全选

---

### 2. 添加商品到购物车
- **路径与方法**：`POST /cart/add`
- **功能描述**：将商品添加到购物车（如果商品已存在则更新数量）
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| productId | Body | Long | ✓ | 商品ID | 100 |
| quantity | Body | Integer | ✓ | 数量，最小1 | 2 |
| selected | Body | Boolean | ✗ | 是否选中，默认true | true |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "添加成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "数量至少为1",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `商品ID不能为空` - productId 未提供
  - `数量不能为空` - quantity 未提供
  - `数量至少为1` - quantity < 1
  - `商品不存在` - productId 对应的商品不存在
  - `商品库存不足` - 要添加的数量超过库存
  - `该商品已下架` - 商品状态为下架（status=0）

---

### 3. 更新商品数量
- **路径与方法**：`PUT /cart/quantity`
- **功能描述**：更新购物车中指定商品的数量
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| productId | Query | Long | ✓ | 商品ID | 100 |
| quantity | Query | Integer | ✓ | 新数量，最小1 | 3 |

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

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "商品不在购物车中",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `商品不在购物车中` - 商品未添加到购物车
  - `数量至少为1` - quantity < 1
  - `商品库存不足` - 新数量超过库存

---

### 4. 删除购物车商品
- **路径与方法**：`DELETE /cart/{productId}`
- **功能描述**：从购物车中删除指定商品
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| productId | Path | Long | ✓ | 商品ID | 100 |

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
  "message": "商品不在购物车中",
  "data": null,
  "timestamp": 1704067200000
}
```

---

### 5. 清空购物车
- **路径与方法**：`DELETE /cart/clear`
- **功能描述**：清空当前用户的购物车中的所有商品
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：无

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "清空成功",
  "data": null,
  "timestamp": 1704067200000
}
```

---

### 6. 获取购物车商品数量
- **路径与方法**：`GET /cart/count`
- **功能描述**：获取购物车中的商品总数（不同商品的个数）
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：无

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": 2,
  "timestamp": 1704067200000
}
```

**说明**：`data` 为购物车中不同商品的个数

---

### 7. 选中/取消选中商品
- **路径与方法**：`PUT /cart/select`
- **功能描述**：选中或取消选中购物车中的指定商品
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| productId | Query | Long | ✓ | 商品ID | 100 |
| selected | Query | Boolean | ✓ | 是否选中：true-选中 false-取消选中 | true |

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

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "商品不在购物车中",
  "data": null,
  "timestamp": 1704067200000
}
```

---

### 8. 全选/取消全选
- **路径与方法**：`PUT /cart/selectAll`
- **功能描述**：全选或取消全选购物车中的所有商品
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| selected | Query | Boolean | ✓ | 是否全选：true-全选 false-全不选 | true |

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

## 使用建议

### 购物流程示例
1. **查看商品** → 调用商品接口
2. **添加到购物车** → `POST /cart/add`
3. **查看购物车** → `GET /cart`
4. **修改数量** → `PUT /cart/quantity`
5. **选中要购买的商品** → `PUT /cart/select`
6. **创建订单** → `POST /order/create`（使用选中的商品）
7. **清空购物车** → `DELETE /cart/clear`

### 购物车数据交互
- 购物车数据存储在 Redis 中，支持会话级别的缓存
- 用户登出后购物车数据会保留（取决于 Redis 过期策略）
- 建议在创建订单后清空已购商品的购物车记录

---

## 附注
- 本文档由 AI 基于当前代码自动生成，上线前需人工核对
- 所有购物车操作都是基于当前登录用户的
- 购物车中的价格为商品当前销售价格的实时快照
- 时间戳采用毫秒级 Unix 时间戳
- 建议前端定期刷新购物车状态以获取最新的库存信息
