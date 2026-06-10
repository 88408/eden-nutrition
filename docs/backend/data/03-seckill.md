# 数据字典：seckill

## 表信息

| 表名 | 业务说明 | 主键策略 | 审计字段说明 |
|---|---|---|---|
| seckill | 秒杀活动表，记录商品的秒杀价格、库存及时间窗口 | id BIGINT AUTO_INCREMENT | create_time: 创建时间；update_time: 更新时间（ON UPDATE CURRENT_TIMESTAMP） |

## 字段清单

| 字段名 | 数据类型 | 约束(NOT NULL/UNIQUE等) | 默认值 | 业务说明 | 索引类型 |
|---|---|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 秒杀ID | PRIMARY |
| product_id | BIGINT | NOT NULL | - | 商品ID（关联 product.id） | BTREE(idx_product_id) |
| seckill_price | DECIMAL(10,2) | NOT NULL | - | 秒杀价格 | - |
| stock | INT | NOT NULL | - | 秒杀库存（用于并发扣减） | - |
| limit_per_user | INT | - | 1 | 每人限购数量 | - |
| start_time | DATETIME | NOT NULL | - | 秒杀开始时间 | BTREE(idx_start_time) |
| end_time | DATETIME | NOT NULL | - | 秒杀结束时间 | - |
| status | TINYINT | - | 0 | 状态：0-未开始 1-进行中 2-已结束 | BTREE(idx_status) |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 | - |
| update_time | DATETIME | - | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 | - |

## 外键关联说明

- `product_id` -> `product.id`（商品表）[推断]
- 秒杀订单表 `seckill_order.seckill_id` -> `seckill.id` [存在唯一索引用于防重复]

## 枚举字段取值字典

### status

| 值 | 含义 |
|---|---|
| 0 | 未开始 |
| 1 | 进行中 |
| 2 | 已结束 |

## 索引设计说明

- `idx_product_id(product_id)`：用于按商品查询秒杀活动，便于后台管理和检测冲突（SeckillMapper.countOverlappingSeckill）。
- `idx_start_time(start_time)`：用于查询短期内即将开始或正在进行的活动（selectUpcoming/selectOngoing）。
- `idx_status(status)`：用于快速过滤有效活动（status=1）。

## 字段来源

- DDL：sql/05-promotion.sql、sql/schema.sql
- MyBatis：eden-mapper/src/main/resources/mapper/SeckillMapper.xml
- 实体注释：eden-pojo/src/main/java/eden/pojo/SeckillProduct.java

---

本文档由 AI 基于当前代码结构推导，实际以数据库 DDL 为准
