-- ============================================
-- Eden Nutrition 满分迭代增量迁移
-- 覆盖：RBAC、商品 SKU、退款售后、订单明细规格快照
-- ============================================

USE `eden_db`;

-- 订单明细增加 SKU 快照字段，保留历史订单展示的规格信息。
SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_item' AND COLUMN_NAME = 'sku_id'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE `order_item` ADD COLUMN `sku_id` BIGINT NULL COMMENT ''商品SKU ID'' AFTER `product_id`',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_item' AND COLUMN_NAME = 'sku_spec_name'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE `order_item` ADD COLUMN `sku_spec_name` VARCHAR(120) NULL COMMENT ''SKU规格快照'' AFTER `sku_id`',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

SET @idx_exists := (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'order_item' AND INDEX_NAME = 'idx_sku_id'
);
SET @sql := IF(@idx_exists = 0,
    'ALTER TABLE `order_item` ADD INDEX `idx_sku_id` (`sku_id`)',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

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
    UNIQUE KEY `uk_permission_code` (`code`),
    KEY `idx_parent_id` (`parent_id`)
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
    UNIQUE KEY `uk_refund_no` (`refund_no`),
    KEY `idx_order_no` (`order_no`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='售后退款申请表';

SET @col_exists := (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'refund_apply' AND COLUMN_NAME = 'original_order_status'
);
SET @sql := IF(@col_exists = 0,
    'ALTER TABLE `refund_apply` ADD COLUMN `original_order_status` TINYINT NULL COMMENT ''申请退款前订单状态'' AFTER `refund_amount`',
    'SELECT 1'
);
PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- 权限种子：菜单和按钮/接口权限分开，便于演示按钮级控制。
INSERT INTO `permission` (`id`, `parent_id`, `name`, `code`, `path`, `component`, `icon`, `type`, `sort_order`, `status`) VALUES
(1, 0, '仪表盘', 'dashboard:view', '/dashboard', 'Dashboard', 'LayoutDashboard', 1, 10, 1),
(2, 0, '商品管理', 'product:view', '/products', 'ProductList', 'Package', 1, 20, 1),
(3, 2, '新增商品', 'product:create', NULL, NULL, NULL, 2, 21, 1),
(4, 2, '修改商品', 'product:update', NULL, NULL, NULL, 2, 22, 1),
(5, 2, '删除商品', 'product:delete', NULL, NULL, NULL, 2, 23, 1),
(6, 0, '订单管理', 'order:view', '/orders', 'OrderList', 'ShoppingCart', 1, 30, 1),
(7, 6, '订单发货', 'order:deliver', NULL, NULL, NULL, 2, 31, 1),
(8, 0, '秒杀管理', 'seckill:manage', '/seckill', 'SeckillList', 'Zap', 1, 40, 1),
(9, 0, '退款售后', 'refund:view', '/refunds', 'RefundList', 'Undo2', 1, 50, 1),
(10, 9, '退款审核', 'refund:audit', NULL, NULL, NULL, 2, 51, 1),
(11, 9, '执行退款', 'refund:execute', NULL, NULL, NULL, 2, 52, 1),
(12, 0, 'RBAC权限', 'rbac:manage', '/roles', 'RoleList', 'ShieldCheck', 1, 60, 1)
ON DUPLICATE KEY UPDATE
    `name` = VALUES(`name`), `parent_id` = VALUES(`parent_id`), `path` = VALUES(`path`),
    `component` = VALUES(`component`), `icon` = VALUES(`icon`), `type` = VALUES(`type`),
    `sort_order` = VALUES(`sort_order`), `status` = VALUES(`status`);

INSERT INTO `role` (`id`, `name`, `code`, `description`, `status`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', '拥有全部后台权限', 1),
(2, '商品管理员', 'PRODUCT_MANAGER', '维护商品、规格和库存', 1),
(3, '订单管理员', 'ORDER_MANAGER', '处理订单和发货', 1),
(4, '运营管理员', 'OPS_MANAGER', '查看报表并维护秒杀活动', 1),
(5, '客服管理员', 'SUPPORT_MANAGER', '处理退款售后', 1)
ON DUPLICATE KEY UPDATE `name` = VALUES(`name`), `description` = VALUES(`description`), `status` = VALUES(`status`);

INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`)
SELECT 1, id FROM `permission`;
INSERT IGNORE INTO `role_permission` (`role_id`, `permission_id`) VALUES
(2, 2), (2, 3), (2, 4), (2, 5),
(3, 6), (3, 7),
(4, 1), (4, 8),
(5, 9), (5, 10), (5, 11);

INSERT IGNORE INTO `user_role` (`user_id`, `role_id`)
SELECT id, 1 FROM `user` WHERE username = 'admin';

-- 为现有商品补充一组默认 SKU，保证商品详情页和后台规格管理可直接演示。
INSERT INTO `product_sku` (`product_id`, `spec_name`, `flavor`, `package_size`, `price`, `stock`, `image_url`, `status`)
SELECT p.id, '标准装', '原味', '1瓶/盒', p.price, p.stock, p.main_image, 1
FROM `product` p
WHERE NOT EXISTS (SELECT 1 FROM `product_sku` s WHERE s.product_id = p.id);
