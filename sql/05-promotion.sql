-- ============================================
-- 促销模块表结构（优惠券、秒杀）
-- ============================================

USE eden_db;

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
