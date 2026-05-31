-- ============================================
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
(1, '印尼进口即食燕窝 70g*6瓶', '精选印尼金丝燕窝', 7, '/images/yanwo.jpg', '[]', '精选印尼金丝燕窝，开盖即食，方便营养', 799.00, 599.00, 100, 256, 1, 1, 0),
(2, '长白山野山参 10年参龄 50g', '长白山原产地直供', 10, '/images/shanshen.jpg', '[]', '长白山原产地直供，10年以上参龄，品质保证', 1599.00, 1299.00, 50, 128, 1, 1, 0),
(3, '东阿阿胶块 250g 礼盒装', '国家非物质文化遗产', 3, '/images/ejiao.jpg', '[]', '国家非物质文化遗产，正宗东阿阿胶', 999.00, 899.00, 200, 512, 1, 1, 0),
(4, '破壁灵芝孢子粉 1g*60袋', '高破壁率，易吸收', 4, '/images/lingzhi.jpg', '[]', '高破壁率，易吸收，增强免疫力', 499.00, 399.00, 150, 320, 1, 0, 0),
(5, '宁夏枸杞王 特级 500g', '宁夏中宁原产地', 5, '/images/gouqi.jpg', '[]', '宁夏中宁原产地，颗粒饱满，天然晾晒', 168.00, 128.00, 300, 890, 1, 1, 0);

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
