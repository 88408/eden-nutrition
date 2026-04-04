# 后台订单列表分页异常修复说明

## 1. 问题现象

调用后台订单列表接口 `GET /admin/order/list` 时，后端返回 `500`。

日志中的核心异常为：

- `MyBatisSystemException`
- `NoSuchPropertyException: eden.pojo.dto.AdminOrderQueryDTO.limit`

## 2. 根因分析

问题出在 `eden-mapper/src/main/resources/mapper/OrderMapper.xml` 的管理端订单列表 SQL：

```xml
<if test="query.offset != null and query.limit != null">
    LIMIT #{query.offset}, #{query.limit}
</if>
```

但实际传入的 `AdminOrderQueryDTO` 继承自 `PageDTO`，现有分页字段只有：

- `pageNum`
- `pageSize`
- `getOffset()`

并不存在 `limit` 字段，所以 MyBatis 在解析 `query.limit` 时抛出异常，导致接口返回 500。

## 3. 本次修复

将分页条件改为基于现有字段判断：

- 使用 `query.pageSize` 判断是否需要分页
- `LIMIT` 长度改为 `#{query.pageSize}`
- 起始偏移量仍然使用 `#{query.offset}`，它来自 `PageDTO#getOffset()`

修复后，SQL 只依赖 DTO 中真实存在的分页字段，不再引用不存在的 `limit`。

## 4. 影响范围

本次改动仅影响管理端订单列表查询：

- `OrderMapper.xml` 中 `selectAdminOrderList`

不需要修改：

- `AdminOrderController`
- `OrderServiceImpl.getAdminOrderPage`
- `AdminOrderQueryDTO`
- `PageDTO`

原因是这些层已经正确提供了分页参数并完成了参数归一化，错误只在 Mapper 的动态 SQL 判断条件。

## 5. 验证结论

修复后，订单列表接口应正常完成分页 SQL 组装，不再因为 `query.limit` 缺失而抛出异常。

如果后续仍有 500，应继续检查：

- 数据库表字段是否与 SQL 映射一致
- 前端是否正确传递 `pageNum/pageSize`
- 是否存在其他 Mapper 动态条件引用了不存在的属性

