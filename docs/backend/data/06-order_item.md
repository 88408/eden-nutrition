# 数据字典：order_item

## 表信息

| 表名 | 业务说明 | 主键策略 | 审计字段说明 |
|---|---|---|---|
| order_item | 订单明细表，保存订单内每个商品的快照信息（名称、图片、价格、数量） | id BIGINT AUTO_INCREMENT | create_time: 创建时间 |

## 字段清单

| 字段名 | 数据类型 | 约束(NOT NULL/UNIQUE等) | 默认值 | 业务说明 | 索引类型 |
|---|---|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 明细ID | PRIMARY |
| order_id | BIGINT | NOT NULL | - | 订单ID（关联 order.id） | BTREE(idx_order_id) |
| order_no | VARCHAR(50) | NOT NULL | - | 订单编号（快照） | BTREE(idx_order_no) [推断] |
| product_id | BIGINT | NOT NULL | - | 商品ID（关联 product.id） | BTREE(idx_product_id) |
| product_name | VARCHAR(200) | NOT NULL | - | 商品名称快照 | - |
| product_image | VARCHAR(500) | - | NULL | 商品图片快照 | - |
| price | DECIMAL(10,2) | NOT NULL | - | 商品单价（下单时快照） | - |
| quantity | INT | NOT NULL | - | 购买数量 | - |
| total_price | DECIMAL(10,2) | NOT NULL | - | 小计金额（price * quantity） | - |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 | - |

## 外键关联说明

- `order_id` -> `order.id`（所属订单）[存在索引]
- `product_id` -> `product.id`（商品引用，快照记录保留历史信息）[推断]

## 索引设计说明

- `idx_order_id(order_id)`：用于按订单批量查询明细（OrderItemMapper.selectByOrderId）。
- `idx_product_id(product_id)`：用于按商品维度检索历史订单项（如退货统计、销量回溯）。

## 字段来源

- DDL：sql/04-order.sql
- MyBatis：eden-mapper/src/main/resources/mapper/OrderItemMapper.xml
- 实体注释：eden-pojo/src/main/java/eden/pojo/OrderItem.java

---

本文档由 AI 基于当前代码结构推导，实际以数据库 DDL 为准
