# 项目升级方案：Eden Nutrition

## 1. 升级目标
- **当前状态**: Spring Boot 2.7.18, Java 17
- **目标状态**: Spring Boot 3.2.x (或最新稳定版), Java 21

## 2. 升级必要性
- **Spring Boot 2.x 停止维护**: Spring Boot 2.7 已于 2023 年 11 月停止免费支持。
- **性能提升**: Spring Boot 3 基于 Spring Framework 6，配合 Java 21 的虚拟线程（Virtual Threads）等特性，可显著提升并发性能。
- **安全性**: 获取最新的安全补丁和依赖更新。
- **新特性**: 利用 AOT 编译（GraalVM Native Image）等新技术。

## 3. 前置准备
- 确保开发环境和 CI/CD 环境已安装 **JDK 21**。
- 备份当前代码库或创建新的升级分支 `feature/upgrade-sb3`。

## 4. 详细升级步骤

### 4.1 Java 版本升级 (JDK 17 -> 21)
修改根目录 `pom.xml` 中的 Java 版本属性：
```xml
<properties>
    <java.version>21</java.version>
    <maven.compiler.source>21</maven.compiler.source>
    <maven.compiler.target>21</maven.compiler.target>
</properties>
```

### 4.2 Spring Boot 版本升级
将父工程版本更新为 3.x 系列：
```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.4</version> <!-- 建议使用最新稳定版 -->
</parent>
```

### 4.3 依赖库升级 (关键)
Spring Boot 3 引入了重大变更，许多第三方库需要升级到适配版本：

| 依赖库 | 当前版本 (估计) | 目标版本 (建议) | 说明 |
| :--- | :--- | :--- | :--- |
| `mybatis-spring-boot-starter` | 2.3.1 | **3.0.3+** | 必须升级以支持 Spring Boot 3 |
| `druid` | 1.2.18 | **1.2.20+** | 建议使用 `druid-spring-boot-3-starter` |
| `knife4j` / Swagger | 3.0.3 | **4.4.0+** | Swagger 2 (Springfox) 不再支持，需迁移到 SpringDoc / Knife4j 4.x |
| `mysql-connector-j` | 8.0.33 | **8.3.0+** |  |
| `hutool` | 5.8.22 | **5.8.26+** | 保持最新 |

### 4.4 Jakarta EE 迁移 (javax.* -> jakarta.*)
Spring Boot 3 基于 Jakarta EE 10，所有 `javax.*` 包名需要替换为 `jakarta.*`。
- **涉及包**: Servlet, JPA (Persistence), Bean Validation, Annotation 等。
- **操作**: 全局替换 `import javax.` 为 `import jakarta.` (注意：`javax.sql.*` 等 JDK 自带包通常不需要替换)。

### 4.5 Spring Security 6 适配
Spring Boot 3 使用 Spring Security 6，配置方式有显著变化：
- 移除 `WebSecurityConfigurerAdapter`。
- 使用 `SecurityFilterChain` Bean 进行配置。
- `antMatchers` 变更为 `requestMatchers`。

### 4.6 配置文件变更
- 检查 `application.yml` 或 `application.properties` 中是否有废弃的配置项。
- Spring Boot 3 对 Redis、Spring MVC 等配置的前缀或属性名可能有所调整。

## 5. 验证计划
1. **编译检查**: 执行 `mvn clean compile`，修复所有编译错误（重点是 javax -> jakarta 和 Security 配置）。
2. **单元测试**: 运行 `mvn test`，确保所有单元测试通过。
3. **启动验证**: 启动 `eden-admin`, `eden-web` 等核心模块，检查启动日志是否有异常。
4. **接口测试**: 验证登录、商品查询、下单等核心接口功能正常。
5. **性能测试**: 简单压测对比升级前后的启动时间和接口响应时间。

## 6. 附录：常用资源
- [Spring Boot 3.0 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.0-Migration-Guide)
- [Spring Security 6.0 Migration Guide](https://docs.spring.io/spring-security/reference/migration/index.html)
