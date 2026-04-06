-- ============================================
-- 商品模块表结构
-- ============================================

USE eden_db;

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
    `main_image` LONGTEXT COMMENT '主图Base64或URL',
    `sub_images` LONGTEXT COMMENT '商品图片列表（JSON数组）',
    `detail` LONGTEXT COMMENT '商品详情（富文本）',
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
