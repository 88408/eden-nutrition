# Eden Nutrition - B端后台管理 API 接口文档

本文档汇集了 `eden-admin` 模块下，B端管理后台专属的核心业务 API 接口规范，主要供 `eden-admin-vue` 等前端系统对接使用。

---

## 目录
1. [商品管理 (Product Management)](#1-商品管理-product-management)
2. [订单管理 (Order Management)](#2-订单管理-order-management)

---

## 1. 商品管理 (Product Management)

商品管理模块的统一路由前缀：`/admin/product`

### 1.1 获取商品分页列表 (全量/含下架)
* **接口描述**: 无视上架状态，获取系统的全量商品列表。支持条件筛选。
* **请求路径**: `/admin/product/list`
* **请求方式**: `GET`
* **Query 参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | `pageNum` | Integer | 否 | 页码，默认 1 |
  | `pageSize` | Integer | 否 | 每页条数，默认 10 |
  | `keyword` | String | 否 | 商品名称或副标题模糊搜索 |
  | `categoryId` | Long | 否 | 商品分类 ID 筛选 |
  | `status` | Integer | 否 | 上下架状态：0-下架，1-上架 |
* **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "id": 1,
        "name": "商品名称",
        "subtitle": "副标题",
        "price": 99.00,
        "stock": 100,
        "status": 1
        // ... 其他商品字段
      }
    ],
    "total": 100,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 1.2 获取商品详情
* **接口描述**: 根据 ID 获取单个商品的完整详细信息（不校验商品上下架状态）。
* **请求路径**: `/admin/product/{id}`
* **请求方式**: `GET`
* **Path 参数**:
  * `id`: 商品主键ID
* **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "商品详情名称",
    "detail": "<p>富文本详情内容</p>"
    // ...
  }
}
```

### 1.3 新增商品
* **接口描述**: 创建一条新的商品记录。
* **请求路径**: `/admin/product`
* **请求方式**: `POST`
* **Request Body** (`application/json`):
```json
{
  "name": "新产品",
  "subtitle": "测试副标题",
  "categoryId": 10,
  "mainImage": "http://img.url",
  "subImages": "img1.url,img2.url",
  "detail": "<p>图文</p>",
  "originalPrice": 120.00,
  "price": 99.00,
  "stock": 500,
  "isHot": 0,
  "isNew": 1,
  "status": 1
}
```
* **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

### 1.4 修改商品信息
* **接口描述**: 全量更新某一商品的各类信息（需携带原有ID）。
* **请求路径**: `/admin/product`
* **请求方式**: `PUT`
* **Request Body** (`application/json`): 结构同新增，但 `id` 为必填项。
* **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

### 1.5 快捷修改商品上下架状态
* **接口描述**: 单独切换商品的可见状态（例如在列表中点击滑块操作）。
* **请求路径**: `/admin/product/status/{id}/{status}`
* **请求方式**: `PUT`
* **Path 参数**:
  * `id`: 商品主键ID
  * `status`: 目标状态 (0：下架，1：上架)
* **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

### 1.6 删除单个商品
* **接口描述**: 系统进行商品判定和库表数据删除。
* **请求路径**: `/admin/product/{id}`
* **请求方式**: `DELETE`
* **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

## 2. 订单管理 (Order Management)

订单管理模块的统一路由前缀：`/admin/order`

### 2.1 获取订单分页列表
* **接口描述**: 获取系统的全量用户交易订单列表，支持按单号、状态检索。
* **请求路径**: `/admin/order/list`
* **请求方式**: `GET`
* **Query 参数**:
  | 参数名 | 类型 | 必填 | 描述 |
  | :--- | :--- | :--- | :--- |
  | `pageNum` | Integer | 否 | 页码，默认 1 |
  | `pageSize` | Integer | 否 | 每页条数，默认 10 |
  | `orderSn` | String | 否 | 订单号精确/模糊检索 |
  | `status` | Integer | 否 | 订单状态 (0待付款，1待发货，2已发货等) |
  | `userId` | Long | 否 | 用户买家 ID |
* **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "list": [
      {
        "id": 1001,
        "orderSn": "ORD202603310001",
        "userId": 5,
        "payAmount": 198.00,
        "status": 1
      }
    ],
    "total": 50,
    "pageNum": 1,
    "pageSize": 10
  }
}
```

