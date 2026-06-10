# 数据字典：user

## 表信息

| 表名 | 业务说明 | 主键策略 | 审计字段说明 |
|---|---|---|---|
| user | 用户基础信息表（登录、资料、角色、状态、积分） | id BIGINT AUTO_INCREMENT | create_time: 创建时间；update_time: 更新时间（ON UPDATE CURRENT_TIMESTAMP） |

## 字段清单

| 字段名 | 数据类型 | 约束(NOT NULL/UNIQUE等) | 默认值 | 业务说明 | 索引类型 |
|---|---|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 用户ID | PRIMARY |
| username | VARCHAR(50) | NOT NULL, UNIQUE | - | 用户名 | UNIQUE, BTREE(idx_username) |
| phone | VARCHAR(20) | UNIQUE | NULL | 手机号 | UNIQUE, BTREE(idx_phone) |
| email | VARCHAR(100) | UNIQUE | NULL | 邮箱 | UNIQUE |
| password | VARCHAR(255) | NOT NULL | - | 密码（加密存储） | - |
| nickname | VARCHAR(50) | - | NULL | 昵称 | - |
| avatar | VARCHAR(500) | - | NULL | 头像URL | - |
| gender | TINYINT | - | 0 | 性别：0-未知 1-男 2-女 | - |
| points | INT | - | 0 | 积分 | - |
| status | TINYINT | - | 1 | 状态：0-禁用 1-正常 | - |
| role | VARCHAR(20) | - | 'USER' | 角色：USER-普通用户 ADMIN-管理员 | - |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 | - |
| update_time | DATETIME | - | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 | - |

## 外键关联说明

- 当前 DDL 未显式声明外键约束。
- 本表作为主表被多表引用：
- user_address.user_id -> user.id [推断]
- order.user_id -> user.id [推断]
- user_coupon.user_id -> user.id [推断]
- seckill_order.user_id -> user.id [推断]
- product_review.user_id -> user.id [推断]

## 枚举字段取值字典

### gender

| 值 | 含义 |
|---|---|
| 0 | 未知 |
| 1 | 男 |
| 2 | 女 |

### status

| 值 | 含义 |
|---|---|
| 0 | 禁用 |
| 1 | 正常 |

### role

| 值 | 含义 |
|---|---|
| USER | 普通用户 |
| ADMIN | 管理员 |

## 索引设计说明

- PRIMARY(id)：主键聚簇索引，支撑按 ID 精确查询。
- idx_username(username)：支撑按用户名登录查询（UserMapper.selectByUsername）。
- idx_phone(phone)：支撑按手机号登录/查重（UserMapper.selectByPhone）。
- email UNIQUE：用于邮箱唯一性校验与查重 [推断]。

## 字段来源

- DDL：sql/02-user.sql
- MyBatis：eden-mapper/src/main/resources/mapper/UserMapper.xml
- 实体注释：eden-pojo/src/main/java/eden/pojo/User.java

---

本文档由 AI 基于当前代码结构推导，实际以数据库 DDL 为准
