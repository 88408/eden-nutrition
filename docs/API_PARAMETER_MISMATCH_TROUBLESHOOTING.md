# 接口参数不匹配与图片超长问题排查记录

## 1. `POST /admin/seckill` 500 (Internal Server Error)
**问题描述：**
管理端在保存/修改秒杀活动时，浏览器控制台抛出 `500 Internal Server Error`，而对应的后端日志给出了 `UnrecognizedPropertyException: Unrecognized field "stock"`。

**原因分析：**
在后端的 `AdminSeckillSaveDTO` 模型中，接收秒杀库存量的字段被定义成了 `stockCount`，而不是 `stock`。但在前端 `seckill.ts` 中定义的类型和表单 `SeckillList.tsx` 内部，均使用了名称 `stock` 进行数据绑定（由于后端列表返回时包含了 `stock`，导致前端误认为保存和提交也是使用 `stock` 字段）。当 Spring Boot 的 Jackson 解析 JSON 时，遇到严格未定义的属性 `stock` 便会产生反序列化失败的 500 异常。

**解决方案：**
1. 在 `seckill.ts` 中的 `SeckillVO` 增加了对 `stockCount` 字段的定义。
2. 将 `SeckillList.tsx` 中 `formData` 使用的名由 `stock` 全面变更为 `stockCount` 并重构了表单提交事件，确保以 `stockCount: Number(formData.stockCount)` 传至后端，准确符合 DTO 的消费期望。

---

## 2. `PUT /admin/product` 500 (Internal Server Error)
**问题描述：**
管理端更新商品时偶尔触发 500 异常。

**原因分析：**
在之前解决 `Data too long for column 'main_image'` 的问题时，我们仅修改了位于 `sql/schema.sql` 和 `sql/03-product.sql` 的源代码结构将 `VARCHAR(500)` 修改为了 `LONGTEXT`。
由于项目没有使用自动的数据库迁移脚本（如 Flyway/Liquibase），这意味正在运行的 `eden_db` 中的底层 `product` 真实物理表字段**尚未**应用该变化，依然处于 `VARCHAR(500)` 状态。当带有几百万字符的 base64 编码的图片更新时，会导致 MySql 的数据截断（Data truncation）异常，继而使得更新操作失败抛出 500 错误。

**解决方案：**
已经在后台连接至开发环境的 MySQL 容器，直接进入数据库内部以 `root/eden` 账户通过执行 DDL (Data Definition Language) 补丁彻底解决了问题。
执行了如下命令：
```sql
ALTER TABLE eden_db.product MODIFY COLUMN main_image LONGTEXT; 
ALTER TABLE eden_db.product MODIFY COLUMN sub_images LONGTEXT; 
ALTER TABLE eden_db.product MODIFY COLUMN detail LONGTEXT;
```
自此，物理表正式拓展容量，支持无长度上限的 Base64 编码图数据直写。