# 伊甸滋补线上商店 API 接口文档

**版本**: v1.0.0  
**基础路径**: `/api/v1`  
**更新日期**: 2025-12-06

---

## 目录

1. [通用说明](#通用说明)
2. [用户模块](#用户模块)
3. [商品模块](#商品模块)
4. [分类模块](#分类模块)
5. [购物车模块](#购物车模块)
6. [订单模块](#订单模块)
7. [优惠券模块](#优惠券模块)
8. [秒杀模块](#秒杀模块)
9. [地址模块](#地址模块)
10. [评价模块](#评价模块)

---

## 通用说明

### 请求格式

- **Content-Type**: `application/json`
- **字符编码**: UTF-8

### 响应格式

所有接口统一返回格式：

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {}
}
```

### 状态码说明

| 状态码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或Token过期 |
| 403 | 无权限访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 1001 | 用户不存在 |
| 1002 | 密码错误 |
| 1003 | 用户已存在 |
| 1004 | 手机号已注册 |
| 2001 | 商品不存在 |
| 2002 | 库存不足 |
| 3001 | 订单不存在 |
| 3002 | 订单状态异常 |
| 4001 | 优惠券不存在 |
| 4002 | 优惠券已过期 |
| 4003 | 优惠券已领完 |
| 5001 | 秒杀未开始 |
| 5002 | 秒杀已结束 |
| 5003 | 已参与过秒杀 |

### 认证方式

需要登录的接口需在请求头中携带 Token：

```
Authorization: Bearer <token>
```

---

## 用户模块

### 1. 用户注册

**接口地址**: `POST /user/register`

**是否需要登录**: 否

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名（4-20位字母数字） |
| password | String | 是 | 密码（6-20位） |
| phone | String | 否 | 手机号 |
| verifyCode | String | 否 | 验证码 |

**请求示例**:

```json
{
    "username": "zhangsan",
    "password": "123456",
    "phone": "13800138000",
    "verifyCode": "1234"
}
```

**响应示例**:

```json
{
    "code": 200,
    "message": "注册成功",
    "data": null
}
```

---

### 2. 用户登录

**接口地址**: `POST /user/login`

**是否需要登录**: 否

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**请求示例**:

```json
{
    "username": "zhangsan",
    "password": "123456"
}
```

**响应示例**:

```json
{
    "code": 200,
    "message": "登录成功",
    "data": {
        "userId": 10001,
        "username": "zhangsan",
        "nickname": "张三",
        "avatar": "https://cdn.example.com/avatar/default.png",
        "role": "USER",
        "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
}
```

---

### 3. 获取当前用户信息

**接口地址**: `GET /user/info`

**是否需要登录**: 是

**请求参数**: 无

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 10001,
        "username": "zhangsan",
        "nickname": "张三",
        "phone": "138****8000",
        "email": "zhangsan@example.com",
        "avatar": "https://cdn.example.com/avatar/default.png",
        "gender": 1,
        "birthday": "1990-01-01",
        "points": 1000,
        "createTime": "2024-01-01 10:00:00"
    }
}
```

---

### 4. 修改用户信息

**接口地址**: `PUT /user/info`

**是否需要登录**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| nickname | String | 否 | 昵称 |
| avatar | String | 否 | 头像URL |
| gender | Integer | 否 | 性别（0-未知，1-男，2-女） |
| birthday | String | 否 | 生日（yyyy-MM-dd） |
| email | String | 否 | 邮箱 |

**请求示例**:

```json
{
    "nickname": "小明",
    "gender": 1,
    "birthday": "1990-05-20"
}
```

**响应示例**:

```json
{
    "code": 200,
    "message": "修改成功",
    "data": null
}
```

---

### 5. 修改密码

**接口地址**: `PUT /user/password`

**是否需要登录**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| oldPassword | String | 是 | 原密码 |
| newPassword | String | 是 | 新密码 |

**请求示例**:

```json
{
    "oldPassword": "123456",
    "newPassword": "654321"
}
```

**响应示例**:

```json
{
    "code": 200,
    "message": "密码修改成功",
    "data": null
}
```

---

### 6. 用户登出

**接口地址**: `POST /user/logout`

**是否需要登录**: 是

**请求参数**: 无

**响应示例**:

```json
{
    "code": 200,
    "message": "登出成功",
    "data": null
}
```

---

## 商品模块

### 1. 商品列表查询

**接口地址**: `GET /product/list`

**是否需要登录**: 否

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| keyword | String | 否 | 搜索关键词 |
| categoryId | Long | 否 | 分类ID |
| minPrice | BigDecimal | 否 | 最低价格 |
| maxPrice | BigDecimal | 否 | 最高价格 |
| sortBy | String | 否 | 排序字段（price/sales/createTime） |
| sortOrder | String | 否 | 排序方向（asc/desc） |
| pageNum | Integer | 否 | 页码（默认1） |
| pageSize | Integer | 否 | 每页数量（默认10） |

**请求示例**:

```
GET /product/list?keyword=燕窝&categoryId=1&minPrice=100&maxPrice=1000&sortBy=sales&sortOrder=desc&pageNum=1&pageSize=10
```

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "total": 100,
        "pages": 10,
        "pageNum": 1,
        "pageSize": 10,
        "list": [
            {
                "id": 1001,
                "name": "印尼进口燕窝 100g",
                "categoryId": 1,
                "categoryName": "燕窝",
                "price": 599.00,
                "originalPrice": 799.00,
                "mainImage": "https://cdn.example.com/product/yanwo1.jpg",
                "sales": 1234,
                "stock": 500,
                "status": 1
            }
        ]
    }
}
```

---

### 2. 商品详情

**接口地址**: `GET /product/{id}`

**是否需要登录**: 否

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 商品ID |

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 1001,
        "name": "印尼进口燕窝 100g",
        "categoryId": 1,
        "categoryName": "燕窝",
        "price": 599.00,
        "originalPrice": 799.00,
        "mainImage": "https://cdn.example.com/product/yanwo1.jpg",
        "images": [
            "https://cdn.example.com/product/yanwo1.jpg",
            "https://cdn.example.com/product/yanwo2.jpg",
            "https://cdn.example.com/product/yanwo3.jpg"
        ],
        "detail": "<p>商品详情HTML内容</p>",
        "specs": "净含量: 100g; 产地: 印尼; 保质期: 24个月",
        "sales": 1234,
        "stock": 500,
        "status": 1,
        "rating": 4.8,
        "reviewCount": 256,
        "createTime": "2024-01-01 10:00:00"
    }
}
```

---

### 3. 热销商品

**接口地址**: `GET /product/hot`

**是否需要登录**: 否

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| limit | Integer | 否 | 数量限制（默认10） |

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 1001,
            "name": "印尼进口燕窝 100g",
            "price": 599.00,
            "mainImage": "https://cdn.example.com/product/yanwo1.jpg",
            "sales": 1234
        }
    ]
}
```

---

### 4. 新品推荐

**接口地址**: `GET /product/new`

**是否需要登录**: 否

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| limit | Integer | 否 | 数量限制（默认10） |

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 1005,
            "name": "长白山野生人参 50g",
            "price": 899.00,
            "mainImage": "https://cdn.example.com/product/renshen1.jpg",
            "createTime": "2024-12-01 10:00:00"
        }
    ]
}
```

---

## 分类模块

### 1. 获取分类树

**接口地址**: `GET /category/tree`

**是否需要登录**: 否

**请求参数**: 无

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "name": "燕窝",
            "icon": "https://cdn.example.com/icon/yanwo.png",
            "sort": 1,
            "children": [
                {
                    "id": 11,
                    "name": "即食燕窝",
                    "icon": null,
                    "sort": 1,
                    "children": []
                },
                {
                    "id": 12,
                    "name": "干燕窝",
                    "icon": null,
                    "sort": 2,
                    "children": []
                }
            ]
        },
        {
            "id": 2,
            "name": "人参",
            "icon": "https://cdn.example.com/icon/renshen.png",
            "sort": 2,
            "children": []
        }
    ]
}
```

---

### 2. 获取分类列表

**接口地址**: `GET /category/list`

**是否需要登录**: 否

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| parentId | Long | 否 | 父分类ID（不传则获取顶级分类） |

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "name": "燕窝",
            "parentId": 0,
            "level": 1,
            "icon": "https://cdn.example.com/icon/yanwo.png",
            "sort": 1
        },
        {
            "id": 2,
            "name": "人参",
            "parentId": 0,
            "level": 1,
            "icon": "https://cdn.example.com/icon/renshen.png",
            "sort": 2
        }
    ]
}
```

---

## 购物车模块

### 1. 获取购物车

**接口地址**: `GET /cart`

**是否需要登录**: 是

**请求参数**: 无

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "items": [
            {
                "id": 1,
                "productId": 1001,
                "productName": "印尼进口燕窝 100g",
                "productImage": "https://cdn.example.com/product/yanwo1.jpg",
                "price": 599.00,
                "quantity": 2,
                "subtotal": 1198.00,
                "selected": true,
                "stock": 500,
                "stockEnough": true
            }
        ],
        "totalQuantity": 2,
        "totalAmount": 1198.00,
        "allSelected": true
    }
}
```

---

### 2. 添加商品到购物车

**接口地址**: `POST /cart/add`

**是否需要登录**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |
| quantity | Integer | 是 | 数量 |

**请求示例**:

```json
{
    "productId": 1001,
    "quantity": 1
}
```

**响应示例**:

```json
{
    "code": 200,
    "message": "添加成功",
    "data": null
}
```

---

### 3. 修改购物车商品数量

**接口地址**: `PUT /cart/update`

**是否需要登录**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |
| quantity | Integer | 是 | 数量 |

**请求示例**:

```json
{
    "productId": 1001,
    "quantity": 3
}
```

**响应示例**:

```json
{
    "code": 200,
    "message": "修改成功",
    "data": null
}
```

---

### 4. 删除购物车商品

**接口地址**: `DELETE /cart/{productId}`

**是否需要登录**: 是

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |

**响应示例**:

```json
{
    "code": 200,
    "message": "删除成功",
    "data": null
}
```

---

### 5. 清空购物车

**接口地址**: `DELETE /cart/clear`

**是否需要登录**: 是

**请求参数**: 无

**响应示例**:

```json
{
    "code": 200,
    "message": "清空成功",
    "data": null
}
```

---

### 6. 切换商品选中状态

**接口地址**: `PUT /cart/select/{productId}`

**是否需要登录**: 是

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| selected | Boolean | 是 | 是否选中 |

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": null
}
```

