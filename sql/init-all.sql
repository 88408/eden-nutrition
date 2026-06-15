-- ============================================
-- Eden Nutrition 一键初始化脚本
-- 按顺序执行所有 SQL 文件
-- ============================================

-- 使用方法:
-- 方式1: 在 MySQL 客户端中依次执行各文件
-- 方式2: 使用 source 命令执行本文件（需要调整路径）

-- 执行顺序:
-- 1. 01-init-database.sql  - 创建数据库
-- 2. 02-user.sql           - 用户模块
-- 3. 03-product.sql        - 商品模块
-- 4. 04-order.sql          - 订单模块
-- 5. 05-promotion.sql      - 促销模块
-- 6. 06-system.sql         - 系统模块
-- 7. 07-init-data.sql      - 初始化数据
-- 8. 08-user-experience.sql - 客服、通知等用户体验扩展模块
-- 9. 09-runtime-compat.sql  - 老运行库兼容性增量迁移
-- 10. 10-full-score-iteration.sql - RBAC、SKU、退款售后、真实报表配套迁移

-- 如果使用 Docker，可以用以下命令批量执行:
-- 
-- Windows PowerShell:
-- Get-ChildItem -Path "sql" -Filter "*.sql" | Sort-Object Name | ForEach-Object { Get-Content $_.FullName } | docker exec -i eden-mysql mysql -u root -prootpass
--
-- 或者逐个执行:
-- docker exec -i eden-mysql mysql -u root -prootpass < sql/01-init-database.sql
-- docker exec -i eden-mysql mysql -u root -prootpass < sql/02-user.sql
-- docker exec -i eden-mysql mysql -u root -prootpass < sql/03-product.sql
-- docker exec -i eden-mysql mysql -u root -prootpass < sql/04-order.sql
-- docker exec -i eden-mysql mysql -u root -prootpass < sql/05-promotion.sql
-- docker exec -i eden-mysql mysql -u root -prootpass < sql/06-system.sql
-- docker exec -i eden-mysql mysql -u root -prootpass < sql/07-init-data.sql
-- docker exec -i eden-mysql mysql -u root -prootpass < sql/08-user-experience.sql
-- docker exec -i eden-mysql mysql -u root -prootpass < sql/09-runtime-compat.sql
-- docker exec -i eden-mysql mysql -u root -prootpass < sql/10-full-score-iteration.sql
-- docker exec -i eden-mysql mysql -u root -prootpass < sql/11-product-images.sql

-- 已有运行库升级时，优先执行 09-runtime-compat.sql 和 10-full-score-iteration.sql；这些脚本只补字段、索引、缺失表和演示种子数据，不会清空现有数据。
