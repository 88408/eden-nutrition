# Eden Nutrition - 系统模块架构详细说明 (System Modules Documentation)

Eden Nutrition 是一个采用前后端分离架构的现代电商平台。后端基于 Spring Boot 2.x 和 Maven 构建了多模块架构以实现高内聚、低耦合，前端基于 Vite 等现代构建工具提供用户商城和管理后台。

以下是系统中各个核心模块的详细介绍：

## 1. 后端模块 (Backend Modules - Java/Spring Boot)

本项目采用了标准的 DDD 分层和 MVC 分层结合的思想，将整个系统拆分为多个子 Maven 模块。

### 1.1 `eden-common` (公共基础模块)
系统的底层基础设施层，不包含具体业务逻辑，被其他所有模块依赖。
- **作用**：提供全局共享的工具和规范。
- **核心内容**：
  - **异常处理 (`exception`)**：自定义全局异常类和全局异常处理器。
  - **常数 (`constant`)**：系统使用的各类常量定义（如 Redis key、MQ 队列名、订单状态常量等）。
  - **统一响应 (`result`)**：标准化的 RESTful API 响应结构 (`Result`, `ResultCode`)。
  - **工具类 (`utils`)**：如全局 ID 生成器、JSON 转换、JWT 处理、Redis 操作封装等。

### 1.2 `eden-pojo` (数据模型模块)
模型层，统一管理系统中所有的数据流转对象。
- **作用**：定义数据结构，解解其他模块之间基于数据的耦合。
- **核心内容**：
  - **Entity (实体类)**：数据库表对应的实体对象（如 `Order`, `Product`, `User`）。
  - **DTO (Data Transfer Object)**：前端向后端传递的数据接收对象（如 `LoginDTO`, `OrderCreateDTO`）。
  - **VO (View Object)**：后端向前端返回的视图展示对象（如 `CartVO`, `CategoryTreeVO`）。

### 1.3 `eden-mapper` (数据访问层/持久层)
DAO 层，负责与 MySQL 数据库进行交互。
- **作用**：隔离数据库查询逻辑。
- **核心内容**：基于 MyBatis/MyBatis-Plus 的 `Mapper` 接口类以及放置在 `resources/mapper/` 目录下的 MyBatis XML 映射文件（负责编写复杂 SQL 语句）。涵盖了如 `UserMapper`, `ProductMapper`, `OrderMapper` 等。

### 1.4 `eden-service` (业务逻辑层)
Service 层，系统的核心“大脑”。
- **作用**：处理所有的核心业务规则、事务管理和跨模块调用。
- **核心内容**：
  - 包含了服务接口（`service`）及具体实现类（`impl`文件夹）。
  - **消息监听器 (`listener`)**：处理异步消息，如订单处理队列、秒杀库存扣减、库存同步等（如 `OrderMessageListener`）。
  - **定时任务 (`task`)**：处理周期性业务，例如订单超时自动取消、促销活动状态定期更新等（如 `OrderTask`）。

### 1.5 `eden-config` (全局配置模块)
配置核心层，剥离各种中间件的基础装配。
- **作用**：集中管理 Spring Boot 和各类中间件的核心配置类。
- **核心内容**：包括数据库连接配置、MyBatis 配置、安全授权配置、Swagger（API 文档）配置、RabbitMQ 与 Redis 的序列化及连接配置等。同时也存放了不同环境的配置文件（`application-dev.yml`, `application-prod.yml`）。

### 1.6 `eden-web` (C端用户接口层)
面向商城普通用户的 Web/Controller 层。
- **作用**：接收用户请求，调用 Service 层处理业务，并返回结果给前端应用。
- **核心内容**：
  - **Controllers**：如 `ProductController`, `OrderController`, `CartController`。
  - **安全与拦截 (`security`, `interceptor`, `filter`)**：实现 C 端用户的 JWT 认证鉴权、请求拦截。
  - **自定义注解与解析器 (`annotation`, `resolver`)**：方便地注入当前登录用户信息（如 `@CurrentUser`）。

### 1.7 `eden-admin` (B端管理后台接口层)
面向系统管理员或商家运营的 Web 层。
- **作用**：提供商品上架、订单管理、用户管理、活动配置等后台管理 API。
- **核心内容**：与 `eden-web` 类似，但权限级别更高，专注于后台管理场景。它负责启动整个后端管理应用，包含入口主类 `EdenApplication`。

---

## 2. 前端模块 (Frontend Modules)

### 2.1 `eden-vue` (用户端商城前端)
- 面向消费者的 C 端门户，主要用于用户浏览商品、加入购物车、下单支付、个人中心等场景。
- 采用的技术栈通常为：Vue 3 (或 React) + TypeScript + Vite + TailwindCSS 等。

### 2.2 `eden-admin-vue` (管理后台前端)
- 面向内部管理员和运营人员的 B 端管理系统。提供数据看板、用户管理、商品增删改查、订单发货、营销活动发起等功能。
- 与后端的 `eden-admin` 服务对接，要求更强调表单、数据表格的可视化与批量操作体验。

---

## 3. 后端模块依赖关系 (Dependencies Architecture)

通常，由于分层架构的设计，Maven 模块的依赖链条从上至下依次如下（箭头指向被依赖的模块）：

1. **`eden-web` & `eden-admin`** 依赖 -> `eden-service`, `eden-config`, `eden-common`。
2. **`eden-service`** 依赖 -> `eden-mapper`, `eden-pojo`, `eden-common`。
3. **`eden-mapper`** 依赖 -> `eden-pojo`, `eden-common`。
4. **`eden-pojo`** 依赖 -> 无具体业务依赖，极少外部依赖。
5. **`eden-common`** 依赖 -> 通用第三方库（如 Hutool, FastJSON 等）。

这种架构保证了底层的领域对象和数据层能够在不同的表现层中被重用，同时保证业务逻辑核心处于安全的边界保护之内。