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

-- 插入示例订单 (6天前到现在)
INSERT INTO `order` (`id`, `order_no`, `user_id`, `total_amount`, `pay_amount`, `freight_amount`, `discount_amount`, `pay_type`, `status`, `order_type`, `receiver_name`, `receiver_phone`, `receiver_address`, `pay_time`, `create_time`) VALUES
(1, 'ORD202604020001', 1, 599.00, 599.00, 0.00, 0.00, 1, 3, 0, '张三', '13812345678', '上海市浦东新区某某路1号', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 'ORD202604020002', 1, 1299.00, 1299.00, 0.00, 0.00, 2, 3, 0, '李四', '13912345678', '北京市朝阳区某某路2号', DATE_SUB(NOW(), INTERVAL 6 DAY), DATE_SUB(NOW(), INTERVAL 6 DAY)),
(3, 'ORD202604030001', 1, 899.00, 899.00, 0.00, 0.00, 1, 3, 0, '王五', '13712345678', '广州市天河区某某路3号', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 'ORD202604030002', 1, 399.00, 399.00, 0.00, 0.00, 2, 3, 0, '赵六', '13612345678', '深圳市南山区某某路4号', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(5, 'ORD202604030003', 1, 128.00, 128.00, 0.00, 0.00, 1, 3, 0, '孙七', '13512345678', '杭州市西湖区某某路5号', DATE_SUB(NOW(), INTERVAL 5 DAY), DATE_SUB(NOW(), INTERVAL 5 DAY)),
(6, 'ORD202604040001', 1, 599.00, 569.00, 0.00, 30.00, 1, 3, 0, '周八', '13412345678', '苏州市吴中区某某路6号', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(7, 'ORD202604040002', 1, 1299.00, 1299.00, 0.00, 0.00, 2, 3, 0, '吴九', '13312345678', '南京市玄武区某某路7号', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(8, 'ORD202604040003', 1, 899.00, 899.00, 0.00, 0.00, 1, 3, 0, '郑十', '13212345678', '成都市武侯区某某路8号', DATE_SUB(NOW(), INTERVAL 4 DAY), DATE_SUB(NOW(), INTERVAL 4 DAY)),
(9, 'ORD202604050001', 1, 399.00, 399.00, 0.00, 0.00, 2, 3, 0, '钱十一', '13112345678', '重庆市渝中区某某路9号', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(10, 'ORD202604050002', 1, 256.00, 256.00, 0.00, 0.00, 1, 3, 0, '陈十二', '13012345678', '武汉市江汉区某某路10号', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(11, 'ORD202604050003', 1, 599.00, 599.00, 0.00, 0.00, 2, 2, 0, '林十三', '15812345678', '西安市雁塔区某某路11号', DATE_SUB(NOW(), INTERVAL 3 DAY), DATE_SUB(NOW(), INTERVAL 3 DAY)),
(12, 'ORD202604060001', 1, 1299.00, 1299.00, 0.00, 0.00, 1, 2, 0, '黄十四', '15912345678', '长沙市芙蓉区某某路12号', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(13, 'ORD202604060002', 1, 899.00, 849.00, 0.00, 50.00, 2, 2, 0, '朱十五', '15712345678', '青岛市市南区某某路13号', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(14, 'ORD202604060003', 1, 399.00, 399.00, 0.00, 0.00, 1, 2, 0, '秦十六', '15612345678', '大连市中山区某某路14号', DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY)),
(15, 'ORD202604070001', 1, 128.00, 128.00, 10.00, 0.00, 2, 1, 0, '尤十七', '15512345678', '宁波市海曙区某某路15号', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(16, 'ORD202604070002', 1, 599.00, 599.00, 0.00, 0.00, 1, 1, 0, '许十八', '15412345678', '厦门市思明区某某路16号', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(17, 'ORD202604070003', 1, 1299.00, 1299.00, 0.00, 0.00, 2, 1, 0, '何十九', '15312345678', '福州市鼓楼区某某路17号', DATE_SUB(NOW(), INTERVAL 1 DAY), DATE_SUB(NOW(), INTERVAL 1 DAY)),
(18, 'ORD202604080001', 1, 899.00, 899.00, 0.00, 0.00, 1, 0, 0, '吕二十', '15212345678', '无锡市滨湖区某某路18号', NULL, NOW()),
(19, 'ORD202604080002', 1, 399.00, 399.00, 0.00, 0.00, 2, 0, 0, '施二十一', '15112345678', '常州市新北区某某路19号', NULL, NOW()),
(20, 'ORD202604080003', 1, 128.00, 128.00, 10.00, 0.00, 1, 0, 0, '张二十二', '15012345678', '南通市崇川区某某路20号', NULL, NOW());

-- 插入示例订单明细
INSERT INTO `order_item` (`order_id`, `order_no`, `product_id`, `product_name`, `product_image`, `price`, `quantity`, `total_price`, `create_time`) VALUES
(1, 'ORD202604020001', 1, '印尼进口即食燕窝 70g*6瓶', '/images/yanwo.jpg', 599.00, 1, 599.00, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(2, 'ORD202604020002', 2, '长白山野山参 10年参龄 50g', '/images/shanshen.jpg', 1299.00, 1, 1299.00, DATE_SUB(NOW(), INTERVAL 6 DAY)),
(3, 'ORD202604030001', 3, '东阿阿胶块 250g 礼盒装', '/images/ejiao.jpg', 899.00, 1, 899.00, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(4, 'ORD202604030002', 4, '破壁灵芝孢子粉 1g*60袋', '/images/lingzhi.jpg', 399.00, 1, 399.00, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(5, 'ORD202604030003', 5, '宁夏枸杞王 特级 500g', '/images/gouqi.jpg', 128.00, 1, 128.00, DATE_SUB(NOW(), INTERVAL 5 DAY)),
(6, 'ORD202604040001', 1, '印尼进口即食燕窝 70g*6瓶', '/images/yanwo.jpg', 599.00, 1, 599.00, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(7, 'ORD202604040002', 2, '长白山野山参 10年参龄 50g', '/images/shanshen.jpg', 1299.00, 1, 1299.00, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(8, 'ORD202604040003', 3, '东阿阿胶块 250g 礼盒装', '/images/ejiao.jpg', 899.00, 1, 899.00, DATE_SUB(NOW(), INTERVAL 4 DAY)),
(9, 'ORD202604050001', 4, '破壁灵芝孢子粉 1g*60袋', '/images/lingzhi.jpg', 399.00, 1, 399.00, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(10, 'ORD202604050002', 5, '宁夏枸杞王 特级 500g', '/images/gouqi.jpg', 128.00, 2, 256.00, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(11, 'ORD202604050003', 1, '印尼进口即食燕窝 70g*6瓶', '/images/yanwo.jpg', 599.00, 1, 599.00, DATE_SUB(NOW(), INTERVAL 3 DAY)),
(12, 'ORD202604060001', 2, '长白山野山参 10年参龄 50g', '/images/shanshen.jpg', 1299.00, 1, 1299.00, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(13, 'ORD202604060002', 3, '东阿阿胶块 250g 礼盒装', '/images/ejiao.jpg', 899.00, 1, 899.00, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(14, 'ORD202604060003', 4, '破壁灵芝孢子粉 1g*60袋', '/images/lingzhi.jpg', 399.00, 1, 399.00, DATE_SUB(NOW(), INTERVAL 2 DAY)),
(15, 'ORD202604070001', 5, '宁夏枸杞王 特级 500g', '/images/gouqi.jpg', 128.00, 1, 128.00, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(16, 'ORD202604070002', 1, '印尼进口即食燕窝 70g*6瓶', '/images/yanwo.jpg', 599.00, 1, 599.00, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(17, 'ORD202604070003', 2, '长白山野山参 10年参龄 50g', '/images/shanshen.jpg', 1299.00, 1, 1299.00, DATE_SUB(NOW(), INTERVAL 1 DAY)),
(18, 'ORD202604080001', 3, '东阿阿胶块 250g 礼盒装', '/images/ejiao.jpg', 899.00, 1, 899.00, NOW()),
(19, 'ORD202604080002', 4, '破壁灵芝孢子粉 1g*60袋', '/images/lingzhi.jpg', 399.00, 1, 399.00, NOW()),
(20, 'ORD202604080003', 5, '宁夏枸杞王 特级 500g', '/images/gouqi.jpg', 128.00, 1, 128.00, NOW());