---

### 7. 全选/取消全选

**接口地址**: `PUT /cart/selectAll`

**是否需要登录**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| selected | Boolean | 是 | 是否选中 |

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": null
}
```

---

## 订单模块

### 1. 创建订单

**接口地址**: `POST /order/create`

**是否需要登录**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| addressId | Long | 是 | 收货地址ID |
| userCouponId | Long | 否 | 用户优惠券ID |
| remark | String | 否 | 订单备注 |
| productIds | List&lt;Long&gt; | 是 | 购物车中选中的商品ID列表 |

**请求示例**:

```json
{
    "addressId": 1,
    "userCouponId": 100,
    "remark": "请尽快发货",
    "productIds": [1001, 1002]
}
```

**响应示例**:

```json
{
    "code": 200,
    "message": "订单创建成功",
    "data": {
        "orderId": 20241206001,
        "orderNo": "202412060000010001",
        "totalAmount": 1198.00,
        "discountAmount": 50.00,
        "payAmount": 1148.00,
        "createTime": "2024-12-06 10:00:00"
    }
}
```

---

### 2. 订单列表

**接口地址**: `GET /order/list`

**是否需要登录**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | Integer | 否 | 订单状态（0-待支付,1-已支付,2-已发货,3-已收货,4-已完成,5-已取消） |
| pageNum | Integer | 否 | 页码（默认1） |
| pageSize | Integer | 否 | 每页数量（默认10） |

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "total": 50,
        "pages": 5,
        "pageNum": 1,
        "pageSize": 10,
        "list": [
            {
                "id": 20241206001,
                "orderNo": "202412060000010001",
                "status": 0,
                "statusText": "待支付",
                "totalAmount": 1198.00,
                "discountAmount": 50.00,
                "payAmount": 1148.00,
                "shippingFee": 0.00,
                "createTime": "2024-12-06 10:00:00",
                "payDeadline": "2024-12-06 10:30:00",
                "orderItems": [
                    {
                        "productId": 1001,
                        "productName": "印尼进口燕窝 100g",
                        "productImage": "https://cdn.example.com/product/yanwo1.jpg",
                        "price": 599.00,
                        "quantity": 2,
                        "subtotal": 1198.00
                    }
                ]
            }
        ]
    }
}
```

