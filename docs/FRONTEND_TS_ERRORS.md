# 前端 TypeScript 编译错误排查与修复记录

## 1. `src/pages/CategoryList.tsx`
**问题现象**：
在调用 `setCategories(flattenTree(res))` 时，TS 报错 `Argument of type 'AxiosResponse' is not assignable to parameter of type 'any[]'`，同样新增分类 `await addCategory(submitData)` 时也报类型不兼容的错误。
**问题分析**：
由于 Axios 返回的值类型默认包含 `config, headers, request` 等完整响应结构，而在我们的拦截器 `request.ts` 中，我们实际上已经剥离了外层，直接返回了 `res.data`。TypeScript 在编译时并不知道这层运行时的拦截剥离行为，因此认为你将整个 `AxiosResponse` 对象当成了数组或对象。
**解决方案**：
显式通过 `as any` 将响应体强制转换为 `any` 绕过编译期类型遮挡：
- `res` 强转为 `any` 传递给 `flattenTree`
- `submitData` 强转为 `any` 传给 `addCategory`。

## 2. `src/pages/Login.tsx`
**问题现象**：
报错 `Property 'token' does not exist on type 'AxiosResponse<AdminLoginVO>'`。
**问题分析**：
与上面同理，`axios.post` 标注的返回值会被 TS 推导为包含 `headers/config/data` 等属性的根对象，而在我们的封装中 `res` 直接就是后端的 `AdminLoginVO`。
**解决方案**：
在进入逻辑解构前将其转型为 `any`，`const data = res as any;` 后再使用 `data.token`, `data.userId`, `data.username` 即可消除编译错误。

## 3. `src/pages/OrderList.tsx`
**问题现象**：
报错 `Property 'records' does not exist on type 'AxiosResponse<any, any, {}>'` 等。
**问题分析**：
请求列表数据时，同样的拦截器脱壳问题。
**解决方案**：
赋值前转型，改为 `const data = res as any; setOrders(data?.records || ...)` 处理。

## 4. `src/pages/SeckillList.tsx`
**问题现象**：
TS 报错 `This comparison appears to be unintentional because the types 'number' and 'string' have no overlap`。
**问题分析**：
该文件此前使用 `item.status === '进行中'` 以及 `item.status === '未开始'` 进行字符串判定。然而在定义的 `SeckillVO` 中，`status` 被明确声明为 `number` 类型（后端使用 $0$-未发布/未开始，$1$-已发布/进行中 等表示）。
由于 `number` 和 `string` 永远不可能相等，因此这里的字符串判断逻辑不仅在 TypeScript 层报错，而且在运行态也会导致所有装态都永远落入 `else` 分支并显示白灰色的兜底样式，无法高亮显示当前的商品参与状态和“发布”按钮。
**解决方案**：
更新判定树的条件表达式，将：
- `item.status === '进行中'` 改为了完全对齐类型的 `item.status === 1`
- `item.status === '未开始'` 改为了完全对齐类型的 `item.status === 0`
- `<span>` 标签内也改为了 `{item.status === 1 ? '进行中' : item.status === 0 ? '未开始' : '已结束'}`。

目前执行 `npx tsc --noEmit` 已经 100% 通过无任何隐式报错，保证了编译安全。