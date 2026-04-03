# 认证模块异常排查说明 (Auth Module Troubleshooting)

在接入了 `eden-admin` 后端的专属登录拦截机制与接口 (`AdminAuthInterceptor` / `AdminUserController`) 后，系统出现了一些编译与语法级别的问题。这些问题主要集中在 Java API 调用的兼容性以及重构时的代码排版遗漏。

此报告将这些问题及修复过程记录如下，以保证文档上下文的完整性。

---

## 1. 拦截器获取 Token Payload 时的找不到方法报错 (`The method getClaimsFromToken(String) is undefined...`)

### 出错现象
在拦截器 `AdminAuthInterceptor.java` 中，对前台传递的 `Bearer token` 尝试抛出其携带的信息时报出这个错：
`The method getClaimsFromToken(String) is undefined for the type JwtUtils`

### 原因分析
当时拦截器直接手写调用了 `jwtUtils.getClaimsFromToken(token);`。经过对 `eden-common` 包下既有的工具类 `JwtUtils.java` 源码审计发现，买家版 (`eden-web`) 最初暴露抛出的解析函数名为 `parseToken(String token)`。
另外获取 UserID 的逻辑原本是要求强制将字符串强制转换成 `Long` 的形式(`Long.valueOf(claims.getSubject())`)，但现存在的方法中早已经封装好 `getUserId(String token)` 接口。

### 修复方案
对 `AdminAuthInterceptor.java` 第 61 行左右的代码进行了对齐：
- 由 `jwtUtils.getClaimsFromToken(token)` 替换为 `jwtUtils.parseToken(token)`。
- 通过现成的 `Long userId = jwtUtils.getUserId(token);` 来替代冗长的 `Long.valueOf(claims.getSubject())`。

---

## 2. API 控制器的返回值接口未定义 (`The method unauthorized(null) is undefined for the type Result`)

### 出错现象
在 `AdminUserController.java` 中编写用来验证当前会话的 `/info` 接口时，由于未能成功从 Header 获取到的 `adminId` 所返回的一个响应出现错误：
`The method unauthorized(null) is undefined for the type Result`

### 原因分析
起因是希望给前端返回一个标准的 401 Unauthorized 错误码对象。但是审计 `Result.java` 发现并没有预设一个叫 `unauthorized(T)` 的静态工厂方法。现有的错误返回通常借由 `Result.fail()` 结合定义在 `eden-common` 内的错误状态码常量 `ResultCode`。

### 修复方案
将 `AdminUserController.java` 中的相关响应替换为标准的构建方式：
`return Result.fail(eden.common.result.ResultCode.UNAUTHORIZED);`

---

## 3. 服务类括号嵌套及逻辑遗漏 (`Syntax error, insert "}" to complete Statement`)

### 出错现象
在重构登录逻辑，从原本直接硬编码的角色 `USER` 支持，泛化成接受 `expectedRole` 来分离后台管理员 (`ADMIN`) 登录和 C 端买家 (`USER`) 登录时，在 `UserServiceImpl.java` 内新增的鉴权判断代码出现了块级语法括号缺失的严重错误：
`Syntax error, insert "}" to complete Statement`

### 原因分析
由于直接对 Java 代码采取局部的内容替换，导致在引入新增加的“检查角色合法性 (`expectedRole != null && !expectedRole.equals(user.getRole())`)”逻辑时，闭合的 `}` 误被忽略（覆盖）。

### 修复方案
修正了这部分的代码缩进与语句块的配对，并在相应位置补充了正确的右括号：
```java
// 验证角色
if (expectedRole != null && !expectedRole.equals(user.getRole())) {     
    incrementLoginFail(failKey);
    if ("ADMIN".equals(expectedRole)) {
        throw new BusinessException("后台管理系统仅允许管理员登录");    
    } else {
        throw new BusinessException("普通用户入口，禁止管理员登录");    
    }
} // <==== 补全了这部分缺失的括号
```

---

目前这些异常已经全部被追溯修复，`mvn clean package` 后端编译能够成功通过。前端通过修改 `Login.tsx` 中的错别字也成功消除了 `npm run build` 带来的编译拦截。项目恢复正常运行状态。