---

### 3. 订单详情

**接口地址**: `GET /order/{id}`

**是否需要登录**: 是

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 订单ID |

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 20241206001,
        "orderNo": "202412060000010001",
        "status": 0,
        "statusText": "待支付",
        "totalAmount": 1198.00,
        "discountAmount": 50.00,
        "payAmount": 1148.00,
        "shippingFee": 0.00,
        "remark": "请尽快发货",
        "createTime": "2024-12-06 10:00:00",
        "payTime": null,
        "shipTime": null,
        "receiveTime": null,
        "payDeadline": "2024-12-06 10:30:00",
        "address": {
            "receiverName": "张三",
            "receiverPhone": "13800138000",
            "province": "广东省",
            "city": "深圳市",
            "district": "南山区",
            "detail": "科技园南区XX大厦"
        },
        "orderItems": [
            {
                "productId": 1001,
                "productName": "印尼进口燕窝 100g",
                "productImage": "https://cdn.example.com/product/yanwo1.jpg",
                "price": 599.00,
                "quantity": 2,
                "subtotal": 1198.00
            }
        ],
        "coupon": {
            "id": 100,
            "name": "满1000减50",
            "value": 50.00
        }
    }
}
```

---

### 4. 取消订单

**接口地址**: `POST /order/{id}/cancel`

**是否需要登录**: 是

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 订单ID |

**响应示例**:

```json
{
    "code": 200,
    "message": "订单已取消",
    "data": null
}
```

---

### 5. 确认收货

**接口地址**: `POST /order/{id}/receive`

**是否需要登录**: 是

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 订单ID |

**响应示例**:

```json
{
    "code": 200,
    "message": "确认收货成功",
    "data": null
}
```

---

### 6. 支付订单（模拟）

**接口地址**: `POST /order/{id}/pay`

**是否需要登录**: 是

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 订单ID |

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| paymentMethod | Integer | 是 | 支付方式（1-支付宝,2-微信,3-银行卡） |

**请求示例**:

```json
{
    "paymentMethod": 1
}
```

**响应示例**:

```json
{
    "code": 200,
    "message": "支付成功",
    "data": {
        "orderNo": "202412060000010001",
        "payAmount": 1148.00,
        "payTime": "2024-12-06 10:05:00"
    }
}
```

---

## 优惠券模块

### 1. 可领取优惠券列表

**接口地址**: `GET /coupon/available`

**是否需要登录**: 否

**请求参数**: 无

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "name": "新人专享券",
            "type": 1,
            "typeText": "满减券",
            "value": 50.00,
            "minAmount": 200.00,
            "startTime": "2024-12-01 00:00:00",
            "endTime": "2024-12-31 23:59:59",
            "totalCount": 1000,
            "remainCount": 500,
            "limitPerUser": 1,
            "received": false
        }
    ]
}
```

