---
name: config-field-alignment
description: "Use when troubleshooting Connection Refused errors, service startup failures, 'Unknown column' database errors, or when configuring docker-compose environments. Prevents mixing up internal/external container ports, yml configurations, and database fields."
---

# 配置与字段对齐排查规范 (Config and Field Alignment)

当你被要求排查微服务启动失败、连接拒绝 (Connection Refused) 或者数据库实体报错时，请严格执行以下排查流程，以避免搞混配置、端口或字段。

## 1. 容器网络与端口梳理 (Internal vs External Ports)
在 Docker 和 docker-compose 环境下，切勿将宿主机端口与容器内端口混淆：
- **检查环境**：确认微服务是在 Docker 容器内运行，还是在宿主机（物理机/本机的 IDE）直接运行。
- **内部通信优先**：如果服务A依赖服务B（例如 `ai-qa-service` 依赖 `nacos` 或 `redis`），且都在同一个 docker 桥接网络下，**必须使用容器内部端口**（例如 Redis 用 6379，Nacos 用 8848）。
- **核对映射**：不要直接将 `application.yml` 中给本地 IDE 测试用的宿主机映射端口（如 6316, 18848）原样带入 Docker 环境中供容器互相通信。

## 2. 配置覆写检查 (Configuration Override Check)
代码配置和部署配置容易发生割裂，务必执行“以部署环境为准”的检查：
- **查看基础配置**：检查代码中的默认配置（如 `application.yml`、`bootstrap.yml`）。
- **确认覆写情况**：检查 `docker-compose.yml` 或运行脚本中的 `environment:` / `-e` 环境变量。确保诸如 `NACOS_SERVER_ADDR`、`SPRING_DATA_REDIS_PORT` 等变量通过部署脚本正确注入，并覆盖了代码中针对本地的默认配置（例如将 127.0.0.1 覆盖为 docker 网络中的 service name）。
- **注意 127.0.0.1 陷阱**：在容器环境下，`127.0.0.1` 只指向容器自身，不要将其用于连接其他容器。请替换为对应的容器名或服务名（如 `mysql`, `redis`, `nacos`）。

## 3. 实体表和字段同步排查 (Database Schema vs Spring Entity)
- **报错特征**：当看到如 `Unknown column 'xyz' in 'field list'` 报错时。
- **双向核对**：
  1. 查看 Java/Spring 代码中的 JPA/MyBatis Entity 类，确认字段注解映射是否发生变更或新增。
  2. 查看当前数据库结构的迁移脚本（Flyway/Liquibase 或手动 SQL 文件）。
- **结论输出**：必须确认代码中要求的字段确实已经在数据库当前 Schema 中创建，不要凭空猜测字段已经存在。

## 4. 完成排查后的输出要求
在向用户报告时，如果有发现上述混淆或未对齐的地方，请明确列出：
- **[期望配置]** vs **[实际使用的故障配置]**
- **[宿主机映射端口]** vs **[容器内实际通信端口]**
- 提供可以直接复制粘贴并替换的 YAML 配置片段。