# 支付宝沙箱支付改造记录

[2026-05-28 19:20:24] 修改摘要
- 修改内容：新增支付宝沙箱 PagePay 支付配置、SDK 依赖、支付表单生成、异步通知验签、同步返回跳转和订单支付幂等落库流程；旧 `/order/pay/{orderNo}` 兼容入口不再直接模拟支付成功；补充订单支付单元测试；在 `AGENTS.md` 记录本仓库前端目录已弃用，用户端前端改动应落到 `D:/code/Eden-Nutrition-Mall`。
- 注意事项：现有数据库需执行一次性迁移 SQL：`ALTER TABLE \`order\` ADD COLUMN \`payment_trade_no\` VARCHAR(100) DEFAULT NULL COMMENT '第三方支付交易号' AFTER \`payment_method\`, ADD INDEX \`idx_payment_trade_no\` (\`payment_trade_no\`);`。支付宝异步通知地址必须公网可访问，支付状态以异步通知验签结果为准，同步返回只用于页面跳转。当前本机使用 Java 24，已显式配置 Lombok annotation processor 以保证 Maven 重编译稳定。

[2026-05-28 21:50:55] 修改摘要
- 修改内容：完成本地 `eden_db.order` 表数据迁移，新增并验证 `payment_trade_no` 字段和 `idx_payment_trade_no` 索引；新增微信开发者工具调试用支付宝沙箱支付桥接流程，包括一次性 Redis token、`/order/pay/alipay/weapp-debug/{orderNo}`、`/order/pay/alipay/bridge` 和 `/order/pay/alipay/weapp-debug-return`；扩展订单支付单元测试覆盖桥接 token、桥接 HTML 和过期 token。
- 注意事项：当前本地运行库没有 `payment_method` 字段，迁移实际将 `payment_trade_no` 放在 `pay_type` 后，并已将支付落库 SQL 调整为不依赖 `payment_method`。微信小程序桥接支付仅用于开发者工具沙箱调试，正式微信小程序支付仍应接入微信支付。

[2026-05-28 22:33:56] 修改摘要
- 修改内容：新增 `eden.alipay.weapp-debug-bridge-base-url` 配置，微信开发者工具调试支付优先使用稳定的后端 `/api` 根地址生成 bridgeUrl；bridge/return 匿名访问兼容 `/order/...` 与 `/api/order/...`；bridge token 失效或支付异常时返回可读 HTML 错误页；秒杀下单服务端增加收货地址归属校验，并补充单元测试覆盖地址不属于当前用户和合法秒杀下单。
- 注意事项：本地默认 bridge 根地址为 `http://localhost:8080/api`，如后端实际端口、context-path 或内网穿透地址不同，需要通过 `ALIPAY_WEAPP_DEBUG_BRIDGE_BASE_URL` 覆盖。秒杀接口仍要求前端传入当前用户自己的默认地址 ID。

[2026-05-28 22:42:24] 修改摘要
- 修改内容：修复点击秒杀下单发送 MQ 时 `SeckillOrder.createTime` 序列化失败的问题，RabbitMQ 的 `Jackson2JsonMessageConverter` 改为复用应用级 `ObjectMapper`，从而继承已注册的 `JavaTimeModule` 和 `LocalDateTime` 格式化配置。
- 注意事项：该异常发生在秒杀订单消息发送前，表现为 `MessageConversionException` 和 `LocalDateTime not supported by default`。修复后无需新增依赖，已通过 `mvn -pl eden-admin -am -DskipTests compile` 验证。

[2026-05-29 13:30:28] 修改摘要
- 修改内容：后端配置改为通过 Spring Boot 原生 `spring.config.import` 加载仓库根目录 `.env`，新增 `.env.example` 作为支付宝沙箱配置模板，并在 `.gitignore` 中忽略本地真实 `.env` 与旧 resources 目录下的 `.env`，避免密钥误提交。
- 注意事项：本地 `.env` 必须放在仓库根目录且使用 `KEY=value` 格式；IDEA 或 Maven 启动时 working directory 应为 `D:\code\eden-nutrition`。支付宝异步通知真正可用仍需要将 `ALIPAY_NOTIFY_URL` 配置为公网可访问地址。

[2026-05-31 15:14:00] 修改摘要
- 修改内容：调整 `docker-compose.yml` 中 MySQL 服务时区配置，新增 `TZ=Asia/Shanghai` 与 `--default-time-zone=+08:00`，并移除 Windows/Docker Desktop 下未生效的 `/etc/localtime`、`/etc/timezone` 挂载，确保容器重建后 MySQL `NOW()` 返回北京时间。
- 注意事项：本次只修正 MySQL/Compose 时区，不修改支付业务逻辑；已被误关为 `status=5` 的历史订单不会自动恢复，需要重新下单或单独人工处理。

[2026-05-31 15:22:21] 修改摘要
- 修改内容：在实际启动模块 `eden-admin` 的 `application.yml` 中补充仓库根目录 `.env` 导入和 `eden.alipay` 支付宝沙箱配置段，确保 `EdenApplication` 运行时可以读取 `ALIPAY_APP_ID`、`ALIPAY_PRIVATE_KEY`、回调地址等配置。
- 注意事项：本次只修复启动配置加载链路，不修改支付业务逻辑；IDEA 启动后端时仍需将 working directory 保持为 `D:\code\eden-nutrition`，修改后需要重启后端进程才能生效。

