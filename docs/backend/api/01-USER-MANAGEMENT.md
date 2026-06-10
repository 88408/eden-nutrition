# 用户管理接口文档

## 概述
用户管理模块包含用户登录、注册、信息查询、密码修改等功能。支持前台普通用户和后台管理员两套接口体系。

---

## 公共说明

### 全局请求头
```
Authorization: Bearer {token}    // JWT token（除登录/注册外的接口都需要）
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

**常见状态码：**
| 代码 | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未授权/需登录 |
| 403 | 禁止访问 |
| 500 | 服务器错误 |

---

## 前台用户接口（/user）

### 1. 用户注册
- **路径与方法**：`POST /user/register`
- **功能描述**：新用户注册账户
- **权限要求**：无需登录
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| username | Body | String | ✓ | 用户名，4-20字符 | "john_doe" |
| password | Body | String | ✓ | 密码，6-20字符 | "password123" |
| phone | Body | String | ✓ | 手机号，1开头11位 | "13800138000" |
| nickname | Body | String | ✗ | 昵称 | "张三" |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "用户名长度为4-20个字符",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `用户名已存在` - username 已被注册
  - `手机号已存在` - phone 已被注册
  - `用户名长度为4-20个字符` - 用户名不符合长度要求
  - `密码长度为6-20个字符` - 密码不符合长度要求
  - `手机号格式不正确` - 手机号不符合格式

---

### 2. 用户登录
- **路径与方法**：`POST /user/login`
- **功能描述**：用户登录，返回 JWT Token
- **权限要求**：无需登录
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| username | Body | String | ✓ | 用户名 | "john_doe" |
| password | Body | String | ✓ | 密码 | "password123" |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userId": 123,
    "username": "john_doe",
    "nickname": "张三",
    "avatar": "https://example.com/avatar.jpg",
    "role": "USER",
    "user": {
      "id": 123,
      "username": "john_doe",
      "phone": "13800138000",
      "email": "john@example.com",
      "nickname": "张三",
      "avatar": "https://example.com/avatar.jpg",
      "gender": 1,
      "points": 100,
      "role": "USER"
    }
  },
  "timestamp": 1704067200000
}
```

**失败示例（401 Unauthorized）：**
```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `用户名或密码错误` - 登录凭据不匹配
  - `用户不存在` - username 未注册
  - `账户已禁用` - 用户状态为禁用

---

### 3. 用户登出
- **路径与方法**：`POST /user/logout`
- **功能描述**：用户登出，清除 Token
- **权限要求**：需登录（@RequireLogin）
- **请求参数**：无

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null,
  "timestamp": 1704067200000
}
```

---

### 4. 获取当前用户信息
- **路径与方法**：`GET /user/info`
- **功能描述**：获取当前登录用户的详细信息
- **权限要求**：需登录（@RequireLogin）
- **请求参数**：无

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 123,
    "username": "john_doe",
    "phone": "13800138000",
    "email": "john@example.com",
    "nickname": "张三",
    "avatar": "https://example.com/avatar.jpg",
    "gender": 1,
    "points": 100,
    "role": "USER"
  },
  "timestamp": 1704067200000
}
```

---

### 5. 更新用户信息
- **路径与方法**：`PUT /user/info`
- **功能描述**：更新用户个人信息（不含用户名、手机号、密码）
- **权限要求**：需登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| email | Body | String | ✗ | 邮箱 | "john@example.com" |
| nickname | Body | String | ✗ | 昵称 | "老王" |
| avatar | Body | String | ✗ | 头像URL | "https://example.com/avatar.jpg" |
| gender | Body | Integer | ✗ | 性别 0-未知 1-男 2-女 | 1 |

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

### 6. 修改密码
- **路径与方法**：`PUT /user/password`
- **功能描述**：修改当前用户密码
- **权限要求**：需登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| oldPassword | Query | String | ✓ | 旧密码 | "password123" |
| newPassword | Query | String | ✓ | 新密码，6-20字符 | "newpass456" |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "密码修改成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "旧密码不正确",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `旧密码不正确` - 输入的旧密码与系统不匹配
  - `新密码不能与旧密码相同` - 新旧密码相同

---

### 7. 检查用户名是否存在
- **路径与方法**：`GET /user/check/username`
- **功能描述**：检查用户名是否已被注册
- **权限要求**：无需登录
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| username | Query | String | ✓ | 待检查的用户名 | "john_doe" |

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

**说明**：`data: true` 表示用户名已存在；`data: false` 表示用户名可用

---

### 8. 检查手机号是否存在
- **路径与方法**：`GET /user/check/phone`
- **功能描述**：检查手机号是否已被注册
- **权限要求**：无需登录
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| phone | Query | String | ✓ | 待检查的手机号 | "13800138000" |

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

**说明**：`data: true` 表示手机号已存在；`data: false` 表示手机号可用

---

## 后台管理员接口（/admin/user）

### 1. 管理员登录
- **路径与方法**：`POST /admin/user/login`
- **功能描述**：管理员登录，返回 JWT Token
- **权限要求**：无需登录
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| username | Body | String | ✓ | 管理员用户名 | "admin" |
| password | Body | String | ✓ | 管理员密码 | "admin123" |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400,
    "userId": 1,
    "username": "admin",
    "nickname": "系统管理员",
    "avatar": "https://example.com/admin.jpg",
    "role": "ADMIN",
    "user": {
      "id": 1,
      "username": "admin",
      "phone": "13800138000",
      "email": "admin@example.com",
      "nickname": "系统管理员",
      "avatar": "https://example.com/admin.jpg",
      "gender": 1,
      "points": 0,
      "role": "ADMIN"
    }
  },
  "timestamp": 1704067200000
}
```

**失败示例（401 Unauthorized）：**
```json
{
  "code": 401,
  "message": "用户名或密码错误",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `用户名或密码错误` - 登录凭据不匹配
  - `无管理员权限` - 该用户不是管理员

---

### 2. 获取当前管理员信息
- **路径与方法**：`GET /admin/user/info`
- **功能描述**：获取当前登录管理员的信息
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：无

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "username": "admin",
    "phone": "13800138000",
    "email": "admin@example.com",
    "nickname": "系统管理员",
    "avatar": "https://example.com/admin.jpg",
    "gender": 1,
    "points": 0,
    "role": "ADMIN"
  },
  "timestamp": 1704067200000
}
```

---

### 3. 管理员注销
- **路径与方法**：`POST /admin/user/logout`
- **功能描述**：管理员登出，清除 Token
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：无

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null,
  "timestamp": 1704067200000
}
```

---

## 附注
- 本文档由 AI 基于当前代码自动生成，上线前需人工核对
- 所有时间戳采用毫秒级 Unix 时间戳
- JWT Token 有效期通常为 24 小时，可在登录响应中查看 `expiresIn` 字段
- [推断] 部分字段/逻辑是根据代码方法名与参数结构推断而出
