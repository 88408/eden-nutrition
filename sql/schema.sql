-- ============================================
-- Eden Nutrition 数据库初始化脚本
-- 伊甸滋补线上商店
-- ============================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS eden_db DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE eden_db;

-- ============================================
-- 1. 用户模块
-- ============================================

-- 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    `username` VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    `phone` VARCHAR(20) UNIQUE COMMENT '手机号',
    `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
    `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar` VARCHAR(500) COMMENT '头像URL',
    `gender` TINYINT DEFAULT 0 COMMENT '性别：0-未知 1-男 2-女',
    `points` INT DEFAULT 0 COMMENT '积分',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-正常',
    `role` VARCHAR(20) DEFAULT 'USER' COMMENT '角色：USER-普通用户 ADMIN-管理员',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_phone` (`phone`),
    INDEX `idx_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 收货地址表
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '地址ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
    `province` VARCHAR(50) NOT NULL COMMENT '省',
    `city` VARCHAR(50) NOT NULL COMMENT '市',
    `district` VARCHAR(50) NOT NULL COMMENT '区/县',
    `detail_address` VARCHAR(200) NOT NULL COMMENT '详细地址',
    `is_default` TINYINT DEFAULT 0 COMMENT '是否默认：0-否 1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- ============================================
-- 2. 商品模块
-- ============================================

-- 商品分类表
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `parent_id` BIGINT DEFAULT 0 COMMENT '父分类ID，0表示一级分类',
    `level` TINYINT DEFAULT 1 COMMENT '分类层级：1-一级 2-二级',
    `sort_order` INT DEFAULT 0 COMMENT '排序值',
    `icon` VARCHAR(255) COMMENT '分类图标',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品表
DROP TABLE IF EXISTS `product`;
CREATE TABLE `product` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品ID',
    `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `subtitle` VARCHAR(500) COMMENT '商品副标题',
    `category_id` BIGINT NOT NULL COMMENT '分类ID',
    `main_image` VARCHAR(500) COMMENT '主图URL',
    `sub_images` TEXT COMMENT '商品图片列表（JSON数组）',
    `detail` TEXT COMMENT '商品详情（富文本）',
    `original_price` DECIMAL(10, 2) COMMENT '原价',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '销售价格',
    `stock` INT NOT NULL DEFAULT 0 COMMENT '库存',
    `sales` INT DEFAULT 0 COMMENT '销量',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-下架 1-上架',
    `is_hot` TINYINT DEFAULT 0 COMMENT '是否热门：0-否 1-是',
    `is_new` TINYINT DEFAULT 0 COMMENT '是否新品：0-否 1-是',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_status` (`status`),
    FULLTEXT INDEX `idx_name_ft` (`name`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

-- 商品评价表
DROP TABLE IF EXISTS `product_review`;
CREATE TABLE `product_review` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评价ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `order_id` BIGINT COMMENT '订单ID',
    `rating` TINYINT NOT NULL COMMENT '评分：1-5星',
    `content` TEXT COMMENT '评价内容',
    `images` TEXT COMMENT '评价图片（JSON数组）',
    `is_anonymous` TINYINT DEFAULT 0 COMMENT '是否匿名：0-否 1-是',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-隐藏 1-显示',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

-- ============================================
-- 3. 订单模块
-- ============================================

-- 订单表
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单编号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `total_amount` DECIMAL(10, 2) NOT NULL COMMENT '订单总金额',
    `pay_amount` DECIMAL(10, 2) NOT NULL COMMENT '实付金额',
    `freight_amount` DECIMAL(10, 2) DEFAULT 0 COMMENT '运费',
    `discount_amount` DECIMAL(10, 2) DEFAULT 0 COMMENT '优惠金额',
    `coupon_id` BIGINT COMMENT '使用的优惠券ID',
    `pay_type` TINYINT COMMENT '支付方式：1-支付宝 2-微信',
    `payment_method` VARCHAR(50) DEFAULT NULL COMMENT '支付方式名称',
    `payment_trade_no` VARCHAR(100) DEFAULT NULL COMMENT '第三方支付交易号',
    `status` TINYINT DEFAULT 0 COMMENT '订单状态：0-待支付 1-已支付 2-已发货 3-已完成 4-已取消 5-已退款',
    `order_type` TINYINT DEFAULT 0 COMMENT '订单类型：0-普通订单 1-秒杀订单 2-团购订单',
    `receiver_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `receiver_phone` VARCHAR(20) NOT NULL COMMENT '收货人电话',
    `receiver_address` VARCHAR(500) NOT NULL COMMENT '收货地址',
    `remark` VARCHAR(500) COMMENT '订单备注',
    `pay_time` DATETIME COMMENT '支付时间',
    `ship_time` DATETIME COMMENT '发货时间',
    `receive_time` DATETIME COMMENT '收货时间',
    `delivery_company` VARCHAR(50) DEFAULT NULL COMMENT '物流公司',
    `delivery_sn` VARCHAR(100) DEFAULT NULL COMMENT '物流单号',
    `complete_time` DATETIME COMMENT '完成时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_payment_trade_no` (`payment_trade_no`),
    INDEX `idx_status` (`status`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 订单明细表
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `sku_id` BIGINT DEFAULT NULL COMMENT '商品SKU ID',
    `sku_spec_name` VARCHAR(120) DEFAULT NULL COMMENT 'SKU规格快照',
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称（快照）',
    `product_image` VARCHAR(500) COMMENT '商品图片（快照）',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '商品单价（快照）',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `total_price` DECIMAL(10, 2) NOT NULL COMMENT '小计金额',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_sku_id` (`sku_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- ============================================
-- 4. 促销模块
-- ============================================

-- 优惠券表
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '优惠券ID',
    `name` VARCHAR(100) NOT NULL COMMENT '优惠券名称',
    `type` TINYINT NOT NULL COMMENT '类型：1-满减券 2-折扣券',
    `value` DECIMAL(10, 2) NOT NULL COMMENT '优惠值（满减金额或折扣比例）',
    `min_amount` DECIMAL(10, 2) DEFAULT 0 COMMENT '最低消费金额',
    `total_count` INT NOT NULL COMMENT '发放总量',
    `remain_count` INT NOT NULL COMMENT '剩余数量',
    `start_time` DATETIME NOT NULL COMMENT '生效开始时间',
    `end_time` DATETIME NOT NULL COMMENT '生效结束时间',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券表';

-- 用户优惠券表
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `coupon_id` BIGINT NOT NULL COMMENT '优惠券ID',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-未使用 1-已使用 2-已过期',
    `used_time` DATETIME COMMENT '使用时间',
    `order_id` BIGINT COMMENT '使用的订单ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_coupon_id` (`coupon_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- 秒杀活动表
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

-- 秒杀订单表（防止重复秒杀）
DROP TABLE IF EXISTS `seckill_order`;
CREATE TABLE `seckill_order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(50) DEFAULT NULL COMMENT '订单编号',
    `seckill_id` BIGINT NOT NULL COMMENT '秒杀ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `amount` DECIMAL(10, 2) DEFAULT NULL COMMENT '秒杀金额',
    `status` TINYINT DEFAULT 0 COMMENT '订单状态：0-未支付 1-已支付 2-已取消',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_seckill` (`user_id`, `seckill_id`),
    INDEX `idx_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';

-- 秒杀商品兼容表（旧接口仍可能读取该表）
DROP TABLE IF EXISTS `seckill_product`;
CREATE TABLE `seckill_product` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '秒杀ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `seckill_price` DECIMAL(10, 2) NOT NULL COMMENT '秒杀价格',
    `stock` INT NOT NULL COMMENT '秒杀库存',
    `start_time` DATETIME NOT NULL COMMENT '秒杀开始时间',
    `end_time` DATETIME NOT NULL COMMENT '秒杀结束时间',
    `status` TINYINT DEFAULT 0 COMMENT '状态：0-未开始 1-进行中 2-已结束',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_product_id` (`product_id`),
    INDEX `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品表';

-- ============================================
-- 5. 系统模块
-- ============================================

-- 操作日志表
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT COMMENT '操作用户ID',
    `username` VARCHAR(50) COMMENT '操作用户名',
    `operation` VARCHAR(100) COMMENT '操作描述',
    `method` VARCHAR(200) COMMENT '请求方法',
    `params` TEXT COMMENT '请求参数',
    `ip` VARCHAR(50) COMMENT 'IP地址',
    `duration` BIGINT COMMENT '执行时长（毫秒）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- ============================================
-- ============================================
-- 5. 用户体验扩展模块
-- ============================================

-- 客服会话表
DROP TABLE IF EXISTS `support_message`;
DROP TABLE IF EXISTS `support_session`;
CREATE TABLE `support_session` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '客服会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT DEFAULT NULL COMMENT '来源商品ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-关闭 1-进行中',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_product` (`user_id`, `product_id`),
    INDEX `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服会话表';

-- 客服消息表
CREATE TABLE `support_message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '客服消息ID',
    `session_id` BIGINT NOT NULL COMMENT '客服会话ID',
    `sender_type` VARCHAR(20) NOT NULL COMMENT '发送方：USER/STAFF/SYSTEM',
    `content` VARCHAR(1000) NOT NULL COMMENT '消息内容',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_session_time` (`session_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服消息表';

-- 站内通知表
DROP TABLE IF EXISTS `notice`;
CREATE TABLE `notice` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '通知ID',
    `user_id` BIGINT NOT NULL COMMENT '接收用户ID',
    `type` VARCHAR(20) NOT NULL COMMENT '类型：ORDER/COUPON/SYSTEM',
    `title` VARCHAR(100) NOT NULL COMMENT '通知标题',
    `content` VARCHAR(500) NOT NULL COMMENT '通知内容',
    `target` VARCHAR(255) DEFAULT NULL COMMENT '业务跳转目标',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `read_time` DATETIME DEFAULT NULL COMMENT '已读时间',
    INDEX `idx_user_read_time` (`user_id`, `is_read`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='站内通知表';

-- 初始化数据
-- ============================================

USE eden_db;
SET NAMES utf8mb4;

-- 插入当前库内已有用户，保留原始 ID，避免订单、地址等初始化数据引用错位。
INSERT INTO `user` (`id`, `username`, `phone`, `email`, `password`, `nickname`, `avatar`, `gender`, `points`, `status`, `role`, `create_time`, `update_time`) VALUES
(1, 'admin', '13800000000', NULL, '$2a$12$wNrp/fy0eW5019GNLAJ1cOg9Id0dZLsLsfeFeA2h1BSAuIdyoXh7m', '管理员', NULL, 0, 0, 1, 'ADMIN', '2026-03-18 08:32:59', '2026-05-03 03:36:24'),
(3, 'kang', '13900000000', NULL, '$2a$12$GYHrHUzVVvR9sTQ14hyuQ.VGYjITLDyJaDIv4iXKlBzcti6jJwPWu', '康', NULL, 0, 0, 1, 'ADMIN', '2026-05-03 03:33:50', '2026-05-03 12:28:56'),
(4, 'yu', '14000000000', NULL, '$2a$12$GYHrHUzVVvR9sTQ14hyuQ.VGYjITLDyJaDIv4iXKlBzcti6jJwPWu', '宇', NULL, 0, 4804, 1, 'USER', '2026-05-03 12:40:24', '2026-05-31 22:12:14');

-- 插入当前库内已有收货地址，保留 user_id 关系。
INSERT INTO `user_address` (`id`, `user_id`, `receiver_name`, `receiver_phone`, `province`, `city`, `district`, `detail_address`, `is_default`, `create_time`, `update_time`) VALUES
(1, 4, '1', '1', '1', '1', '1', '1', 1, '2026-05-03 14:27:31', '2026-05-03 14:27:31');

-- 插入商品一级分类
INSERT INTO `category` (`id`, `name`, `parent_id`, `level`, `sort_order`) VALUES
(1, '燕窝', 0, 1, 1),
(2, '人参', 0, 1, 2),
(3, '阿胶', 0, 1, 3),
(4, '灵芝', 0, 1, 4),
(5, '枸杞', 0, 1, 5),
(6, '虫草', 0, 1, 6);

-- 插入商品二级分类
INSERT INTO `category` (`id`, `name`, `parent_id`, `level`, `sort_order`) VALUES
(7, '即食燕窝', 1, 2, 1),
(8, '干燕窝', 1, 2, 2),
(9, '燕窝礼盒', 1, 2, 3),
(10, '野山参', 2, 2, 1),
(11, '高丽参', 2, 2, 2),
(12, '西洋参', 2, 2, 3);

-- 插入示例商品，显式保留商品 ID，供订单明细和秒杀活动稳定引用。
INSERT INTO `product` (`id`, `name`, `subtitle`, `category_id`, `main_image`, `sub_images`, `detail`, `original_price`, `price`, `stock`, `sales`, `status`, `is_hot`, `is_new`) VALUES
(1, '印尼进口即食燕窝 70g*6瓶', '精选印尼金丝燕窝', 7, '/api/images/products/bird-nest.png', '["/api/images/products/bird-nest.png"]', '精选印尼金丝燕窝，开盖即食，方便营养', 799.00, 599.00, 100, 256, 1, 1, 0),
(2, '长白山野山参 10年参龄 50g', '长白山原产地直供', 10, '/api/images/products/ginseng.png', '["/api/images/products/ginseng.png"]', '长白山原产地直供，10年以上参龄，品质保证', 1599.00, 1299.00, 50, 128, 1, 1, 0),
(3, '东阿阿胶块 250g 礼盒装', '国家非物质文化遗产', 3, '/api/images/products/ejiao.png', '["/api/images/products/ejiao.png"]', '国家非物质文化遗产，正宗东阿阿胶', 999.00, 899.00, 200, 512, 1, 1, 0),
(4, '破壁灵芝孢子粉 1g*60袋', '高破壁率，易吸收', 4, '/api/images/products/herbal.png', '["/api/images/products/herbal.png"]', '高破壁率，易吸收，增强免疫力', 499.00, 399.00, 150, 320, 1, 0, 0),
(5, '宁夏枸杞王 特级 500g', '宁夏中宁原产地', 5, '/api/images/products/vitamin.png', '["/api/images/products/vitamin.png"]', '宁夏中宁原产地，颗粒饱满，天然晾晒', 168.00, 128.00, 300, 890, 1, 1, 0);

-- 插入示例优惠券
INSERT INTO `coupon` (`id`, `name`, `type`, `value`, `min_amount`, `total_count`, `remain_count`, `start_time`, `end_time`) VALUES
(1, '新人专享50元券', 1, 50.00, 200.00, 1000, 1000, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
(2, '满300减30', 1, 30.00, 300.00, 500, 500, NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY)),
(3, '会员9折券', 2, 0.90, 100.00, 200, 200, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY));

-- 为所有已有用户发放一组初始优惠券，保证用户侧优惠券列表有可验证数据。
INSERT INTO `user_coupon` (`user_id`, `coupon_id`, `status`, `create_time`)
SELECT u.id, c.id, 0, NOW()
FROM `user` u
CROSS JOIN `coupon` c
WHERE c.id IN (1, 2, 3);

-- 插入秒杀活动（关联商品 1、2、3）
INSERT INTO `seckill` (`id`, `product_id`, `seckill_price`, `stock`, `limit_per_user`, `start_time`, `end_time`, `status`) VALUES
(1, 1, 499.00, 50, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 2 HOUR), 1),
(2, 2, 999.00, 20, 2, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 0),
(3, 3, 699.00, 0, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 2);

