-- 八个预留功能落地所需的用户体验扩展表。

USE eden_db;

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

CREATE TABLE `support_message` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '客服消息ID',
    `session_id` BIGINT NOT NULL COMMENT '客服会话ID',
    `sender_type` VARCHAR(20) NOT NULL COMMENT '发送方：USER/STAFF/SYSTEM',
    `content` VARCHAR(1000) NOT NULL COMMENT '消息内容',
    `is_read` TINYINT DEFAULT 0 COMMENT '是否已读：0-未读 1-已读',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX `idx_session_time` (`session_id`, `create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='客服消息表';

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
