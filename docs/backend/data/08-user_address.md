# 数据字典：user_address

## 表信息

| 表名 | 业务说明 | 主键策略 | 审计字段说明 |
|---|---|---|---|
| user_address | 用户收货地址表，保存用户的多个收货地址与默认地址标记 | id BIGINT AUTO_INCREMENT | create_time: 创建时间；update_time: 更新时间（ON UPDATE CURRENT_TIMESTAMP） |

## 字段清单

| 字段名 | 数据类型 | 约束(NOT NULL/UNIQUE等) | 默认值 | 业务说明 | 索引类型 |
|---|---|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 地址ID | PRIMARY |
| user_id | BIGINT | NOT NULL | - | 所属用户ID（关联 user.id） | BTREE(idx_user_id) |
| receiver_name | VARCHAR(50) | NOT NULL | - | 收货人姓名 | - |
| receiver_phone | VARCHAR(20) | NOT NULL | - | 收货人电话 | - |
| province | VARCHAR(50) | NOT NULL | - | 省份 | - |
| city | VARCHAR(50) | NOT NULL | - | 城市 | - |
| district | VARCHAR(50) | NOT NULL | - | 区/县 | - |
| detail_address | VARCHAR(200) | NOT NULL | - | 详细地址 | - |
| is_default | TINYINT | - | 0 | 是否默认：0-否 1-是 | - |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 | - |
| update_time | DATETIME | - | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 | - |

## 外键关联说明

- `user_id` -> `user.id`（所属用户）[推断]

## 业务规则与索引说明

- `is_default` 标记用于优先返回默认地址，Mapper 查询常以 `ORDER BY is_default DESC, create_time DESC` 返回地址列表（见 UserAddressMapper.selectByUserId）。
- `idx_user_id(user_id)`：支持按用户检索所有地址与设置默认地址的批量操作（clearDefault/setDefault）。

## 字段来源

- DDL：sql/02-user.sql
- MyBatis：eden-mapper/src/main/resources/mapper/UserAddressMapper.xml
- 实体注释：eden-pojo/src/main/java/eden/pojo/UserAddress.java

---

本文档由 AI 基于当前代码结构推导，实际以数据库 DDL 为准