-- 兼容旧秒杀商品表，保留与 seckill 表一致的基础活动数据。
INSERT INTO `seckill_product` (`id`, `product_id`, `seckill_price`, `stock`, `start_time`, `end_time`, `status`) VALUES
(1, 1, 499.00, 50, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 2 HOUR), 1),
(2, 2, 999.00, 20, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 0),
(3, 3, 699.00, 0, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 2);

-- 构造 100 条最近 30 天内的订单种子。RAND(n) 使用固定种子，让数据看起来分散且每次初始化可复现。
DROP TEMPORARY TABLE IF EXISTS `tmp_seed_order`;
CREATE TEMPORARY TABLE `tmp_seed_order` (
    `id` BIGINT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `product_id` BIGINT NOT NULL,
    `quantity` INT NOT NULL,
    `status` TINYINT NOT NULL,
    `pay_type` TINYINT NOT NULL,
    `freight_amount` DECIMAL(10, 2) NOT NULL,
    `discount_amount` DECIMAL(10, 2) NOT NULL,
    `create_time` DATETIME NOT NULL
);

INSERT INTO `tmp_seed_order` (`id`, `user_id`, `product_id`, `quantity`, `status`, `pay_type`, `freight_amount`, `discount_amount`, `create_time`)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 100
)
SELECT
    n AS id,
    ELT(1 + MOD(n - 1, 3), 1, 3, 4) AS user_id,
    1 + MOD(n * 7, 5) AS product_id,
    1 + MOD(n * 11, 3) AS quantity,
    CASE
        WHEN MOD(n, 10) = 0 THEN 0
        WHEN MOD(n, 13) = 0 THEN 4
        WHEN MOD(n, 4) = 0 THEN 2
        WHEN MOD(n, 3) = 0 THEN 1
        ELSE 3
    END AS status,
    CASE WHEN MOD(n, 2) = 0 THEN 1 ELSE 2 END AS pay_type,
    CASE WHEN MOD(n, 7) = 0 THEN 10.00 ELSE 0.00 END AS freight_amount,
    CASE
        WHEN MOD(n, 11) = 0 THEN 50.00
        WHEN MOD(n, 6) = 0 THEN 30.00
        ELSE 0.00
    END AS discount_amount,
    DATE_SUB(
        DATE_SUB(NOW(), INTERVAL FLOOR(RAND(n * 17) * 30) DAY),
        INTERVAL FLOOR(RAND(n * 31) * 86400) SECOND
    ) AS create_time
