# 分类管理接口文档

## 概述
分类管理分为两个部分：
- **后台管理接口**：管理员进行分类的增删改查
- **前台用户接口**：用户查询分类、浏览分类树

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

### 分类状态
| 状态码 | 含义 |
|--------|------|
| 0 | 禁用 |
| 1 | 启用 |

### 分类层级
| 代码 | 含义 |
|------|------|
| 1 | 一级分类 |
| 2 | 二级分类 |

---

## 后台分类接口（/admin/category）

### 1. 获取分类树
- **路径与方法**：`GET /admin/category/tree`
- **功能描述**：获取所有分类的树形结构
- **权限要求**：需管理员登录（@RequireAdminLogin）
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
      "name": "电子产品",
      "parentId": 0,
      "level": 1,
      "sortOrder": 1,
      "icon": "https://example.com/icon.png",
      "children": [
        {
          "id": 10,
          "name": "手机",
          "parentId": 1,
          "level": 2,
          "sortOrder": 1,
          "icon": "https://example.com/phone-icon.png",
          "children": []
        },
        {
          "id": 11,
          "name": "平板",
          "parentId": 1,
          "level": 2,
          "sortOrder": 2,
          "icon": "https://example.com/tablet-icon.png",
          "children": []
        }
      ]
    },
    {
      "id": 2,
      "name": "服装箱包",
      "parentId": 0,
      "level": 1,
      "sortOrder": 2,
      "icon": "https://example.com/clothes-icon.png",
      "children": []
    }
  ],
  "timestamp": 1704067200000
}
```

---

### 2. 新增分类
- **路径与方法**：`POST /admin/category`
- **功能描述**：创建新的分类
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| name | Body | String | ✓ | 分类名称 | "手机" |
| parentId | Body | Long | ✓ | 父分类ID，顶级为0 | 1 |
| level | Body | Integer | ✓ | 分类层级：1-一级 2-二级 | 2 |
| sortOrder | Body | Integer | ✗ | 排序值，默认999 | 1 |
| icon | Body | String | ✗ | 分类图标URL | "https://example.com/phone-icon.png" |
| status | Body | Integer | ✗ | 状态：0-禁用 1-启用，默认1 | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "分类新增成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "分类名称不能为空",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `分类名称不能为空` - name 未提供
  - `父分类不存在` - parentId 对应的分类不存在
  - `分类层级不合法` - level 不是 1 或 2
  - `同级分类名称重复` - 相同父分类下已存在该名称的分类

---

### 3. 修改分类
- **路径与方法**：`PUT /admin/category`
- **功能描述**：编辑分类信息
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Body | Long | ✓ | 分类ID（修改时必须提供） | 10 |
| name | Body | String | ✓ | 分类名称 | "手机（更新）" |
| parentId | Body | Long | ✓ | 父分类ID | 1 |
| level | Body | Integer | ✓ | 分类层级 | 2 |
| sortOrder | Body | Integer | ✗ | 排序值 | 2 |
| icon | Body | String | ✗ | 分类图标URL | "https://example.com/phone-icon-new.png" |
| status | Body | Integer | ✗ | 状态 | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "分类修改成功",
  "data": null,
  "timestamp": 1704067200000
}
```

---

### 4. 删除分类
- **路径与方法**：`DELETE /admin/category/{id}`
- **功能描述**：删除指定分类（只能删除无子分类的分类）
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Path | Long | ✓ | 分类ID | 10 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "分类删除成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "该分类下有子分类，不能删除",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `分类不存在` - id 对应的分类不存在
  - `该分类下有子分类，不能删除` - 该分类有子分类
  - `该分类下有商品，不能删除` - 该分类关联了商品

---

### 5. 修改分类状态
- **路径与方法**：`PUT /admin/category/status/{id}/{status}`
- **功能描述**：快速修改分类的启用/禁用状态
- **权限要求**：需管理员登录（@RequireAdminLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Path | Long | ✓ | 分类ID | 10 |
| status | Path | Integer | ✓ | 新状态：0-禁用 1-启用 | 1 |

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

