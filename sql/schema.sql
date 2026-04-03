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
    `payment_method` VARCHAR(50) DEFAULT NULL COMMENT '支付方式',
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
    `product_name` VARCHAR(200) NOT NULL COMMENT '商品名称（快照）',
    `product_image` VARCHAR(500) COMMENT '商品图片（快照）',
    `price` DECIMAL(10, 2) NOT NULL COMMENT '商品单价（快照）',
    `quantity` INT NOT NULL COMMENT '购买数量',
    `total_price` DECIMAL(10, 2) NOT NULL COMMENT '小计金额',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_product_id` (`product_id`)
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
    `seckill_id` BIGINT NOT NULL COMMENT '秒杀ID',
    `product_id` BIGINT NOT NULL COMMENT '商品ID',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_seckill` (`user_id`, `seckill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';

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
-- 初始化数据
-- ============================================

-- 插入管理员用户（密码: admin123，使用BCrypt加密后的值）
INSERT INTO `user` (`username`, `phone`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '13800000000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iectP3IA2M7DX5GmJKrL4XH5Y.CK', '管理员', 'ADMIN', 1);

-- 插入商品分类
INSERT INTO `category` (`name`, `parent_id`, `level`, `sort_order`) VALUES
('燕窝', 0, 1, 1),
('人参', 0, 1, 2),
('阿胶', 0, 1, 3),
('灵芝', 0, 1, 4),
('枸杞', 0, 1, 5),
('虫草', 0, 1, 6);

-- 插入二级分类
INSERT INTO `category` (`name`, `parent_id`, `level`, `sort_order`) VALUES
('即食燕窝', 1, 2, 1),
('干燕窝', 1, 2, 2),
('燕窝礼盒', 1, 2, 3),
('野山参', 2, 2, 1),
('高丽参', 2, 2, 2),
('西洋参', 2, 2, 3);

-- 插入示例商品
INSERT INTO `product` (`name`, `subtitle`, `category_id`, `main_image`, `sub_images`, `detail`, `original_price`, `price`, `stock`, `sales`, `status`, `is_hot`) VALUES
('印尼进口即食燕窝 70g*6瓶', '精选印尼金丝燕窝', 7, '/images/yanwo.jpg', '[]', '精选印尼金丝燕窝，开盖即食，方便营养', 799.00, 599.00, 100, 256, 1, 1),
('长白山野山参 10年参龄 50g', '长白山原产地直供', 10, '/images/shanshen.jpg', '[]', '长白山原产地直供，10年以上参龄，品质保证', 1599.00, 1299.00, 50, 128, 1, 1),
('东阿阿胶块 250g 礼盒装', '国家非物质文化遗产', 3, '/images/ejiao.jpg', '[]', '国家非物质文化遗产，正宗东阿阿胶', 999.00, 899.00, 200, 512, 1, 1),
('破壁灵芝孢子粉 1g*60袋', '高破壁率，易吸收', 4, '/images/lingzhi.jpg', '[]', '高破壁率，易吸收，增强免疫力', 499.00, 399.00, 150, 320, 1, 0),
('宁夏枸杞王 特级 500g', '宁夏中宁原产地', 5, '/images/gouqi.jpg', '[]', '宁夏中宁原产地，颗粒饱满，天然晾晒', 168.00, 128.00, 300, 890, 1, 1);

-- 插入示例优惠券
INSERT INTO `coupon` (`name`, `type`, `value`, `min_amount`, `total_count`, `remain_count`, `start_time`, `end_time`) VALUES
('新人专享50元券', 1, 50.00, 200.00, 1000, 1000, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY)),
('满300减30', 1, 30.00, 300.00, 500, 500, NOW(), DATE_ADD(NOW(), INTERVAL 15 DAY)),
('会员9折券', 2, 0.90, 100.00, 200, 200, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY));
