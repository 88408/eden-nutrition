404 错误分析与解决方案
1. 问题描述
症状：
前端请求：POST http://localhost:3000/api/category/add 返回 404 Not Found
Axios 报错：Request failed with status code 404
2. 根本原因分析
2.1 后端配置（application.yml）
Spring Boot 后端配置了特定的上下文路径（context path）：
yaml
123
server:
  servlet:
    context-path: /api
这意味着所有后端接口路径都需要以 /api 为前缀。
例如，分类控制器（Category Controller）的实际地址为：http://localhost:8080/api/category/...
2.2 前端代理配置（vite.config.ts）
Vite 开发服务器配置了路径重写规则：
typescript
1234567
proxy: {
  '/api': {
    target: 'http://localhost:8080',
    changeOrigin: true,
    rewrite: (path) => path.replace(/^\/api/, ''), // <--- 问题所在
  },
}
rewrite 函数会在请求转发给后端之前，将路径中的 /api 前缀移除。
2.3 冲突分析
浏览器发送请求：http://localhost:3000/api/category/add
Vite 代理匹配到 /api 路径
重写规则将路径修改为：/category/add（移除了 /api）
转发后的请求：http://localhost:8080/category/add
后端接收到的请求路径为 /category/add
后端响应：404 Not Found（因为后端期望的路径是 /api/category/add）
3. 解决方案
移除 vite.config.ts 中的路径重写规则。
移除 rewrite 后，代理将完整转发原始路径：
浏览器：http://localhost:3000/api/category/add
Vite 代理：匹配到 /api
转发后的请求：http://localhost:8080/api/category/add
后端：成功匹配上下文路径 /api 和控制器路径 /category/add → 请求成功 ✅
4. 实施步骤
更新 eden-admin-vue/vite.config.ts 文件：
typescript
1234567
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          // rewrite: (path) => path.replace(/^\/api/, ''), // 已移除
        },
      },
5. 后续操作
重启前端开发服务器（执行 npm run dev）以使配置变更生效
重新尝试"添加分类"操作，验证问题是否解决
💡 小贴士：在开发环境中，建议保持前端请求路径与后端 context-path 一致，避免额外的路径重写逻辑，可减少此类 404 问题的发生。生产环境部署时，请确保 Nginx 或其他反向代理也正确配置了路径转发规则。
