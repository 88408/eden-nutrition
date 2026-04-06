# 秒杀活动提交报 JSON 解析异常排查记录 (LocalDateTime)

## 问题描述
在管理端继续添加或修改秒杀活动时，发起了 `POST /admin/seckill` 或 `PUT /admin/seckill` 请求，服务器再次抛出 500 异常。
后端日志详情为：
```
org.springframework.http.converter.HttpMessageNotReadableException: JSON parse error: Cannot deserialize value of type java.time.LocalDateTime from String "2026-04-06T18:30:00.000Z"
```

## 原因分析
本项目的后端 `application.yml` 明确配置了统一的时间序列化/反序列化格式：
```yaml
spring:
  jackson:
    date-format: yyyy-MM-dd HH:mm:ss
```
这意味着后端在解析 JSON 中的时间字符串为 `LocalDateTime` 对象时，仅认可使用空格分隔的完整格式（如 `2026-04-06 18:30:00`）。

而在前端 `SeckillList.tsx` 表单中，原生 `<input type="datetime-local">` 收集到的值为 `YYYY-MM-DDTHH:mm`。先前的代码使用了 `new Date(formData.startTime).toISOString()` 将其发回，这不仅会将输入的原样带 `T` 字符的格式发送，还带上了后缀毫秒级和代表 UTC 时区的 `Z`（变成 `2026-04-06T18:30:00.000Z`），不仅格式不匹配，且可能因为隐式时区转化造成 8 小时的时差错乱。

## 解决方案
**修复前端时间传输的组装器逻辑：**
我们在被提交前拦截了 `formData.startTime` 和 `formData.endTime`，不经过 `new Date().toISOString()` 转制，而是直接在本地字符基础上做简单的格式清理，满足后端约定的标准。

修改方案如下：
```javascript
// 原本：
startTime: new Date(formData.startTime).toISOString()

// 修复后（直接将 'T' 替换为空格，并在末尾补齐缺失的 ':00' 秒数位）：
startTime: formData.startTime.replace('T', ' ') + (formData.startTime.length === 16 ? ':00' : '')
```

目前该补丁已热更新至开发环境，再次点击保存/提交秒杀活动时，传递过去的时间载荷将是纯净的 `2026-04-06 18:30:00`，后端 Jackson 现在可以极其平滑地将其转化为 `LocalDateTime` 进而写入数据库。