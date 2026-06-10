# 数据字典：order

## 表信息

| 表名 | 业务说明 | 主键策略 | 审计字段说明 |
|---|---|---|---|
| order | 订单主表，记录用户下单、支付、发货与收货等流程信息 | id BIGINT AUTO_INCREMENT | create_time: 下单时间；update_time: 更新时间（ON UPDATE CURRENT_TIMESTAMP） |

## 字段清单

| 字段名 | 数据类型 | 约束(NOT NULL/UNIQUE等) | 默认值 | 业务说明 | 索引类型 |
|---|---|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 订单ID | PRIMARY |
| order_no | VARCHAR(50) | NOT NULL, UNIQUE | - | 订单编号（业务单号） | UNIQUE(idx_order_no) |
| user_id | BIGINT | NOT NULL | - | 下单用户ID（关联 user.id） | BTREE(idx_user_id) |
| total_amount | DECIMAL(10,2) | NOT NULL | - | 订单总金额（包含运费、优惠前） | - |
| pay_amount | DECIMAL(10,2) | NOT NULL | - | 实付金额（实际支付） | - |
| freight_amount | DECIMAL(10,2) | - | 0 | 运费 | - |
| discount_amount | DECIMAL(10,2) | - | 0 | 优惠总额（券/活动等） | - |
| coupon_id | BIGINT | - | NULL | 使用的优惠券ID（关联 coupon.id） | BTREE(idx_coupon_id) [推断] |
| pay_type | TINYINT | - | NULL | 支付类型：1-支付宝 2-微信 | - |
| payment_method | VARCHAR(50) | - | NULL | 支付方式描述（兼容字段） | - |
| status | TINYINT | - | 0 | 订单状态：0-待支付 1-已支付 2-已发货 3-已完成 4-已取消 5-已退款 | BTREE(idx_status) |
| order_type | TINYINT | - | 0 | 订单类型：0-普通 1-秒杀 2-团购 | - |
| receiver_name | VARCHAR(50) | NOT NULL | - | 收货人姓名 | - |
| receiver_phone | VARCHAR(20) | NOT NULL | - | 收货人电话 | - |
| receiver_address | VARCHAR(500) | NOT NULL | - | 收货地址（快照） | - |
| remark | VARCHAR(500) | - | NULL | 订单备注 | - |
| pay_time | DATETIME | - | NULL | 支付时间 | - |
| ship_time | DATETIME | - | NULL | 发货时间 | - |
| receive_time | DATETIME | - | NULL | 收货时间 | - |
| delivery_company | VARCHAR(50) | - | NULL | 物流公司 | - |
| delivery_sn | VARCHAR(100) | - | NULL | 物流单号 | - |
| complete_time | DATETIME | - | NULL | 订单完成时间 | - |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 | - |
| update_time | DATETIME | - | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 | - |

## 外键关联说明

- `user_id` -> `user.id`（下单用户）[推断]
- `coupon_id` -> `coupon.id`（使用的优惠券）[推断]
- 订单明细表 `order_item.order_id` -> `order.id`（订单下的明细）[存在索引]

## 枚举字段取值字典

### status

| 值 | 含义 |
|---|---|
| 0 | 待支付 |
| 1 | 已支付 |
| 2 | 已发货 |
| 3 | 已完成 |
| 4 | 已取消 |
| 5 | 已退款 |

### pay_type

| 值 | 含义 |
|---|---|
| 1 | 支付宝 |
| 2 | 微信 |

### order_type

| 值 | 含义 |
|---|---|
| 0 | 普通订单 |
| 1 | 秒杀订单 |
| 2 | 团购订单 |

## 索引设计说明

- `idx_user_id(user_id)`：支持按用户查询订单（OrderMapper.selectByUserId）。
- `idx_order_no(order_no)`：支持按业务单号检索订单（selectByOrderNo）。
- `idx_status(status)`：支持按订单状态的筛选与定时任务处理（selectTimeoutOrders 等）。
- `idx_create_time(create_time)`：支持按下单时间的范围查询与统计。

## 字段来源

- DDL：sql/04-order.sql
- MyBatis：eden-mapper/src/main/resources/mapper/OrderMapper.xml
- 实体注释：eden-pojo/src/main/java/eden/pojo/Order.java

---

本文档由 AI 基于当前代码结构推导，实际以数据库 DDL 为准
