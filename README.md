# OA Management

分布式微服务OA办公管理系统，基于 Spring Cloud Alibaba + Vue3 的前后端分离架构。

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端框架 | SpringBoot 3.2.5 + Spring Cloud Alibaba 2023.0.1 |
| ORM | MyBatis-Plus 3.5.7 |
| 注册/配置中心 | Nacos |
| 网关 | Spring Cloud Gateway（统一鉴权 + 路由转发） |
| 数据库 | MySQL 8.0（每微服务独立库） |
| 缓存 | Redis |
| 认证 | JWT + BCrypt 密码加密 |
| 定时任务 | XXL-Job |
| 消息队列 | RocketMQ |
| 熔断降级 | Sentinel |
| 接口文档 | Knife4j |
| 前端 | Vue3 + Vite + Element Plus + Pinia + ECharts + DataV |
| 移动端 | Flutter (Android) |
| 容器化 | Docker + Docker Compose |

## 项目结构

```
OA_Management/
├── oa-common/          # 公共模块（统一响应、异常处理、JWT/Redis 工具类）
├── oa-gateway/         # 网关服务（路由分发、JWT 鉴权、跨域）
├── oa-user/            # 用户权限服务（登录、RBAC、部门/岗位/员工管理）
├── oa-attendance/      # 考勤服务（打卡、班次、考勤统计）
├── oa-approval/        # 审批服务（请假/加班/外出申请、审批流转）
├── oa-notice/          # 公告通知服务（公告发布、已读状态、站内信）
├── oa-asset/           # 资产人事服务（资产登记领用、人事变动、合同管理）
├── oa-ai/              # AI 智能服务（智能填单、数据分析、知识问答）
├── oa-data-visual/     # 数据可视化服务（考勤/人事/审批统计大屏）
├── oa-web/             # Vue3 前端管理后台
├── oa_app/             # Flutter Android 移动端
├── sql/                # 数据库初始化脚本
├── docs/               # 接口文档与开发指引
└── docker-compose.yml  # 容器编排
```

## 功能模块

### 用户权限管理
- 账号登录 / JWT 鉴权 / RBAC 角色权限模型
- 部门（树形结构）、岗位、员工 CRUD
- 菜单三级管理（目录 → 页面 → 按钮），动态路由
- 数据权限（全部 / 本部门及下级 / 本部门 / 本人）

### 考勤打卡
- 班次模板定义（标准 / 弹性班次），按人或部门分配
- 上下班打卡，Redis 分布式锁防重
- 个人 / 部门 / 全公司考勤记录查询
- XXL-Job 每日凌晨自动统计迟到/早退/旷工

### 审批流程
- 请假 / 加班 / 外出三种申请，附件上传
- 自动匹配直属主管为审批人
- 审批时间线追溯，待办/已办/我的申请列表
- WebSocket 站内信通知审批结果

### 公告通知
- 公告发布（富文本 + 发布范围控制），过期自动下架
- 已读/未读状态（数据库持久化，跨设备同步）
- 站内消息分类（审批通知 / 考勤通知 / 系统通知）

### 资产与人事
- 固定资产 / 办公用品登记、领用、归还
- 员工档案（学历、合同、紧急联系人）
- 人事变动记录（入职 / 转正 / 调岗 / 离职）
- 合同到期预警

### AI 智能办公助手
- 自然语言智能填单（请假/加班/外出自动解析）
- 考勤异常分析 + 员工风险预警
- OA 知识库问答（RAG 检索增强）

### 数据可视化大屏
- 人事概览屏（部门分布、入职离职趋势、学历司龄分布）
- 考勤数据屏（出勤率仪表盘、迟到率排名、加班对比）
- 审批流转屏（通过率、审批效率、积压量）
- 自动轮播切换 + 定时刷新

### Android 移动端
- 快速打卡 / 考勤概览
- 消息通知 + 待审批处理
- 请假/加班申请提交

## 快速启动

### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis 6+
- Nacos 2.x
- Node.js 18+ / pnpm

### 后端启动

```bash
# 1. 初始化数据库：执行 sql/oa_init.sql

# 2. 启动 Nacos（默认 standalone 模式）
# windows: startup.cmd -m standalone
# linux: sh startup.sh -m standalone

# 3. 修改各服务 src/main/resources/application.yml 中的数据库连接和 Nacos 地址

# 4. 按顺序启动服务
# gateway → common 已作为依赖引入，无需单独启动
# 业务服务启动顺序不限：oa-user, oa-attendance, oa-approval, oa-notice, oa-asset, oa-ai, oa-data-visual
```

### 前端启动

```bash
cd oa-web
pnpm install
pnpm dev
```

### Docker 一键部署

```bash
# 修改 .env 中的环境变量（数据库密码等）
docker compose up -d
```

## API 约定

所有接口返回统一格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1753000000000
}
```

RESTful 路由规范：`/api/{service}/{resource}`，详细接口文档见 `docs/` 目录。

## 状态码

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 401 | 未登录或 Token 过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |
| 10001+ | 用户模块业务异常 |
| 20001+ | 考勤模块业务异常 |
| 30001+ | 审批模块业务异常 |
| 40001+ | 公告模块业务异常 |
| 50001+ | AI 模块业务异常 |
