# 商品状态更新 405 Method Not Allowed 报错排查报告

## 问题现象 (Issue)

在前端页面进行商品上下架切换时，控制台报错：
```
Failed to load resource: the server responded with a status of 405 (Method Not Allowed)
```
后端打印出日志 `WARN : 请求方法不支持: POST`。该错误表明前端发送请求的 Method 或路径不符合后端的定义。

## 根本原因 (Root Cause)

* 前端的 `updateProductStatus` 方法中定义的接口请求是 `POST /admin/product/status`，并将 `id` 和 `status` 放在了 Request Body。
* 而后端的 `AdminProductController.java` 中，所定义的修改商品上下架状态接口为 `@PutMapping("/status/{id}/{status}")`。
* 结果导致：前端发起 `POST` 请求，而后端只支持 `PUT`，同时 URL 传参格式也完全不匹配，进而引发 `405 Method Not Allowed` 且后端的路由解析也失败了。

## 解决方案 (Solution)

需要将前端请求修改为匹配后端定义的 `PUT` 请求以及 URL 路径参数。

修改了前端代码 `eden-nutrition-admin-front/src/api/product.ts` 中的 `updateProductStatus`：

**修改前：**
```typescript
export const updateProductStatus = (id: number, status: number) => {
  return request.post('/admin/product/status', { id, status });
};
```

**修改后：**
```typescript
export const updateProductStatus = (id: number, status: number) => {
  return request.put(`/admin/product/status/${id}/${status}`);
};
```

此修复已经完成，这使得前端直接调用符合后端预期的 URL（类似于 `PUT /admin/product/status/1/0`），成功实现修改商品上下架状态。