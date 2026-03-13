-- ============================================
-- Eden Nutrition 初始数据插入脚本 (程序制品)
-- 包含：产品类别管理 (服装类别、菜品类别、房型类别)
-- 包含：产品管理 (服装、菜品及套餐、房间)
-- ============================================

USE eden_db;

-- 1. 插入一级分类 (产品类别管理)
-- ID: 100 - 服装类别
-- ID: 200 - 菜品类别 (健康餐)
-- ID: 300 - 房型类别 (疗养住宿)
INSERT INTO category (id, name, parent_id, level, sort_order, icon, status, create_time, update_time) VALUES 
(100, 'Eden运动服饰', 0, 1, 10, 'http://oss.eden-nutrition.com/icons/clothing.png', 1, NOW(), NOW()),
(200, 'Eden健康食疗', 0, 1, 20, 'http://oss.eden-nutrition.com/icons/food.png', 1, NOW(), NOW()),
(300, 'Eden疗养住宿', 0, 1, 30, 'http://oss.eden-nutrition.com/icons/room.png', 1, NOW(), NOW());

-- 2. 插入二级分类
-- 服装子类：瑜伽服, 运动T恤
INSERT INTO category (id, name, parent_id, level, sort_order, icon, status, create_time, update_time) VALUES 
(101, '瑜伽服/裤', 100, 2, 1, NULL, 1, NOW(), NOW()),
(102, '速干T恤', 100, 2, 2, NULL, 1, NOW(), NOW());

-- 菜品子类：减脂餐, 增肌餐
INSERT INTO category (id, name, parent_id, level, sort_order, icon, status, create_time, update_time) VALUES 
(201, '减脂套餐', 200, 2, 1, NULL, 1, NOW(), NOW()),
(202, '增肌能量碗', 200, 2, 2, NULL, 1, NOW(), NOW());

-- 房型子类：单人静修房, 双人疗愈套房
INSERT INTO category (id, name, parent_id, level, sort_order, icon, status, create_time, update_time) VALUES 
(301, '单人静修房', 300, 2, 1, NULL, 1, NOW(), NOW()),
(302, '双人疗愈套房', 300, 2, 2, NULL, 1, NOW(), NOW());


-- 3. 插入商品数据 (产品管理)
-- 3.1 服装产品
INSERT INTO product (
    id, category_id, name, subtitle, main_image, sub_images, detail, 
    original_price, price, stock, sales, status, is_hot, is_new, create_time, update_time
) VALUES (
    1001, 101, 'Eden专业高腰瑜伽裤', '裸感亲肤，吸湿排汗，塑形显瘦', 
    'http://oss.eden-nutrition.com/products/yoga-pants-main.jpg', 
    '["http://oss.eden-nutrition.com/products/yoga-pants-1.jpg", "http://oss.eden-nutrition.com/products/yoga-pants-2.jpg"]', 
    '<p>采用进口高弹面料，四向弹力，随心而动。适合瑜伽、普拉提等运动。</p><img src="http://oss.eden-nutrition.com/details/size-chart.jpg"/>',
    399.00, 298.00, 500, 120, 1, 1, 1, NOW(), NOW()
);

-- 3.2 菜品及套餐
INSERT INTO product (
    id, category_id, name, subtitle, main_image, sub_images, detail, 
    original_price, price, stock, sales, status, is_hot, is_new, create_time, update_time
) VALUES (
    2001, 201, '7日轻断食排毒蔬果汁套餐', '冷压鲜榨，每日配送，清肠排毒', 
    'http://oss.eden-nutrition.com/products/juice-set-main.jpg', 
    '["http://oss.eden-nutrition.com/products/juice-1.jpg"]', 
    '<p>精选有机蔬果，科学配比，不仅美味更健康。包含：羽衣甘蓝汁、胡萝卜姜汁等。</p>',
    599.00, 458.00, 100, 56, 1, 1, 0, NOW(), NOW()
);

-- 3.3 房间 (住宿产品)
INSERT INTO product (
    id, category_id, name, subtitle, main_image, sub_images, detail, 
    original_price, price, stock, sales, status, is_hot, is_new, create_time, update_time
) VALUES (
    3001, 301, '森林氧吧静修单人房(含三餐)', '远离尘嚣，森林景观，含全天健康餐饮', 
    'http://oss.eden-nutrition.com/products/room-single-main.jpg', 
    '["http://oss.eden-nutrition.com/products/room-1.jpg", "http://oss.eden-nutrition.com/products/room-2.jpg"]', 
    '<h3>房型介绍</h3><p>35平米独立空间，配有冥想角、香薰机。提供定制化健康餐饮服务。</p>',
    1288.00, 888.00, 10, 30, 1, 0, 1, NOW(), NOW()
);

-- ============================================
-- End of Script
-- ============================================