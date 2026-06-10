# 商品管理接口文档

## 概述
商品管理分为两个部分：
- **后台管理接口**：管理员进行商品的增删改查、上下架管理
- **前台用户接口**：用户浏览商品、搜索、分类查询、查看热门和新品

---

## 公共说明

### 全局请求头
```
Authorization: Bearer {token}    // 后台接口需要
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

### 商品状态
| 状态码 | 含义 |
|--------|------|
| 0 | 下架 |
| 1 | 上架 |

---

## 后台商品接口（/admin/product）

### 1. 获取商品分页列表
- **路径与方法**：`GET /admin/product/list` 或 `GET /admin/product/page`
- **功能描述**：分页查询所有商品（包括下架商品），支持按商品ID筛选
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| page | Query | Integer | ✗ | 页码，默认1 | 1 |
| id | Query | Long | ✗ | 商品ID，用于筛选 | 100 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "list": [
      {
        "id": 100,
        "name": "苹果 iPhone 15",
        "subtitle": "5G 智能手机",
        "categoryId": 10,
        "mainImage": "https://example.com/iphone15.jpg",
        "subImages": "[\"img1.jpg\",\"img2.jpg\"]",
        "detail": "<p>详细介绍...</p>",
        "originalPrice": 7999.00,
        "price": 5999.00,
        "stock": 100,
        "sales": 250,
        "status": 1,
        "isHot": 1,
        "isNew": 0,
        "createTime": "2024-01-01T00:00:00",
        "updateTime": "2024-01-15T10:30:00"
      }
    ],
    "total": 1250,
    "pageNum": 1,
    "pageSize": 10,
    "pages": 125,
    "hasNext": true,
    "hasPrev": false
  },
  "timestamp": 1704067200000
}
```

---

### 2. 获取商品详情
- **路径与方法**：`GET /admin/product/{id}`
- **功能描述**：获取指定商品的完整详情
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Path | Long | ✓ | 商品ID | 100 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 100,
    "name": "苹果 iPhone 15",
    "subtitle": "5G 智能手机",
    "categoryId": 10,
    "mainImage": "https://example.com/iphone15.jpg",
    "subImages": "[\"img1.jpg\",\"img2.jpg\",\"img3.jpg\"]",
    "detail": "<p>最新 A17 Pro 芯片，拍照升级...</p>",
    "originalPrice": 7999.00,
    "price": 5999.00,
    "stock": 100,
    "sales": 250,
    "status": 1,
    "isHot": 1,
    "isNew": 0,
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
  "message": "商品不存在",
  "data": null,
  "timestamp": 1704067200000
}
```

---

### 3. 新增商品
- **路径与方法**：`POST /admin/product`
- **功能描述**：创建新商品
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| name | Body | String | ✓ | 商品名称 | "苹果 iPhone 15" |
| subtitle | Body | String | ✓ | 商品副标题 | "5G 智能手机" |
| categoryId | Body | Long | ✓ | 分类ID | 10 |
| mainImage | Body | String | ✓ | 主图URL | "https://example.com/iphone15.jpg" |
| subImages | Body | String | ✗ | 副图URL，JSON数组 | "[\"img1.jpg\",\"img2.jpg\"]" |
| detail | Body | String | ✗ | 商品详情（富文本） | "<p>详细介绍...</p>" |
| originalPrice | Body | BigDecimal | ✓ | 划线价 | 7999.00 |
| price | Body | BigDecimal | ✓ | 销售价 | 5999.00 |
| stock | Body | Integer | ✓ | 库存数量 | 100 |
| isHot | Body | Integer | ✗ | 是否热门：0-否 1-是，默认0 | 1 |
| isNew | Body | Integer | ✗ | 是否新品：0-否 1-是，默认0 | 0 |
| status | Body | Integer | ✗ | 状态：0-下架 1-上架，默认1 | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "商品新增成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "商品名称不能为空",
  "data": null,
  "timestamp": 1704067200000
}
```

---

### 4. 修改商品
- **路径与方法**：`PUT /admin/product`
- **功能描述**：编辑商品信息
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Body | Long | ✓ | 商品ID（修改时必须提供） | 100 |
| name | Body | String | ✓ | 商品名称 | "苹果 iPhone 15 Pro" |
| subtitle | Body | String | ✓ | 商品副标题 | "Pro 5G 智能手机" |
| categoryId | Body | Long | ✓ | 分类ID | 10 |
| mainImage | Body | String | ✓ | 主图URL | "https://example.com/iphone15pro.jpg" |
| subImages | Body | String | ✗ | 副图URL，JSON数组 | "[\"img1.jpg\",\"img2.jpg\"]" |
| detail | Body | String | ✗ | 商品详情（富文本） | "<p>详细介绍...</p>" |
| originalPrice | Body | BigDecimal | ✓ | 划线价 | 8999.00 |
| price | Body | BigDecimal | ✓ | 销售价 | 6999.00 |
| stock | Body | Integer | ✓ | 库存数量 | 150 |
| isHot | Body | Integer | ✗ | 是否热门 | 1 |
| isNew | Body | Integer | ✗ | 是否新品 | 1 |
| status | Body | Integer | ✗ | 状态 | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "商品修改成功",
  "data": null,
  "timestamp": 1704067200000
}
```

---

### 5. 修改商品上下架状态
- **路径与方法**：`PUT /admin/product/status/{id}/{status}`
- **功能描述**：快速修改商品的上下架状态
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Path | Long | ✓ | 商品ID | 100 |
| status | Path | Integer | ✓ | 新状态：0-下架 1-上架 | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "状态修改成功",
  "data": null,
  "timestamp": 1704067200000
}
```