FROM seq;

INSERT INTO `order` (
    `id`, `order_no`, `user_id`, `total_amount`, `pay_amount`, `freight_amount`, `discount_amount`, `coupon_id`,
    `pay_type`, `payment_trade_no`, `status`, `order_type`, `receiver_name`, `receiver_phone`, `receiver_address`, `remark`,
    `pay_time`, `ship_time`, `receive_time`, `delivery_company`, `delivery_sn`, `complete_time`, `create_time`, `update_time`
)
SELECT
    t.id,
    CONCAT('ORD', DATE_FORMAT(t.create_time, '%Y%m%d'), LPAD(t.id, 4, '0')),
    t.user_id,
    ROUND(p.price * t.quantity, 2) AS total_amount,
    ROUND(p.price * t.quantity + t.freight_amount - LEAST(t.discount_amount, p.price * t.quantity), 2) AS pay_amount,
    t.freight_amount,
    LEAST(t.discount_amount, p.price * t.quantity) AS discount_amount,
    CASE
        WHEN t.discount_amount >= 50 THEN 1
        WHEN t.discount_amount >= 30 THEN 2
        ELSE NULL
    END AS coupon_id,
    CASE WHEN t.status IN (1, 2, 3) THEN t.pay_type ELSE NULL END AS pay_type,
    CASE WHEN t.status IN (1, 2, 3) THEN CONCAT('TRADE', DATE_FORMAT(t.create_time, '%Y%m%d%H%i%s'), LPAD(t.id, 4, '0')) ELSE NULL END AS payment_trade_no,
    t.status,
    0 AS order_type,
    CASE t.user_id WHEN 1 THEN '管理员' WHEN 3 THEN '康' ELSE '宇' END AS receiver_name,
    COALESCE(u.phone, '13800000000') AS receiver_phone,
    CASE t.user_id
        WHEN 1 THEN '上海市浦东新区初始化路1号'
        WHEN 3 THEN '北京市朝阳区初始化路3号'
        ELSE '杭州市西湖区初始化路4号'
    END AS receiver_address,
    CONCAT('初始化随机订单-', LPAD(t.id, 3, '0')) AS remark,
    CASE WHEN t.status IN (1, 2, 3) THEN LEAST(DATE_ADD(t.create_time, INTERVAL 30 MINUTE), NOW()) ELSE NULL END AS pay_time,
    CASE WHEN t.status IN (2, 3) THEN LEAST(DATE_ADD(t.create_time, INTERVAL 1 DAY), NOW()) ELSE NULL END AS ship_time,
    CASE WHEN t.status = 3 THEN LEAST(DATE_ADD(t.create_time, INTERVAL 3 DAY), NOW()) ELSE NULL END AS receive_time,
    CASE WHEN t.status IN (2, 3) THEN ELT(1 + MOD(t.id, 3), '顺丰速运', '京东快递', '中通快递') ELSE NULL END AS delivery_company,
    CASE WHEN t.status IN (2, 3) THEN CONCAT('EXP', DATE_FORMAT(t.create_time, '%Y%m%d'), LPAD(t.id, 6, '0')) ELSE NULL END AS delivery_sn,
    CASE WHEN t.status = 3 THEN LEAST(DATE_ADD(t.create_time, INTERVAL 4 DAY), NOW()) ELSE NULL END AS complete_time,
    t.create_time,
    CASE
        WHEN t.status = 3 THEN LEAST(DATE_ADD(t.create_time, INTERVAL 4 DAY), NOW())
        WHEN t.status IN (2, 1) THEN LEAST(DATE_ADD(t.create_time, INTERVAL 1 DAY), NOW())
        ELSE t.create_time
    END AS update_time
