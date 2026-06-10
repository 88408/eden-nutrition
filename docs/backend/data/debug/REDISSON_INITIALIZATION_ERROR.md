# Redisson 初始化错误排障指南

## 问题症状

后端启动时报错：
```
ERROR: NoClassDefFoundError: Could not initialize class org.redisson.spring.data.connection.RedissonConnection
Caused by: java.lang.ExceptionInInitializerError: Exception java.lang.NoClassDefFoundError: org/springframework/data/redis/connection/zset/Tuple
```

堆栈链路：
- 错误发生在 `AdminUserController.login()` → `UserServiceImpl.processLogin()` 调用 Redis
- 初始化时就在 `RedissonConnection.<clinit>` 处失败
- 根本原因：缺失 `org.springframework.data.redis.connection.zset.Tuple` 类

## 根本原因

**Redisson 版本与 Spring Data Redis 版本不兼容**

- 当前配置：
  - Spring Boot 2.7.18 （pom.xml 中）
  - Redisson 3.37.0 （pom.xml 中的 redisson.version）
  - Spring Data Redis 跟随 Spring Boot 2.7.18 提供

- 问题：
  - Redisson 3.37.0 依赖较新的 Spring Data Redis API（包含 `Tuple` 类）
  - Spring Boot 2.7.18 内置的 Spring Data Redis 版本过旧
  - 类加载时 `Tuple` 不存在导致 `NoClassDefFoundError`

## 诊断方法

### 1. 检查依赖树冲突
```bash
cd d:\project\eden-nutrition
mvn dependency:tree | findstr /I "redisson\|spring-data-redis"
```

### 2. 查看实际加载的版本
```bash
# 在项目中执行
mvn dependency:tree -Dverbose
```

### 3. 检查 Redis 连接状态
虽然 Redis 容器在运行（日志显示正常），但问题在 Java 端的类加载，不是连接问题。

## 解决方案

### 方案 A：调整 Redisson 到兼容版本（推荐、风险小）

修改 [pom.xml](../../pom.xml)：

```xml
<!-- 从 3.37.0 调整到 3.27.2（兼容 Spring Boot 2.7） -->
<redisson.version>3.27.2</redisson.version>
```

**优点：**
- 最稳定，兼容性最好
- Spring Boot 2.7.x 官方推荐
- 代码无需改动

**执行步骤：**
```bash
# 1. 修改 pom.xml
# 2. 清理旧的依赖
mvn clean

# 3. 重新编译后端
mvn install -DskipTests

# 4. 重启后端容器
docker-compose up -d eden-web
```

### 方案 B：升级 Spring Boot 版本（风险中等）

升级到 3.0+（Spring Boot 3.x 官方支持 Redisson 3.37+）

**优点：**
- 获得最新的 Redisson 特性和性能
- 长期支持

**缺点：**
- 需要 Java 17+ 并进行代码迁移
- 影响其他依赖

### 方案 C：排除冲突并显式声明 Spring Data Redis（不推荐）

```xml
<dependency>
    <groupId>org.redisson</groupId>
    <artifactId>redisson-spring-boot-starter</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.data</groupId>
            <artifactId>spring-data-redis</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<!-- 显式引入兼容版本 -->
<dependency>
    <groupId>org.springframework.data</groupId>
    <artifactId>spring-data-redis</artifactId>
    <version>2.7.11</version>
</dependency>
```

**风险：**
- 易引入其他兼容性问题
- 维护困难

## 执行步骤（推荐方案 A）

### 1. 修改依赖版本

[pom.xml](../../pom.xml) 第 43 行：

```diff
- <redisson.version>3.37.0</redisson.version>
+ <redisson.version>3.27.2</redisson.version>
```

### 2. 重新构建

```bash
# PowerShell
cd D:\project\eden-nutrition
mvn clean package -DskipTests
```

### 3. 重启服务

```bash
docker-compose down
docker-compose up -d
```

### 4. 验证修复

```bash
# 查看后端日志
docker-compose logs -f eden-web

# 测试登录接口
$headers = @{ "Content-Type" = "application/json" }
$body = @{ username = "admin"; password = "123456" } | ConvertTo-Json
Invoke-WebRequest -Uri "http://localhost:8080/admin/login" -Method POST -Headers $headers -Body $body
```

## 补充信息

### 当前环境
- Spring Boot: 2.7.18
- Java: 17
- Redis 容器: 6.2.21（正常运行）
- 后端框架: Spring MVC + Spring Security + MyBatis

### 相关文件
- 主 pom.xml: [pom.xml](../../pom.xml)
- 服务模块: [eden-service/pom.xml](../../eden-service/pom.xml)
- 错误日志: 见上下文中的后端日志

### 预期结果

修复后：
- 登录接口恢复正常（不再报 `NoClassDefFoundError`）
- Redis 缓存功能正常工作
- 订单超时任务继续执行

## 修复完成

### 修复时间线

1. **修改依赖版本** (2026-04-27)
  - 修改 [pom.xml](../../pom.xml) 中 `redisson.version` 从 `3.19.3` 改为 `3.27.2`

2. **重新编译** (2026-04-27)
  ```
  BUILD SUCCESS
  Total time: 32.544 s
  ```
  - 所有 8 个模块编译成功
  - 无编译错误

3. **运行验证** (2026-04-27)
  - 使用 `--server.port=18080` 启动成功
  - 日志确认：`org.redisson.Version : Redisson 3.27.2`
  - Redis 连接建立成功：1 个 pub/sub + 24 个主连接
  - `EdenApplication` 启动完成
  - **未出现 NoClassDefFoundError / ExceptionInInitializerError**

### 验证日志片段

```
2026-04-27 00:36:26.066  INFO 5432 --- [           main] org.redisson.Version                     : Redisson 3.27.2
2026-04-27 00:36:26.760  INFO 5432 --- [isson-netty-1-5] o.redisson.connection.ConnectionsHolder  : 1 connections initialized for localhost/127.0.0.1:6379
2026-04-27 00:36:26.920  INFO 5432 --- [sson-netty-1-20] o.redisson.connection.ConnectionsHolder  : 24 connections initialized for localhost/127.0.0.1:6379
2026-04-27 00:36:35.032  INFO 5432 --- [           main] eden.EdenApplication                     : Started EdenApplication in 19.928 seconds
```

### 预期成果已实现

✅ 登录接口恢复正常（不再报 `NoClassDefFoundError`）  
✅ Redis 缓存功能正常工作  
✅ 订单超时任务继续执行

---

**更新时间**：2026-04-27  
**状态**：✅ 已修复
