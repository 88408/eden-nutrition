# 收货地址接口文档

## 概述
收货地址模块提供用户管理收货地址的功能，包括添加、查询、修改、删除地址以及设置默认地址等操作。

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

## 接口列表（/address）

所有收货地址接口均需用户登录（@RequireLogin）。

### 1. 获取收货地址列表
- **路径与方法**：`GET /address/list`
- **功能描述**：获取当前用户的所有收货地址列表
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
      "id": 1,
      "userId": 123,
      "receiverName": "张三",
      "receiverPhone": "13800138000",
      "province": "北京市",
      "city": "朝阳区",
      "district": "建国路",
      "detailAddress": "某大厦1001号",
      "isDefault": 1,
      "createTime": "2024-01-01T10:30:00",
      "updateTime": "2024-01-15T14:20:00"
    },
    {
      "id": 2,
      "userId": 123,
      "receiverName": "李四",
      "receiverPhone": "13900139000",
      "province": "浙江省",
      "city": "杭州市",
      "district": "西湖区",
      "detailAddress": "学院路101号",
      "isDefault": 0,
      "createTime": "2024-01-05T09:15:00",
      "updateTime": "2024-01-05T09:15:00"
    }
  ],
  "timestamp": 1704067200000
}
```

---

### 2. 获取默认地址
- **路径与方法**：`GET /address/default`
- **功能描述**：获取当前用户设置的默认收货地址
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：无

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "userId": 123,
    "receiverName": "张三",
    "receiverPhone": "13800138000",
    "province": "北京市",
    "city": "朝阳区",
    "district": "建国路",
    "detailAddress": "某大厦1001号",
    "isDefault": 1,
    "createTime": "2024-01-01T10:30:00",
    "updateTime": "2024-01-15T14:20:00"
  },
  "timestamp": 1704067200000
}
```

**失败示例（404 Not Found）：**
```json
{
  "code": 404,
  "message": "未找到默认地址",
  "data": null,
  "timestamp": 1704067200000
}
```

---

### 3. 获取地址详情
- **路径与方法**：`GET /address/{id}`
- **功能描述**：获取指定地址的详细信息
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Path | Long | ✓ | 地址ID | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "成功",
  "data": {
    "id": 1,
    "userId": 123,
    "receiverName": "张三",
    "receiverPhone": "13800138000",
    "province": "北京市",
    "city": "朝阳区",
    "district": "建国路",
    "detailAddress": "某大厦1001号",
    "isDefault": 1,
    "createTime": "2024-01-01T10:30:00",
    "updateTime": "2024-01-15T14:20:00"
  },
  "timestamp": 1704067200000
}
```

**失败示例（404 Not Found）：**
```json
{
  "code": 404,
  "message": "地址不存在",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `地址不存在` - id 对应的地址不存在或不属于当前用户
  - `无权限访问此地址` - 当前用户不是该地址的所有者

---

### 4. 添加收货地址
- **路径与方法**：`POST /address`
- **功能描述**：为当前用户添加新的收货地址
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| receiverName | Body | String | ✓ | 收货人姓名 | "张三" |
| receiverPhone | Body | String | ✓ | 收货人电话，1开头11位 | "13800138000" |
| province | Body | String | ✓ | 省份 | "北京市" |
| city | Body | String | ✓ | 城市 | "朝阳区" |
| district | Body | String | ✓ | 区县 | "建国路" |
| detailAddress | Body | String | ✓ | 详细地址 | "某大厦1001号" |
| isDefault | Body | Integer | ✗ | 是否默认：0-否 1-是，默认0 | 0 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "地址添加成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "收货人姓名不能为空",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `收货人姓名不能为空` - receiverName 未提供
  - `收货人电话不能为空` - receiverPhone 未提供
  - `电话号码格式不正确` - receiverPhone 不符合格式
  - `省份不能为空` - province 未提供
  - `城市不能为空` - city 未提供
  - `详细地址不能为空` - detailAddress 未提供
  - `收货地址超过限制` - 同一用户最多添加 10 个地址

---

### 5. 更新收货地址
- **路径与方法**：`PUT /address`
- **功能描述**：修改当前用户的收货地址
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Body | Long | ✓ | 地址ID（修改时必须提供） | 1 |
| receiverName | Body | String | ✓ | 收货人姓名 | "张三（更新）" |
| receiverPhone | Body | String | ✓ | 收货人电话 | "13800138001" |
| province | Body | String | ✓ | 省份 | "北京市" |
| city | Body | String | ✓ | 城市 | "东城区" |
| district | Body | String | ✓ | 区县 | "王府井大街" |
| detailAddress | Body | String | ✓ | 详细地址 | "某大厦2001号" |
| isDefault | Body | Integer | ✗ | 是否默认 | 0 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "地址修改成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（404 Not Found）：**
```json
{
  "code": 404,
  "message": "地址不存在",
  "data": null,
  "timestamp": 1704067200000
}
```

---

### 6. 删除收货地址
- **路径与方法**：`DELETE /address/{id}`
- **功能描述**：删除指定的收货地址
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Path | Long | ✓ | 地址ID | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "地址删除成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（400 Bad Request）：**
```json
{
  "code": 400,
  "message": "不能删除默认地址",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `地址不存在` - id 对应的地址不存在或不属于当前用户
  - `不能删除默认地址` - 该地址已设置为默认地址
  - `无权限删除此地址` - 当前用户不是该地址的所有者

---

### 7. 设置默认地址
- **路径与方法**：`PUT /address/default/{id}`
- **功能描述**：将指定地址设置为默认收货地址
- **权限要求**：需用户登录（@RequireLogin）
- **请求参数**：

| 参数名 | 位置 | 类型 | 必填 | 说明 | 示例 |
|--------|------|------|------|------|------|
| id | Path | Long | ✓ | 地址ID | 1 |

- **响应结构**：

**成功示例（200 OK）：**
```json
{
  "code": 200,
  "message": "默认地址设置成功",
  "data": null,
  "timestamp": 1704067200000
}
```

**失败示例（404 Not Found）：**
```json
{
  "code": 404,
  "message": "地址不存在",
  "data": null,
  "timestamp": 1704067200000
}
```

- **可能的业务错误码**：
  - `地址不存在` - id 对应的地址不存在或不属于当前用户
  - `无权限操作此地址` - 当前用户不是该地址的所有者

---

## 使用建议

### 地址管理流程示例
1. **查看所有地址** → `GET /address/list`
2. **添加新地址** → `POST /address`
3. **查看默认地址** → `GET /address/default`
4. **修改地址** → `PUT /address`
5. **设置为默认地址** → `PUT /address/default/{id}`
6. **删除地址** → `DELETE /address/{id}`（先删除非默认地址）

### 创建订单时的地址选择
- 用户创建订单时需要指定 `addressId`
- 如果未选择地址，自动使用默认地址
- 推荐提供快速选择最近使用的地址的功能

### 地址信息规范
- 收货人姓名：2-10 个字符
- 收货人电话：1 开头的 11 位数字
- 省份、城市、区县：支持自动完成，建议调用三级地址选择器
- 详细地址：20-100 字符，包括楼号、门牌号等具体位置信息

---

## 附注
- 本文档由 AI 基于当前代码自动生成，上线前需人工核对
- 每个用户最多可添加 10 个收货地址
- 用户必须至少保留一个地址（不能全部删除）
- 删除地址前自动将默认地址设置为第一个地址
- 修改默认地址时，系统自动取消其他地址的默认状态
- 时间戳采用毫秒级 Unix 时间戳
- 地址信息完整性会影响订单配送速度，建议提示用户填写详细地址
