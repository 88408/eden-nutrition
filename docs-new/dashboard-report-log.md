[2026-06-09 23:55:00] 满分迭代：真实报表和管理端仪表盘
- 修改内容：新增报表 Mapper、Service 和 `/admin/report/*` 接口，支持分类销售额、销售趋势、订单趋势、热销商品和 CSV 导出；管理端 Dashboard 替换静态 mock，改为调用真实报表接口并支持时间筛选和导出。
- 注意事项：CSV 导出接口返回 Base64 文本，前端负责解码下载；用户端 H5 构建存在入口体积警告，不影响功能。
[2026-06-10 13:28:58] 报表订单状态统计口径修复
- 修改内容：修复分类销售、销售趋势、热销商品 SQL 的订单状态过滤，销售额只统计已支付、已发货、已收货、已完成订单，取消订单不计入销售，已退款订单只计入退款额；分类统计避免无效订单项被 LEFT JOIN 误计入。
- 注意事项：报表口径已与 OrderConstants 对齐，后续新增订单状态时需同步更新报表 SQL。
[2026-06-10 13:39:56] 订单趋势报表口径补齐
- 修改内容：修复订单趋势 SQL，订单数和销售额只统计已支付、已发货、已收货、已完成订单，待支付、已取消、退款中、已退款不再计入订单趋势销售额。
- 注意事项：退款金额仍由销售趋势和分类销售的 refundAmount 字段展示，订单趋势保持成交订单趋势口径。
[2026-06-14 18:30:00] 报表导出改为直接文件流 + UTF-8 BOM 修复乱码
- 修改内容：`AdminReportController` 导出端点改为注入 `HttpServletResponse` 直接写入 CSV 文件流，设置 `Content-Type: text/csv; charset=utf-8` 和 `Content-Disposition` 下载头；`ReportService.exportCsv` 改为 `void exportCsv(ReportQueryDTO, OutputStream)`；CSV 内容添加 UTF-8 BOM（EF BB BF）解决 Excel 打开中文乱码。
- 注意事项：前端不再需要 Base64 解码，直接通过浏览器下载文件。若前端有自定义下载逻辑需同步移除 Base64 解码步骤。
