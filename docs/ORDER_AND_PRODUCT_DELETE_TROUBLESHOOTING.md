# 删除商品及订单消息监听导致报错的排查记录

## 问题一：管理端删除商品报 405 Method Not Allowed
**错误现象**：  
前端发起请求 `DELETE /admin/product?id=5`，后端响应 `405 Method Not Allowed`，表示当前接口不支持该请求方法。  

**原因分析**：  
查看后端 `AdminProductController.java`，删除商品的接口定义为 `@DeleteMapping("/{id}")`。该写法要求发起 RESTful 的路径传参形式（即 `DELETE /admin/product/5`）。而在前端 `product.ts` 中使用的是：
```typescript
export const deleteProduct = (id: number) => {
  return request.delete('/admin/product', { params: { id } });
};
```
该写法会将 `id` 拼接在查询字符串上，最终构建出的是 `/admin/product?id=5` 的请求路径，因此未能匹配到后端声明的接口并报 `405` 错。

**解决方案**：  
修改前端请求代码为：
```typescript
export const deleteProduct = (id: number) => {
  return request.delete(`/admin/product/${id}`);
};
```
此方案可以直接匹配后端 `@DeleteMapping("/{id}")`，商品删除功能可恢复正常。

---

## 问题二：处理订单支付成功消息抛出 NumberFormatException
**错误现象**：  
RabbitMQ 监听模块 `OrderMessageListener` 处理支付成功回调时遇到以下致命异常：
```java
java.lang.NumberFormatException: For input string: "2025121713395639540001"
```

**原因分析**：  
异常信息表明，`OrderMessageListener.java` 在收到队列消息后将消息体解析成了长整型：
```java
Order order = orderMapper.selectById(Long.parseLong(orderId));
```
然而实际上，由于雪花算法或订单生成的时序字符串太长，订单号 `"2025121713395639540001"` 是 22 位的，远远超出了 Java `Long` 类型能够承载的最大值范围（19位，最高 9223372036854775807）。  
其实这是一个非常常见的混淆问题：消息队列中传递的 `orderId` 在这里其实代表了业务订单号（`order_no`，字符串类型），并不是自增数据库主键 `id`（Long 类型）。

**解决方案**：  
修复后端的 `OrderMessageListener.java` 监听逻辑，不再做强制转化类型并在查询时使用 `orderNo` 代替原先错误的 `id` 查询。

修复后代码如下：
```java
@RabbitListener(queues = MQConstants.ORDER_PAY_SUCCESS_QUEUE)
public void handleOrderPaySuccess(String orderNo) {
    logger.info("收到订单支付成功消息，订单号: {}", orderNo);
    try {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            logger.warn("订单不存在，订单号: {}", orderNo);
            return;
        }

        logger.info("订单 {} 支付成功，金额: {}", orderNo, order.getPayAmount());
    } catch (Exception e) {
        ...
    }
}
```

以上后端模块已修复并重新编译，前端代码也已保存。前端支持热更新无需操作，后端服务请**自行重启**即可恢复正常的商品删除操作及接收异步队列支付消息的功能。