# Eden Nutrition Admin - 认证模块(Login)真实API接入与开发方案

## 1. 背景与现状痛点
根据目前前端迁移的进度，**认证模块 (`Login.tsx`)** 处于 **⚠️ 阻塞 / 暂未替换** 状态。
**核心原因**：`eden-admin-vue/eden-nutrition-admin-front` 目前依赖本地硬编码的逻辑生成假 JWT (Mock Token)。而在后端工程结构中，现有的 JWT Security 认证、权限过滤器（如 `JwtAuthenticationTokenFilter`、`SecurityConfig`）以及登录控制器（如 `UserController.java`）全部集中在买家端工程 `eden-web` 中，**专用于管理后台的 `eden-admin` 工程没有任何认证与登录相关的 Controller 和配置**。如果我们直接前端放开请求，会面临 404 (无接口) 和 401 (无鉴权) 的错误。

本方案旨在为 `eden-admin` 工程设计一套安全、独立且与现有生态相兼容的管理员认证模块开发计划。

---

## 2. 后端开发方案 (`eden-admin` & `eden-service` 改造)

### 2.1 鉴权架构设计 (Security / 拦截器)
由于买家端(`eden-web`)已经实现了一套 Spring Security + JWT 体系，`eden-admin` 在安全控制上推荐以下两种路径（建议采用**路径B**以保持强管控与代码隔离）：
- **路径A (组件下沉复用)**：将 `eden-web` 中的安全配置包 (`eden.web.security`、`eden.web.filter`) 下沉转移至 `eden-common`，以便 `eden-admin` 进行复用。
- **路径B (Admin专用拦截器或轻型Security)**：保持独立性，在 `eden-admin` 代码中单独实现一个 `AdminAuthInterceptor` 或者复制专门针对管理员路由的 `AdminSecurityConfig` 和 `AdminJwtFilter`。

### 2.2 数据库与实体层适配 (`eden-pojo` & `eden-mapper`)
- 检查 `user` 表是否有区分“普通用户”与“管理员”的字段 (例如 `role_id`、`is_admin`或 `type` 字段)。
- 如果没有该字段，需要通过 SQL 脚本 (如 `02-user.sql`) 对 `user` 表进行扩充：`ALTER TABLE user ADD COLUMN role TINYINT DEFAULT 0 COMMENT '0-普通用户 1-后台管理员';`
- 实体类重用：输入参数可继续使用 `eden-pojo` 包中的 `LoginDTO`，返回数据重用 `LoginVO` 和 `Result`。

### 2.3 业务服务层 (`eden-service/UserService`)
在 `UserService.java` 以及 `UserServiceImpl.java` 中新增专属的管理员登录校验逻辑：
```java
// 区别于普通用户登录，校验时增加角色查验过滤
LoginVO adminLogin(LoginDTO loginDTO);
UserVO getAdminUserInfo(Long adminId);
```
**逻辑流程**：
1. 接收 `username` 和 `password`。
2. 依据 `username` 及其 `role = 1 (管理员)` 标识查询 `User` 记录。如果不存在或密码不匹配抛出异常。
3. 利用 `eden-common` 下现有的 `JwtUtils.java` 签发载有 `adminId` 的 Token 串。
4. (可选) 将 Token 或者管理员会话状态预存至 Redis 以实现单点登录/多端被顶功能 (`RedisConstants.java`)。

### 2.4 控制器层API定义 (`eden-admin`)
在 `eden-admin/src/main/java/eden/admin/controller/` 目录下创建 `AdminUserController.java`。提供如下基础接口：

| 方法 | 接口路径 | 载荷要求 | 功能描述 |
|---|---|---|---|
| `POST` | `/admin/user/login` | `LoginDTO` | 管理员账号密码登录，下发 JWT token。 |
| `GET` | `/admin/user/info` | Head: `Authorization` | 获取当前登录管理员的详细信息和权限集。 |
| `POST` | `/admin/user/logout` | Head: `Authorization` | 退出登录，注销/清除 Redis 内的临时缓存会话。 |

---

## 3. 前端对接方案 (`eden-nutrition-admin-front` 改造)

待后端接口部署测试通过后，即可解封前端进度。

### 3.1 增加 Auth 专属 API 请求模块
新建 `src/api/auth.ts`，利用已有的 `request.ts` 拦截器：
```typescript
import request from './request';

// 定义类型
export interface AdminLoginDTO { username: string; password: string; }
export interface AdminLoginVO { token: string; id: number; username: string; }

export const adminLogin = (data: AdminLoginDTO) => {
  return request.post<AdminLoginVO>('/admin/user/login', data);
};

export const getAdminInfo = () => {
  return request.get<any>('/admin/user/info');
};
```

### 3.2 改造全局状态管理 (`src/store/useAuthStore.ts`)
替换当前依赖硬编码验证的方式，改用 API 调用更新内部状态。确保 `token` 及时持久化至 `localStorage`。

### 3.3 改造登录页组件 (`src/pages/Login.tsx`)
1. 移除文件中的假账号检验 (`if(username === 'admin' && pass === '123456') ...`)。
2. 将点击“登录”按钮的流程替换为 `try-catch` 包裹的 `adminLogin` 方法：
    - 等待服务器返回结果。
    - 将返回的真实 token 设置给全局状态。
    - 如果服务器返回 401 或报错（如密码错误），则向用户丢出对应的 `toast.error(message)` 冒泡。
    - 成功后利用 React Router `navigate('/')` 重定向到后台管理主页。

---

## 4. 实施推进时间线与步骤

* **步骤 1 (后端 DBA & 实体)**：检查数据库结构，引入角色权限概念（用于识别前端登录请求是否为真管理员）。
* **步骤 2 (后端 API)**：在 `eden-admin` 与 `eden-service` 内全面构建 `/admin/user/login` 与 Auth 拦截链路。确保 Postman 调测畅通。
* **步骤 3 (前端 联调)**：创建前端 `src/api/auth.ts`，对接真实接口重构 `Login.tsx` 中的处理闭包。
* **步骤 4 (清理文档)**：更新 `docs/MOCK_REPLACEMENT_PROGRESS.md`，将认证模块状态标记为 `✅ 已完成`。
