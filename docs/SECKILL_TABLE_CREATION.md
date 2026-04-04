# 秒杀模块数据库表创建说明 (Seckill Table Creation Report)

## 1. 概述与现状说明

根据业务规划、秒杀持久层 (`SeckillMapper`) 以及业务实现层 (`SeckillServiceImpl`) 对秒杀活动的逻辑诉求，已完成了秒杀活动基础表 `seckill` 的结构设计和 SQL 脚本生成。

当前新生成的建立表 SQL 文件位置在：`sql/seckill_table.sql`，可以直接在对应的 MySQL 业务数据库执行。

---

## 2. 表结构设计思路

基于现有的 `SeckillProduct` 实体和 Mapper.xml 取值，该表的主要字段设计如下：

### 2.1 核心字段说明
*   `id` (`BIGINT`, `AUTO_INCREMENT`): 秒杀活动主键。
*   `product_id` (`BIGINT`): 外键或关联列，关联 `product` 表的商品ID。
*   `seckill_price` (`DECIMAL(10,2)`): 秒杀期售价，精准到小数点后两位。
*   `stock` (`INT`): 具体分配到该活动当期的总库存量，支持负库存安全卡点 (`stock >= #{quantity}` 更新保护)。
*   `limit_per_user` (`INT`): 根据服务层设定限制单个用户的参与份数。
*   `start_time` / `end_time` (`DATETIME`): 秒杀开始/结束时间点。
*   `status` (`TINYINT`): 活动状态，业务代码写定为 `0`(未开始/待发生), `1`(进行中/生效), `2`(已结束 或被物理下架状态)。

### 2.2 索引设计 (Index Design)
为了支撑 `SeckillMapper` 中高频和复合的查询场景（例如查询已在进行中，或者即将开始的）：
1.  **`idx_product_id`**: 支撑管理端对某个单品排查它所有的秒杀排期或者查询重叠数 (`countOverlappingSeckill`)。
2.  **`idx_status_time` (`status`, `start_time`, `end_time`)**: 高效命中 `selectOngoing`, `selectUpcoming`, 和 `selectByTimeRange` 等联合范围检索场景。

---

## 3. 下一步建议

1.  **执行并验证**: 登录数据库控制台，执行 `sql/seckill_table.sql`，验证 `seckill` 表是否成功挂载。
2.  如果之前在 `docs/SQL_DATABASE_SCHEMA.md` 管理了其他 DDL，可以考虑将这段 SQL 也同步合并到那个总文档中去。

