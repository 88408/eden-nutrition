# 优惠券接口文档

## 概述
优惠券模块提供用户查看、领取、使用优惠券的功能。

---

## 公共说明

### 全局请求头
```
Authorization: Bearer {token}    // 受保护的接口需要
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

### 优惠券类型
| 类型码 | 含义 |
|--------|------|
| 1 | 满减券 |
| 2 | 折扣券 |

### 优惠券状态
| 状态码 | 含义 |
|--------|------|
| 0 | 禁用 |
| 1 | 启用 |

### 用户优惠券状态
| 状态码 | 含义 |
|--------|------|
| 0 | 未使用 |
| 1 | 已使用 |
| 2 | 已过期 |

---

## 接口列表（/coupon）

### 1. 获取可领取的优惠券列表
- **路径与方法**：`GET /coupon/available`
- **功能描述**：获取当前可领取的优惠券列表（未过期、剩余数量>0 的优惠券）
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
      "name": "新人首单满100减20",
      "type": 1,
      "discountValue": 20.00,
      "value": 20.00,
      "minAmount": 100.00,
      "maxDiscount": null,
      "totalCount": 1000,
      "remainCount": 850,
      "startTime": "2024-01-01T00:00:00",
      "endTime": "2024-12-31T23:59:59",
      "status": 1
    },
    {
      "id": 2,
      "name": "全场9折优惠券",
      "type": 2,
      "discountValue": 0.90,
      "value": 0.90,
      "minAmount": 50.00,
      "maxDiscount": 100.00,
      "totalCount": 5000,
      "remainCount": 3200,
      "startTime": "2024-01-01T00:00:00",
      "endTime": "2024-12-31T23:59:59",
      "status": 1
    }
  ],
  "timestamp": 1704067200000
}
```

**字段说明：**
- `type=1` 时，`discountValue` 为满减金额；`type=2` 时，`discountValue` 为折扣比例（0-1）
- `minAmount`：最低消费金额
- `maxDiscount`：仅对折扣券有效，为最大优惠金额上限

---

### 2. 领取优惠券
- **路径与方法**：`POST /coupon/receive/{couponId}`
- **功能描述**：用户领取指定的优惠券
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| couponId | Path | Long | ✓ | 优惠券ID | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "优惠券领取成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "优惠券已领完",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `优惠券不存在` - couponId 对应的优惠券不存在
  - `优惠券已过期` - 优惠券已超过领取时间
  - `优惠券已领完` - 剩余数量为 0
  - `您已领取过此优惠券` - 用户已领取过该优惠券（限制每个用户只能领一次）
  - `优惠券已禁用` - 优惠券状态为禁用（status=0）

---

### 3. 获取我的优惠券列表
- **路径与方法**：`GET /coupon/my`
- **功能描述**：获取当前用户的优惠券列表，支持按状态筛选
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| status | Query | Integer | ✗ | 优惠券状态：0-未使用 1-已使用 2-已过期 | 0 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 10,
      "couponId": 1,
      "userId": 123,
      "couponName": "新人首单满100减20",
      "couponType": 1,
      "discountValue": 20.00,
      "minAmount": 100.00,
      "maxDiscount": null,
      "status": 0,
      "receiveTime": "2024-01-15T10:30:00",
      "useTime": null,
      "expireTime": "2024-12-31T23:59:59"
    },
    {
      "id": 11,
      "couponId": 2,
      "userId": 123,
      "couponName": "全场9折优惠券",
      "couponType": 2,
      "discountValue": 0.90,
      "minAmount": 50.00,
      "maxDiscount": 100.00,
      "status": 1,
      "receiveTime": "2024-01-10T15:20:00",
      "useTime": "2024-01-15T11:00:00",
      "expireTime": "2024-12-31T23:59:59"
    }
  ],
  "timestamp": 1704067200000
}
```

---

### 4. 获取可用的优惠券（下单时）
- **路径与方法**：`GET /coupon/usable`
- **功能描述**：获取当前用户可用的优惠券列表（未使用且未过期的优惠券）
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：无

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 10,
      "couponId": 1,
      "userId": 123,
      "couponName": "新人首单满100减20",
      "couponType": 1,
      "discountValue": 20.00,
      "minAmount": 100.00,
      "maxDiscount": null,
      "status": 0,
      "receiveTime": "2024-01-15T10:30:00",
      "useTime": null,
      "expireTime": "2024-12-31T23:59:59"
    }
  ],
  "timestamp": 1704067200000
}
```

**说明**：下单时使用此接口获取用户可用的优惠券，用户可选择在创建订单时应用

---

## 使用建议

### 优惠券流程示例
1. **浏览可领取优惠券** → `GET /coupon/available`
2. **领取优惠券** → `POST /coupon/receive/{couponId}`
3. **查看我的优惠券** → `GET /coupon/my`
4. **下单时选择优惠券** → `GET /coupon/usable` → 创建订单时传入 `couponId`

### 优惠券类型说明

#### 满减券（type=1）
- 示例：满 100 减 20
- 计算：订单金额 >= minAmount 时，优惠 discountValue 元
- 查询字段：`discountValue` 为优惠金额

#### 折扣券（type=2）
- 示例：9 折，最多优惠 100 元
- 计算：订单金额 * discountValue，但不超过 maxDiscount
- 查询字段：`discountValue` 为折扣比例（如 0.9 表示 9 折）
- `maxDiscount` 为最大优惠金额上限

---

## 附注
- 本文档由 AI 基于当前代码自动生成，上线前需人工核对
- 优惠券使用遵循先到先得原则
- 用户领取优惠券后一般不可退回
- 过期的优惠券在 `expireTime` 后自动变为已过期状态
- 时间戳采用毫秒级 Unix 时间戳
- 订单创建时优惠券状态会从"未使用"变为"已使用"，`useTime` 自动记录为订单创建时间
