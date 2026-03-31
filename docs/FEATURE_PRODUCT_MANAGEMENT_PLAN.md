# Eden Nutrition - 后台商品管理功能开发方案

## 1. 概述与背景 (Overview)
当前 C 端商城（`eden-web`）的商品浏览、架构链路已完善，但 B 端管理系统（`eden-admin-vue`）的“商品管理”模块只有前端页面骨架，后端接口 (`eden-admin`) 尚未实现。
为了支持运营人员能够在后台进行**全量商品的增删改查、上下架控制**等日常管理，特制定本后端开发落地文档。

## 2. 核心痛点与需求分析
通过对现有底层代码（如 `ProductMapper.xml`）的审查，发现现有的 `selectByCondition` 方法中**硬编码了 `status = 1`（仅查询已上架商品）**，这导致 C 端查询逻辑无法直接复用于 B 端后台。
**主要需求包含：**
1. **B 端专属复合查询**：需要无视上线状态进行全量分页查询，同时支持按 `指定状态`、`指定分类ID`、`商品名称关键词` 进行动态筛选。
2. **商品发布与编辑**：实现结构化的商品保存逻辑，涵盖类目、价格（售价与划线价）、库存、主/副图图集以及详情图文富文本。
3. **快捷状态管理**：提供独立的快捷接口，一键变更商品 `status`（0下架，1上架）。
4. **商品删除**：提供针对废弃商品的删除接口。

---

## 3. 架构设计与模块职责 (Architecture & Responsibilities)

功能开发将严格遵循本项目的 DDD/微服务多模块最佳实践：

### 3.1 `eden-pojo` (实体与数据传输层)
新建专用于 B 端交互的数据传输模型，隔离 C 端业务。
* **`AdminProductQueryDTO.java`**: 
  * 继承 `PageDTO`，包含字段：`keyword` (模糊匹配 name/subtitle)，`categoryId`，`status` (可选条件)。
* **`ProductSaveDTO.java`**:
  * 包含新增和更新时的接收字段：`id` (更新时存在), `name`, `subtitle`, `categoryId`, `mainImage`, `subImages`, `detail`, `originalPrice`, `price`, `stock`, `isHot`, `isNew`。

### 3.2 `eden-mapper` (持久层)
在 `ProductMapper.java` 和对应的 `ProductMapper.xml` 补充管理员专属数据访问层：
* **`selectAdminList(AdminProductQueryDTO query)`**: 
  * 动态 SQL `<where>` 判断：去掉 `status = 1` 的硬编码，根据参数中的 `status` 动态拼接。
* **`countAdminList(AdminProductQueryDTO query)`**: 
  * 配合分页条数统计的专属 SQL。
* *(注：现有的 `insert`, `update`, `deleteById`, `updateStatus` 可以直接复用于后续流程。)*

### 3.3 `eden-service` (业务逻辑层)
在 `ProductService` 及 `ProductServiceImpl` 中扩展后台能力：
* `PageVO<Product> getAdminProductPage(AdminProductQueryDTO queryDTO)`
* `void saveProduct(ProductSaveDTO dto)`
* `void updateProduct(ProductSaveDTO dto)`
* `void updateProductStatus(Long id, Integer status)`
* `void deleteProduct(Long id)`

### 3.4 `eden-admin` (接口暴露层)
创建 `AdminProductController`，提供标准 RESTful API：
* `GET /admin/product/list`
* `GET /admin/product/{id}` (回显商品信息用)
* `POST /admin/product` (新增)
* `PUT /admin/product` (修改全量信息)
* `PUT /admin/product/status/{id}/{status}` (操作上下架)
* `DELETE /admin/product/{id}`

---

## 4. RESTful API 接口规约 (API Specifications)

### 4.1 获取商品分页列表
* **路径**: `GET /admin/product/list`
* **参数**: `page`, `pageSize`, `keyword` (可选), `categoryId` (可选), `status` (可选)
* **响应**: `Result<PageVO<Product>>`

### 4.2 获取商品详情
* **路径**: `GET /admin/product/{id}`
* **响应**: `Result<Product>`

### 4.3 新增商品
* **路径**: `POST /admin/product`
* **Body**: JSON 格式的 `ProductSaveDTO`
* **响应**: `Result<Void>`

### 4.4 修改商品
* **路径**: `PUT /admin/product`
* **Body**: JSON 格式的 `ProductSaveDTO` (携带 `id`)
* **响应**: `Result<Void>`

### 4.5 修改商品上下架状态
* **路径**: `PUT /admin/product/status/{id}/{status}`
* **响应**: `Result<Void>`

### 4.6 删除商品
* **路径**: `DELETE /admin/product/{id}`
* **响应**: `Result<Void>`

---

## 5. 开发落地计划与行动项 (Execution Plan)

开发过程分为 **3个阶段** (建议立即执行)：

* **阶段 1：底层与实体搭建** 
  * 在 `eden-pojo` 建立所需的 `DTO`。
  * 在 `ProductMapper.xml` 增加 `<select id="selectAdminList">` 和对应的 `count` 统计查询。
* **阶段 2：服务层与控制层组装**
  * 在 `ProductServiceImpl` 编写各个 admin 结尾的增删改查实现类，确保入参及合法性边界校验（例如分类ID是否存在，价格格式等）。
  * 在 `eden-admin` 中创建 `AdminProductController`，并引入 `@RestController` 和权限校验（由于 Admin 已配置统一拦截，路由置于 `/admin/**` 下）。
* **阶段 3：前后端联调测试**
  * 启动后端 `EdenApplication`，通过 Postman / Swagger 调试接口。
  * 修改 `eden-admin-vue/src/api/product.ts` 匹配对应的后端 API，进行页面联动测试。