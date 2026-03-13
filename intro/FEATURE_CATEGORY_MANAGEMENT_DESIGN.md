# 商品分类管理功能详细设计 (Category Management Design)

## 1. 背景与目标
当前系统在添加商品 (`Product Management`) 时需选择商品分类，但缺乏对分类本身的增删改查 (CRUD) 管理功能。本设计旨在补全该缺失环节，使管理员能够动态维护商品分类体系。

## 2. 需求分析
### 2.1 核心功能
1.  **分类列表展示**：以树形结构展示多级分类（目前支持两级：一级分类 -> 二级分类）。
2.  **新增分类**：
    *   支持添加一级分类。
    *   支持在现有分类下添加子分类。
    *   字段：名称、图标、排序优先级、状态。
3.  **编辑分类**：修改名称、图标、排序、状态。
4.  **删除分类**：只能删除无子分类且无关联商品的分类（逻辑删除或校验拦截）。
5.  **状态管理**：快速启用/禁用分类。

### 2.2 业务规则
*   **层级限制**：建议限制为 2 级或 3 级（目前系统设计倾向于 2 级）。
*   **唯一性**：同级分类名称不可重复。
*   **删除约束**：若分类下存在商品，禁止删除；若分类下存在子分类，禁止删除。

## 3. 接口设计 (API Design)

需在 `eden-web` 模块的 `CategoryController` 中扩展以下接口：

### 3.1 新增分类
*   **Path**: `POST /category/create`
*   **Request Body**:
    ```json
    {
      "name": "维生素",
      "parentId": 0,       // 0 表示一级分类
      "level": 1,          // 1-一级, 2-二级
      "sortOrder": 100,
      "icon": "http://...",
      "status": 1
    }
    ```
*   **Response**: `Result<Void>`

### 3.2 更新分类
*   **Path**: `PUT /category/update`
*   **Request Body**:
    ```json
    {
      "id": 101,
      "name": "维生素C",
      "sortOrder": 99,
      "icon": "http://...",
      "status": 1
    }
    ```
*   **Response**: `Result<Void>`

### 3.3 删除分类
*   **Path**: `DELETE /category/delete/{id}`
*   **Query Param**: `id`
*   **Logic**: Check children -> Check products -> Delete.
*   **Response**: `Result<Void>`

### 3.4 修改状态
*   **Path**: `PUT /category/status/{id}`
*   **Query Body**: `{ "status": 0 }`
*   **Response**: `Result<Void>`

## 4. 前端设计 (Frontend Design - eden-admin-vue)

### 4.1 新增页面
*   **Route**: `/product/category`
*   **File**: `src/pages/Product/Category.tsx`

### 4.2 页面布局
采用 **左树右表** 或 **树形表格 (Tree Table)** 布局。推荐使用 **Tree Table**，因字段较少且层级简单。

*   **Top Bar**: "新增一级分类" 按钮。
*   **Table Columns**:
    *   分类名称 (Expandable)
    *   图标 (Avatar)
    *   排序 (Input/Text)
    *   状态 (Switch)
    *   操作 (编辑 | 删除 | 新增子分类)

### 4.3 交互流程
1.  **新增一级分类**: 点击顶部按钮 -> 弹窗填写表单 -> 提交 -> 刷新列表。
2.  **新增子分类**: 点击行内 "新增子分类" -> 弹窗 (自动填充父级ID) -> 提交 -> 刷新。
3.  **编辑**: 点击行内 "编辑" -> 弹窗回显数据 -> 提交 -> 刷新。
4.  **删除**: 点击 "删除" -> 二次确认 -> 提交 -> 处理成功/失败提示。

## 5. 数据库设计 (Database Schema)
基于现有 `category` 表，无需修改 Schema，确认字段如下：
*   `id`: BIGINT (PK)
*   `parent_id`: BIGINT (Default 0)
*   `name`: VARCHAR
*   `level`: TINYINT
*   `sort_order`: INT
*   `icon`: VARCHAR
*   `status`: TINYINT
