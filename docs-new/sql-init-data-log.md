[2026-05-31 22:25:16] 修改摘要
- 修改内容：根据当前 MySQL 表结构同步更新 sql 初始化脚本，调整商品字段类型、订单支付流水字段、秒杀活动与兼容秒杀商品表结构；将现有用户 admin、kang、yu 及已有地址写入初始化数据，并通过递归 CTE 生成最近 30 天内随机分散的 100 条订单和对应 100 条订单明细，同时为已有用户发放初始优惠券。
- 注意事项：订单初始化依赖 MySQL 8 递归 CTE 与 `RAND(n)` 固定种子，适合当前 Docker MySQL 8 环境；执行初始化脚本会重建表或插入显式 ID 数据，应在需要重置初始化数据时使用。

[2026-06-14 15:21:00] 补充数据库缺失字段
- 修改内容：
  - `order` 表新增 `payment_method` VARCHAR(50) 列，对应 Order 实体中已声明但无数据库列的 paymentMethod 字段
  - `seckill_order` 表新增 `order_no` VARCHAR(50)、`amount` DECIMAL(10,2)、`status` TINYINT 三列及 `idx_order_no` 索引，对齐 SeckillOrder 实体定义
  - `OrderMapper.xml` BaseResultMap 补充 `delivery_company`→`deliveryCompany` 映射（修复读取时 deliveryCompany 始终为 null 的 bug）
  - `OrderMapper.xml` BaseResultMap 和 Base_Column_List 补充 `payment_method` 列映射
  - `OrderMapper.xml` 的 `updateById` 和 `update` 语句修复 `paymentMethod` 字段映射目标 pay_type → payment_method（原为类型不匹配的 bug）
  - `SeckillOrderMapper.xml` BaseResultMap、Base_Column_List 和 INSERT 补充 order_no、amount、status 映射
  - 同步更新 04-order.sql、05-promotion.sql 与 schema.sql 保持一致
- 注意事项：新列均有 DEFAULT NULL 或 DEFAULT 0，不影响现有数据；paymentMethod 字段当前无业务代码写入，列为后续使用预留；deliveryCompany 修复后所有读订单操作将正确返回该字段。
