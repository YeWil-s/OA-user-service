# 企业智能 OA 办公管理系统

本仓库当前已完成 `6.1 基础设施层` 与 `6.5 notice-service 公告通知服务`，采用 Spring Boot 3、Spring Cloud 2025、Spring Cloud Alibaba、Gateway 统一鉴权、Spring Security WebFlux、JWT、Nacos、Redis、MyBatis Plus 与 Knife4j 作为基础技术栈。

## 模块结构

```text
oa-system-ai
├── common              # 统一响应、异常处理、基础实体
├── utils               # JSON、时间等通用工具
├── model               # 跨服务共享模型
├── api                 # 服务间接口常量与 Feign 预留
├── security            # JWT 配置、Token 生成解析
├── gateway-service     # Spring Cloud Gateway 统一入口
├── user-service        # 用户权限服务骨架
├── attendance-service  # 考勤服务骨架
├── approval-service    # 审批服务骨架
├── notice-service      # 公告发布、已读未读、站内消息
├── asset-service       # 资产人事服务骨架
├── ai-service          # AI 办公服务骨架
└── visual-service      # 数据可视化服务骨架
```

## 已完成的基础设施能力

- Maven 父工程和 13 个子模块。
- `common` 公共模块：统一响应 `Result<T>`、业务状态码、基础实体、业务异常、全局异常处理。
- `common` 基础能力：网关用户请求头解析、MyBatis Plus 分页插件、Redis 缓存与分布式锁工具。
- `security` 安全模块：JWT 配置、Token 生成解析。
- `gateway-service`：Spring Security WebFlux + JWT 统一鉴权、白名单、CORS、用户上下文请求头透传。
- 各业务服务空壳：应用入口、健康检查接口、Nacos、Redis、MySQL、MyBatis Plus、Knife4j 基础配置。
- `notice-service`：公告发布、公告分页、公告详情并标记已读、公告编辑/下架、未读数量、站内消息列表、消息已读。
- 本地中间件编排：`docker-compose.infra.yml`。
- 数据库脚本：当前目录已有 `oa_init.sql`，compose 会将其挂载到 MySQL 初始化目录。

## 本地基础设施启动

```powershell
.\scripts\start-infra.ps1
```

该脚本会执行：

```powershell
docker compose -f docker-compose.infra.yml up -d
```

默认端口：

| 服务 | 端口 |
| --- | --- |
| MySQL | 3306 |
| Redis | 6379 |
| Nacos | 8848 |

## Maven 构建

当前机器 Maven 默认绑定 Java 8，不满足 Spring Boot 3 的要求。请使用仓库提供的 JDK 21 包装脚本：

```powershell
.\scripts\mvn-jdk21.ps1 clean package -DskipTests
```

也可以手动设置：

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-21'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
mvn clean package -DskipTests
```

## 服务启动顺序

1. 启动 MySQL、Redis、Nacos。
2. 执行或确认 MySQL 已加载 `oa_init.sql`。
3. 启动业务服务，例如 `user-service`。
4. 启动 `gateway-service`。
5. 通过 Gateway 访问接口。

示例：

```text
GET http://localhost:9000/api/user/health
```

登录接口 `/api/user/login` 已在 Gateway 白名单中放行。当前演示账号：

| 用户名 | 密码 | 角色 |
| --- | --- | --- |
| admin | 123456 | ROLE_ADMIN |
| zhangsan | 123456 | ROLE_EMPLOYEE |

## notice-service 接口

完整接口文档见 [notice-service-api.md](docs/notice-service-api.md)。

所有业务接口建议通过 Gateway 访问，并携带登录返回的 JWT：

```text
Authorization: Bearer <token>
```

| 功能 | 方法与路径 |
| --- | --- |
| 公告发布 | `POST /api/notice/list` |
| 公告分页 | `GET /api/notice/list?current=1&size=10` |
| 公告详情并标记已读 | `GET /api/notice/list/{id}` |
| 公告编辑 | `PUT /api/notice/list/{id}` |
| 公告下架 | `DELETE /api/notice/list/{id}` |
| 未读数量 | `GET /api/notice/unread-count` |
| 站内消息分页 | `GET /api/notice/messages?current=1&size=10` |
| 创建站内消息 | `POST /api/notice/messages` |
| 标记消息已读 | `PUT /api/notice/messages/{id}/read` |
