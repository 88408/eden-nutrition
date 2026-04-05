# 管理员后台登录后又跳转回登录界面的问题排查报告

## 问题现象 (Issue)

在 `eden-admin-vue` (商家后台) 输入正确的账号密码点击登录时，显示“登录成功”但页面却立刻（或者在很短时间内）重新跳回了 `/login` 登录界面，有时还会伴随“登录已过期，请重新登录”的 Toast 提示。

## 根本原因 (Root Cause)

问题的核心在于 **Spring Security 权限拦截规则与 Axios 全局拦截器的相互作用**。

1. **工程结构**：`eden-admin` (后台服务) 在 `pom.xml` 中引入了 `eden-web` (C端服务) 作为依赖，并在其启动类使用了 `@SpringBootApplication(scanBasePackages = "eden")`。这导致 `eden-web` 模块中的 `SecurityConfig` 配置也被加载并生效在了后台服务中。
2. **Security 拦截配置**：在原先的 `SecurityConfig.java` 中，针对匿名接口只放行了 `/user/login` 和 `/user/register`：
   ```java
   .antMatchers("/user/login", "/user/register").permitAll()
   .anyRequest().authenticated();
   ```
   它**遗漏了** `/admin/user/login` (管理员登录接口)。
3. **前端跳回逻辑**：
   * 当前端发送 `/admin/user/login` 请求时，因为未携带 Token ，触发了 Spring Security 的 `403 Forbidden` 或 `401 Unauthorized` 拦截拦截。
   * 前端 `request.ts` 的响应拦截器一旦接收到 `401 / 403` 状态码，便会直接执行：
     ```typescript
     localStorage.removeItem('token');
     window.location.href = '/login';
     ```
   * 结果，尽管前端刚尝试发起登录动作，却立刻因为 Security 的拦截触发了清除 Token 并回到登录页的重定向操作。

## 解决方案 (Solution)

在 `eden-web` 模块下的 `eden.web.config.SecurityConfig` 里面，将管理员的登录接口 `/admin/user/login` 也加入到 `permitAll()` 放行列表中即可。

```java
// 修改前
.antMatchers("/user/login", "/user/register").permitAll()

// 修改后
.antMatchers("/user/login", "/user/register", "/admin/user/login").permitAll()
```

已通过工具调用对该代码进行了修复。现在在商家后台即可正常登录并进入 `Dashboard` 页面，不再被重复踢回登录页。