FROM `tmp_seed_order` t
JOIN `product` p ON p.id = t.product_id
JOIN `user` u ON u.id = t.user_id;

-- 每个订单生成一条与订单金额匹配的明细，保证订单列表和详情页都能正常展示。
INSERT INTO `order_item` (`order_id`, `order_no`, `product_id`, `product_name`, `product_image`, `price`, `quantity`, `total_price`, `create_time`)
SELECT
    o.id,
    o.order_no,
    p.id,
    p.name,
    p.main_image,
    p.price,
    t.quantity,
    ROUND(p.price * t.quantity, 2) AS total_price,
    t.create_time
FROM `tmp_seed_order` t
JOIN `order` o ON o.id = t.id
JOIN `product` p ON p.id = t.product_id;

DROP TEMPORARY TABLE IF EXISTS `tmp_seed_order`;

-- ============================================
-- 满分迭代扩展结构：RBAC、SKU、退款售后
-- 如使用分文件初始化，也可执行 10-full-score-iteration.sql
-- ============================================

CREATE TABLE IF NOT EXISTS `product_sku` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'SKU ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `spec_name` VARCHAR(80) NOT NULL COMMENT '规格名称',
    `flavor` VARCHAR(80) DEFAULT NULL COMMENT '口味',
    `package_size` VARCHAR(80) DEFAULT NULL COMMENT '包装/净含量',
    `price` DECIMAL(10, 2) NOT NULL COMMENT 'SKU售价',
    `stock` INT NOT NULL DEFAULT 0 COMMENT 'SKU库存',
    `image_url` VARCHAR(500) DEFAULT NULL COMMENT 'SKU图片',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格SKU表';

