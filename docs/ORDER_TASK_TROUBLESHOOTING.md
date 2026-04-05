# 订单超时关闭任务报错排查报告 (Order Task Troubleshooting)

## 问题描述 (Issue Description)

在执行定时任务 `OrderTask.closeTimeoutOrders` 时，程序打印了如下异常：
```text
org.springframework.jdbc.BadSqlGrammarException: 
### Error querying database.  Cause: java.sql.SQLSyntaxErrorException: Unknown column 'delivery_company' in 'field list'
```
导致此报错的原因是 MyBatis 执行查询超时待关闭订单时，构建的 `SELECT` 语句包含 `delivery_company` 字段，但是目前所连接的 MySQL 数据库的 `order` 表中**不存在该字段**。

## 根本原因 (Root Cause)

* 代码中的实体类 `Order` 被增加了一些新的字段（比如实体类/Mapper XML 中加入了 `delivery_company`、`delivery_sn` 等），而在项目根目录下的 SQL 脚本中（如 `sql/04-order.sql`）也已经对表结构进行了更新。
* 遗憾的是，**已经处于运行状态的本地开发数据库（`eden_db`）尚未应用这些修改**。

由于程序运行时的代码已经跟最新的表结构对齐，导致数据库返回了 `Unknown column` 的语法异常。

## 解决方案 (Solution)

你需要对本地数据库中现有的 `order` 表进行结构更新。可以通过任意数据库客户端 （如 Navicat, DataGrip, VSCode 的 MySQL 插件等）执行以下 SQL 语句来追加遗露的列：

```sql
USE eden_db;

ALTER TABLE `order` 
ADD COLUMN `delivery_company` VARCHAR(50) DEFAULT NULL COMMENT '物流公司' AFTER `receive_time`,
ADD COLUMN `delivery_sn` VARCHAR(100) DEFAULT NULL COMMENT '物流单号' AFTER `delivery_company`,
ADD COLUMN `payment_method` VARCHAR(50) DEFAULT NULL COMMENT '支付方式' AFTER `pay_type`,
ADD COLUMN `order_type` TINYINT DEFAULT 0 COMMENT '订单类型：0-普通订单 1-秒杀订单 2-团购订单' AFTER `status`;
```

> **提示**：如果测试数据不重要，你也可以直接重新执行修改后的完整初始化脚本（`sql/init-all.sql` 或者 `sql/04-order.sql`）重新建表，但这会清空现有的订单数据。

## 验证 (Verification)

执行完上面的 SQL 后，等待一分钟，再次观察控制台的日志，`OrderTask` 的相关报错应当消失，任务能够正常查询并处理超时未支付的订单。
