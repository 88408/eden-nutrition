# RESTful API 接口文档 - 总览

## 📚 项目概述

本文档为 **Eden 营养电商平台** 的完整 RESTful API 接口说明，包含所有后台管理和前台业务模块。

**项目类型**：Java Spring Boot 微服务架构
**API 版本**：v1.0
**文档生成时间**：2024-01-15
**更新日期**：2026-04-27

---

## 📋 文档导航

### 模块列表（9 个）

| # | 模块名 | 文件 | 描述 |
|---|--------|------|------|
| 1 | **用户管理** | [01-USER-MANAGEMENT.md](01-USER-MANAGEMENT.md) | 用户登录、注册、信息管理、前台用户和后台管理员接口 |
| 2 | **秒杀管理** | [02-SECKILL-MANAGEMENT.md](02-SECKILL-MANAGEMENT.md) | 秒杀活动管理、秒杀参与、库存控制 |
| 3 | **订单管理** | [03-ORDER-MANAGEMENT.md](03-ORDER-MANAGEMENT.md) | 订单创建、查询、支付、发货、收货 |
| 4 | **商品管理** | [04-PRODUCT-MANAGEMENT.md](04-PRODUCT-MANAGEMENT.md) | 商品 CRUD、搜索、分类查询、热门/新品 |
| 5 | **分类管理** | [05-CATEGORY-MANAGEMENT.md](05-CATEGORY-MANAGEMENT.md) | 分类树、增删改查、层级管理 |
| 6 | **购物车** | [06-SHOPPING-CART.md](06-SHOPPING-CART.md) | 添加/删除商品、更新数量、选中管理 |
| 7 | **优惠券** | [07-COUPON.md](07-COUPON.md) | 优惠券领取、查询、使用管理 |
| 8 | **商品评价** | [08-PRODUCT-REVIEW.md](08-PRODUCT-REVIEW.md) | 评价查询、发表、删除、统计 |
| 9 | **收货地址** | [09-ADDRESS-MANAGEMENT.md](09-ADDRESS-MANAGEMENT.md) | 地址 CRUD、默认地址管理 |

---

## 🔑 全局规范

### 1️⃣ 服务基础 URL

| 环境 | 地址 |
|------|------|
| 开发环境 | `http://localhost:8080/api` |
| 测试环境 | `http://test-api.example.com/api` |
| 生产环境 | `https://api.example.com/api` |

### 2️⃣ 统一请求头

**所有请求必须包含：**
```http
Content-Type: application/json
```

**需要身份验证的请求必须包含：**
```http
Authorization: Bearer {jwt_token}
```

### 3️⃣ 统一响应格式

所有接口统一返回以下 JSON 格式：

```json
{
  "code": 200,
  "message": "成功",
  "data": {},
  "timestamp": 1704067200000
}
```

**字段说明：**
- `code`（Integer）：状态码，200 为成功
- `message`（String）：状态描述信息
- `data`（Any）：业务数据，成功时返回具体数据，失败时为 null
- `timestamp`（Long）：服务器响应时间戳（毫秒级）

### 4️⃣ 全局状态码

| 状态码 | 含义 | 处理建议 |
|--------|------|---------|
| 200 | 成功 | 正常处理响应数据 |
| 400 | 请求参数错误 | 检查请求参数是否正确 |
| 401 | 未授权 / Token 无效 | 需要登录或重新登录 |
| 403 | 禁止访问 / 权限不足 | 用户权限不足，无法访问 |
| 404 | 资源不存在 | 请求的资源不存在 |
| 500 | 服务器错误 | 服务器异常，联系管理员 |

### 5️⃣ 分页规范

**分页请求参数：**
```
pageNum: 页码（从 1 开始），默认 1
pageSize: 每页数量，默认 10
```

**分页响应格式：**
```json
{
  "list": [],              // 数据列表
  "total": 100,            // 总记录数
  "pageNum": 1,            // 当前页码
  "pageSize": 10,          // 每页数量
  "pages": 10,             // 总页数
  "hasNext": true,         // 是否有下一页
  "hasPrev": false         // 是否有上一页
}
```

### 6️⃣ 时间戳规范

- **格式**：毫秒级 Unix 时间戳（13 位数字）
- **示例**：`1704067200000`
- **转换**：JavaScript 可直接用 `new Date(timestamp)` 转换
- **日期时间字符串**：采用 ISO 8601 格式 `yyyy-MM-ddTHH:mm:ss`

