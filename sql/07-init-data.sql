-- ============================================
-- 初始化数据
-- ============================================

USE eden_db;

-- 插入管理员用户（密码: admin123，使用BCrypt加密后的值）
INSERT INTO `user` (`username`, `phone`, `password`, `nickname`, `role`, `status`) VALUES
('admin', '13800000000', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iectP3IA2M7DX5GmJKrL4XH5Y.CK', '管理员', 'ADMIN', 1);

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

-- 插入秒杀活动 (关联商品1, 2, 3)
INSERT INTO `seckill` (`product_id`, `seckill_price`, `stock`, `limit_per_user`, `start_time`, `end_time`, `status`) VALUES
(1, 499.00, 50, 1, DATE_SUB(NOW(), INTERVAL 1 HOUR), DATE_ADD(NOW(), INTERVAL 2 HOUR), 1), -- 进行中
(2, 999.00, 20, 2, DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY), 0),   -- 未开始
(3, 699.00, 0, 1, DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY), 2);    -- 已结束
