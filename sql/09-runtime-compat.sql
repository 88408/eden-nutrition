-- ============================================
-- 运行库兼容性增量迁移脚本
-- 用于补齐历史数据库中缺失的支付、通知与客服结构。
-- 本脚本只做新增字段/索引/表，不删除或重建已有业务数据，可重复执行。
-- ============================================

USE eden_db;

-- 订单支付流水号字段：当前 Mapper 与支付回调会读写该字段，老库缺失时会导致定时任务查询失败。
SET @has_payment_trade_no := (
    SELECT COUNT(*)
    FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'order'
      AND COLUMN_NAME = 'payment_trade_no'
);
SET @ddl := IF(
    @has_payment_trade_no = 0,
    'ALTER TABLE `order` ADD COLUMN `payment_trade_no` VARCHAR(100) DEFAULT NULL COMMENT ''第三方支付交易号'' AFTER `pay_type`',
    'SELECT ''payment_trade_no already exists'' AS message'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 支付流水号索引用于支付回调、排查与后续查询扩展；已存在时跳过。
SET @has_payment_trade_no_index := (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'order'
      AND INDEX_NAME = 'idx_payment_trade_no'
);
SET @ddl := IF(
    @has_payment_trade_no_index = 0,
    'ALTER TABLE `order` ADD INDEX `idx_payment_trade_no` (`payment_trade_no`)',
    'SELECT ''idx_payment_trade_no already exists'' AS message'
);
PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 客服会话表：使用 IF NOT EXISTS 避免升级脚本误清空已有客服会话。
CREATE TABLE IF NOT EXISTS `support_session` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '客服会话ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `product_id` BIGINT DEFAULT NULL COMMENT '来源商品ID',
    `status` TINYINT DEFAULT 1 COMMENT '状态：0-关闭 1-进行中',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX `idx_user_product` (`user_id`, `product_id`),
    INDEX `idx_update_time` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服会话表';

-- 客服消息表：只在缺表时创建，保留已有消息数据。
CREATE TABLE IF NOT EXISTS `support_message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '客服消息ID',
    `session_id` BIGINT NOT NULL COMMENT '客服会话ID',
    `sender_type` VARCHAR(20) NOT NULL COMMENT '发送方：USER/STAFF/SYSTEM',
    `content` VARCHAR(1000) NOT NULL COMMENT '消息内容',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_session_time` (`session_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服消息表';

-- 站内通知表：支撑首页未读数和通知中心，升级脚本不能覆盖用户已读状态。
CREATE TABLE IF NOT EXISTS `notice` (
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
