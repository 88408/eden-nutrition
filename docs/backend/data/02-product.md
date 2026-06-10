# 数据字典：product

## 表信息

| 表名 | 业务说明 | 主键策略 | 审计字段说明 |
|---|---|---|---|
| product | 商品主数据表（商品信息、价格、库存、上下架及运营标签） | id BIGINT AUTO_INCREMENT | create_time: 创建时间；update_time: 更新时间（ON UPDATE CURRENT_TIMESTAMP） |

## 字段清单

| 字段名 | 数据类型 | 约束(NOT NULL/UNIQUE等) | 默认值 | 业务说明 | 索引类型 |
|---|---|---|---|---|---|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | - | 商品ID | PRIMARY |
| name | VARCHAR(200) | NOT NULL | - | 商品名称 | FULLTEXT(idx_name_ft) |
| subtitle | VARCHAR(500) | - | NULL | 商品副标题 | - |
| category_id | BIGINT | NOT NULL | - | 分类ID | BTREE(idx_category_id) |
| main_image | VARCHAR(500) | - | NULL | 主图URL | - |
| sub_images | TEXT | - | NULL | 商品图片列表（JSON数组） | - |
| detail | TEXT | - | NULL | 商品详情（富文本） | - |
| original_price | DECIMAL(10,2) | - | NULL | 原价 | - |
| price | DECIMAL(10,2) | NOT NULL | - | 销售价格 | - |
| stock | INT | NOT NULL | 0 | 库存 | - |
| sales | INT | - | 0 | 销量 | - |
| status | TINYINT | - | 1 | 状态：0-下架 1-上架 | BTREE(idx_status) |
| is_hot | TINYINT | - | 0 | 是否热门：0-否 1-是 | - |
| is_new | TINYINT | - | 0 | 是否新品：0-否 1-是 | - |
| create_time | DATETIME | - | CURRENT_TIMESTAMP | 创建时间 | - |
| update_time | DATETIME | - | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 | - |

## 外键关联说明

- 当前 DDL 未显式声明外键约束。
- category_id -> category.id（商品所属分类）[推断]
- 本表被下列表关联引用：
- seckill.product_id -> product.id [推断]
- order_item.product_id -> product.id [推断]
- product_review.product_id -> product.id [推断]

## 枚举字段取值字典

### status

| 值 | 含义 |
|---|---|
| 0 | 下架 |
| 1 | 上架 |

### is_hot

| 值 | 含义 |
|---|---|
| 0 | 否 |
| 1 | 是 |

### is_new

| 值 | 含义 |
|---|---|
| 0 | 否 |
| 1 | 是 |

## 索引设计说明

- PRIMARY(id)：主键聚簇索引，支撑商品详情按 ID 查询。
- idx_category_id(category_id)：支撑分类维度筛选（ProductMapper.selectByCategoryId/selectByCondition）。
- idx_status(status)：支撑上架状态筛选（ProductMapper 多处 where status=1）。
- idx_name_ft(name) FULLTEXT WITH PARSER ngram：支撑商品名称全文检索；当前 Mapper 使用 LIKE 查询，全文索引可用于后续搜索优化 [推断]。

## 字段来源

- DDL：sql/03-product.sql
- MyBatis：eden-mapper/src/main/resources/mapper/ProductMapper.xml
- 实体注释：eden-pojo/src/main/java/eden/pojo/Product.java
- DTO注释：eden-pojo/src/main/java/eden/pojo/dto/ProductSaveDTO.java

---

本文档由 AI 基于当前代码结构推导，实际以数据库 DDL 为准
