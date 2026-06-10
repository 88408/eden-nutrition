# 数据字典：coupon

## 表信息

| 表名 | 业务说明 | 主键策略 | 审计字段说明 |
|---|---|---|---|
| coupon | 优惠券基础表，定义可发放的优惠券规则（满减/折扣/有效期/库存） | id BIGINT AUTO_INCREMENT | create_time: 创建时间；update_time: 更新时间（ON UPDATE CURRENT_TIMESTAMP） |

## 字段清单

| 字段名 | 数据类型 | 约束(NOT NULL/UNIQUE等) | 默认值 | 业务说明 | 索引类型 |
|---|---|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 优惠券ID | PRIMARY |
| name | VARCHAR(100) | NOT NULL | - | 优惠券名称 | - |
| type | TINYINT | NOT NULL | - | 类型：1-满减券 2-折扣券 | - |
| value | DECIMAL(10,2) | NOT NULL | - | 面值或折扣值（兼容字段） | - |
| min_amount | DECIMAL(10,2) | - | 0 | 最低消费金额 | - |
| total_count | INT | NOT NULL | - | 发放总量 | - |
| remain_count | INT | NOT NULL | - | 剩余数量 | - |
| start_time | DATETIME | NOT NULL | - | 生效开始时间 | - |
| end_time | DATETIME | NOT NULL | - | 生效结束时间 | - |
| status | TINYINT | - | 1 | 状态：0-禁用 1-启用 | - |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 | - |
| update_time | DATETIME | - | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 | - |

## 外键关联说明

- `user_coupon.coupon_id` -> `coupon.id`（用户优惠券引用）[推断]

## 枚举字段取值字典

### type

| 值 | 含义 |
|---|---|
| 1 | 满减券 |
| 2 | 折扣券 |

### status

| 值 | 含义 |
|---|---|
| 0 | 禁用 |
| 1 | 启用 |

## 索引设计说明

- `idx_start_time` / `idx_end_time`（若存在）：用于查询有效期内可用券（Mapper.selectAvailable 使用时间范围判断）
- `idx_coupon_id` 在 `user_coupon` 表上用于快速查询用户持有的优惠券

## 字段来源

- DDL：sql/05-promotion.sql
- MyBatis：eden-mapper/src/main/resources/mapper/CouponMapper.xml
- 实体注释：eden-pojo/src/main/java/eden/pojo/Coupon.java

---

本文档由 AI 基于当前代码结构推导，实际以数据库 DDL 为准
