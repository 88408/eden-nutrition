# 数据库 SQL 表结构说明与更新基准文档 (SQL Schema Update Report)

本文档旨在梳理当前后台管理系统重点模块（特别是近期完成对接的**分类模块**和**订单模块**）所依赖的底层关系型数据库（MySQL）表结构。因后端实体（Entity）与前端字段（如 `sortOrder`、`orderNo`）均已严格对齐，当前无需通过 `ALTER TABLE` 对表结构进行强更。

以下为当前系统所依赖的最新基准 SQL 表定义，供开发、实施与灰度升级时参考核对。

---

## 一、 分类模块 (Category)

商品类目支持无限极树状结构，主要利用 `parent_id` 实现分层。前端通过展开这些层级映射出 `CategoryTreeVO`。

### 1. `category` 类目表
用于保存商店中所有的内部与展示分类信息。

```sql
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID，0为顶级分类',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `level` TINYINT DEFAULT 1 COMMENT '层级：1-一级 2-二级 3-三级',
    `sort_order` INT DEFAULT 0 COMMENT '排序权值（前端对应 sortOrder）',
    `icon` VARCHAR(255) COMMENT '分类图标（存入预设 Icon 名或 URL）',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';
```
> **注意环节**：前端传回的 `sortOrder` 与 `parentId` 会分别匹配为后端的 `sort_order` 及 `parent_id`。新增时若不特别传递 `level`，后端服务会自动根据 `parent_id` 的层数计算加一。

---

## 二、 订单模块 (Order Module)

订单模块被拆分为主表（`order`）和明细表（`order_item`）。以满足一个订单具有多个商品 SKU 及不同发货状态的业务流转。

### 1. `order` 订单主表
记录用户购买主体信息、金额信息、发货信息等（供后端 `AdminOrderController` 拉取）。

```sql
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL UNIQUE COMMENT '业务单号',
    `user_id` BIGINT NOT NULL COMMENT '下单用户ID',
    
    -- 金额相关
    `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
    `freight_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '运费金额',
    `discount_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '促销/优惠券抵扣金额',
    `pay_amount` DECIMAL(10,2) NOT NULL COMMENT '实际支付金额',
    
    -- 支付与状态
    `pay_type` TINYINT COMMENT '支付方式：1-支付宝 2-微信',
    `status` TINYINT DEFAULT 0 COMMENT '核心状态：0-待支付 1-已支付/待发货 2-已发货 3-已收货 4-已完成 5-已取消 6-退款中 7-已退款',
    `order_type` TINYINT DEFAULT 0 COMMENT '类型：0-普通 1-秒杀 2-团购',
    
    -- 物流与收货人信息
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
    `receiver_province` VARCHAR(50) COMMENT '省份',
    `receiver_city` VARCHAR(50) COMMENT '城市',
    `receiver_region` VARCHAR(50) COMMENT '区县',
    `receiver_detail_address` VARCHAR(200) NOT NULL COMMENT '详细地址',
    `delivery_company` VARCHAR(50) COMMENT '物流公司',
    `delivery_sn` VARCHAR(64) COMMENT '物流单号',
    
    -- 时间线追踪
    `payment_time` DATETIME COMMENT '支付时间',
    `delivery_time` DATETIME COMMENT '发货时间',
    `receive_time` DATETIME COMMENT '收货时间',
    `comment_time` DATETIME COMMENT '评价时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单主表';
```
> **注意环节**：在前端发货功能 (`deliverOrder`) 被调用时，后端主要是执行 `UPDATE order SET status = 2, delivery_company = ?, delivery_sn = ?, delivery_time = NOW() WHERE id = ?`。

### 2. `order_item` 订单商品明细表
该表保存每次下单时拍下的具体商品快照，被组装在 `OrderDetailAdminVO` 的 `orderItems` 列表中返回给后台用于"订单详情"的展示。

```sql
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单编号',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品快照名称',
    `product_pic` VARCHAR(500) COMMENT '商品图片照',
    `product_price` DECIMAL(10,2) NOT NULL COMMENT '购买时单价',
    `product_quantity` INT NOT NULL COMMENT '购买数量',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单商品明细表';
```

---

## 三、 秒杀模块 (Seckill Module)

秒杀模块主要涉及活动发布和高并发下的防超卖控制。它不保存完整的业务订单，而是关联主订单体系，同时利用唯一索引防止单用户重复秒杀。

### 1. `seckill` 秒杀活动表
记录秒杀活动的商品设置及起止时间限制。

```sql
DROP TABLE IF EXISTS `seckill`;
CREATE TABLE `seckill` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '秒杀ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `seckill_price` DECIMAL(10, 2) NOT NULL COMMENT '秒杀价格',
    `stock` INT NOT NULL COMMENT '秒杀库存',
    `limit_per_user` INT DEFAULT 1 COMMENT '每人限购数量',
    `start_time` DATETIME NOT NULL COMMENT '秒杀开始时间',
    `end_time` DATETIME NOT NULL COMMENT '秒杀结束时间',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-未开始 1-进行中 2-已结束',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_start_time` (`start_time`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动表';
```
> **注意环节**：后端的 `AdminSeckillController` 提供基于该表的增删改查。当 `start_time` 和 `end_time` 到达条件时，配合定时任务或前端状态展示，秒杀活动即可自动生效。

### 2. `seckill_order` 秒杀订单防重表
用于在秒杀成功但主订单未支付完成前，通过数据库级别的**唯一联合索引**拦截同一用户重复抢购的行为。

```sql
DROP TABLE IF EXISTS `seckill_order`;
CREATE TABLE `seckill_order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `order_id` BIGINT NOT NULL COMMENT '生成的底层主订单ID',
    `seckill_id` BIGINT NOT NULL COMMENT '秒杀ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_seckill` (`user_id`, `seckill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单防重复表';
```
> **注意环节**：`UNIQUE KEY uk_user_seckill` 在数据库防刷上扮演重要角色。高并发时，若 Redis 漏检或延迟，数据库的唯一约束能充当最后一道防线抛出异常并回滚。

---

## 四、 执行方式说明
本系统使用 JPA/MyBatis 构建，并配合了已固化的 SQL 文件 (`sql/schema.sql`、`sql/07-init-data.sql`)。  
* 如果您是首次部署或数据库重建：请直接在 MySQL 客户端中导入根目录下的 `sql/init-all.sql`。
* 此前已经部署过的环境：无需执行任何表结构的变更 (ALTER TABLE)，本期前后台联调已经保障了现有数据库列名的稳定及充足性，**100% 向下兼容**。 
## ���ݿ���½���

Ŀǰ���� \order\ ���� Java ʵ���� \Order.java\ �����ֶβ��죬����ִ������ SQL �����и��£�

\\\sql
ALTER TABLE \order\
ADD COLUMN \delivery_company\ VARCHAR 50 DEFAULT NULL COMMENT ������˾ AFTER \
eceive_time\,
ADD COLUMN \delivery_sn\ VARCHAR 100 DEFAULT NULL COMMENT �������� AFTER \delivery_company\,
ADD COLUMN \payment_method\ VARCHAR 50 DEFAULT NULL COMMENT ֧����ʽ���ַ��ͣ� AFTER \pay_type\,
ADD COLUMN \order_type\ TINYINT DEFAULT 0 COMMENT �������ͣ�0-��ͨ���� 1-��ɱ���� 2-�Ź����� AFTER \status\;
\\\