### 7️⃣ 路径参数规范

- **用户标识**：`@CurrentUser Long userId` 从 Token 中自动提取
- **ID 参数**：均为 Long 类型
- **字符串参数**：需要 URL 编码

### 8️⃣ 查询参数规范

| 参数类型 | 位置 | 说明 |
|---------|------|------|
| Query | URL 查询字符串 | 过滤、排序、分页参数 |
| Path | URL 路径 | 资源唯一标识（ID） |
| Body | 请求体 | 创建、修改操作的数据 |
| Header | 请求头 | 认证、内容类型等 |

### 9️⃣ 错误响应示例

**参数验证错误：**
```json
{
  "code": 400,
  "message": "用户名不能为空",
  "data": null,
  "timestamp": 1704067200000
}
```

**未授权错误：**
```json
{
  "code": 401,
  "message": "Token 已过期，请重新登录",
  "data": null,
  "timestamp": 1704067200000
}
```

**资源不存在：**
```json
{
  "code": 404,
  "message": "商品不存在",
  "data": null,
  "timestamp": 1704067200000
}
```

---

## 🛡️ 安全与认证

### JWT Token 规范

**获取 Token：**
- 前台用户：`POST /user/login`
- 后台管理员：`POST /admin/user/login`

**Token 使用：**
```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Token 有效期：**
- 标准设置：24 小时
- 可在登录响应的 `expiresIn` 字段查看
- 过期后需要重新登录

**Token 刷新：**
- 当前版本暂不支持 Token 刷新
- Token 过期后需重新登录获取新 Token

### 权限控制

**前台用户操作：**
- 需要 `@RequireLogin` 注解的接口必须提供有效 Token
- 操作仅限于自己的数据（订单、地址、购物车等）

**后台管理员操作：**
- 需要 `@RequireAdminLogin` 注解的接口必须提供管理员 Token
- 管理员拥有所有后台操作权限

---

## 📱 API 端点概览

### 前台用户相关
| 模块 | 方法 | 路径 | 功能 |
|------|------|------|------|
| 用户 | POST | `/user/login` | 用户登录 |
| 用户 | POST | `/user/register` | 用户注册 |
| 用户 | POST | `/user/logout` | 用户登出 |
| 用户 | GET | `/user/info` | 获取用户信息 |
| 商品 | GET | `/product/list` | 商品搜索 |
| 商品 | GET | `/product/{id}` | 商品详情 |
| 分类 | GET | `/category/tree` | 分类树 |
| 秒杀 | GET | `/seckill/list` | 秒杀列表 |
| 秒杀 | POST | `/seckill/do` | 参与秒杀 |
| 订单 | POST | `/order/create` | 创建订单 |
| 订单 | GET | `/order/list` | 订单列表 |
| 购物车 | GET | `/cart` | 获取购物车 |
| 购物车 | POST | `/cart/add` | 添加到购物车 |
| 地址 | GET | `/address/list` | 地址列表 |
| 地址 | POST | `/address` | 添加地址 |
| 评价 | GET | `/review/product/{id}` | 商品评价 |
| 优惠券 | GET | `/coupon/available` | 可领取优惠券 |

### 后台管理相关
| 模块 | 方法 | 路径 | 功能 |
|------|------|------|------|
| 用户 | POST | `/admin/user/login` | 管理员登录 |
| 商品 | GET | `/admin/product/list` | 商品列表 |
| 商品 | POST | `/admin/product` | 新增商品 |
| 商品 | PUT | `/admin/product` | 修改商品 |
| 分类 | GET | `/admin/category/tree` | 分类树 |
| 分类 | POST | `/admin/category` | 新增分类 |
| 秒杀 | GET | `/admin/seckill/page` | 秒杀列表 |
| 秒杀 | POST | `/admin/seckill` | 新增秒杀 |
| 秒杀 | PUT | `/admin/seckill` | 修改秒杀 |
| 订单 | GET | `/admin/order/list` | 订单列表 |
| 订单 | POST | `/admin/order/deliver` | 订单发货 |

---

## 🧪 调用示例

### cURL 示例

**用户登录：**
```bash
curl -X POST http://localhost:8080/api/user/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "test_user",
    "password": "password123"
  }'