## 前台分类接口（/category）

### 1. 获取分类树
- **路径与方法**：`GET /category/tree`
- **功能描述**：获取所有启用状态的分类树形结构
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
      "name": "电子产品",
      "parentId": 0,
      "level": 1,
      "sortOrder": 1,
      "icon": "https://example.com/icon.png",
      "children": [
        {
          "id": 10,
          "name": "手机",
          "parentId": 1,
          "level": 2,
          "sortOrder": 1,
          "icon": "https://example.com/phone-icon.png",
          "children": []
        }
      ]
    }
  ],
  "timestamp": 1704067200000
}
```

---

### 2. 获取一级分类
- **路径与方法**：`GET /category/first`
- **功能描述**：获取所有启用状态的一级分类列表
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
      "name": "电子产品",
      "parentId": 0,
      "level": 1,
      "sortOrder": 1,
      "icon": "https://example.com/icon.png",
      "status": 1,
      "createTime": "2024-01-01T00:00:00",
      "updateTime": "2024-01-01T00:00:00"
    },
    {
      "id": 2,
      "name": "服装箱包",
      "parentId": 0,
      "level": 1,
      "sortOrder": 2,
      "icon": "https://example.com/clothes-icon.png",
      "status": 1,
      "createTime": "2024-01-01T00:00:00",
      "updateTime": "2024-01-01T00:00:00"
    }
  ],
  "timestamp": 1704067200000
}
```

---

### 3. 获取子分类
- **路径与方法**：`GET /category/children/{parentId}`
- **功能描述**：获取指定分类的所有子分类
- **权限要求**：无需登录
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| parentId | Path | Long | ✓ | 父分类ID | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": [
    {
      "id": 10,
      "name": "手机",
      "parentId": 1,
      "level": 2,
      "sortOrder": 1,
      "icon": "https://example.com/phone-icon.png",
      "status": 1,
      "createTime": "2024-01-01T00:00:00",
      "updateTime": "2024-01-01T00:00:00"
    },
    {
      "id": 11,
      "name": "平板",
      "parentId": 1,
      "level": 2,
      "sortOrder": 2,
      "icon": "https://example.com/tablet-icon.png",
      "status": 1,
      "createTime": "2024-01-01T00:00:00",
      "updateTime": "2024-01-01T00:00:00"
    }
  ],
  "timestamp": 1704067200000
}
```

---

### 4. 获取分类详情
- **路径与方法**：`GET /category/{id}`
- **功能描述**：获取指定分类的详细信息
- **权限要求**：无需登录
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Path | Long | ✓ | 分类ID | 10 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 10,
    "name": "手机",
    "parentId": 1,
    "level": 2,
    "sortOrder": 1,
    "icon": "https://example.com/phone-icon.png",
    "status": 1,
    "createTime": "2024-01-01T00:00:00",
    "updateTime": "2024-01-01T00:00:00"
  },
  "timestamp": 1704067200000
}
```

---

### 5. 新增分类 [推断]
- **路径与方法**：`POST /category/add`
- **功能描述**：[推断] 用户端新增分类接口（应受权限保护）
- **权限要求**：无需登录（代码未强制要求）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| name | Body | String | ✓ | 分类名称 | "手机" |
| parentId | Body | Long | ✓ | 父分类ID | 1 |
| level | Body | Integer | ✓ | 分类层级 | 2 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1704067200000
}
```

---

## 附注
- 本文档由 AI 基于当前代码自动生成，上线前需人工核对
- [推断] 标记的接口需人工验证权限控制
- 前台仅显示状态为 1（启用）的分类
- 后台可查看所有分类包括禁用的（status=0）
- 分类树按 sortOrder 字段升序排列
- 时间戳采用毫秒级 Unix 时间戳
