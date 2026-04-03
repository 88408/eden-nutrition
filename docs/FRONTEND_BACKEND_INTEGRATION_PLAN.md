# Eden Nutrition - 管理后台前后端联调联通方案 (Phase 4)

**文档状态**: 已规划完成
**受众**: 前端工程师 (React / Vite) / 后端研发

---

## 一、 现状与架构差异分析

目前我们已完成了后端管理端接口在 `eden-admin` 模块的开发（包括商品、订单、秒杀模块），并集成了标准接口文档 `ADMIN_API_DOCUMENTATION.md`。而前端项目（位于 `eden-admin-vue/eden-nutrition-admin-front`）采用的是 **React + Vite + TypeScript** 技术栈。

通过对此前前端暂定的契约（`api-front.txt`）与实际后端已实现的 API 规范进行对比，发现需要进行以下核心适配：

1. **响应结构差（核心）**：
   - 之前前端预期：直接返回数据阵列 `[ { ... } ]` 或 `{ list: [], total: 100 }`。
   - 实际后端规范：使用泛型包装类 `Result<T>`。所有成功的请求均返回：`{ "code": 200, "message": "操作成功", "data": ... }`。
2. **路由路径前缀差**：
   - 之前前端预期：使用 `/seckills`，`/products`。
   - 实际后端规范：使用 `/admin/seckill/...`，`/admin/product/...`。
3. **鉴权 Header 校验**：前端需要在所有接口头部带入由后台发放的 JWT Token。

本方案旨在指导如何通过前端基建的改造，将两端进行完美贴合。

---

## 二、 网络层与跨域配置 (Vite 代理)

为解决本地联调时的通过浏览器的跨域 (CORS) 限制，需调整前端 `vite.config.ts` 文件，将所有的 `/admin` 或 `/api` 开头的请求代理到 Spring Boot 后端服务（默认端口 `8080`）。

**修改 `vite.config.ts`：**
```typescript
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      '/admin': {
        target: 'http://localhost:8080', // 指向 Spring Boot 本地服务
        changeOrigin: true,
        // 如果后端接口没有 /admin 前缀则需要 rewrite，但我们的后端有 /admin，故无需重写
      },
      '/api': {
         target: 'http://localhost:8080',
         changeOrigin: true,
      }
    }
  }
});
```

---

## 三、 全局请求库 (Axios) 封装改造

对应前端的 `src/api/request.ts` 需要进行结构剥离改造，使其能够：
1. 自动注入 `Authorization: Bearer <token>`。
2. 自动扒掉后端的 `{ code, message, data }` 外衣，直接向业务层抛出 `data`。
3. 全局拦截异常。

**改造 `src/api/request.ts` 核心逻辑：**
```typescript
import axios from 'axios';
import useAuthStore from '../store/useAuthStore'; // 假设使用了 zustand 或类似状态管理

const request = axios.create({
  baseURL: '', // 由 Vite proxy 接管
  timeout: 10000,
});

// 请求拦截器：注入 Token
request.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token && config.headers) {
    config.headers['Authorization'] = `Bearer ${token}`;
  }
  return config;
});

// 响应拦截器：剥离 Result 包装
request.interceptors.response.use(
  (response) => {
    const res = response.data;
    // 后端约定的成功 Code 为 200
    if (res.code === 200) {
      return res.data; // 直接将 data 扔给页面组件
    }
    
    // 处理 Token 失效
    if (res.code === 401) {
       useAuthStore.getState().logout();
       window.location.href = '/login';
    }
    
    // 其他业务异常抛出
    alert(res.message || '系统错误');
    return Promise.reject(new Error(res.message || 'Error'));
  },
  (error) => {
    alert(error.response?.data?.message || '网络或服务器错误');
    return Promise.reject(error);
  }
);

export default request;
```

---

## 四、 接口定义与模型 (TypeScript 定义)

以刚完成的秒杀模块 (Phase 3) 为例，在 `src/api/seckill.ts` 中声明所有接口：

```typescript
import request from './request';

// 1. 类型抽取 (完全对齐后端的 AdminSeckillVO 与 AdminSeckillSaveDTO)
export interface SeckillVO {
  id: number;
  productId: number;
  productName: string;
  productMainImage: string;
  seckillPrice: number;
  stock: number;
  limitPerUser: number;
  status: number;
  startTime: string;
  endTime: string;
}

export interface PageResult<T> {
  total: number;
  pages: number;
  list: T[];
}

export interface SeckillQuery {
  page?: number;
  pageSize?: number;
  productId?: number;
  status?: number;
}

// 2. 接口封装 (全面对齐 ADMIN_API_DOCUMENTATION.md)
export const getSeckillPage = (params: SeckillQuery) =>
  request.get<any, PageResult<SeckillVO>>('/admin/seckill/page', { params });

export const getSeckillDetail = (id: number) =>
  request.get<any, SeckillVO>(`/admin/seckill/${id}`);

export const addSeckill = (data: Partial<SeckillVO>) =>
  request.post<any, void>('/admin/seckill', data);

export const updateSeckill = (data: Partial<SeckillVO>) =>
  request.put<any, void>('/admin/seckill', data);

export const deleteSeckill = (id: number) =>
  request.delete<any, void>(`/admin/seckill/${id}`);

export const finishSeckill = (id: number) =>
  request.put<any, void>(`/admin/seckill/finish/${id}`);
```

---

## 五、 组件页面联调实战路线 (React 侧)

接下来在 `src/pages/SeckillList.tsx` 中的开发顺序如下：

1. **列表渲染钩子**：
   引入 `useEffect` 和提取好的 `getSeckillPage` 函数，请求参数默认为 `page: 1, pageSize: 10`。
   解构拿到 `list` 直接传给底层的 UI `Table` 组件（并绑定 `productMainImage` 作缩略图展示），将 `total` 传给分页组件。

2. **状态翻译**：
   在 Table 的 render 中，利用 Mapper 把 `status: 0` 转为 "未开始", `1` 转为 "进行中", `2` 转为 "已结束"。

3. **抽屉/模态框编辑 (Modal/Form)**：
   - 点击**[新增]**，直接装载空白表单，收集 `productId`、`seckillPrice` 等。
   - 提交时遇到后端防重校验抛出时（如报时间重叠），前置拦截器拦截后 `alert` 会自动弹出后端的错误信息提示，无需单独处理异常弹窗。

4. **快捷中止活动**：
   对处于 `status: 1` 的行显示 [强制结束] 按钮，点击调用 `ConfirmModal` 二次确认后发送 `finishSeckill(id)` 请求。

## 六、 联调工作流建议

为保障接下来的 Phase 4 工作顺利进行，推荐操作路径：
1. **启动应用**：后台开启 Redis + MySQL，使用 `mvn spring-boot:run` 启动 `eden-admin`。前端运行 `npm run dev` (Vite)。
2. **打通鉴权登录**：确保 `Login.tsx` 中的管理员登录逻辑能获取到 Token。
3. **开始跑通增删改查**：直接依据上述 API TS 文件开展前端页面的渲染改造。