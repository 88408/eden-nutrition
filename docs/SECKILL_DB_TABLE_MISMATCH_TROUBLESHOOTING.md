# 后台保存后列表无数据显示的问题排查记录

## 问题描述
在解决完时区格式传递、SQL 库存(stock) `NULL` 校验异常后，前端页面终于成功收到了 `200 OK` 或者正常的添加成功提示。  
然而页面自动刷新获取秒杀列表时，刚刚成功添加的秒杀活动**不显示在列表中**，列表返回的数据依然是空。  
通过后端数据库执行日志观察到：
1. 保存操作时，执行的是：
   ```sql
   INSERT INTO seckill_product (...) VALUES (...)
   ```
2. 查询操作时，执行的是：
   ```sql
   SELECT ... FROM seckill s LEFT JOIN product p ON s.product_id = p.id
   ```
可以看出，插入数据和查询数据所指向的并不是同一张表（`seckill_product` vs `seckill`）。

## 原因分析
这是典型的持久层配置混乱，或者代码自动生成/人工补丁覆盖导致的错乱问题。  
本项目中对于 B 端管理员（后台管理界面）操作的接口统一定义在了 `SeckillServiceImpl` 的对应方法（如 `addAdminSeckill`，`getAdminPage` 等）。  

然而在业务层发现：
- 列表查询及状态统计使用的是 `seckillMapper`，其对应的 Mybatis Mapper `SeckillMapper.xml` 映射的数据库表名为 `seckill`。
- 执行后台的新增（`addAdminSeckill`）、修改、结束和删除等写入操作时，代码里错误调用并注入了 `seckillProductMapper`，其对应的 Mybatis Mapper `SeckillProductMapper.xml` 映射的数据库表名为 `seckill_product`。

由于数据被写入了 `seckill_product` 表，而读取总数、读取分页去查询的是 `seckill` 表。导致了“写在这个表，读从那个表”，出现了前后端联调时最经典的“幽灵数据”问题。

## 解决方案
**统一 B 端（管理员层面）的数据流流向**。由于在系统的底层 Mapper 和实体类配置中，`seckillMapper` 被设定为主表的映射器，我们在 `SeckillServiceImpl.java` 中把 B 端增删改的误写进行批量回指：

1. **修正查询详情：** `seckillProductMapper.selectById()` 替换为 `seckillMapper.selectById()`。
2. **修正新增：** `seckillProductMapper.insert()` 替换为 `seckillMapper.insert()`。
3. **修正修改：** `seckillProductMapper.update()` 替换为 `seckillMapper.update()`。
4. **修正状态更新与废弃：** `seckillProductMapper.updateStatus(...)` 替换为常规的对象重装更新：
   ```java
   SeckillProduct tempSp = new SeckillProduct();
   tempSp.setId(id);
   tempSp.setStatus(2);
   seckillMapper.update(tempSp); // 软拉或结束
   ```

已使用脚本完成全局修正并重新编译 `eden-service`。由于前后端目前逻辑结构均已梳理完毕，请**重启后端服务**，并在页面重新提交创建一遍秒杀活动，列表将会正常展现所建内容。