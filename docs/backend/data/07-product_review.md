# 数据字典：product_review

## 表信息

| 表名 | 业务说明 | 主键策略 | 审计字段说明 |
|---|---|---|---|
| product_review | 商品评价表，保存用户对商品的评分、内容与图片快照 | id BIGINT AUTO_INCREMENT | create_time: 创建时间 |

## 字段清单

| 字段名 | 数据类型 | 约束(NOT NULL/UNIQUE等) | 默认值 | 业务说明 | 索引类型 |
|---|---|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 评价ID | PRIMARY |
| product_id | BIGINT | NOT NULL | - | 商品ID（关联 product.id） | BTREE(idx_product_id) |
| user_id | BIGINT | NOT NULL | - | 用户ID（关联 user.id） | BTREE(idx_user_id) |
| order_id | BIGINT | - | NULL | 订单ID（用于校验购买关系） | - |
| rating | TINYINT | NOT NULL | - | 评分：1-5星 | - |
| content | TEXT | - | NULL | 评价内容 | - |
| images | TEXT | - | NULL | 评价图片（JSON数组） | - |
| is_anonymous | TINYINT | - | 0 | 是否匿名：0-否 1-是 | - |
| status | TINYINT | - | 1 | 状态：0-隐藏 1-显示 | - |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 | - |

## 外键关联说明

- `product_id` -> `product.id`（商品）[推断]
- `user_id` -> `user.id`（评价用户）[推断]
- `order_id` -> `order.id`（若存在，则为购买时对应订单）[推断]

## 枚举字段取值字典

### rating

| 值 | 含义 |
|---|---|
| 1 | 1 星 |
| 2 | 2 星 |
| 3 | 3 星 |
| 4 | 4 星 |
| 5 | 5 星 |

### is_anonymous

| 值 | 含义 |
|---|---|
| 0 | 否 |
| 1 | 是 |

### status

| 值 | 含义 |
|---|---|
| 0 | 隐藏 |
| 1 | 显示 |

## 索引设计说明

- `idx_product_id(product_id)`：支持按商品分页查询展示评价（ProductReviewMapper.selectByProductId）。
- `idx_user_id(user_id)`：支持按用户维度查询用户历史评价（退单/申诉依据）。

## 字段来源

- DDL：sql/03-product.sql
- MyBatis：eden-mapper/src/main/resources/mapper/ProductReviewMapper.xml
- 实体注释：eden-pojo/src/main/java/eden/pojo/ProductReview.java

---

本文档由 AI 基于当前代码结构推导，实际以数据库 DDL 为准
