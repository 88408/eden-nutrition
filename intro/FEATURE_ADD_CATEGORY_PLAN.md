# 新增商品分类功能开发方案

## 1. 目标
实现商品分类的新增功能，允许管理员在商品管理页面直接添加新的分类，无需手动操作数据库。

## 2. 后端开发
涉及模块：`eden-web`, `eden-service`, `eden-mapper`

### 2.1 Mapper 层
**文件**: `eden-mapper/src/main/resources/mapper/CategoryMapper.xml`
*   **任务**: 添加 `<insert>` 语句。
*   **SQL逻辑**:
    ```xml
    <insert id="insert" parameterType="eden.pojo.Category" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO category (name, parent_id, level, sort_order, icon, status, create_time, update_time)
        VALUES (#{name}, #{parentId}, #{level}, #{sortOrder}, #{icon}, #{status}, NOW(), NOW())
    </insert>
    ```

**文件**: `eden-mapper/src/main/java/eden/mapper/CategoryMapper.java`
*   **任务**: 添加接口方法。
    ```java
    int insert(Category category);
    ```

### 2.2 Service 层
**文件**: `eden-service/src/main/java/eden/service/CategoryService.java`
*   **任务**: 添加 `add` 方法定义。
    ```java
    void add(Category category);
    ```

**文件**: `eden-service/src/main/java/eden/service/impl/CategoryServiceImpl.java`
*   **任务**: 实现 `add` 方法。
    *   设置默认值（如果未提供）：`status` = 1 (启用), `createTime` = new Date(), `updateTime` = new Date()。
    *   调用 `categoryMapper.insert(category)`。

### 2.3 Controller 层
**文件**: `eden-web/src/main/java/eden/web/controller/CategoryController.java`
*   **任务**: 添加 POST 接口。
    *   路径: `/category/add`
    *   方法: `@PostMapping("/add")`
    *   接收参数: `Category` 对象。
    *   返回: `Result<String>` ("操作成功")。

## 3. 前端开发
涉及项目：`eden-admin-vue`

### 3.1 API 封装
**文件**: `src/api/category.ts`
*   **任务**: 添加 `addCategory` 方法。
    ```typescript
    export const addCategory = async (data: Partial<Category>): Promise<void> => {
      return client.post('/category/add', data);
    };
    ```

### 3.2 页面交互 (ProductManagement)
**文件**: `src/pages/ProductManagement/index.tsx`

*   **UI 变更**:
    *   在“新增商品”弹窗中的“商品分类”下拉框右侧，添加一个小的 "+" 按钮。
    *   点击 "+" 按钮，弹出一个简单的 `SweetAlert` 或内嵌的小 Modal，用于输入新分类名称。
    *   **简化版交互**: 使用 `window.prompt` 或简单的输入框即可满足快速添加需求。

*   **逻辑变更**:
    *   处理 "+" 按钮点击事件。
    *   调用 `addCategory` API。
    *   成功后，重新调用 `getCategories()` 刷新下拉框数据，通过 `setCategories` 更新状态，并自动选中新添加的分类。

## 4. 开发步骤
1.  **后端**: 完成 Mapper XML -> Mapper Interface -> Service -> Controller 的代码编写与自测。
2.  **前端**: 封装 API，修改 React 组件添加交互。
3.  **联调**: 启动前后端，测试添加分类功能是否生效，数据是否持久化。
