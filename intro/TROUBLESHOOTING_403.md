403 Forbidden 错误分析报告
1. 问题描述
所有 API 请求（例如 /api/category/add、/api/product/list）均返回 403 Forbidden（禁止访问）。
该错误发生的原因是：后端接口要求身份认证，但前端未发送有效的 JWT 令牌（Token）。
2. 根本原因
🔹 后端缺少认证凭证
后端 SecurityConfig 配置了 anyRequest().authenticated()，即除登录/注册接口和静态资源外，所有请求均需认证。
🔹 前端缺少登录流程
管理前端项目（eden-admin-vue）此前未实现登录页面，也没有获取 Token 的逻辑。
🔹 请求缺失 Token
因此，前端发送 API 请求时未携带 Authorization: Bearer <token> 请求头，导致请求被后端拒绝。
3. 已实施的解决方案
我们为管理面板添加了完整的认证流程：
✅ 创建 src/api/user.ts
封装调用 /user/login 接口的辅助函数，用于用户登录并获取 Token。
✅ 创建 src/pages/Login.tsx
新增登录页面，已预填充默认管理员账号：
用户名：admin
密码：admin123
✅ 更新 src/App.tsx
新增 /login 路由，用于访问登录页；
添加 ProtectedRoute 组件：未认证用户访问受保护路由时，自动重定向至 /login；
将所有 /admin 管理路由包裹在 ProtectedRoute 中，实现路由级权限控制。
4. 验证步骤
1️⃣ 刷新管理页面
访问：http://localhost:3000
2️⃣ 自动跳转登录页
页面应自动重定向至 /login。
3️⃣ 点击「登录」按钮
使用默认凭证（已预填充）：
用户名：admin
密码：admin123
4️⃣ 登录成功后跳转
认证成功后，页面将自动重定向至管理后台仪表盘（Dashboard）。
5️⃣ 重试先前失败的操作
再次尝试添加商品分类。此时请求会自动携带有效 Token，操作应能成功执行。
💡 排查提示：
若登录后仍报 403，请按 F12 打开浏览器开发者工具，查看 Network 标签页中请求是否包含 Authorization 请求头；
确认后端服务是否正常运行，且 /user/login 接口可正常返回 Token；
检查本地存储（localStorage/sessionStorage）中是否正确保存了 Token。
