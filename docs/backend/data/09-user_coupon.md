# 数据字典：user_coupon

## 表信息

| 表名 | 业务说明 | 主键策略 | 审计字段说明 |
|---|---|---|---|
| user_coupon | 用户持有的优惠券记录表，记录用户领取/使用的优惠券状态与使用时间 | id BIGINT AUTO_INCREMENT | create_time: 创建时间 |

## 字段清单

| 字段名 | 数据类型 | 约束(NOT NULL/UNIQUE等) | 默认值 | 业务说明 | 索引类型 |
|---|---|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 记录ID | PRIMARY |
| user_id | BIGINT | NOT NULL | - | 用户ID（关联 `user.id`） | BTREE(idx_user_id) |
| coupon_id | BIGINT | NOT NULL | - | 优惠券ID（关联 `coupon.id`） | BTREE(idx_coupon_id) |
| status | TINYINT | - | 0 | 使用状态：0-未使用 1-已使用 2-已过期 | - |
| used_time | DATETIME | - | NULL | 使用时间（若已使用则有值） | - |
| order_id | BIGINT | - | NULL | 使用时关联的订单ID（order.id） | - |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 记录创建时间（领取时间） | - |

## 外键关联说明

- `user_id` -> `user.id`（用户表）[推断：DDL 未声明外键约束，但 Mapper/业务逻辑视为关联]
- `coupon_id` -> `coupon.id`（优惠券表）[推断]
- `order_id` -> `order.id`（使用优惠券时记录的订单）[推断]

## 枚举字段取值字典

### status

| 值 | 含义 |
|---|---|
| 0 | 未使用 |
| 1 | 已使用 |
| 2 | 已过期 |

## 索引设计说明

- `idx_user_id(user_id)`：用于查询某用户所有持有的优惠券（如用户中心展示）。
- `idx_coupon_id(coupon_id)`：用于统计或回溯某张优惠券的发放/领取情况。

## 字段来源

- DDL：sql/05-promotion.sql、sql/schema.sql
- MyBatis：eden-mapper/src/main/resources/mapper/UserCouponMapper.xml
- 实体/DTO：eden-pojo 下相关类（user_coupon 业务映射以 Mapper 为准）

---

本文档由 AI 基于当前代码结构推导，实际以数据库 DDL 为准