```

**获取商品列表（需 Token）：**
```bash
curl -X GET "http://localhost:8080/api/product/list?pageNum=1&pageSize=10" \
  -H "Authorization: Bearer {token}"
```

### JavaScript/Fetch 示例

**用户登录：**
```javascript
fetch('http://localhost:8080/api/user/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'test_user',
    password: 'password123'
  })
})
.then(res => res.json())
.then(data => {
  const token = data.data.token;
  localStorage.setItem('token', token);
});
```

**获取购物车（需 Token）：**
```javascript
const token = localStorage.getItem('token');
fetch('http://localhost:8080/api/cart', {
  method: 'GET',
  headers: { 'Authorization': `Bearer ${token}` }
})
.then(res => res.json())
.then(data => console.log(data.data));
```

### Postman 示例

1. **创建新请求**
   - 选择请求方法（GET/POST/PUT/DELETE）
   - 输入完整 URL

2. **设置请求头**
   - Key: `Content-Type`, Value: `application/json`
   - Key: `Authorization`, Value: `Bearer {token}`

3. **设置请求体（Body）**
   - 选择 `raw` 类型
   - 输入 JSON 数据

4. **点击 Send 发送**

---

## 🔄 常见业务流程

### 用户购物完整流程

```
1. 用户注册 → POST /user/register
2. 用户登录 → POST /user/login（获取 token）
3. 浏览商品 → GET /product/list
4. 添加到购物车 → POST /cart/add
5. 查看购物车 → GET /cart
6. 创建订单 → POST /order/create
7. 支付订单 → POST /order/pay/{orderNo}
8. 查看订单 → GET /order/{orderNo}
9. 确认收货 → POST /order/confirm/{orderNo}
10. 发表评价 → POST /review
```

### 秒杀参与流程

```
1. 查看秒杀列表 → GET /seckill/list
2. 查看秒杀详情 → GET /seckill/{id}
3. 检查是否已秒杀 → GET /seckill/check/{id}
4. 执行秒杀 → POST /seckill/do
5. 查看订单 → GET /order/{orderNo}
```

### 后台商品管理流程

```
1. 管理员登录 → POST /admin/user/login
2. 查看商品列表 → GET /admin/product/list
3. 新增商品 → POST /admin/product
4. 修改商品 → PUT /admin/product
5. 修改上下架状态 → PUT /admin/product/status/{id}/{status}
6. 删除商品 → DELETE /admin/product/{id}
```

---

## 📞 常见问题

### Q1: 如何处理 Token 过期？
**A:** 当收到 401 错误时，需要重新登录获取新 Token，然后重新发送请求。

### Q2: 购物车数据会丢失吗？
**A:** 购物车数据存储在 Redis 中，用户登出后数据保留，登录时可恢复。

### Q3: 优惠券可以与秒杀同时使用吗？
**A:** 根据业务规则，秒杀订单通常不支持使用优惠券，请在创建订单前确认。

### Q4: 能否批量删除商品？
**A:** 当前 API 不支持批量删除，需要逐个删除。

### Q5: 如何处理 API 超时？
**A:** 建议设置合理的超时时间（如 10-15 秒），超时后可重试或提示用户。

---

## 📝 更新日志

| 版本 | 日期 | 更新内容 |
|------|------|---------|
| v1.0 | 2026-04-27 | 初版文档，包含 9 个模块共 68+ 个 API 接口 |

---

## ⚠️ 声明

- ✅ 本文档由 AI 基于当前代码自动生成
- ⚠️ **上线前需人工核对**，确保接口、参数、错误码与实际代码一致
- 🔍 [推断] 标记的接口表示根据代码逻辑推断的内容，建议额外验证
- 📅 文档与代码应保持同步更新，有改动时需同时更新文档
- 🤝 如发现文档错误，请及时反馈修正

---

## 📖 如何使用本文档

1. **快速查找**：使用导航表找到需要的模块文档
2. **参考接口**：在模块文档中查看接口详情、请求参数、响应示例
3. **复制示例**：使用提供的 JSON 示例进行测试和集成
4. **错误处理**：参考错误码表进行异常处理编程
5. **保持更新**：代码有改动时同步更新对应文档

**需要帮助？** 联系技术支持或查看具体模块的详细文档。

---

**最后更新**: 2026-04-27  
**文档版本**: v1.0  
**维护团队**: AI 文档生成系统