---

### 2. 我的优惠券

**接口地址**: `GET /coupon/my`

**是否需要登录**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| status | Integer | 否 | 状态（0-未使用,1-已使用,2-已过期） |

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 100,
            "couponId": 1,
            "name": "新人专享券",
            "type": 1,
            "typeText": "满减券",
            "value": 50.00,
            "minAmount": 200.00,
            "status": 0,
            "statusText": "未使用",
            "receiveTime": "2024-12-01 10:00:00",
            "expireTime": "2024-12-31 23:59:59"
        }
    ]
}
```

---

### 3. 领取优惠券

**接口地址**: `POST /coupon/{id}/receive`

**是否需要登录**: 是

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 优惠券ID |

**响应示例**:

```json
{
    "code": 200,
    "message": "领取成功",
    "data": null
}
```

---

### 4. 订单可用优惠券

**接口地址**: `GET /coupon/usable`

**是否需要登录**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| amount | BigDecimal | 是 | 订单金额 |

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 100,
            "name": "满200减50",
            "value": 50.00,
            "minAmount": 200.00,
            "expireTime": "2024-12-31 23:59:59"
        }
    ]
}
```

---

## 秒杀模块

### 1. 秒杀活动列表

**接口地址**: `GET /seckill/list`

**是否需要登录**: 否