[2026-05-31 15:32:00] 修改摘要
- 修改内容：将微信开发者工具支付宝调试桥接 token 过期时间从 10 分钟放宽到 60 分钟，并在支付宝 PagePay `biz_content` 中显式设置 `timeout_express=60m`，给沙箱跳转、登录和支付操作保留更长调试窗口；新增单元测试覆盖 PagePay 超时参数。
- 注意事项：该修改只放宽本地沙箱调试链路的有效期，不改变订单 30 分钟未支付自动关闭策略；若仍出现 504，需要继续排查内网穿透域名、支付宝沙箱网关或后端接口响应超时。

[2026-05-31 16:09:55] 修改摘要
- 修改内容：新增支付宝沙箱商家 PID 配置 `eden.alipay.seller-id` / `ALIPAY_SELLER_ID`，在 PagePay `biz_content` 中随订单参数提交 `seller_id`，并将本地 `.env` 配置为当前沙箱绑定商家 PID `2088721101816988`；同步更新 `.env.example`、启动配置和单元测试。
- 注意事项：`seller_id` 只用于明确沙箱收款方，不替代 `ALIPAY_PUBLIC_KEY`；支付宝后台接口加签方式仍需与本地 `ALIPAY_PRIVATE_KEY` 保持一致，使用自定义私钥时后台应选择“自定义密钥”并保存对应应用公钥。

[2026-05-31 16:19:34] 修改摘要
- 修改内容：新增支付宝手机网站支付 WapPay 表单生成方法，微信开发者工具 bridge 调试链路改为使用 `QUICK_WAP_WAY`，普通 H5 PagePay 仍保留 `FAST_INSTANT_TRADE_PAY`；同时优化后端桥接页、错误页和调试返回页的移动端 viewport 与全屏居中样式。
- 注意事项：本次仅优化微信开发者工具内 web-view 调试比例，不保证支付宝官方沙箱收银台在所有容器中都可被完全控制；若 WapPay 页面仍异常，需要继续排查支付宝沙箱页面或微信开发者工具 web-view 限制。

[2026-05-31 21:03:34] 修改摘要
- 修改内容：确认支付宝 WapPay 在微信开发者工具 web-view 中会触发 `alipay://` 原生协议并导致 `ERR_UNKNOWN_URL_SCHEME`，因此微信开发者工具调试 bridge 回退为 PagePay 表单；保留 bridge 移动端外壳、手动提交按钮、`X-Frame-Options` 小范围放宽和公网 `weapp-debug-return` 地址生成逻辑。
- 注意事项：当前优先保证微信开发者工具内可完成支付宝沙箱支付调试，页面比例不再强制使用 WapPay；如后续做真机或外部浏览器调试，可单独评估是否重新启用 WapPay。

[2026-05-31 21:17:53] 修改摘要
- 修改内容：增强支付宝沙箱 bridge 异常诊断，将桥接页生成异常日志从 `WARN` 调整为 `ERROR`，并在调试错误页展示异常 message，避免微信开发者工具 web-view 只看到空白或泛化错误时无法定位根因。
- 注意事项：当前 logback 仅将 `INFO` 与 `ERROR` 写入文件，`WARN` 不会进入 `logs/app.log` 或 `logs/error.log`；修改后需要重启后端并重新发起支付生成新的 bridge token。

[2026-05-31 21:21:02] 修改摘要
- 修改内容：确认支付宝沙箱 PagePay 使用 POST 表单提交时网关返回 `504 Gateway Time-out`，同一组参数改为 GET 跳转可返回 `302` 收银台分配页；因此微信开发者工具 bridge 改为生成 PagePay GET 跳转地址并自动 `window.location.replace`，同时保留手动打开链接兜底。
- 注意事项：普通 H5 支付入口仍保留 SDK 默认 PagePay 表单提交；微信开发者工具调试链路需要重启后端并重新生成 bridge token 后才会使用 GET 跳转。

[2026-05-31 21:36:07] 修改摘要
- 修改内容：针对微信开发者工具点击支付后仍空白的问题，后端 bridge 页取消自动跳转支付宝，改为先展示可见的手动打开按钮；同时为 `/order/pay/alipay/weapp-debug/{orderNo}` 响应补充外部手机浏览器兜底用的 WapPay GET 地址 `externalPayUrl`，便于开发者工具无法承载支付宝页面时复制到外部环境完成沙箱支付。
- 注意事项：外部手机支付链接仅用于沙箱联调兜底，普通 H5/PagePay 入口不受影响；支付成功落库仍依赖 `ALIPAY_NOTIFY_URL` 可被支付宝公网回调访问，后端重启并重新生成支付链接后新字段才会生效。

[2026-05-31 21:57:10] 修改摘要
- 修改内容：增强支付宝沙箱 `/order/pay/alipay/weapp-debug-return` 调试返回页，读取 `out_trade_no` 后引入微信 web-view JSSDK，并调用 `wx.miniProgram.redirectTo` 自动跳回小程序订单详情页；同时保留“返回订单详情”手动按钮和订单号缺失提示。
- 注意事项：自动回跳只适用于微信开发者工具 web-view 内的支付链路；复制外部手机支付链接完成支付时无法保证自动回到开发者工具，仍需手动返回小程序刷新订单状态。

[2026-06-02 13:00:50] 修改摘要
- 修改内容：统一支付宝沙箱 bridge、PagePay/WapPay 本地表单承载页、调试返回页和错误页的移动端 HTML 头部，补齐标准 viewport `width=device-width, initial-scale=1.0, viewport-fit=cover`，并通过 helper 复用避免后续字符串配置漂移。
- 注意事项：本次只控制本地 bridge/wrapper/return/error 页面比例，无法直接修改支付宝官方 PagePay 收银台自身的 CSS；如果官方页仍按 PC 比例展示，需要继续使用手机版 WapPay 链接作为移动端调试兜底。
