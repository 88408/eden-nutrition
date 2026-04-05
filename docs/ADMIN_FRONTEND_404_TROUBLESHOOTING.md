# 前端请求 404 与图表警告故障排查说明

## 1. 接口 404 问题 (Failed to load resource: 404 Not Found)

### 问题描述
在访问管理后台时，浏览器控制台抛出了由于接口加载失败导致的 `404 Not Found`：
* `/admin/product/page?page=1&pageSize=100` -> 404
* `/admin/category/tree` -> 404

### 根本原因
后端 Spring Boot 应用程序的配置文件中显式配置了**统一上下文路径 (context-path)**：
```yaml
server:
  servlet:
    context-path: /api
```
这意味着后端所有的真实接口地址都会带上 `/api` 前缀。例如：`http://localhost:8080/api/admin/product/page`。

然而，前端 Vite 开发服务器通过代理（`vite.config.ts` 中的 `proxy`）拦截 `/admin` 路径并没有补全这个前缀：
```typescript
'/admin': {
  target: 'http://localhost:8080',
  changeOrigin: true,
}
```
此时前端发送请求到 `/admin/product/page`，Vite 代理转发到 `http://localhost:8080/admin/product/page`，后端找不着这个无 `/api` 的资源，从而导致 `404 错误`。

### 解决方案
我已经更新了前端 `eden-nutrition-admin-front/vite.config.ts` 中的代理规则，增加了代理的 `rewrite` 功能，以确保包含正确的上下文前缀：
```typescript
'/admin': {
  target: 'http://localhost:8080',
  changeOrigin: true,
  rewrite: (path) => `/api${path}`, // 自动在代理发往真实后端前补上 /api
}
```

---

## 2. Recharts 图表宽高警告 (The width(-1) and height(-1) of chart should be greater than 0)

### 问题描述
控制台报错：
> `The width(-1) and height(-1) of chart should be greater than 0...`

### 根本原因
由于网络接口 404，没有获取到后台数据，此时前端状态可能处于空页面渲染，或者仪表盘(`Dashboard`)页面的图表父容器在初次渲染时还没有明确的尺寸。`Recharts` 中 `<ResponsiveContainer>` 生成图表时的宽度与高度默认依赖其父级 div 进行自动伸缩计算。当所处 Flex 容器中或没提供定宽高的元素被渲染时会报此警告。

### 解决方案
可以忽略（这不影响图表的正常展示），或者通过给图表的父包装容器添加最小宽高约束来屏蔽它：
```tsx
// 示例修改在图表的包裹元素上
<div style={{ width: '100%', height: 300, minHeight: 300 }}>
  <ResponsiveContainer width="100%" height="100%">
    ...
  </ResponsiveContainer>
</div>
```
现在我们已经解决了数据的 404 获取问题，接下来这些图表便能正确渲染实际宽度与高度数据。