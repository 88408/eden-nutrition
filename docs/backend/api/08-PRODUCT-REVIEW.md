# 商品评价接口文档

## 概述
商品评价模块提供用户查看商品评价、添加评价、删除评价等功能。

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

### 评分等级
| 评分 | 含义 |
|------|------|
| 1 | 非常不满意 |
| 2 | 不满意 |
| 3 | 一般 |
| 4 | 满意 |
| 5 | 非常满意 |

### 评价状态
| 状态码 | 含义 |
|--------|------|
| 0 | 隐藏（被举报或删除） |
| 1 | 显示 |

---

## 接口列表（/review）

### 1. 获取商品评价列表
- **路径与方法**：`GET /review/product/{productId}`
- **功能描述**：分页获取指定商品的评价列表
- **权限要求**：无需登录
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| productId | Path | Long | ✓ | 商品ID | 100 |
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
        "productId": 100,
        "userId": 123,
        "orderId": 1001,
        "rating": 5,
        "content": "非常满意，商品质量好，发货速度快！",
        "images": "[\"img1.jpg\",\"img2.jpg\"]",
        "isAnonymous": 0,
        "status": 1,
        "createTime": "2024-01-15T10:30:00",
        "userNickname": "用户张三",
        "userAvatar": "https://example.com/avatar.jpg"
      },
      {
        "id": 2,
        "productId": 100,
        "userId": 124,
        "orderId": 1002,
        "rating": 4,
        "content": "商品不错，就是物流有点慢",
        "images": "[]",
        "isAnonymous": 1,
        "status": 1,
        "createTime": "2024-01-14T15:20:00",
        "userNickname": "匿名用户",
        "userAvatar": null
      }
    ],
    "total": 325,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 33,
    "hasNext": true,
    "hasPrev": false
  },
  "timestamp": 1704067200000
}
```

**字段说明：**
- `isAnonymous=1` 时，`userNickname` 显示为 "匿名用户"，不显示真实用户信息
- `images` 为评价图片 URL 的 JSON 数组
- `status=1` 时显示评价，`status=0` 时隐藏评价

---

### 2. 获取商品评价统计
- **路径与方法**：`GET /review/product/{productId}/stats`
- **功能描述**：获取指定商品的评价统计数据（平均评分、评价总数等）
- **权限要求**：无需登录
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| productId | Path | Long | ✓ | 商品ID | 100 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "avgRating": 4.6,
    "totalCount": 325,
    "ratingDistribution": {
      "5": 200,
      "4": 80,
      "3": 30,
      "2": 10,
      "1": 5
    }
  },
  "timestamp": 1704067200000
}
```

**字段说明：**
- `avgRating`：平均评分（0-5）
- `totalCount`：评价总数
- `ratingDistribution`：各星级评价的数量分布

---

### 3. 添加评价
- **路径与方法**：`POST /review`
- **功能描述**：用户为商品添加评价
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| productId | Body | Long | ✓ | 商品ID | 100 |
| orderId | Body | Long | ✓ | 订单ID（用于验证用户是否购买过） | 1001 |
| rating | Body | Integer | ✓ | 评分 1-5 | 5 |
| content | Body | String | ✓ | 评价内容 | "非常满意！" |
| images | Body | String | ✗ | 评价图片 URL 列表，JSON 数组 | "[\"img1.jpg\",\"img2.jpg\"]" |
| isAnonymous | Body | Integer | ✗ | 是否匿名：0-否 1-是，默认0 | 0 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "评价发表成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "您未购买此商品，无法评价",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `商品不存在` - productId 对应的商品不存在
  - `订单不存在` - orderId 对应的订单不存在
  - `您未购买此商品，无法评价` - 订单中未包含该商品或订单不属于当前用户
  - `评分必须在1-5之间` - rating 不在有效范围内
  - `评价内容不能为空` - content 为空或未提供
  - `您已评价过此商品` - 用户已对该商品评过价（限制每个用户只能评一次）
  - `订单未完成，无法评价` - 订单状态不是已完成（status != 3）
  - `评价超过有效期` - 评价超过允许的时间窗口（通常订单完成后 30 天内可评价）

---

### 4. 删除评价
- **路径与方法**：`DELETE /review/{reviewId}`
- **功能描述**：用户删除自己的评价
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| reviewId | Path | Long | ✓ | 评价ID | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "评价已删除",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（403 Forbidden）：**
```json
{
  "code": 403,
  "message": "您无权删除此评价",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `评价不存在` - reviewId 对应的评价不存在
  - `您无权删除此评价` - 当前用户不是评价的所有者
  - `评价已被隐藏，无法删除` - 评价状态为隐藏（status=0）

---

## 使用建议

### 评价流程示例
1. **浏览商品** → 查看商品详情和评价
2. **查看评价统计** → `GET /review/product/{productId}/stats`
3. **查看所有评价** → `GET /review/product/{productId}`
4. **购买商品** → 完成订单支付和收货
5. **发表评价** → `POST /review`
6. **删除评价** → `DELETE /review/{reviewId}`（如需）

### 评价内容规范建议
- 鼓励用户上传 2-5 张图片，增加评价的可信度
- 评价内容建议 20-500 字，过短或过长的评价会被过滤
- 禁止评价内容中包含联系方式、小广告等违规内容
- 建议提供具体的使用体验描述，而非简单的好评/差评

---

## 附注
- 本文档由 AI 基于当前代码自动生成，上线前需人工核对
- 评价信息采用审核机制，新评价默认状态为显示（status=1）
- 恶意评价可被管理员手动隐藏（status=0）
- `images` 字段采用 JSON 数组格式存储多个评价图片 URL
- 用户可在订单完成后的一定时间内（通常 30 天）发表或删除评价
- 时间戳采用毫秒级 Unix 时间戳
- 前端应定期从商品详情页缓存评价数据以提高加载速度
