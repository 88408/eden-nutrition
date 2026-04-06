# 秒杀活动保存时 stock 字段为 null 异常排查记录

## 问题描述
在解决完时间格式（DateTimeParseException）的问题后，再次在管理后台提交（POST/PUT）秒杀活动，服务器仍返回 `500 Internal Server Error`。  
查看 Spring Boot 后端日志发现如下错误：
```
java.sql.SQLIntegrityConstraintViolationException: Column 'stock' cannot be null
### SQL: INSERT INTO seckill_product (product_id, seckill_price, stock, start_time, end_time, status, create_time, update_time) VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())
```

## 原因分析
这是典型的后端 DTO 到实体类（Entity）的对象拷贝遗漏问题。  
前端提交过来的数据中，秒杀件数使用的是 `stockCount` 字段。后端接口接收其参数的 DTO `AdminSeckillSaveDTO` 定义的字段名也确实是 `stockCount`：
```java
private Integer stockCount;
```  
但在后端执行插入数据库前，业务层（`SeckillServiceImpl`）调用了 `BeanUtils.copyProperties(dto, seckillProduct)` 方法进行对象属性拷贝。

由于业务对应的数据库实体类 `SeckillProduct` 虽然兼容保留了 `stockCount` 字段，但在 MyBatis 的 XML 配置文件 `SeckillProductMapper.xml` 的 `INSERT` 原生 SQL 中，实际真正插入的列映射字段叫作 `stock`（`<result column="stock" property="stock"/>`）。  

`BeanUtils.copyProperties` 只能将同名属性映射，也就是把 DTO 的 `stockCount` 拷贝给了实体的 `stockCount`，但实体中用于入库的核心字段 `stock` 被落下了（值为 null）。数据库表设计中 `stock` 列不允许为空（`NOT NULL`），从而导致 MyBatis 执行 `INSERT` 或 `UPDATE` 语句时抛出了 `DataIntegrityViolationException`。

## 解决方案
**修复后端业务层对库存字段的显式赋值：**

在 `SeckillServiceImpl` 的 `addAdminSeckill`（新增）和 `updateAdminSeckill`（修改）方法中，在拷贝完普通属性后，增加了对 `stock` 字段的手动设置逻辑。

```java
// 修改前
SeckillProduct sp = new SeckillProduct();
BeanUtils.copyProperties(dto, sp);
sp.setCreateTime(LocalDateTime.now());
// => MyBatis 直接去读 sp.getStock() 结果为 null，触发 SQL 异常

// 修改后
SeckillProduct sp = new SeckillProduct();
BeanUtils.copyProperties(dto, sp);
if (dto.getStockCount() != null) {
    sp.setStock(dto.getStockCount()); // 将前端传入的 stockCount 值手动赋予即将入库的 stock 字段
}
sp.setCreateTime(LocalDateTime.now());
```

另外，我们一并修复了该 Service 中的 `initSeckillStock` （预热库存到 Redis）的方法，使其向下兼容读取库存。

目前项目已经重新编译成功。请重新运行后端应用，并再次在页面中点击保存。这次表单可以顺利保存到数据库中了！