# 前端 3000 端口服务失联排查记录 (ERR_CONNECTION_REFUSED)

## 问题描述
在前端操作时，浏览器控制台抛出了如下网络报错：
```text
client:1035 WebSocket connection to 'ws://localhost:3000/' failed
POST http://localhost:3000/admin/seckill net::ERR_CONNECTION_REFUSED
```

## 原因分析
这是典型的前端本地开发服务器（Vite Dev Server）掉线导致的直观表现：
1. **端口 3000 拒绝连接**：我们的 `package.json` 中配置了 `vite --port=3000`，意味着整个前端入口以及其内置的 `/admin` 代理（Proxy 转发至后端 8080）均由运行在 3000 端口的进程负责。当你看到 `ERR_CONNECTION_REFUSED` 时，代表这个 3000 端口上已经没有任何应用在监听了。
2. **WebSocket failed**：`ws://localhost:3000/` 是 Vite 维系 HMR（热更新）的长链接，开发服务器终止时，浏览器会因失去宿主立刻抛出断开。

结合我们先前的终端记录：
```
Terminal: esbuild
Last Command: npm run dev
Exit Code: 1
```
以及多次编译器层面的类型报错，可以看出是当前**承载前端服务 `npm run dev` 的终端进程意外退出或是被手动杀死了**。

## 解决方案
当前不存在代码逻辑层面的严重故障，而是环境掉线。
你只需要在前端工程目录 `eden-admin-vue/eden-nutrition-admin-front` 下开启一个新终端，**重新运行启动命令**即可：

```bash
npm run dev
```

启动成功恢复监听后，刷新浏览器页面，所有积压的报错和连接池异常均可自然恢复正常。