# OA办公管理系统 — 项目需求文档

> **项目名称**：分布式微服务OA办公管理系统（前后端分离架构）
> **技术栈**：SpringBoot 3.x + Spring Cloud Alibaba + Vue3 + Element Plus
> **文档日期**：2026-07-20

---

## 目录

1. [功能设计](#一功能设计)
2. [数据库设计](#二数据库设计)
3. [路由命名规范](#三路由命名规范)
4. [拓展功能任务书](#四拓展功能任务书)

---

## 一、功能设计

### 1.1 系统架构概览

系统采用微服务架构，划分为以下服务模块：

| 服务/客户端名称 | 服务职责 | 类型 |
|--------------|---------|------|
| gateway-service | 统一网关（鉴权、路由转发） | 基础设施 |
| user-service | 用户、组织、权限管理 | 核心业务 |
| attendance-service | 考勤打卡管理 | 核心业务 |
| approval-service | 审批流程管理 | 核心业务 |
| notice-service | 公告通知管理 | 核心业务 |
| asset-service | 资产与人事管理 | 扩展业务 |
| ai-service | AI智能办公助手 | 扩展功能 |
| data-visual-service | 大数据可视化看板 | 扩展功能 |
| oa-web (Vue3) | Web管理后台 | 前端客户端 |
| oa-mobile (Android) | 移动端快速办公 | 前端客户端 |

---

### 1.2 用户组织权限模块（user-service）

#### 1.2.1 用户认证

| 功能点 | 描述 |
|-------|------|
| 账号登录 | 员工使用账号+密码登录，后端校验并签发JWT令牌（有效期2小时），到期自动跳转登录页 |
| 退出登录 | 客户端清除Token |

**登录流程**：
1. 用户提交账号密码
2. 后端校验（密码BCrypt加密比对）
3. 查询用户所属角色、权限列表
4. 生成JWT（payload含userId、username、roles、permissions），有效期2小时
5. 返回accessToken

#### 1.2.2 组织管理

| 功能点 | 描述 |
|-------|------|
| 部门管理 | 支持多级部门树形结构，CRUD操作 |
| 岗位管理 | 岗位隶属于部门，CRUD操作 |
| 部门-岗位绑定 | 为部门设置对应岗位列表 |

**部门字段**：部门名称、部门编码、上级部门（parent_id=0 表示顶级部门，无上级）、负责人、排序号（列表展示时的排列顺序，数字越小越靠前）、状态（启用/停用）

**岗位字段**：岗位名称、岗位编码、所属部门、排序号（同上，控制列表展示顺序）、状态

#### 1.2.3 员工管理

| 功能点 | 描述 |
|-------|------|
| 员工列表 | 分页查询，支持按部门/岗位/姓名/状态筛选 |
| 员工新增 | 录入姓名、工号、手机号、邮箱、部门、岗位、入职日期 |
| 员工编辑 | 修改基本信息及部门岗位调换 |
| 员工离职 | 标记离职状态，冻结登录权限（非物理删除） |
| 密码重置 | 管理员重置员工密码为默认密码 |

#### 1.2.4 角色访问权限管理

| 功能点 | 描述 |
|-------|------|
| 角色管理 | 创建/编辑/删除角色，角色分配权限 |
| 菜单管理 | 树形菜单结构管理，三级结构：①目录=侧边栏折叠分组（如"系统管理"），②菜单=可点击的页面入口（如"员工管理"），③按钮=页面内的操作权限（如"新增""删除"按钮）。通过 menu_type 字段区分级别 |
| 角色-菜单绑定 | 多对多关联，控制可见范围 |
| 接口权限控制 | 基于角色标识的接口级别访问控制 |
| 数据权限 | 按部门级别控制数据可见范围（全部/本部门及下级/本部门/本人） |

**RBAC模型关系**：

```
用户(N) ←→ (N)角色(N) ←→ (N)菜单
              |
              ↓ (N:N)
          接口权限(permissions)
```

**内置角色预设**：
- 超级管理员：全部权限
- HR：员工管理、考勤查看、公告发布
- 部门主管：下属考勤查看、审批处理、部门员工管理
- 普通员工：个人打卡、请假申请、公告查看、个人信息

---

### 1.3 考勤打卡模块（attendance-service）

#### 1.3.1 班次管理

| 功能点 | 描述 |
|-------|------|
| 班次定义 | 创建班次模板（上班时间/下班时间/迟到阈值/早退阈值） |
| 弹性班次 | 支持设置弹性打卡时间段（如 8:00-9:00 弹性上班） |
| 班次分配 | 按部门或按人分配班次 |
| Nacos配置 | 考勤规则参数（迟到阈值、旷工阈值等）统一Nacos管理，支持动态刷新 |

#### 1.3.2 打卡功能

| 功能点 | 描述 |
|-------|------|
| 上班打卡 | 员工点击打卡，记录打卡时间、IP（预留字段）、设备类型 |
| 下班打卡 | 同上 |
| 重复打卡防护 | 同一时段已打卡，禁止再次打卡（Redis分布式锁 + 数据库唯一约束） |
| 外勤打卡 | 支持GPS定位记录（预留字段） |

#### 1.3.3 考勤记录

| 功能点 | 描述 |
|-------|------|
| 个人考勤查询 | 按日/按月查询本人打卡记录与考勤状态 |
| 全部门考勤查询 | 部门主管查看本部门员工考勤 |
| 全公司考勤查询 | HR/管理员查看全公司考勤数据 |
| 考勤状态 | 正常、迟到、早退、旷工、缺卡 |

#### 1.3.4 考勤统计（XXL-Job定时任务）

| 功能点 | 描述 |
|-------|------|
| 每日凌晨统计 | 自动对比打卡记录与班次标准，生成每日考勤统计表 |
| 月度考勤汇总 | 按月汇总迟到次数、早退次数、旷工天数、加班时长 |
| 统计结果存储 | 写入考勤统计表，供可视化看板消费 |

---

### 1.4 审批流程模块（approval-service）

#### 1.4.1 申请提交

| 功能点 | 描述 |
|-------|------|
| 请假申请 | 选择请假类型（年假/事假/病假/婚假等）、起止时间、原因 |
| 加班申请 | 加班日期、起止时间、加班原因 |
| 外出申请 | 外出日期、起止时间、外出原因 |
| 附件上传 | 支持上传证明材料图片（如医院证明） |

#### 1.4.2 审批处理

| 功能点 | 描述 |
|-------|------|
| 待审批列表 | 显示当前登录人待处理的审批任务 |
| 一键同意/驳回 | 审批意见（必填）+ 同意/驳回按钮 |
| 审批记录 | 查看该申请单的完整审批流转记录 |
| 流程追溯 | 时间线展示每个审批节点的处理人和处理时间 |

#### 1.4.3 申请管理

| 功能点 | 描述 |
|-------|------|
| 我的申请 | 我提交的所有申请列表（草稿/审批中/已通过/已驳回/已撤销） |
| 已办申请 | 我审批过的申请记录 |
| 申请详情 | 查看申请表单、审批链路（时间线） |
| 撤销申请 | 审批中状态可撤销，已审批不可撤销 |

#### 1.4.4 流程说明（简化版，非Flowable）

- 员工提交申请 → 系统自动匹配直属上级 → 上级审批（一级审批）
- 若不满足一级审批，可预留多级审批扩展接口
- 审批通过或驳回，通过WebSocket/站内信通知申请人

---

### 1.5 公告通知模块（notice-service）

#### 1.5.1 公告管理

| 功能点 | 描述 |
|-------|------|
| 发布公告 | 管理员/HR输入标题、内容、生效时间范围、发布范围 |
| 编辑公告 | 修改未过期公告 |
| 下架公告 | 手动下架或过期自动下架 |
| 公告列表 | 按发布时间倒序、分页展示 |

#### 1.5.2 通知中心

| 功能点 | 描述 |
|-------|------|
| 公告查阅 | 员工查看公告详情，后端写入 ntc_read_status 表记录已读（数据库持久化，跨设备同步已读状态） |
| 已读/未读状态 | 公告列表标注已读/未读（从 ntc_read_status 表查询），未读公告红色角标提醒 |
| 站内消息推送 | 审批结果、考勤异常、系统公告触发站内信（预留RocketMQ异步实现） |
| 消息列表 | 分类展示（审批通知/系统通知/考勤通知） |

---

### 1.6 资产与人事管理模块（asset-service）— 扩展业务

#### 1.6.1 人事管理

| 功能点 | 描述 |
|-------|------|
| 入职管理 | 录入新员工信息、生成员工档案 |
| 转正管理 | 试用期到期提醒、转正审批 |
| 调岗记录 | 员工部门/岗位变动历史 |
| 离职管理 | 离职登记、账号冻结 |
| 合同管理 | 劳动合同信息维护、到期预警 |

#### 1.6.2 资产管理

| 功能点 | 描述 |
|-------|------|
| 资产登记 | 录入固定资产/办公用品信息（名称、编码、分类、购置日期、价值） |
| 资产领用 | 员工提交领用申请、管理员审批、记录领用时间 |
| 资产归还 | 员工归还操作，更新资产状态为"可领用" |
| 资产盘点 | 定期盘点清单、状态核对 |

---

### 1.7 基础设施功能

| 功能点 | 描述 |
|-------|------|
| 统一异常处理 | @RestControllerAdvice 全局捕获异常，返回统一JSON格式 |
| 统一响应封装 | 所有接口返回 `{ code, message, data }` 标准格式 |
| Gateway全局鉴权 | 网关层校验JWT Token，白名单路径跳过（登录、公开接口） |
| 权限注解 | 自定义 @RequiresRole / @RequiresPermission 注解，AOP切面校验 |
| 操作日志 | AOP记录用户操作（接口、参数、IP、耗时），异步入库 |
| 参数校验 | Hibernate Validator，统一返回校验失败信息 |
| 数据脱敏 | 手机号、身份证等敏感字段序列化脱敏 |

---

### 1.8 非功能性需求

| 需求项 | 说明 |
|-------|------|
| 安全性 | JWT无状态会话、BCrypt密码加密、SQL防注入（MyBatis参数化）、XSS过滤 |
| 高可用 | 服务多实例部署、Nacos健康检查、Gateway负载均衡 |
| 可扩展 | 微服务独立部署、独立数据库、独立扩展 |
| 可维护 | 统一日志规范、统一返回格式、Swagger/Knife4j接口文档 |
| 性能 | Redis缓存热点数据、连接池优化、慢SQL监控 |

---

## 二、数据库设计

> **分库原则**：每个微服务对应一个独立的MySQL数据库，禁止跨库联表查询，跨服务数据交互通过远程REST接口调用实现。

### 2.1 数据库分库总览

| 数据库名称 | 所属微服务 | 说明 |
|-----------|-----------|------|
| user_db | user-service | 用户、组织、权限 |
| attendance_db | attendance-service | 考勤打卡、班次、统计 |
| approval_db | approval-service | 审批申请、审批记录 |
| notice_db | notice-service | 公告、站内信 |
| asset_db | asset-service | 人事档案、资产 |
| ai_db | ai-service | AI对话记录、分析报告 |
| statistics_db | data-visual-service | 统计结果缓存表 |

---

### 2.2 用户权限数据库（user_db）

#### 2.2.1 sys_dept（部门表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| parent_id | BIGINT | 20 | 上级部门ID，0=根部门 |
| dept_name | VARCHAR | 50 | 部门名称 |
| dept_code | VARCHAR | 30 | 部门编码，唯一 |
| leader_id | BIGINT | 20 | 部门负责人ID（关联sys_user） |
| sort_order | INT | 11 | 排序号 |
| status | TINYINT | 1 | 状态：0=停用，1=启用 |
| create_time | DATETIME | - | 创建时间 |
| update_time | DATETIME | - | 更新时间 |
| is_deleted | TINYINT | 1 | 逻辑删除：0=未删除，1=已删除 |

#### 2.2.2 sys_position（岗位表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| position_name | VARCHAR | 50 | 岗位名称 |
| position_code | VARCHAR | 30 | 岗位编码，唯一 |
| dept_id | BIGINT | 20 | 所属部门ID |
| sort_order | INT | 11 | 排序号 |
| status | TINYINT | 1 | 状态：0=停用，1=启用 |
| create_time | DATETIME | - | 创建时间 |
| update_time | DATETIME | - | 更新时间 |
| is_deleted | TINYINT | 1 | 逻辑删除 |

#### 2.2.3 sys_user（员工/用户表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| username | VARCHAR | 30 | 登录账号（工号），唯一 |
| password | VARCHAR | 200 | BCrypt加密密码 |
| real_name | VARCHAR | 30 | 真实姓名 |
| phone | VARCHAR | 20 | 手机号 |
| email | VARCHAR | 50 | 邮箱 |
| gender | TINYINT | 1 | 性别：0=女，1=男 |
| dept_id | BIGINT | 20 | 所属部门ID |
| position_id | BIGINT | 20 | 岗位ID |
| avatar_url | VARCHAR | 200 | 头像URL |
| entry_date | DATE | - | 入职日期 |
| status | TINYINT | 1 | 状态：0=离职，1=在职，2=冻结 |
| last_login_time | DATETIME | - | 最后登录时间 |
| create_time | DATETIME | - | 创建时间 |
| update_time | DATETIME | - | 更新时间 |
| is_deleted | TINYINT | 1 | 逻辑删除 |

**索引**：唯一索引 `uk_username` (username)、普通索引 `idx_dept_id` (dept_id)、`idx_status` (status)

#### 2.2.4 sys_role（角色表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| role_name | VARCHAR | 30 | 角色名称 |
| role_code | VARCHAR | 30 | 角色编码（ROLE_ADMIN/ROLE_HR/ROLE_LEADER/ROLE_EMPLOYEE），唯一 |
| role_desc | VARCHAR | 200 | 角色描述 |
| data_scope | TINYINT | 1 | 数据权限范围：0=全部，1=本部门及下级，2=本部门，3=本人 |
| sort_order | INT | 11 | 排序号 |
| status | TINYINT | 1 | 状态：0=停用，1=启用 |
| create_time | DATETIME | - | 创建时间 |
| update_time | DATETIME | - | 更新时间 |
| is_deleted | TINYINT | 1 | 逻辑删除 |

#### 2.2.5 sys_menu（菜单/权限表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| parent_id | BIGINT | 20 | 父菜单ID，0=根 |
| menu_name | VARCHAR | 50 | 菜单名称 |
| menu_type | TINYINT | 1 | 类型：1=目录，2=菜单，3=按钮 |
| path | VARCHAR | 200 | 前端路由path（菜单型） |
| component | VARCHAR | 200 | 前端组件路径 |
| permission_code | VARCHAR | 100 | 权限标识（如 user:list, user:add） |
| icon | VARCHAR | 50 | 菜单图标 |
| sort_order | INT | 11 | 排序号 |
| visible | TINYINT | 1 | 是否可见：0=隐藏，1=显示 |
| status | TINYINT | 1 | 状态 |
| create_time | DATETIME | - | 创建时间 |
| update_time | DATETIME | - | 更新时间 |

#### 2.2.6 sys_user_role（用户角色关联表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| user_id | BIGINT | 20 | 用户ID |
| role_id | BIGINT | 20 | 角色ID |

**索引**：唯一索引 `uk_user_role` (user_id, role_id)

#### 2.2.7 sys_role_menu（角色菜单关联表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| role_id | BIGINT | 20 | 角色ID |
| menu_id | BIGINT | 20 | 菜单ID |

**索引**：唯一索引 `uk_role_menu` (role_id, menu_id)

---

### 2.3 考勤数据库（attendance_db）

#### 2.3.1 att_shift（班次表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| shift_name | VARCHAR | 50 | 班次名称（早班、晚班、弹性班） |
| start_time | TIME | - | 上班标准时间 |
| end_time | TIME | - | 下班标准时间 |
| flex_start | TIME | - | 弹性打卡起始时间（可选，弹性班才填） |
| flex_end | TIME | - | 弹性打卡截止时间（可选，弹性班才填） |
| status | TINYINT | 1 | 状态：0=停用，1=启用 |
| create_time | DATETIME | - | 创建时间 |
| update_time | DATETIME | - | 更新时间 |

> **迟到/早退/旷工判定阈值**（如迟到≥30分钟、旷工≥2小时）不存储在班次表中，而是通过 **Nacos 配置中心** 统一管理（如 `attendance.late.threshold=30`、`attendance.absent.threshold=120`），支持动态刷新，无需重启服务。考勤统计算法在代码中读取这些配置值进行判定。

#### 2.3.2 att_record（打卡流水表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| user_id | BIGINT | 20 | 员工ID |
| record_date | DATE | - | 打卡日期 |
| punch_in_time | DATETIME | - | 上班打卡时间 |
| punch_out_time | DATETIME | - | 下班打卡时间 |
| punch_type | TINYINT | 1 | 打卡类型：1=现场，2=外勤 |
| device_info | VARCHAR | 100 | 设备信息/IP |
| location | VARCHAR | 200 | 打卡地点（外勤预留） |
| create_time | DATETIME | - | 记录创建时间 |

**索引**：唯一索引 `uk_user_date` (user_id, record_date)

#### 2.3.3 att_daily_summary（每日考勤统计表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| user_id | BIGINT | 20 | 员工ID |
| dept_id | BIGINT | 20 | 部门ID |
| summary_date | DATE | - | 统计日期 |
| shift_id | BIGINT | 20 | 班次ID |
| punch_in_time | DATETIME | - | 实际上班打卡时间 |
| punch_out_time | DATETIME | - | 实际下班打卡时间 |
| status | TINYINT | 1 | 考勤状态：1=正常，2=迟到，3=早退，4=旷工，5=缺卡，6=请假，7=加班 |
| late_minutes | INT | 11 | 迟到分钟数 |
| early_minutes | INT | 11 | 早退分钟数 |
| work_hours | DECIMAL | 5,1 | 出勤工时 |
| overtime_hours | DECIMAL | 5,1 | 加班工时 |
| create_time | DATETIME | - | 统计时间 |

**索引**：`idx_user_date` (user_id, summary_date)、`idx_dept_date` (dept_id, summary_date)

#### 2.3.4 user_shift（用户班次关联表）

> 说明：班次表 att_shift 是"模板"，定义有哪些班次类型；user_shift 是"分配"，指定哪个员工当前用哪个班次。两者不是重复功能——前者管模板定义，后者管人员分配。

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| user_id | BIGINT | 20 | 员工ID，唯一 |
| shift_id | BIGINT | 20 | 班次ID |
| create_time | DATETIME | - | 创建时间 |
| update_time | DATETIME | - | 更新时间 |

**索引**：唯一索引 `uk_user_id` (user_id)

---

### 2.4 审批数据库（approval_db）

#### 2.4.1 app_application（申请单表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| application_no | VARCHAR | 50 | 申请单号，唯一（如 LV20260720001） |
| user_id | BIGINT | 20 | 申请人ID |
| dept_id | BIGINT | 20 | 申请人部门ID |
| app_type | TINYINT | 1 | 申请类型：1=请假，2=加班，3=外出 |
| leave_type | TINYINT | 1 | 请假子类型：1=年假，2=事假，3=病假，4=婚假，5=产假（仅请假类型时有效） |
| start_time | DATETIME | - | 开始时间 |
| end_time | DATETIME | - | 结束时间 |
| duration | DECIMAL | 5,1 | 时长（天或小时） |
| reason | VARCHAR | 500 | 申请原因 |
| attachments | VARCHAR | 500 | 附件URL，逗号分隔 |
| status | TINYINT | 1 | 状态：0=草稿，1=审批中，2=已通过，3=已驳回，4=已撤销 |
| current_approver_id | BIGINT | 20 | 当前审批人ID |
| create_time | DATETIME | - | 创建时间 |
| update_time | DATETIME | - | 更新时间 |

**索引**：唯一索引 `uk_app_no` (application_no)、`idx_user_status` (user_id, status)、`idx_approver` (current_approver_id)

#### 2.4.2 app_approval_record（审批记录表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| application_id | BIGINT | 20 | 申请单ID |
| approver_id | BIGINT | 20 | 审批人ID |
| action | TINYINT | 1 | 操作：1=同意，2=驳回 |
| comment | VARCHAR | 500 | 审批意见 |
| action_time | DATETIME | - | 操作时间 |

**索引**：`idx_application` (application_id)

---

### 2.5 公告数据库（notice_db）

#### 2.5.1 ntc_notice（公告表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| title | VARCHAR | 200 | 公告标题 |
| content | TEXT | - | 公告内容（富文本） |
| publisher_id | BIGINT | 20 | 发布人ID |
| notice_type | TINYINT | 1 | 类型：1=公司公告，2=部门通知，3=系统通知 |
| target_type | TINYINT | 1 | 发布范围：1=全公司，2=指定部门，3=指定人 |
| target_ids | VARCHAR | 500 | 目标ID列表，逗号分隔（target_type非1时有效） |
| start_time | DATETIME | - | 生效时间 |
| end_time | DATETIME | - | 失效时间 |
| status | TINYINT | 1 | 状态：0=草稿，1=已发布，2=已下架 |
| create_time | DATETIME | - | 创建时间 |
| update_time | DATETIME | - | 更新时间 |

#### 2.5.2 ntc_read_status（已读状态表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| notice_id | BIGINT | 20 | 公告ID |
| user_id | BIGINT | 20 | 用户ID |
| is_read | TINYINT | 1 | 是否已读：0=未读，1=已读 |
| read_time | DATETIME | - | 阅读时间 |

**索引**：唯一索引 `uk_notice_user` (notice_id, user_id)

#### 2.5.3 ntc_message（站内消息表 — 预留）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| user_id | BIGINT | 20 | 接收人ID |
| title | VARCHAR | 200 | 消息标题 |
| content | VARCHAR | 500 | 消息内容 |
| msg_type | TINYINT | 1 | 消息类型：1=审批通知，2=考勤通知，3=系统通知 |
| related_id | BIGINT | 20 | 关联业务ID |
| is_read | TINYINT | 1 | 是否已读 |
| create_time | DATETIME | - | 创建时间 |

**索引**：`idx_user_read` (user_id, is_read)

---

### 2.6 资产数据库（asset_db）

#### 2.6.1 ast_employee_archive（员工档案表 — 扩展人事）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| user_id | BIGINT | 20 | 关联用户ID，唯一 |
| id_card | VARCHAR | 18 | 身份证号 |
| education | TINYINT | 1 | 学历：1=高中，2=大专，3=本科，4=硕士，5=博士 |
| major | VARCHAR | 50 | 专业 |
| graduate_school | VARCHAR | 50 | 毕业院校 |
| address | VARCHAR | 200 | 现住址 |
| emergency_contact | VARCHAR | 30 | 紧急联系人 |
| emergency_phone | VARCHAR | 20 | 紧急联系电话 |
| contract_start | DATE | - | 合同开始日期 |
| contract_end | DATE | - | 合同结束日期 |
| create_time | DATETIME | - | 创建时间 |
| update_time | DATETIME | - | 更新时间 |

#### 2.6.2 ast_staff_change（人事变动记录表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| user_id | BIGINT | 20 | 员工ID |
| change_type | TINYINT | 1 | 变动类型：1=入职，2=转正，3=调岗，4=离职 |
| before_dept | BIGINT | 20 | 变动前部门 |
| after_dept | BIGINT | 20 | 变动后部门 |
| before_position | BIGINT | 20 | 变动前岗位 |
| after_position | BIGINT | 20 | 变动后岗位 |
| change_date | DATE | - | 变动日期 |
| remark | VARCHAR | 500 | 备注 |
| create_time | DATETIME | - | 创建时间 |

#### 2.6.3 ast_asset（资产信息表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| asset_name | VARCHAR | 100 | 资产名称 |
| asset_code | VARCHAR | 50 | 资产编码，唯一 |
| category | TINYINT | 1 | 分类：1=固定资产，2=办公用品，3=电子设备 |
| model | VARCHAR | 50 | 规格型号 |
| purchase_date | DATE | - | 购置日期 |
| purchase_price | DECIMAL | 10,2 | 购置价格 |
| status | TINYINT | 1 | 状态：0=报废，1=可领用，2=已领用 |
| create_time | DATETIME | - | 创建时间 |
| update_time | DATETIME | - | 更新时间 |

#### 2.6.4 ast_asset_record（资产领用记录表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| asset_id | BIGINT | 20 | 资产ID |
| user_id | BIGINT | 20 | 领用人ID |
| borrow_date | DATE | - | 领用日期 |
| expect_return_date | DATE | - | 预计归还日期 |
| actual_return_date | DATE | - | 实际归还日期 |
| status | TINYINT | 1 | 状态：1=领用中，2=已归还 |
| create_time | DATETIME | - | 创建时间 |

---

### 2.7 AI数据库（ai_db）

#### 2.7.1 ai_conversation（AI对话记录表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| user_id | BIGINT | 20 | 用户ID |
| session_id | VARCHAR | 50 | 会话ID（多轮对话关联） |
| question | TEXT | - | 用户问题 |
| answer | TEXT | - | AI回答 |
| category | TINYINT | 1 | 类别：1=智能填单，2=数据分析，3=知识问答 |
| tokens_used | INT | 11 | 消耗Token数 |
| create_time | DATETIME | - | 创建时间 |

**索引**：`idx_user` (user_id)、`idx_session` (session_id)

#### 2.7.2 ai_analysis_report（AI分析报告表）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| report_type | TINYINT | 1 | 报告类型：1=考勤异常，2=效能分析，3=风险预警 |
| target_user_id | BIGINT | 20 | 目标员工（可空=部门级别） |
| target_dept_id | BIGINT | 20 | 目标部门（可空） |
| analysis_period | VARCHAR | 20 | 分析周期（如 2026-07） |
| content | TEXT | - | 报告内容（JSON格式） |
| summary | VARCHAR | 500 | 报告摘要 |
| create_time | DATETIME | - | 生成时间 |

---

### 2.8 统计数据库（statistics_db — 可视化用）

#### 2.8.1 stat_attendance_monthly（月度考勤统计）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| stat_month | VARCHAR | 7 | 统计月份（如 2026-07） |
| dept_id | BIGINT | 20 | 部门ID |
| total_employees | INT | 11 | 部门总人数 |
| normal_count | INT | 11 | 正常出勤人次 |
| late_count | INT | 11 | 迟到达人次 |
| early_count | INT | 11 | 早退人次 |
| absent_count | INT | 11 | 旷工人次 |
| leave_count | INT | 11 | 请假人次 |
| overtime_total | DECIMAL | 10,1 | 加班总时长 |
| create_time | DATETIME | - | 统计时间 |

#### 2.8.2 stat_approval_summary（审批数据汇总）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| stat_month | VARCHAR | 7 | 统计月份 |
| dept_id | BIGINT | 20 | 部门ID |
| total_applications | INT | 11 | 申请总数 |
| approved_count | INT | 11 | 通过数 |
| rejected_count | INT | 11 | 驳回数 |
| pending_count | INT | 11 | 待审批数 |
| avg_approval_hours | DECIMAL | 5,1 | 平均审批耗时（小时） |
| create_time | DATETIME | - | 统计时间 |

#### 2.8.3 stat_dept_overview（部门概览统计）

| 字段名 | 类型 | 长度 | 说明 |
|-------|------|------|------|
| id | BIGINT | 20 | 主键，自增 |
| stat_month | VARCHAR | 7 | 统计月份 |
| dept_id | BIGINT | 20 | 部门ID |
| active_employees | INT | 11 | 在职人数 |
| new_hires | INT | 11 | 入职人数 |
| resignations | INT | 11 | 离职人数 |
| attendance_rate | DECIMAL | 5,2 | 出勤率（百分比） |
| create_time | DATETIME | - | 统计时间 |

---

### 2.9 全局数据表关系图

```
┌─────────────────────────────────────────────────────────────────┐
│  user_db                                                        │
│  sys_dept ──┬── sys_position                                    │
│             │                                                    │
│  sys_user ──┼── sys_user_role ── sys_role ── sys_role_menu      │
│             │                                └── sys_menu        │
│  (user_id) 用来跨库关联其他服务的 user_id                          │
└─────────────────────────────────────────────────────────────────┘

┌─────────────────────────┐     ┌──────────────────────────────┐
│  attendance_db          │     │  approval_db                 │
│  att_shift              │     │  app_application             │
│  att_record (user_id)───┼─────┤  app_approval_record         │
│  att_daily_summary      │     │        (application_id)      │
│  att_shift_allocation   │     │                              │
└─────────────────────────┘     └──────────────────────────────┘

┌─────────────────────────┐     ┌──────────────────────────────┐
│  notice_db              │     │  asset_db                    │
│  ntc_notice             │     │  ast_employee_archive        │
│  ntc_read_status        │     │  ast_staff_change            │
│  ntc_message            │     │  ast_asset ── ast_asset_record│
└─────────────────────────┘     └──────────────────────────────┘

┌─────────────────────────┐     ┌──────────────────────────────┐
│  ai_db                  │     │  statistics_db               │
│  ai_conversation        │     │  stat_attendance_monthly     │
│  ai_analysis_report     │     │  stat_approval_summary        │
│                          │     │  stat_dept_overview           │
└─────────────────────────┘     └──────────────────────────────┘
```

**跨库关联约定**：所有表中的 `user_id`、`dept_id`、`position_id` 仅存储ID值，不持有外键约束。业务层需要关联数据时通过微服务远程接口调用获取。

---

## 三、路由命名规范

### 3.1 后端API路由规范

**通用格式**：`/{服务前缀}/{业务模块}/{操作}`

**RESTful风格约定**：

| HTTP方法 | 路径格式 | 示例 | 说明 |
|----------|---------|------|------|
| GET | `/api/{module}/{resource}` | `GET /api/user/employees` | 列表查询 |
| GET | `/api/{module}/{resource}/{id}` | `GET /api/user/employees/1` | 详情查询 |
| POST | `/api/{module}/{resource}` | `POST /api/user/employees` | 新增 |
| PUT | `/api/{module}/{resource}/{id}` | `PUT /api/user/employees/1` | 修改 |
| DELETE | `/api/{module}/{resource}/{id}` | `DELETE /api/user/employees/1` | 删除 |
| GET | `/api/{module}/{resource}/export` | `GET /api/attendance/records/export` | 导出 |

---

#### 3.1.1 user-service（用户权限服务）

| 路由 | 方法 | 说明 |
|------|------|------|
| `/api/user/login` | POST | 用户登录 |
| `/api/user/logout` | POST | 用户登出 |
| `/api/user/current` | GET | 获取当前登录用户信息 |
| `/api/user/employees` | GET | 员工列表（分页） |
| `/api/user/employees` | POST | 新增员工 |
| `/api/user/employees/{id}` | GET | 员工详情 |
| `/api/user/employees/{id}` | PUT | 编辑员工 |
| `/api/user/employees/{id}` | DELETE | 删除员工（逻辑删除） |
| `/api/user/employees/{id}/reset-pwd` | PUT | 重置密码 |
| `/api/user/depts` | GET | 部门树 |
| `/api/user/depts` | POST | 新增部门 |
| `/api/user/depts/{id}` | PUT | 编辑部门 |
| `/api/user/depts/{id}` | DELETE | 删除部门 |
| `/api/user/positions` | GET | 岗位列表 |
| `/api/user/positions` | POST | 新增岗位 |
| `/api/user/positions/{id}` | PUT | 编辑岗位 |
| `/api/user/positions/{id}` | DELETE | 删除岗位 |
| `/api/user/roles` | GET | 角色列表 |
| `/api/user/roles` | POST | 新增角色 |
| `/api/user/roles/{id}` | PUT | 编辑角色 |
| `/api/user/roles/{id}` | DELETE | 删除角色 |
| `/api/user/roles/{id}/menus` | PUT | 分配角色菜单 |
| `/api/user/menus` | GET | 菜单树（全部） |
| `/api/user/menus/routers` | GET | 获取当前用户可访问的路由（动态路由） |
| `/api/user/menus/{id}` | PUT | 编辑菜单 |

#### 3.1.2 attendance-service（考勤服务）

| 路由 | 方法 | 说明 |
|------|------|------|
| `/api/attendance/shifts` | GET | 班次列表 |
| `/api/attendance/shifts` | POST | 新建班次 |
| `/api/attendance/shifts/{id}` | PUT | 编辑班次 |
| `/api/attendance/shifts/{id}` | DELETE | 删除班次 |
| `/api/attendance/shifts/allocate` | POST | 分配班次（按人/按部门） |
| `/api/attendance/punch/in` | POST | 上班打卡 |
| `/api/attendance/punch/out` | POST | 下班打卡 |
| `/api/attendance/records/mine` | GET | 个人考勤记录（按日/按月） |
| `/api/attendance/records/dept` | GET | 部门考勤记录（主管视角） |
| `/api/attendance/records/all` | GET | 全公司考勤记录（HR/Admin） |
| `/api/attendance/records/export` | GET | 导出考勤报表（Excel） |
| `/api/attendance/summary/mine` | GET | 个人月考勤汇总 |
| `/api/attendance/summary/dept` | GET | 部门考勤统计 |

#### 3.1.3 approval-service（审批服务）

| 路由 | 方法 | 说明 |
|------|------|------|
| `/api/approval/applications` | GET | 我的申请列表 |
| `/api/approval/applications` | POST | 提交申请 |
| `/api/approval/applications/{id}` | GET | 申请详情（含审批链路） |
| `/api/approval/applications/{id}` | PUT | 修改申请（仅草稿状态） |
| `/api/approval/applications/{id}/cancel` | PUT | 撤销申请 |
| `/api/approval/pending` | GET | 待审批列表（当前用户为审批人） |
| `/api/approval/pending/{id}/approve` | PUT | 审批（同意/驳回） |
| `/api/approval/processed` | GET | 已办列表 |

#### 3.1.4 notice-service（公告服务）

| 路由 | 方法 | 说明 |
|------|------|------|
| `/api/notice/list` | GET | 公告列表（当前用户可见） |
| `/api/notice/list` | POST | 发布公告（管理员） |
| `/api/notice/list/{id}` | GET | 公告详情（标记已读） |
| `/api/notice/list/{id}` | PUT | 编辑公告 |
| `/api/notice/list/{id}` | DELETE | 下架公告 |
| `/api/notice/unread-count` | GET | 未读公告数量 |
| `/api/notice/messages` | GET | 站内消息列表 |
| `/api/notice/messages/{id}/read` | PUT | 标记消息已读 |

#### 3.1.5 asset-service（资产人事服务）

| 路由 | 方法 | 说明 |
|------|------|------|
| `/api/asset/assets` | GET | 资产列表 |
| `/api/asset/assets` | POST | 资产登记 |
| `/api/asset/assets/{id}` | PUT | 编辑资产 |
| `/api/asset/assets/{id}` | DELETE | 报废/删除资产 |
| `/api/asset/borrow` | POST | 资产领用申请 |
| `/api/asset/borrow/{id}/return` | PUT | 资产归还 |
| `/api/asset/records` | GET | 领用记录查询 |
| `/api/asset/staff/changes` | GET | 人事变动记录 |
| `/api/asset/staff/changes` | POST | 新增人事变动（入职/转正/调岗/离职） |
| `/api/asset/staff/archive/{userId}` | GET | 员工档案详情 |
| `/api/asset/staff/archive/{userId}` | PUT | 编辑员工档案 |
| `/api/asset/contracts` | GET | 合同列表 |
| `/api/asset/contracts/expiring` | GET | 即将到期合同预警 |

#### 3.1.6 ai-service（AI智能服务）

| 路由 | 方法 | 说明 |
|------|------|------|
| `/api/ai/chat` | POST | AI对话（流式/非流式） |
| `/api/ai/chat/history` | GET | 对话历史 |
| `/api/ai/smart-fill` | POST | 智能填单（自动生成请假/加班申请） |
| `/api/ai/analysis/attendance` | POST | 考勤数据分析 |
| `/api/ai/analysis/risk` | POST | 员工风险预警 |
| `/api/ai/knowledge/ask` | POST | OA知识库问答 |
| `/api/ai/reports` | GET | 历史分析报告列表 |
| `/api/ai/reports/{id}` | GET | 报告详情 |

#### 3.1.7 data-visual-service（数据可视化服务）

| 路由 | 方法 | 说明 |
|------|------|------|
| `/api/visual/dashboard/overview` | GET | 全公司概览数据（总人数、出勤率等） |
| `/api/visual/dashboard/dept-distribution` | GET | 部门人员分布 |
| `/api/visual/dashboard/attendance-trend` | GET | 月度考勤趋势 |
| `/api/visual/dashboard/dept-overtime` | GET | 部门加班对比 |
| `/api/visual/dashboard/approval-stats` | GET | 审批流转统计 |
| `/api/visual/dashboard/approval-speed` | GET | 审批效率（平均耗时） |
| `/api/visual/screen/attendance` | GET | 考勤数据大屏数据（实时刷新） |
| `/api/visual/screen/hr` | GET | 人事数据大屏数据 |
| `/api/visual/screen/approval` | GET | 审批数据大屏数据 |

---

### 3.2 前端路由命名规范

#### 3.2.1 路由路径规范

| 模块 | 路由路径 | 页面说明 |
|------|---------|---------|
| 登录 | `/login` | 登录页 |
| 首页 | `/dashboard` | 首页工作台 |
| 个人信息 | `/profile` | 个人信息页 |
| 员工管理 | `/system/employee` | 员工列表 |
| 部门管理 | `/system/dept` | 部门树管理 |
| 岗位管理 | `/system/position` | 岗位管理 |
| 角色管理 | `/system/role` | 角色管理 |
| 菜单管理 | `/system/menu` | 菜单管理 |
| 考勤打卡 | `/attendance/punch` | 打卡页面 |
| 考勤记录 | `/attendance/record` | 考勤记录查询 |
| 班次管理 | `/attendance/shift` | 班次管理 |
| 我的申请 | `/approval/my-apply` | 我的申请列表 |
| 提交申请 | `/approval/submit` | 提交新申请 |
| 待审批 | `/approval/pending` | 待审批列表 |
| 已办审批 | `/approval/processed` | 已办列表 |
| 公告列表 | `/notice/list` | 公告列表 |
| 消息中心 | `/notice/message` | 站内消息 |
| 资产管理 | `/asset/list` | 资产列表 |
| 资产领用 | `/asset/borrow` | 领用/归还 |
| 人事变动 | `/asset/staff-change` | 人事变动记录 |
| AI助手 | `/ai/chat` | AI对话页面 |
| AI分析 | `/ai/analysis` | AI数据分析 |
| 数据大屏 | `/visual/screen` | 实时数据大屏 |
| 数据报表 | `/visual/reports` | 统计报表 |

---

### 3.3 统一响应格式

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1753000000000
}
```

**业务状态码约定**：

| code | 含义 |
|------|------|
| 200 | 成功 |
| 400 | 参数校验失败 |
| 401 | 未登录或Token过期 |
| 403 | 无权限 |
| 404 | 资源不存在 |
| 409 | 业务冲突（如重复打卡） |
| 500 | 服务器内部错误 |
| 10001-10999 | 用户模块业务异常 |
| 20001-20999 | 考勤模块业务异常 |
| 30001-30999 | 审批模块业务异常 |
| 40001-40999 | 公告模块业务异常 |
| 50001-50999 | AI模块业务异常 |

---

## 四、拓展功能任务书

> 以下为可选拓展任务，各小组根据自身能力选择实现。任务按推荐优先级排列，标 ★ 的为本小组选定的重点拓展任务。

### 4.1 拓展任务总览

| 编号 | 拓展任务 | 技术方案 | 优先级 | 状态 |
|------|---------|---------|-------|------|
| EXT-01 | ★ AI智能办公助手 | 大模型API + SSE流式输出 | 必选 | 选定 |
| EXT-02 | ★ 大数据可视化看板 | DataV + ECharts | 必选 | 选定 |
| EXT-03 | ★ Android移动端 | Android原生/Flutter + Retrofit | 必选 | 选定 |
| EXT-04 | 分布式高并发优化 | XXL-Job + RocketMQ + Sentinel | 推荐 | 可选 |
| EXT-05 | 高级审批流程 | Flowable 工作流引擎 | 推荐 | 可选 |
| EXT-06 | 分布式事务 | Seata AT模式 | 推荐 | 可选 |
| EXT-07 | 搜索与报表优化 | Elasticsearch + Excel导出 | 推荐 | 可选 |
| EXT-08 | 链路追踪与接口文档 | SkyWalking + Knife4j | 推荐 | 可选 |
| EXT-09 | 容器化部署 | Docker + Docker Compose | 推荐 | 可选 |

---

### 4.2 EXT-01 ★ AI智能办公助手（ai-service）

#### 4.2.1 任务概述

对接大模型API（通义千问/文心一言/DeepSeek），围绕OA办公业务场景，构建三大智能功能：智能填单、智能数据分析、OA知识问答。

#### 4.2.2 功能详述

**功能1：智能填单助手**

| 子功能 | 描述 |
|-------|------|
| 自然语言填单 | 用户输入"我明天请一天年假，家里有事"，AI自动解析并生成请假申请表单 |
| 意图识别 | 自动判断用户意图是请假、加班还是外出 |
| 表单预览与确认 | AI生成表单后回填到前端表单，用户可手动修改后提交 |
| 合理性校验 | AI检查申请合理性（日期是否已过、时长是否超标），给出提示 |

**功能2：智能数据分析**

| 子功能 | 描述 |
|-------|------|
| 考勤异常识别 | AI分析月度考勤数据，识别频繁迟到/早退员工，生成异常名单及风险等级 |
| 效能分析 | 对比部门间考勤数据，分析出勤率趋势、加班趋势 |
| 风险预警 | 结合请假模式（频繁周一/周五请假），识别潜在离职风险 |
| 报告生成 | AI生成可读的分析报告文本，支持导出 |

**功能3：OA知识库问答**

| 子功能 | 描述 |
|-------|------|
| 流程问答 | "请假流程是什么？"→ AI基于OA知识库回答 |
| 制度查询 | "年假有多少天？"→ AI回答公司休假制度 |
| 操作指引 | "怎么查看我的考勤记录？"→ AI给出操作步骤 |
| 多轮对话 | 支持上下文连续对话，按session_id保持会话状态 |

#### 4.2.3 技术方案

```
┌─────────────────────────────────────────────────────────────┐
│  Vue3 前端 (AI对话组件 + 智能填单组件 + 分析报告展示)         │
│                      │  HTTP / SSE                          │
│  ai-service (后端AI服务)                                     │
│  ├── Controller: 接收请求                                    │
│  ├── Service: 业务编排、Prompt构建、结果解析                  │
│  ├── AI Client: 封装大模型API调用（策略模式，支持多厂商切换）   │
│  └── Mapper: 对话记录/分析报告持久化                          │
│                      │                                      │
│              大模型 API (通义千问/文心一言/DeepSeek)           │
└─────────────────────────────────────────────────────────────┘
```

**关键选型**：
- HTTP客户端：WebClient 调用大模型API
- 流式输出：SSE（Server-Sent Events）逐字输出
- Prompt管理：模板文件统一管理，支持Nacos动态更新
- 模型厂商适配：策略模式 + 配置文件切换

#### 4.2.4 任务拆分

| 子任务 | 预估工时 | 优先级 |
|-------|---------|-------|
| 大模型API调用封装（多厂商适配） | 1人日 | P0 |
| Prompt模板设计与调试 | 0.5人日 | P0 |
| 智能填单（意图识别+结构化输出） | 1人日 | P0 |
| 智能数据分析（考勤异常+风险预警） | 1人日 | P1 |
| OA知识库问答（知识库构建+RAG） | 1人日 | P1 |
| 前端AI对话UI组件 | 0.5人日 | P1 |
| 智能填单前端组件 | 0.5人日 | P1 |
| 分析报告展示页面 | 0.5人日 | P1 |

---

### 4.3 EXT-02 ★ 大数据可视化看板（data-visual-service）

#### 4.3.1 任务概述

使用Vue3 + DataV + ECharts构建企业OA数据驾驶舱，实现人事概览、考勤趋势、审批流转三大主题大屏。

#### 4.3.2 功能详述

**大屏1：人事数据概览屏**

| 子功能 | 可视化形式 |
|-------|-----------|
| 部门人数分布 | 饼图/玫瑰图 |
| 在职/离职趋势 | 堆叠柱状图 + 折线图 |
| 学历分布 | 饼图 |
| 司龄分布 | 柱状图 |
| 本月入职/离职人数 | 数字翻牌器（DataV） |
| 部门编制饱和度 | 进度条 |

**大屏2：考勤数据大屏**

| 子功能 | 可视化形式 |
|-------|-----------|
| 今日出勤率 | 仪表盘 |
| 月度考勤趋势 | 折线图（正常/迟到/早退/旷工） |
| 部门迟到率排名 | 横向柱状图 |
| 部门加班时长对比 | 柱状图 |
| 实时打卡统计 | 数字翻牌器 + 表格 |
| 迟到热力日历 | 日历热力图 |

**大屏3：审批流转大屏**

| 子功能 | 可视化形式 |
|-------|-----------|
| 本月申请/通过/驳回数量 | 数字翻牌器 |
| 审批通过率 | 环形图 |
| 各部门审批效率对比 | 柱状图（平均耗时） |
| 申请类型分布 | 饼图（请假/加班/外出） |
| 审批量趋势 | 面积图 |
| 待审批积压量 | 警告标识 |

#### 4.3.3 技术方案

```
┌────────────────────────────────────────────────────────────┐
│  Vue3 + DataV + ECharts 前端大屏                             │
│  ├── 人事概览屏   (自动轮播, 30s切换主题)                     │
│  ├── 考勤数据屏   (自动刷新, 5min)                           │
│  └── 审批流转屏   (自动刷新, 5min)                           │
│                      │  HTTP 轮询                           │
│  data-visual-service (后端数据服务)                          │
│  ├── 查询统计库预聚合数据                                     │
│  ├── 实时查询业务库（今日打卡数等）                             │
│  └── 缓存热点数据 (Redis, TTL=60s)                           │
│                      │                                      │
│  XXL-Job: 定时聚合原始数据 → statistics_db                    │
└────────────────────────────────────────────────────────────┘
```

#### 4.3.4 任务拆分

| 子任务 | 预估工时 | 优先级 |
|-------|---------|-------|
| 统计数据库建表 + XXL-Job数据聚合任务 | 1.5人日 | P0 |
| 数据可视化后端接口开发 | 1人日 | P0 |
| 前端大屏框架搭建（DataV布局+自适应） | 1人日 | P0 |
| 人事数据概览屏开发 | 0.5人日 | P1 |
| 考勤数据大屏开发 | 0.5人日 | P1 |
| 审批流转大屏开发 | 0.5人日 | P1 |
| 大屏自动刷新+轮播切换 | 0.5人日 | P1 |

---

### 4.4 EXT-03 ★ Android移动端（oa-mobile）

#### 4.4.1 任务概述

开发使用Flutter，提供移动端快速打卡、消息推送、申请审批等核心轻量功能，不要求覆盖全部PC端功能，聚焦"随时随地处理办公事务"。

#### 4.4.2 功能详述

**功能1：登录与身份持久化**

| 子功能 | 描述 |
|-------|------|
| 账号密码登录 | 调用 `/api/user/login` 获取JWT Token |
| Token本地存储 | 使用 SharedPreferences / DataStore 持久化Token |
| 自动登录 | Token未过期时自动跳转首页，过期自动跳转登录页 |
| 退出登录 | 清除本地Token，回到登录页 |

**功能2：快速打卡**

| 子功能 | 描述 |
|-------|------|
| 上班/下班一键打卡 | 主页面核心按钮，调用 `/api/attendance/punch/in` 和 `/api/attendance/punch/out` |
| 打卡状态显示 | 显示今日打卡状态：未打卡 / 已打上班卡 / 已打下班卡 / 今日完成 |
| 打卡时间展示 | 显示今日上班/下班打卡的具体时间 |
| 本月考勤概览 | 简单列表展示本月迟到/早退/旷工/正常天数统计 |

**功能3：消息通知**

| 子功能 | 描述 |
|-------|------|
| 消息列表 | 调用 `/api/notice/messages` 获取站内信列表，分类展示（审批/考勤/系统） |
| 消息已读 | 点击消息标记已读，调用 `/api/notice/messages/{id}/read` |
| 未读角标 | 首页/底部导航栏显示未读消息数量红点 |
| 推送通知 | 使用 Firebase Cloud Messaging（FCM）或第三方推送SDK，后台有新的审批/公告时弹通知栏提醒（可选，看时间） |

**功能4：申请审批**

| 子功能 | 描述 |
|-------|------|
| 提交申请 | 支持请假/加班两种类型，填写基本信息（类型、时间、原因），调用 `/api/approval/applications` 提交 |
| 我的申请列表 | 调用 `/api/approval/applications` 查看提交的申请及状态 |
| 待审批列表 | 有审批权限的用户（主管/HR）查看待审批列表，调用 `/api/approval/pending` |
| 审批操作 | 同意/驳回 + 填写审批意见，调用 `/api/approval/pending/{id}/approve` |

**功能5：个人信息**

| 子功能 | 描述 |
|-------|------|
| 个人信息展示 | 显示头像、姓名、部门、岗位、手机号 |
| 修改密码 | 旧密码 + 新密码 + 确认新密码 |

#### 4.4.3 技术方案

```
┌──────────────────────────────────────────────────────────┐
│  Android App                                              │
│  ├── Retrofit + OkHttp: HTTP请求 + JWT拦截器              │
│  ├── SharedPreferences/DataStore: Token本地持久化         │
│  ├── MVVM架构: ViewModel + LiveData/StateFlow             │
│  └── Material Design 3: UI组件库                          │
│                      │  HTTP (REST API)                   │
│           后端微服务集群 (复用PC端全部API)                  │
└──────────────────────────────────────────────────────────┘
```

**关键选型**：
- 网络请求：Retrofit2 + OkHttp（统一拦截器自动附加JWT Token到请求头）
- 异步处理：Kotlin Coroutines
- UI框架：Jetpack Compose（优先）或 XML + Material Design 3
- 最低SDK：Android 8.0（API 26），目标SDK：Android 14（API 34）

**页面结构**：

| 页面 | 路由 | 说明 |
|------|------|------|
| 登录页 | `/login` | 账号密码输入 + 登录按钮 |
| 首页 | `/home` | 今日打卡状态 + 快捷打卡按钮 + 本月考勤概览 |
| 消息列表 | `/messages` | 站内信分Tab列表（审批/考勤/系统） |
| 消息详情 | `/messages/{id}` | 消息内容 + 关联业务跳转 |
| 申请列表 | `/applications` | 我的申请 + 状态筛选 |
| 提交申请 | `/applications/submit` | 请假/加班表单 |
| 待审批 | `/pending` | 待审批列表 + 审批操作 |
| 我的 | `/profile` | 个人信息 + 修改密码 + 退出登录 |

#### 4.4.4 Android端需调用的后端API汇总

| API路由 | 用途 | 所在微服务 |
|--------|------|-----------|
| `POST /api/user/login` | 登录获取Token | user-service |
| `GET /api/user/current` | 获取当前用户信息 | user-service |
| `POST /api/attendance/punch/in` | 上班打卡 | attendance-service |
| `POST /api/attendance/punch/out` | 下班打卡 | attendance-service |
| `GET /api/attendance/records/mine` | 个人考勤记录 | attendance-service |
| `GET /api/notice/messages` | 站内信列表 | notice-service |
| `PUT /api/notice/messages/{id}/read` | 标记消息已读 | notice-service |
| `GET /api/notice/unread-count` | 未读消息数 | notice-service |
| `POST /api/approval/applications` | 提交申请 | approval-service |
| `GET /api/approval/applications` | 我的申请列表 | approval-service |
| `GET /api/approval/pending` | 待审批列表 | approval-service |
| `PUT /api/approval/pending/{id}/approve` | 审批操作 | approval-service |

> Android端完全复用后端已有的RESTful API，无需为移动端单独开发接口。

#### 4.4.5 任务拆分

| 子任务 | 预估工时 | 优先级 |
|-------|---------|-------|
| 项目初始化（Gradle配置、依赖引入、MVVM架构搭建） | 0.5人日 | P0 |
| 网络层封装（Retrofit + JWT拦截器 + Token持久化） | 0.5人日 | P0 |
| 登录页 + Token自动登录逻辑 | 0.5人日 | P0 |
| 首页 + 快速打卡功能 | 1人日 | P0 |
| 消息列表 + 未读角标 | 0.5人日 | P1 |
| 申请提交 + 申请列表 | 1人日 | P1 |
| 待审批列表 + 审批操作 | 0.5人日 | P1 |
| 个人信息 + 修改密码 | 0.5人日 | P1 |
| APK打包 + 真机测试 | 0.5人日 | P1 |

---

### 4.5 EXT-04 分布式高并发优化

#### 4.5.1 任务概述

引入XXL-Job定时任务、RocketMQ异步消息、Sentinel熔断降级，提升系统在高并发场景下的稳定性与吞吐量。

#### 4.5.2 功能详述

| 能力项 | 技术方案 | 应用场景 |
|-------|---------|---------|
| 分布式定时任务 | XXL-Job | 每日凌晨自动统计考勤数据（对比班次标准，判定迟到/早退/旷工）；每月1号生成上月考勤汇总；定时聚合数据到统计库供可视化看板消费 |
| 异步消息 | RocketMQ | 审批结果站内信通知（审批通过/驳回后异步发送消息，解耦审批服务与通知服务）；操作日志异步记录（AOP切面收集 → 发送MQ → 日志服务消费入库） |
| 熔断降级 | Sentinel | 结合OpenFeign远程调用，当attendance-service宕机时自动熔断并返回降级数据（如"考勤服务暂不可用"），防止级联故障导致系统雪崩 |

#### 4.5.3 任务拆分

| 子任务 | 预估工时 | 优先级 |
|-------|---------|-------|
| XXL-Job调度中心部署 + 执行器集成 | 1人日 | P1 |
| 考勤统计定时任务开发 | 1人日 | P1 |
| RocketMQ部署 + 消息生产者/消费者开发 | 1人日 | P1 |
| 站内信异步通知改造 | 0.5人日 | P1 |
| Sentinel控制台部署 + 降级规则配置 | 0.5人日 | P1 |
| OpenFeign远程调用熔断降级逻辑 | 0.5人日 | P1 |

---

### 4.6 EXT-05 高级审批流程（Flowable工作流引擎）

#### 4.6.1 任务概述

将当前简化的一级审批升级为基于Flowable工作流引擎的多级审批，支持自定义流程模板、多节点流转、会签/或签等高级特性。

#### 4.6.2 功能详述

| 子功能 | 描述 |
|-------|------|
| 流程定义 | 管理员通过Flowable Modeler设计审批流程模板（如：员工→部门主管→HR→总经理） |
| 流程部署 | 将流程定义部署到Flowable引擎，版本化管理 |
| 多级审批流转 | 申请提交后按流程定义自动流转到下一节点审批人 |
| 会签/或签 | 同一节点多审批人时，支持会签（全部同意才通过）和或签（一人同意即通过） |
| 流程监控 | 管理员查看所有运行中的流程实例、历史流程、当前节点 |
| 流程撤回/驳回 | 申请人撤回流程，审批人驳回到上一节点或发起人 |

#### 4.6.3 技术要点

- 引入 `flowable-spring-boot-starter` 依赖
- 替换现有 `app_approval_record` 逻辑为Flowable Task查询
- 前端集成流程图高亮当前节点展示

#### 4.6.4 任务拆分

| 子任务 | 预估工时 | 优先级 |
|-------|---------|-------|
| Flowable集成 + 数据库表初始化 | 0.5人日 | P2 |
| 流程定义设计（请假/加班/外出三条流程） | 0.5人日 | P2 |
| 流程启动/流转/完成任务开发 | 1.5人日 | P2 |
| 流程图前端展示 | 1人日 | P2 |
| 会签/或签逻辑 + 流程监控 | 1人日 | P2 |

---

### 4.7 EXT-06 分布式事务（Seata AT模式）

#### 4.7.1 任务概述

在员工离职等跨多个微服务的业务场景中，使用Seata AT模式保证多个数据库操作要么全部成功、要么全部回滚。

#### 4.7.2 典型场景：员工离职

```
┌─────────────────────────────────────────────────────┐
│  员工离职分布式事务 (@GlobalTransactional)              │
│                                                       │
│  ① user-service:    更新sys_user.status=0（离职）      │
│  ② attendance-service: 归档该员工考勤数据              │
│  ③ asset-service:   强制归还该员工领用的所有资产         │
│  ④ asset-service:   写入离职变动记录                    │
│                                                       │
│  以上4步要么全部成功，要么全部回滚                       │
└─────────────────────────────────────────────────────┘
```

#### 4.7.3 任务拆分

| 子任务 | 预估工时 | 优先级 |
|-------|---------|-------|
| Seata Server部署 + 各微服务AT模式配置 | 1人日 | P2 |
| 员工离职分布式事务开发 | 1人日 | P2 |
| 事务回滚测试（模拟中间步骤失败） | 0.5人日 | P2 |

---

### 4.8 EXT-07 搜索与报表优化

#### 4.8.1 任务概述

引入Elasticsearch实现公告、申请单的全文检索，以及Excel考勤报表导出功能。

#### 4.8.2 功能详述

| 子功能 | 技术方案 | 描述 |
|-------|---------|------|
| 公告全文搜索 | Elasticsearch | 公告发布时同步索引到ES，员工按关键词搜索公告标题和内容 |
| 申请单搜索 | Elasticsearch | 管理员按员工姓名/申请类型/日期范围搜索历史申请单 |
| 月考勤报表导出 | Apache POI / EasyExcel | 按部门/月份导出考勤明细Excel（员工姓名、部门、迟到次数、早退次数、旷工天数、加班时长） |
| 月度人事报表导出 | Apache POI / EasyExcel | 导出在职员工花名册Excel |

#### 4.8.3 任务拆分

| 子任务 | 预估工时 | 优先级 |
|-------|---------|-------|
| ES部署 + Spring Data Elasticsearch集成 | 0.5人日 | P2 |
| 公告索引同步 + 全文搜索接口 | 1人日 | P2 |
| 申请单索引同步 + 搜索接口 | 0.5人日 | P2 |
| EasyExcel引入 + 考勤报表导出 | 0.5人日 | P2 |
| 人事花名册导出 | 0.5人日 | P2 |

---

### 4.9 EXT-08 链路追踪与接口文档

#### 4.9.1 任务概述

使用SkyWalking实现全链路追踪与性能监控，使用Knife4j自动生成接口文档并支持在线调试。

#### 4.9.2 功能详述

| 子功能 | 技术方案 | 描述 |
|-------|---------|------|
| 链路追踪 | SkyWalking | 监控每次API请求在各微服务间的调用链路、响应时间、SQL耗时，快速定位性能瓶颈 |
| 服务拓扑图 | SkyWalking | 自动生成微服务依赖拓扑图，展示服务间调用关系 |
| 接口文档 | Knife4j (Swagger增强) | 每个微服务自动生成接口文档页面，支持在线调试、导出OpenAPI 3.0规范 |
| 全局异常监控 | SkyWalking Alarm | 接口异常率超过阈值时SkyWalking告警（Webhook通知） |

#### 4.9.3 任务拆分

| 子任务 | 预估工时 | 优先级 |
|-------|---------|-------|
| SkyWalking Server部署 + Agent配置 | 0.5人日 | P2 |
| 各微服务接入SkyWalking Agent | 0.5人日 | P2 |
| Knife4j依赖引入 + 配置 | 0.5人日 | P2 |
| 各Controller补充Swagger注解 | 0.5人日 | P2 |

---

### 4.10 EXT-09 容器化部署

#### 4.10.1 任务概述

使用Docker + Docker Compose将全部微服务及中间件容器化，实现一键部署与环境统一。

#### 4.10.2 部署架构

```
┌─────────────────────────────────────────────────────────┐
│  Docker Compose 编排                                      │
│                                                           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │ Nacos    │ │ MySQL    │ │ Redis    │ │ RocketMQ │   │
│  │ (1节点)   │ │ (7实例)   │ │ (1节点)   │ │ (1节点)   │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐   │
│  │ XXL-Job  │ │Sentinel  │ │SkyWalking│ │ Nginx    │   │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘   │
│                                                           │
│  ┌──────────────────────────────────────────────┐       │
│  │  微服务镜像 (gateway, user, attendance,       │       │
│  │  approval, notice, asset, ai, data-visual)    │       │
│  │  每个服务2个实例（支持负载均衡）                  │       │
│  └──────────────────────────────────────────────┘       │
│  ┌──────────────────────────────────────────────┐       │
│  │  Vue3前端 (Nginx提供静态文件服务)                │       │
│  └──────────────────────────────────────────────┘       │
└─────────────────────────────────────────────────────────┘
```

#### 4.10.3 产出物

| 产出物 | 说明 |
|-------|------|
| Dockerfile | 每个微服务一个Dockerfile（多阶段构建，减小镜像体积） |
| docker-compose.yml | 统一编排文件，`docker compose up -d` 一键启动全部服务 |
| .env | 环境变量文件（数据库密码、JWT密钥等敏感配置） |
| 部署文档 | README_DEPLOY.md，含环境要求、启动步骤、常见问题 |

#### 4.10.4 任务拆分

| 子任务 | 预估工时 | 优先级 |
|-------|---------|-------|
| 各微服务Dockerfile编写（多阶段构建） | 0.5人日 | P2 |
| Vue3前端Nginx Dockerfile编写 | 0.5人日 | P2 |
| Docker Compose编排文件编写 | 1人日 | P2 |
| 中间件容器化配置（数据卷持久化、网络隔离） | 0.5人日 | P2 |
| 部署文档编写 | 0.5人日 | P2 |

---

### 4.11 总体任务汇总

| 阶段 | 任务 | 产出物 |
|------|------|-------|
| 基础设施 | 父工程POM、Nacos配置、Gateway路由、统一异常/响应封装 | 可运行的基础工程骨架 |
| 核心业务1 | user-service 完整功能 | 登录、员工CRUD、部门岗位、RBAC |
| 核心业务2 | attendance-service 完整功能 | 班次、打卡、考勤查询统计 |
| 核心业务3 | approval-service 完整功能 | 申请提交、审批处理、流转记录 |
| 核心业务4 | notice-service 完整功能 | 公告发布、已读未读、站内信 |
| 扩展业务 | asset-service | 人事档案、资产登记领用 |
| ★ 扩展功能1 | AI智能办公助手 | 智能填单、数据分析、知识问答 |
| ★ 扩展功能2 | 大数据可视化看板 | 人事/考勤/审批三大数据大屏 |
| ★ 扩展功能3 | Android移动端 | 快速打卡、消息通知、申请审批 |
| 可选扩展4 | 分布式高并发优化 | XXL-Job统计、MQ异步、Sentinel降级 |
| 可选扩展5 | Flowable高级审批 | 多级审批流转、流程监控 |
| 可选扩展6 | Seata分布式事务 | 员工离职多表联动 |
| 可选扩展7 | ES搜索+Excel报表 | 全文检索、考勤报表导出 |
| 可选扩展8 | SkyWalking+Knife4j | 链路追踪、接口文档 |
| 可选扩展9 | Docker容器化部署 | 一键部署、环境统一 |
| 文档 | 项目开发文档 + PPT | 需求文档、设计文档、答辩PPT |

---

## 五、全量开发任务清单与6人分组

### 5.1 任务完整罗列

> 以下按"后端微服务 → 前端页面 → 分布式中间件 → 扩展功能 → 工程化"的维度，穷举全部开发任务，共 **118 项**。

#### 一、基础设施层（项目骨架）

| 编号 | 任务 | 说明 |
|------|------|------|
| T001 | Maven父工程 + 子模块创建 | 父POM统一管理依赖版本（SpringBoot3 + SpringCloud + MyBatis-Plus），创建各微服务子模块 |
| T002 | common公共模块 | 统一响应体 `Result<T>`、统一异常处理、JWT工具类、Redis工具类、BaseEntity基类 |
| T003 | Nacos安装与配置 | 启动Nacos Server，各服务注册到Nacos，配置中心接入 |
| T004 | Gateway网关 | 路由分发规则、跨域配置、全局JWT Token校验过滤器、白名单（登录接口放行） |
| T005 | Redis集成 | 分布式锁封装、热点数据缓存（员工信息、Token黑名单等） |

#### 二、user-service（用户权限服务）

| 编号 | 任务 | 说明 |
|------|------|------|
| T006 | 登录接口 | `POST /api/user/login`，BCrypt密码校验，生成JWT返回 |
| T007 | 登出接口 | `POST /api/user/logout`，Token加入Redis黑名单（可选） |
| T008 | 当前用户信息 | `GET /api/user/current`，返回用户信息+角色+权限列表 |
| T009 | 部门管理 CRUD | 部门树查询、新增、编辑、删除（逻辑删除）、树形结构构建 |
| T010 | 岗位管理 CRUD | 岗位分页列表、新增、编辑、删除，关联部门筛选 |
| T011 | 员工管理 CRUD | 员工分页列表（多条件筛选）、新增、编辑、离职标记、密码重置 |
| T012 | 角色管理 CRUD | 角色列表、新增、编辑、删除、角色分配菜单权限 |
| T013 | 菜单管理 CRUD | 菜单树查询、新增、编辑、删除，三级结构（目录+菜单+按钮） |
| T014 | 用户-角色绑定 | 为用户分配/修改角色 |
| T015 | 动态路由接口 | `GET /api/user/menus/routers`，根据当前用户角色返回可访问的路由表 |
| T016 | 权限注解 AOP | `@RequiresRole` / `@RequiresPermission` 注解，切面校验 |

#### 三、attendance-service（考勤服务）

| 编号 | 任务 | 说明 |
|------|------|------|
| T017 | 班次管理 CRUD | 班次模板增删改查，定义上下班时间、弹性时段 |
| T018 | 上班打卡 | `POST /api/attendance/punch/in`，Redis分布式锁防重 + 校验是否已打卡 |
| T019 | 下班打卡 | `POST /api/attendance/punch/out`，同上 |
| T020 | 个人考勤记录 | `GET /api/attendance/records/mine`，按日/按月查询 |
| T021 | 部门/全公司考勤 | `GET /api/attendance/records/dept` 和 `/all`，分级权限查看 |
| T022 | 用户-班次分配 | user_shift 表维护，为员工分配班次 |

#### 四、approval-service（审批服务 — 基础版）

| 编号 | 任务 | 说明 |
|------|------|------|
| T023 | 申请提交 | `POST /api/approval/applications`，请假/加班/外出三种类型，自动生成申请单号 |
| T024 | 我的申请列表 | `GET /api/approval/applications`，按状态筛选（草稿/审批中/已通过/已驳回/已撤销） |
| T025 | 申请详情 | `GET /api/approval/applications/{id}`，含审批链路时间线 |
| T026 | 撤销申请 | `PUT /api/approval/applications/{id}/cancel`，仅审批中可撤销 |
| T027 | 待审批列表 | `GET /api/approval/pending`，当前用户为审批人的待办列表 |
| T028 | 审批操作 | `PUT /api/approval/pending/{id}/approve`，同意/驳回 + 审批意见 |
| T029 | 已办列表 | `GET /api/approval/processed`，已审批的历史记录 |
| T030 | 自动匹配审批人 | 根据申请人部门查找直属主管作为审批人 |

#### 五、notice-service（公告通知服务）

| 编号 | 任务 | 说明 |
|------|------|------|
| T031 | 公告发布 | `POST /api/notice/list`，标题+富文本内容+发布范围+生效时间 |
| T032 | 公告列表 | `GET /api/notice/list`，分页+按时间倒序，仅返回当前用户可见范围 |
| T033 | 公告详情+标记已读 | `GET /api/notice/list/{id}`，写入 ntc_read_status 记录 |
| T034 | 公告编辑/下架 | `PUT` 和 `DELETE` 公告 |
| T035 | 未读数量 | `GET /api/notice/unread-count`，首页角标用 |
| T036 | 站内消息列表 | `GET /api/notice/messages`，分类展示（审批/考勤/系统） |
| T037 | 标记消息已读 | `PUT /api/notice/messages/{id}/read` |

#### 六、asset-service（资产人事服务）

| 编号 | 任务 | 说明 |
|------|------|------|
| T038 | 员工档案管理 | 档案增改查，含学历、合同、紧急联系人等字段 |
| T039 | 人事变动记录 | 入职/转正/调岗/离职四种变动类型CRUD |
| T040 | 合同到期预警 | 查询即将到期合同列表 |
| T041 | 资产登记 CRUD | 固定资产/办公用品/电子设备信息管理 |
| T042 | 资产领用 | 员工申请领用，记录领用日期 |
| T043 | 资产归还 | 更新归还日期，资产状态恢复为"可领用" |
| T044 | 领用记录查询 | 按资产/按人查询历史领用记录 |

#### 七、ai-service（AI智能办公服务）

| 编号 | 任务 | 说明 |
|------|------|------|
| T045 | 大模型API调用封装 | 策略模式适配多厂商（通义千问/文心一言/DeepSeek） |
| T046 | Prompt模板管理 | 模板文件/Nacos管理，请假/加班/外出三种场景Prompt |
| T047 | 智能填单接口 | 接收自然语言 → 调用大模型 → 返回结构化申请JSON |
| T048 | 考勤异常分析 | 读取月度考勤数据 → AI分析 → 生成异常名单+风险等级 |
| T049 | 员工风险预警 | 结合请假模式（频繁周一周五请假等）识别离职风险 |
| T050 | OA知识库问答 | 构建知识库文档 → RAG检索增强 → 对话式回答 |
| T051 | 对话历史管理 | 按 session_id 存储多轮对话，支持历史查询 |
| T052 | 分析报告管理 | AI分析结果持久化存储，支持列表查询和详情查看 |

#### 八、data-visual-service（数据可视化服务）

| 编号 | 任务 | 说明 |
|------|------|------|
| T053 | 数据聚合定时任务 | XXL-Job定时从各业务库抽取数据到 statistics_db |
| T054 | 人事概览接口 | 部门人数分布、入职离职趋势、学历分布等统计接口 |
| T055 | 考勤统计接口 | 月度趋势、部门迟到率排名、加班对比等统计接口 |
| T056 | 审批统计接口 | 申请/通过/驳回数量、审批效率、类型分布等统计接口 |
| T057 | 实时数据接口 | 今日打卡数、待审批积压量等实时快照 |
| T058 | Redis数据缓存 | 热点统计数据缓存60秒，减少数据库压力 |

#### 九、Vue3前端（Web管理后台）

| 编号 | 任务 | 说明 |
|------|------|------|
| T059 | 项目初始化 | Vue3 + Vite + Element Plus + Vue Router + Pinia + Axios |
| T060 | 登录页 | 账号密码表单 + Token存储 + 401拦截跳转 |
| T061 | 布局框架 | 侧边栏菜单 + 顶部导航 + 主内容区 + 动态路由渲染 |
| T062 | 路由权限控制 | 根据用户角色动态生成路由表，无权限页面不可见 |
| T063 | 系统管理-员工管理 | 列表+搜索+新增/编辑弹窗+离职操作+密码重置 |
| T064 | 系统管理-部门管理 | 树形表格+新增/编辑节点 |
| T065 | 系统管理-岗位管理 | 列表+筛选+新增/编辑弹窗 |
| T066 | 系统管理-角色管理 | 列表+新增/编辑+菜单权限树分配 |
| T067 | 系统管理-菜单管理 | 树形表格+新增目录/菜单/按钮 |
| T068 | 考勤-打卡页面 | 上下班打卡按钮+今日状态+打卡时间显示 |
| T069 | 考勤-考勤记录 | 日历视图+列表视图，按月筛选 |
| T070 | 考勤-班次管理 | 班次列表+新增/编辑弹窗+用户班次分配 |
| T071 | 审批-我的申请 | 状态筛选+列表+申请详情（时间线） |
| T072 | 审批-提交申请 | 请假/加班/外出表单 |
| T073 | 审批-待审批 | 待办列表+审批弹窗（同意/驳回+意见） |
| T074 | 审批-已办列表 | 历史审批记录列表 |
| T075 | 公告-公告列表 | 卡片列表+已读/未读状态+详情弹窗 |
| T076 | 公告-公告发布 | 富文本编辑器+发布范围选择（仅管理员） |
| T077 | 通知-消息中心 | Tab分类+消息列表+已读标记 |
| T078 | 资产-资产列表 | 列表+新增/编辑弹窗+状态筛选 |
| T079 | 资产-领用记录 | 领用/归还操作+记录查询 |
| T080 | 资产-人事变动 | 变动记录列表+新增变动 |
| T081 | AI-对话页面 | 聊天界面+多轮对话+流式输出展示 |
| T082 | AI-智能填单 | 自然语言输入框+AI生成表单预览+一键提交 |
| T083 | AI-分析报告 | 报告列表+报告详情查看 |
| T084 | 数据大屏-框架 | DataV全屏布局+自适应缩放+三屏轮播切换 |
| T085 | 数据大屏-人事屏 | 饼图+柱状图+折线图+数字翻牌器 |
| T086 | 数据大屏-考勤屏 | 仪表盘+柱状图+日历热力图+数字翻牌器 |
| T087 | 数据大屏-审批屏 | 环形图+面积图+柱状图+数字翻牌器 |
| T088 | 数据大屏-报表页 | 统计表格+图表+时间筛选 |

#### 十、Android移动端（Flutter）

| 编号 | 任务 | 说明 |
|------|------|------|
| T089 | Flutter项目初始化 | Flutter工程搭建 + Dio网络库 + Provider/Riverpod状态管理 |
| T090 | 网络层封装 | Dio拦截器自动附加JWT + Token持久化 + 401自动跳登录 |
| T091 | 登录页 | 账号密码登录+Token存储+自动登录判断 |
| T092 | 首页+快速打卡 | 打卡按钮+今日状态展示+本月考勤概览 |
| T093 | 消息列表 | 站内信列表+未读角标+分类Tab |
| T094 | 申请提交 | 请假/加班表单，提交到后端 |
| T095 | 申请列表 | 我的申请列表+状态筛选 |
| T096 | 待审批+审批操作 | 待审批列表+同意/驳回操作 |
| T097 | 个人信息+修改密码 | 用户信息展示+密码修改 |
| T098 | APK打包 | Release签名+混淆+真机测试 |

#### 十一、分布式中间件与高级扩展

| 编号 | 任务 | 说明 |
|------|------|------|
| T099 | XXL-Job考勤统计任务 | 每日凌晨自动对比打卡记录与班次，判定迟到/早退/旷工，写入 att_daily_summary |
| T100 | XXL-Job月报汇总任务 | 每月1日汇总上月考勤数据到 statistics_db |
| T101 | RocketMQ异步通知 | 审批结果/考勤异常触发MQ → notice-service消费写入 ntc_message |
| T102 | RocketMQ操作日志 | AOP收集操作日志 → MQ → 日志服务消费入库 |
| T103 | Sentinel服务熔断 | 各微服务接入Sentinel，OpenFeign远程调用配置降级fallback |
| T104 | Flowable工作流集成 | 引入Flowable依赖，替换简化审批为工作流引擎 |
| T105 | Flowable流程定义 | 请假/加班/外出三条BPMN流程模型设计 |
| T106 | Flowable流程实例 | 启动流程/完成任务/查询待办/查询历史 |
| T107 | Flowable流程图展示 | 前端集成bpmn.js，高亮当前审批节点 |
| T108 | Seata Server部署 | Seata TC Server安装，AT模式配置 |
| T109 | Seata员工离职事务 | 离职联动：user禁用 + asset资产回收 + archive档案归档，@GlobalTransactional |
| T110 | Elasticsearch公告索引 | 公告发布/更新时同步ES索引，搜索接口开发 |
| T111 | Elasticsearch申请单索引 | 申请单同步ES，按员工/类型/日期搜索 |
| T112 | Excel考勤报表导出 | EasyExcel按部门/月份导出考勤明细 |
| T113 | Excel人事花名册导出 | EasyExcel导出在职员工花名册 |
| T114 | SkyWalking链路追踪 | SkyWalking Server + Agent部署，全链路监控 |
| T115 | Knife4j接口文档 | 每个微服务接入Knife4j，Controller添加ApiOperation注解 |
| T116 | Dockerfile编写 | 9个微服务 + Vue3前端Nginx，全部多阶段构建 |
| T117 | Docker Compose编排 | 中间件+微服务+前端一键编排，数据卷持久化 |
| T118 | 部署文档编写 | README_DEPLOY.md，环境要求+启动步骤+FAQ |


> **文档版本**：v1.6
> **下次更新**：根据团队分工和开发进度实时调整