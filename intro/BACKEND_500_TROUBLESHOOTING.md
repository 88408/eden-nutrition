# 问题分析与解决：后端500错误及编译错误

## 1. 问题复现
前端调用 `POST /api/category/add` 返回 500 错误。
Axios 报错：`Request failed with status code 500`。
随后，控制台出现 Bean 创建异常 `Unresolved compilation problems: The import eden.mapper.OrderMapper cannot be resolved`。

## 2. 原因推测
常见的 500 错误原因：
1.  **MyBatis 参数绑定失败**: `CategoryMapper.xml` 中 `insert` 语句未指定 `parameterType` 或 `jdbcType`，导致对于 `null` 值（如 `icon`）无法正确推断类型。
2.  **SQL 语法错误**: `NOW()` 函数在部分数据库方言下可能不兼容，但 MySQL 支持。
3.  **编译环境不一致**: `OrderTask` 依赖的 `OrderMapper` 类未正确编译或类文件过时，导致运行时出现 NoClassDefFoundError 或 Unresolved compilation problems。

## 3. 详细排查与修复

### 3.1 修复 CategoryMapper.xml (已完成)
为了增强 MyBatis 的兼容性并避免参数绑定错误，我们明确指定 `parameterType` 和所有字段的 `jdbcType`。这也解决了潜在的 `NullPointerException` 或无法识别 JDBC 类型的问题。

**修改后**:
```xml
    <insert id="insert" parameterType="eden.pojo.Category" useGeneratedKeys="true" keyProperty="id">
        INSERT INTO category ...
        VALUES (
            #{name, jdbcType=VARCHAR},
            #{parentId, jdbcType=BIGINT},
            #{level, jdbcType=INTEGER},
            #{sortOrder, jdbcType=INTEGER},
            #{icon, jdbcType=VARCHAR},
            #{status, jdbcType=INTEGER},
            NOW(),
            NOW()
        )
    </insert>
```

### 3.2 修复编译错误 (已完成)
发现 `eden-service` 模块中的 `OrderTask` 类无法解析 `OrderMapper` 导入。
这表明之前的增量编译可能导致了依赖关系错乱或类文件损坏。

**解决方案**:
执行了全量清理与重新编译命令：
`mvn clean install -DskipTests`

该操作重新构建了 `eden-mapper` 并将其安装到本地仓库，随后重新编译了 `eden-service`，解决了类路径依赖问题。

## 4. 验证建议
修复代码已应用且项目已重新编译。
请 **重启后端服务** (重新运行 `EdenApplication` 或 Maven 运行命令)，确保加载最新的类文件和 Mapper XML 配置。
再次尝试在前端添加分类。