---

### 6. 删除商品
- **路径与方法**：`DELETE /admin/product/{id}`
- **功能描述**：删除商品
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Path | Long | ✓ | 商品ID | 100 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "商品删除成功",
  "data": null,
  "timestamp": 1704067200000
}
```

---

## 前台商品接口（/product）

### 1. 获取商品详情
- **路径与方法**：`GET /product/{id}`
- **功能描述**：获取指定商品的前台展示信息
- **权限要求**：无需登录
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Path | Long | ✓ | 商品ID | 100 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 100,
    "name": "苹果 iPhone 15",
    "subtitle": "5G 智能手机",
    "categoryId": 10,
    "imageUrl": "https://example.com/iphone15.jpg",
    "subImages": "[\"img1.jpg\",\"img2.jpg\",\"img3.jpg\"]",
    "detail": "<p>最新 A17 Pro 芯片...</p>",
    "originalPrice": 7999.00,
    "price": 5999.00,
    "stock": 100,
    "sales": 250,
    "status": 1,
    "isHot": 1,
    "isNew": 0,
    "rating": 4.8,
    "reviewCount": 325,
    "createTime": "2024-01-01T00:00:00",
    "updateTime": "2024-01-15T10:30:00"
  },
  "timestamp": 1704067200000
}
```

---

### 2. 商品列表查询
- **路径与方法**：`GET /product/list`
- **功能描述**：分页查询商品，支持按分类、关键词、价格范围、排序等条件筛选
- **权限要求**：无需登录
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| categoryId | Query | Long | ✗ | 分类ID | 10 |
| keyword | Query | String | ✗ | 搜索关键词 | "iPhone" |
| minPrice | Query | BigDecimal | ✗ | 最低价格 | 5000 |
| maxPrice | Query | BigDecimal | ✗ | 最高价格 | 8000 |
| sortField | Query | String | ✗ | 排序字段：price/sales/new | "price" |
| sortOrder | Query | String | ✗ | 排序方式：asc/desc | "asc" |
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
        "id": 100,
        "name": "苹果 iPhone 15",
        "subtitle": "5G 智能手机",
        "categoryId": 10,
        "imageUrl": "https://example.com/iphone15.jpg",
        "originalPrice": 7999.00,
        "price": 5999.00,
        "stock": 100,
        "sales": 250,
        "status": 1,
        "isHot": 1,
        "isNew": 0,
        "rating": 4.8,
        "reviewCount": 325
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

### 3. 获取热门商品
- **路径与方法**：`GET /product/hot`
- **功能描述**：获取热门商品列表
- **权限要求**：无需登录
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| limit | Query | Integer | ✗ | 数量限制，默认8 | 8 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 100,
      "name": "苹果 iPhone 15",
      "price": 5999.00,
      "imageUrl": "https://example.com/iphone15.jpg",
      "sales": 250
    }
  ],
  "timestamp": 1704067200000
}
```

---

### 4. 获取推荐商品
- **路径与方法**：`GET /product/recommend`
- **功能描述**：获取推荐商品列表（目前使用热门商品逻辑）
- **权限要求**：无需登录
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| limit | Query | Integer | ✗ | 数量限制，默认10 | 10 |

- **响应结构**：同获取热门商品

---

### 5. 获取新品列表
- **路径与方法**：`GET /product/new`
- **功能描述**：获取新品商品列表
- **权限要求**：无需登录
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| limit | Query | Integer | ✗ | 数量限制，默认8 | 8 |

- **响应结构**：同获取热门商品

---

### 6. 根据分类获取商品
- **路径与方法**：`GET /product/category/{categoryId}`
- **功能描述**：获取指定分类下的所有商品
- **权限要求**：无需登录
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| categoryId | Path | Long | ✓ | 分类ID | 10 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 100,
      "name": "苹果 iPhone 15",
      "subtitle": "5G 智能手机",
      "categoryId": 10,
      "imageUrl": "https://example.com/iphone15.jpg",
      "price": 5999.00,
      "sales": 250
    }
  ],
  "timestamp": 1704067200000
}
```

---

## 附注
- 本文档由 AI 基于当前代码自动生成，上线前需人工核对
- 前台只显示状态为 1（上架）的商品
- 后台可查看所有商品包括下架的（status=0）
- 商品图片采用相对 CDN 的 URL 存储
- `subImages` 字段采用 JSON 数组格式存储多个副图 URL
- 时间戳采用毫秒级 Unix 时间戳