**请求参数**: 无

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "productId": 1001,
            "productName": "印尼进口燕窝 100g",
            "productImage": "https://cdn.example.com/product/yanwo1.jpg",
            "originalPrice": 599.00,
            "seckillPrice": 299.00,
            "stock": 100,
            "remainStock": 50,
            "startTime": "2024-12-06 10:00:00",
            "endTime": "2024-12-06 12:00:00",
            "status": 1,
            "statusText": "进行中"
        }
    ]
}
```

---

### 2. 秒杀商品详情

**接口地址**: `GET /seckill/{id}`

**是否需要登录**: 否

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 秒杀活动ID |

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "id": 1,
        "productId": 1001,
        "productName": "印尼进口燕窝 100g",
        "productImage": "https://cdn.example.com/product/yanwo1.jpg",
        "productDetail": "<p>商品详情</p>",
        "originalPrice": 599.00,
        "seckillPrice": 299.00,
        "stock": 100,
        "remainStock": 50,
        "limitPerUser": 1,
        "startTime": "2024-12-06 10:00:00",
        "endTime": "2024-12-06 12:00:00",
        "status": 1,
        "statusText": "进行中",
        "serverTime": "2024-12-06 10:30:00"
    }
}
```

---

### 3. 执行秒杀

**接口地址**: `POST /seckill/{id}/execute`

**是否需要登录**: 是

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 秒杀活动ID |

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| addressId | Long | 是 | 收货地址ID |

**请求示例**:

```json
{
    "addressId": 1
}
```

**响应示例**:

```json
{
    "code": 200,
    "message": "秒杀成功",
    "data": {
        "orderId": 20241206002,
        "orderNo": "SK2024120600001"
    }
}
```

**错误响应示例**:

```json
{
    "code": 5003,
    "message": "您已参与过该秒杀活动",
    "data": null
}
```

---

### 4. 查询秒杀结果

**接口地址**: `GET /seckill/{id}/result`

**是否需要登录**: 是

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 秒杀活动ID |

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "status": "SUCCESS",
        "orderId": 20241206002,
        "message": "秒杀成功，请尽快支付"
    }
}
```

---

## 地址模块

### 1. 收货地址列表

**接口地址**: `GET /address/list`

**是否需要登录**: 是

**请求参数**: 无

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": [
        {
            "id": 1,
            "receiverName": "张三",
            "receiverPhone": "13800138000",
            "province": "广东省",
            "city": "深圳市",
            "district": "南山区",
            "detail": "科技园南区XX大厦1001室",
            "isDefault": true
        }
    ]
}
```

---

### 2. 添加收货地址

**接口地址**: `POST /address`

**是否需要登录**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| receiverName | String | 是 | 收货人姓名 |
| receiverPhone | String | 是 | 收货人电话 |
| province | String | 是 | 省份 |
| city | String | 是 | 城市 |
| district | String | 是 | 区/县 |
| detail | String | 是 | 详细地址 |
| isDefault | Boolean | 否 | 是否默认地址 |

**请求示例**:

```json
{
    "receiverName": "李四",
    "receiverPhone": "13900139000",
    "province": "北京市",
    "city": "北京市",
    "district": "朝阳区",
    "detail": "望京SOHO T1座",
    "isDefault": false
}
```

**响应示例**:

```json
{
    "code": 200,
    "message": "添加成功",
    "data": {
        "id": 2
    }
}
```

---

### 3. 修改收货地址

**接口地址**: `PUT /address/{id}`

**是否需要登录**: 是

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 地址ID |

**请求参数**: 同添加地址

**响应示例**:

```json
{
    "code": 200,
    "message": "修改成功",
    "data": null
}
```

---

### 4. 删除收货地址

**接口地址**: `DELETE /address/{id}`

**是否需要登录**: 是

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 地址ID |

**响应示例**:

```json
{
    "code": 200,
    "message": "删除成功",
    "data": null
}
```

---

### 5. 设为默认地址

**接口地址**: `PUT /address/{id}/default`

