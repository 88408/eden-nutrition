# 商品主图上传 (main_image) SQL Too Long 故障排查与修复

## 1. 错误现象 (Description)
你在前端管理面板“编辑商品”或“添加商品”并上传了商品主图后提交保存，前端随即报出 500 (Internal Server Error)，此时后端日志输出了数据库层面的截断异常：
```log
org.springframework.dao.DataIntegrityViolationException: 
### Cause: com.mysql.cj.jdbc.exceptions.MysqlDataTruncation: Data truncation: Data too long for column 'main_image' at row 1
```
这意味着后端正在尝试更新商品数据到数据库 (`product` 表)，但是传入到 `main_image` 字段的数据长度超出了 MySQL 中给该字段设立的最大存储长度限制。

## 2. 问题原因分析 (Root Cause)
1. **前端图片处理机制**：根据我们在前端 `ProductList.tsx` 的图片上传实现：
   ```tsx
   const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
     // ...
     const reader = new FileReader();
     reader.onloadend = () => {
       setFormData({ ...formData, mainImage: reader.result as string });
     };
     reader.readAsDataURL(file); // 核心：转化成了Base64长字符串
   };
   ```
   前端直接将图片转化为了完整的 **Base64 字符串**，这种格式虽然方便（免去了搭建图片专门服务器/对象存储 OSS 的成本），但其生成的文本长度极其庞大，2MB 的图片转成的 Base64 长度会达到几百万个字符。
2. **后端数据库定义的冲突**：我们在设计数据库表时，`sql/schema.sql` 对 `product` 表的 `main_image` 字段的定义是 `VARCHAR(500)`（这原本是预留给图片云端外链 URL 准备的）。
3. 当带有几百万字符的图片 Base64 企图塞进只允许 500 字符的字符串列里时，MySQL 就会直接挂起保护限制并报错 `Data truncation`。

## 3. 解决方案 (Solution)
鉴于当前系统环境偏向通过纯 Base64 来传输、展示图片，最彻底、无痛的方案是在你的 MySQL 数据库中直接将存放商品图片的列扩大到可放纳百万字符的无限制长文本类型（**LONGTEXT**）。

**请你打开你的数据库客户端（如 Navicat, DataGrip 或是本地 mysql 命令行）**，
选择你的数据库库名（通常是 `eden_nutrition`），打开查询控制台并执行以下补丁 SQL 语句：

```sql
-- 将商品主图及其它图片相关列的容纳数据类型改为可存储 Base64 的超长文本 (LONGTEXT)
ALTER TABLE `product` MODIFY COLUMN `main_image` LONGTEXT COMMENT '主图URL或Base64';
ALTER TABLE `product` MODIFY COLUMN `sub_images` LONGTEXT COMMENT '副图URL或Base64数组';
ALTER TABLE `product` MODIFY COLUMN `detail` LONGTEXT COMMENT '商品详情HTML或Base64内容';
```

## 目前项目的修复
由于我的终端没法直接调用你的本地 `mysql` 进程工具进行更改，我已将对应的修改 SQL 和解释出具在上文。**请手动在你的数据库管理工具里执行这三行 SQL**。
执行成功后，不管你在前端上传几 MB 的 Base64 图片保存，都不会再发生截断溢出报错了，它将平稳地储存在数据库中。