CREATE TABLE IF NOT EXISTS `permission` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    `parent_id` BIGINT NOT NULL DEFAULT 0 COMMENT '父权限ID',
    `name` VARCHAR(80) NOT NULL COMMENT '权限/菜单名称',
    `code` VARCHAR(80) NOT NULL COMMENT '权限码',
    `path` VARCHAR(160) DEFAULT NULL COMMENT '前端路由',
    `component` VARCHAR(160) DEFAULT NULL COMMENT '前端组件',
    `icon` VARCHAR(80) DEFAULT NULL COMMENT '菜单图标',
    `type` TINYINT NOT NULL COMMENT '1-菜单 2-按钮/接口',
    `sort_order` INT NOT NULL DEFAULT 0 COMMENT '排序',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_permission_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台权限表';

CREATE TABLE IF NOT EXISTS `role` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    `name` VARCHAR(80) NOT NULL COMMENT '角色名称',
    `code` VARCHAR(80) NOT NULL COMMENT '角色编码',
    `description` VARCHAR(255) DEFAULT NULL COMMENT '角色说明',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_role_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='后台角色表';

CREATE TABLE IF NOT EXISTS `role_permission` (
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    `permission_id` BIGINT NOT NULL COMMENT '权限ID',
    PRIMARY KEY (`role_id`, `permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

CREATE TABLE IF NOT EXISTS `user_role` (
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `role_id` BIGINT NOT NULL COMMENT '角色ID',
    PRIMARY KEY (`user_id`, `role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

CREATE TABLE IF NOT EXISTS `refund_apply` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '退款申请ID',
    `refund_no` VARCHAR(64) NOT NULL COMMENT '退款申请号',
    `order_id` BIGINT NOT NULL COMMENT '订单ID',
    `order_no` VARCHAR(64) NOT NULL COMMENT '订单号',
    `order_item_id` BIGINT DEFAULT NULL COMMENT '订单项ID，空表示整单退款',
    `user_id` BIGINT NOT NULL COMMENT '申请用户ID',
    `refund_amount` DECIMAL(10, 2) NOT NULL COMMENT '退款金额',
    `original_order_status` TINYINT DEFAULT NULL COMMENT '申请退款前订单状态',
    `reason` VARCHAR(500) NOT NULL COMMENT '退款原因',
    `images` TEXT DEFAULT NULL COMMENT '凭证图片JSON',
    `status` TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核 1审核通过 2审核拒绝 3退款成功 4退款失败',
    `audit_remark` VARCHAR(500) DEFAULT NULL COMMENT '审核备注',
    `auditor_id` BIGINT DEFAULT NULL COMMENT '审核管理员ID',
    `audit_time` DATETIME DEFAULT NULL COMMENT '审核时间',
    `refund_trade_no` VARCHAR(100) DEFAULT NULL COMMENT '第三方退款号或模拟流水',
    `simulated` TINYINT NOT NULL DEFAULT 0 COMMENT '是否模拟退款：0否 1是',
    `refund_time` DATETIME DEFAULT NULL COMMENT '退款完成时间',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_refund_no` (`refund_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后退款申请表';