**是否需要登录**: 是

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| id | Long | 是 | 地址ID |

**响应示例**:

```json
{
    "code": 200,
    "message": "设置成功",
    "data": null
}
```

---

## 评价模块

### 1. 商品评价列表

**接口地址**: `GET /review/product/{productId}`

**是否需要登录**: 否

**路径参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| productId | Long | 是 | 商品ID |

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| rating | Integer | 否 | 评分筛选（1-5） |
| hasImage | Boolean | 否 | 是否有图 |
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "total": 256,
        "pages": 26,
        "pageNum": 1,
        "pageSize": 10,
        "statistics": {
            "totalCount": 256,
            "averageRating": 4.8,
            "rating5Count": 200,
            "rating4Count": 40,
            "rating3Count": 10,
            "rating2Count": 4,
            "rating1Count": 2,
            "hasImageCount": 50
        },
        "list": [
            {
                "id": 1,
                "userId": 10001,
                "username": "z***n",
                "avatar": "https://cdn.example.com/avatar/default.png",
                "productId": 1001,
                "orderId": 20241205001,
                "rating": 5,
                "content": "品质很好，包装精美，送货速度快！",
                "images": [
                    "https://cdn.example.com/review/1.jpg",
                    "https://cdn.example.com/review/2.jpg"
                ],
                "createTime": "2024-12-05 15:30:00",
                "reply": "感谢您的好评，欢迎再次光临！",
                "replyTime": "2024-12-05 16:00:00"
            }
        ]
    }
}
```

---

### 2. 发表评价

**接口地址**: `POST /review`

**是否需要登录**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| orderId | Long | 是 | 订单ID |
| productId | Long | 是 | 商品ID |
| rating | Integer | 是 | 评分（1-5） |
| content | String | 是 | 评价内容 |
| images | List&lt;String&gt; | 否 | 评价图片URL列表 |

**请求示例**:

```json
{
    "orderId": 20241205001,
    "productId": 1001,
    "rating": 5,
    "content": "品质很好，包装精美，送货速度快！",
    "images": [
        "https://cdn.example.com/review/1.jpg",
        "https://cdn.example.com/review/2.jpg"
    ]
}
```

**响应示例**:

```json
{
    "code": 200,
    "message": "评价成功",
    "data": null
}
```

---

### 3. 我的评价列表

**接口地址**: `GET /review/my`

**是否需要登录**: 是

**请求参数**:

| 参数名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| pageNum | Integer | 否 | 页码 |
| pageSize | Integer | 否 | 每页数量 |

**响应示例**:

```json
{
    "code": 200,
    "message": "操作成功",
    "data": {
        "total": 10,
        "pages": 1,
        "pageNum": 1,
        "pageSize": 10,
        "list": [
            {
                "id": 1,
                "productId": 1001,
                "productName": "印尼进口燕窝 100g",
                "productImage": "https://cdn.example.com/product/yanwo1.jpg",
                "rating": 5,
                "content": "品质很好，包装精美，送货速度快！",
                "images": [],
                "createTime": "2024-12-05 15:30:00"
            }
        ]
    }
}
```

---

## 附录

### A. 订单状态流转图

```
待支付(0) --支付--> 已支付(1) --发货--> 已发货(2) --收货--> 已收货(3) --评价--> 已完成(4)
    |                                                              
    +--取消/超时--> 已取消(5)
    
已支付(1) --申请退款--> 退款中(6) --退款成功--> 已退款(7)
```

### B. 优惠券类型说明

| 类型值 | 说明 | 示例 |
|--------|------|------|
| 1 | 满减券 | 满200减50 |
| 2 | 折扣券 | 8折优惠 |
| 3 | 无门槛券 | 直减20元 |

### C. 秒杀状态说明

| 状态值 | 说明 |
|--------|------|
| 0 | 未开始 |
| 1 | 进行中 |
| 2 | 已结束 |
| 3 | 已售罄 |

---

## 更新日志

| 版本 | 日期 | 说明 |
|------|------|------|
| v1.0.0 | 2025-12-06 | 初始版本，包含全部基础接口 |

---

**文档编写**: Eden Nutrition 开发团队  
**联系方式**: dev@eden-nutrition.com