### 2.2 获取订单详情
* **接口描述**: 查看某个订单的具体信息，包含其关联的所有交易明细 (Order Items)。
* **请求路径**: `/admin/order/{id}`
* **请求方式**: `GET`
* **Path 参数**:
  * `id`: 订单主键 ID
* **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1001,
    "orderSn": "ORD202603310001",
    "deliveryCompany": null,
    "deliverySn": null,
    "orderItems": [
      {
        "productId": 1,
        "productName": "商品名称",
        "quantity": 2,
        "productPrice": 99.00
      }
    ]
  }
}
```

### 2.3 订单发货
* **接口描述**: 针对状态为“待发货”(1) 的订单进行发货操作，在此录入物流承运商与物流单号。
* **请求路径**: `/admin/order/deliver`
* **请求方式**: `POST`
* **Request Body** (`application/json`):
```json
{
  "orderId": 1001,
  "deliveryCompany": "顺丰速运",
  "deliverySn": "SF1234567890"
}
```
* **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

---

## 3. 秒杀活动管理 (Seckill Management)

秒杀活动管理模块统一路由前缀: `/admin/seckill`

### 3.1 获取管理端秒杀活动分页列表
* **接口描述**: 获取B端秒杀活动分页记录，包括活动对应的商品基础信息（商品连表数据）。支持商品ID及活动状态的条件筛选。
* **请求路径**: `/admin/seckill/page`
* **请求方式**: `GET`
* **Query Parameters**:
  * `page` (Integer) - 分页数，默认 1
  * `pageSize` (Integer) - 每页单条数，默认 10
  * `productId` (Long) - [可选] 商品ID筛选
  * `status` (Integer) - [可选] 秒杀状态筛选（0:未开始, 1:进行中, 2:已结束）
* **响应示例**:
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "total": 100,
    "pages": 10,
    "list": [
      {
        "id": 1,
        "productId": 10,
        "productName": "燕窝正品",
        "productMainImage": "http://img.com/a.png",
        "originalPrice": 1299.00,
        "seckillPrice": 699.00,
        "stock": 50,
        "limitPerUser": 2,
        "status": 1,
        "startTime": "2026-05-01 10:00:00",
        "endTime": "2026-05-01 12:00:00",
        "createTime": "2026-04-01 10:00:00"
      }
    ]
  }
}
```

### 3.2 获取管理端秒杀活动详情
* **接口描述**: 根据主键单条精确获取某一秒杀活动的详情配置。
* **请求路径**: `/admin/seckill/{id}`
* **请求方式**: `GET`
* **Path Variables**:
  * `id` (Long) - 秒杀活动主键
* **响应格式**: 返回与 3.1 里的 list 内的单体对象数据结构保持一致。

### 3.3 新增秒杀活动
* **接口描述**: 提交并新增一场秒杀配置限制。内部进行防重校验：同类型商品，时间不能出现重合交集。
* **请求路径**: `/admin/seckill`
* **请求方式**: `POST`
* **Request Body** (`application/json`):
```json
{
  "productId": 10,
  "seckillPrice": 699.00,
  "stock": 50,
  "limitPerUser": 2,
  "startTime": "2026-05-01 10:00:00",
  "endTime": "2026-05-01 12:00:00"
}
```

### 3.4 修改秒杀活动
* **接口描述**: 编辑场次配置和库存上限。内置冲突强校验排查机制。修改后的库存会被异步同步至 Redis 保障数据强一致性。
* **请求路径**: `/admin/seckill`
* **请求方式**: `PUT`
* **Request Body** (`application/json`): 必须包含 id，其余同 3.3 新增接口对象属性一致。

### 3.5 伪删除秒杀活动
* **接口描述**: 逻辑废弃或归档该场秒杀记录。
* **请求路径**: `/admin/seckill/{id}`
* **请求方式**: `DELETE`

### 3.6 强制结束秒杀活动
* **接口描述**: 用于突发运营事故情况直接强制暂停该活动并将截止时间设置为当前时间戳，且后台自动将其 Redis 的秒杀抢购令牌及排队缓冲全部进行清零拦截处理。
* **请求路径**: `/admin/seckill/finish/{id}`
* **请求方式**: `PUT`
