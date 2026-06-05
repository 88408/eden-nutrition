# 秒杀异步 MQ 链路修复记录

[2026-05-28 23:04:13] 修改摘要
- 修改内容：秒杀提交入口改为返回 `SeckillSubmitVO` 处理中状态，发送 `SeckillOrderMessage` 到正确的 `SECKILL_ORDER_ROUTING_KEY`；新增秒杀结果查询接口；MQ 消费端改为消费强类型消息，事务内扣减 `seckill.stock`、创建订单、订单明细和 `seckill_order`，写入 Redis 成功/失败结果，并在手动确认模式下显式 `basicAck`。
- 数据修复：确认本地 `seckillId=2/userId=4` 存在 Redis 孤儿参与标记且 DB 无 `seckill_order`，已清理 `eden:seckill:user:2` 中的该用户标记，并将 `eden:seckill:stock:2` 从 19 修复为 20，与 DB `seckill.stock=20` 对齐。
- 验证结果：`mvn -pl eden-service -am "-Dtest=SeckillServiceImplTest,SeckillMessageListenerTest" -DfailIfNoTests=false test` 通过；`mvn -pl eden-admin -am -DskipTests compile` 通过。

[2026-05-29 11:56:53] 修改摘要
- 修改内容：修复运行库 `eden_db.order` 缺少 `order_type` 导致秒杀 MQ 消费插入订单时报 `Unknown column 'order_type' in 'field list'` 的问题，已执行前向迁移新增 `order_type TINYINT DEFAULT 0 COMMENT '订单类型：0-普通订单 1-秒杀订单 2-团购订单'`，并将历史 NULL 值补为 0。
- 数据修复：迁移前后检查 Redis 不存在 `eden:seckill:user:*` 孤儿参与集合，本次无需额外清理秒杀用户标记或回补 Redis 库存。
- 验证结果：确认 `order_type` 字段存在、默认值为 0、历史订单无 NULL；`mvn -pl eden-service -am "-Dtest=SeckillServiceImplTest,SeckillMessageListenerTest" -DfailIfNoTests=false test` 通过；`mvn -pl eden-admin -am -DskipTests compile` 通过。
