# 故障排查与修复方案：秒杀服务编译错误

## 1. 问题描述
在 `eden-service` 模块的 `SeckillServiceImpl.java` 中存在多处编译错误，导致代码不完整且无法构建。

### 1.1 错误清单
1.  **ResultCode 缺失枚举值**:
    *   `SECKILL_NOT_START`: 秒杀尚未开始
    *   `SECKILL_ENDED`: 秒杀已经结束
    *   `SECKILL_NO_STOCK`: 库存不足
    *   `SECKILL_REPEAT`: 重复秒杀
2.  **SeckillOrder 实体属性缺失**:
    *   `orderNo`: 订单编号
    *   `amount`: 秒杀金额
    *   `status`: 订单状态
3.  **MQConstants 常量缺失**:
    *   `SECKILL_ROUTING_KEY`: 秒杀消息路由键

## 2. 根本原因
代码在迁移或重构过程中，业务逻辑层 (`SeckillServiceImpl`) 引用了尚未在公共模块 (`eden-common`) 和 POJO 模块 (`eden-pojo`) 中定义的常量和字段。

## 3. 修复方案

### 3.1 修复 eden-common
**文件**: `eden-common/src/main/java/eden/common/result/ResultCode.java`
*   添加缺失的秒杀相关错误码枚举。

**文件**: `eden-common/src/main/java/eden/common/constant/MQConstants.java`
*   添加 `SECKILL_ROUTING_KEY` 常量定义。

### 3.2 修复 eden-pojo
**文件**: `eden-pojo/src/main/java/eden/pojo/SeckillOrder.java`
*   添加 `orderNo`, `amount`, `status` 字段及其 Lombok 注解。

## 4. 执行计划
1.  修改 `ResultCode.java` 添加枚举值。
2.  修改 `MQConstants.java` 添加路由键。
3.  修改 `SeckillOrder.java` 添加实体字段。
