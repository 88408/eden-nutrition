-- ----------------------------
-- Table structure for seckill (秒杀活动表)
-- ----------------------------
USE eden_db;
DROP TABLE IF EXISTS `seckill`;
CREATE TABLE `seckill` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '秒杀活动ID',
  `product_id` BIGINT NOT NULL COMMENT '关联的商品ID (关联product表)',
  `seckill_price` DECIMAL(10,2) NOT NULL COMMENT '秒杀价格',
  `stock` INT NOT NULL COMMENT '秒杀总库存 (剩余库存)',
  `limit_per_user` INT NOT NULL COMMENT '每人限购数量',
  `start_time` DATETIME NOT NULL COMMENT '秒杀开始时间',
  `end_time` DATETIME NOT NULL COMMENT '秒杀结束时间',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '活动状态: 0-未开始, 1-进行中, 2-已结束',
  `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_product_id` (`product_id`),
  KEY `idx_status_time` (`status`, `start_time`, `end_time`) COMMENT '用于加快进行中/即将开始等基于时间和状态的过滤'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀活动表';

