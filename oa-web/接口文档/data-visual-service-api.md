# data-visual-service 接口文档

> 服务名：`oa-data-visual-service`  
> 直连地址：`http://localhost:8087`  
> 网关地址：`http://localhost:8080`  
> Swagger/OpenAPI：`http://localhost:8087/v3/api-docs`

---

## 一、通用说明

### 1. 认证

通过网关访问时，除健康检查接口外都需要携带登录后返回的 JWT：

```http
Authorization: Bearer {accessToken}
```

直连 `oa-data-visual-service` 时当前服务不做本地鉴权，建议开发调试直连，联调前端时走 gateway。

### 2. 统一响应结构

所有接口返回当前项目统一响应体：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1760000000000
}
```

### 3. 通用查询参数

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| month | String | 否 | 统计月份，格式 `yyyy-MM`，默认当前月份 |
| endMonth | String | 否 | 趋势结束月份，格式 `yyyy-MM`，默认当前月份 |
| months | Integer | 否 | 趋势月份数，默认 6，范围 1-12 |

月份格式错误时返回：

```json
{
  "code": 400,
  "message": "统计月份格式应为 yyyy-MM",
  "data": null
}
```

---

## 二、健康检查

### 1. 服务健康检查

- **GET** `/api/visual/health`
- **认证**：不需要

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "service": "oa-data-visual-service",
    "status": "UP"
  },
  "timestamp": 1760000000000
}
```

---

## 三、统计同步接口

### 2. 手动同步指定月份统计数据

- **POST** `/api/visual/statistics/sync`
- **Query**：`month=2026-07`
- **说明**：从 `user_db`、`attendance_db`、`approval_db` 抽取并聚合指定月份数据，幂等写入 `statistics_db` 三张统计表。
- **幂等策略**：先删除该月份旧统计数据，再重新插入最新统计结果。

不传 `month` 时默认同步当前月份。

响应 `data` 示例：

```json
{
  "statMonth": "2026-07",
  "deptOverviewRows": 4,
  "attendanceRows": 4,
  "approvalRows": 3,
  "syncTime": "2026-07-21T16:30:00"
}
```

### 3. 手动同步上月统计数据

- **POST** `/api/visual/statistics/sync/last-month`
- **说明**：同步当前日期所在月份的上一个自然月，适合月初补偿或调试 XXL-Job 月报任务。

---

## 四、Dashboard 接口

### 4. 全公司概览数据

- **GET** `/api/visual/dashboard/overview`
- **Query**：`month=2026-07`
- **说明**：返回总人数、出勤率、加班、审批汇总等首页概览指标。

响应 `data` 示例：

```json
{
  "statMonth": "2026-07",
  "totalEmployees": 120,
  "newHires": 8,
  "resignations": 2,
  "attendanceRate": 96.50,
  "overtimeHours": 328.5,
  "totalApplications": 86,
  "approvedCount": 70,
  "rejectedCount": 6,
  "pendingCount": 10,
  "approvalPassRate": 81.40,
  "avgApprovalHours": 5.6,
  "refreshTime": "2026-07-21T15:00:00"
}
```

### 5. 部门人员分布

- **GET** `/api/visual/dashboard/dept-distribution`
- **Query**：`month=2026-07`
- **说明**：按部门返回在职人数和占比，可用于饼图/玫瑰图。

响应 `data` 示例：

```json
[
  {
    "deptId": 1,
    "deptName": "技术部",
    "value": 48,
    "rate": 40.00,
    "unit": "人"
  }
]
```

### 6. 月度考勤趋势

- **GET** `/api/visual/dashboard/attendance-trend`
- **Query**：`endMonth=2026-07&months=6`
- **说明**：返回最近 N 个月考勤趋势，月份没有数据时返回 0 值。

响应 `data` 示例：

```json
[
  {
    "statMonth": "2026-07",
    "normalCount": 2100,
    "lateCount": 35,
    "earlyCount": 12,
    "absentCount": 3,
    "leaveCount": 28,
    "overtimeHours": 328.5,
    "attendanceRate": 96.42
  }
]
```

### 7. 部门加班对比

- **GET** `/api/visual/dashboard/dept-overtime`
- **Query**：`month=2026-07`
- **说明**：按部门返回加班总时长，按加班时长倒序。

响应 `data` 示例：

```json
[
  {
    "deptId": 1,
    "deptName": "技术部",
    "value": 126.5,
    "rate": 0,
    "unit": "小时"
  }
]
```

### 8. 审批流转统计

- **GET** `/api/visual/dashboard/approval-stats`
- **Query**：`month=2026-07`
- **说明**：返回申请总数、通过/驳回/待审批数量和占比。

响应 `data` 示例：

```json
{
  "statMonth": "2026-07",
  "totalApplications": 86,
  "approvedCount": 70,
  "rejectedCount": 6,
  "pendingCount": 10,
  "passRate": 81.40,
  "rejectRate": 6.98,
  "pendingRate": 11.63,
  "statusDistribution": [
    {"name": "已通过", "value": 70, "unit": "件"},
    {"name": "已驳回", "value": 6, "unit": "件"},
    {"name": "审批中", "value": 10, "unit": "件"}
  ]
}
```

### 9. 审批效率

- **GET** `/api/visual/dashboard/approval-speed`
- **Query**：`month=2026-07`
- **说明**：按部门返回平均审批耗时，按耗时升序。

响应 `data` 示例：

```json
[
  {
    "deptId": 1,
    "deptName": "技术部",
    "totalApplications": 32,
    "avgApprovalHours": 4.5
  }
]
```

---

## 五、大屏接口

### 10. 考勤数据大屏

- **GET** `/api/visual/screen/attendance`
- **Query**：`month=2026-07`
- **说明**：聚合考勤概览、月度趋势、部门加班、部门迟到率、今日打卡实时数据。

响应 `data` 结构：

```json
{
  "statMonth": "2026-07",
  "overview": {},
  "monthlyTrend": [],
  "deptOvertime": [],
  "deptLateRanking": [],
  "realtime": {
    "date": "2026-07-21",
    "activeEmployees": 120,
    "punchInCount": 116,
    "punchOutCount": 0,
    "punchInRate": 96.67
  },
  "refreshTime": "2026-07-21T15:00:00"
}
```

### 11. 人事数据大屏

- **GET** `/api/visual/screen/hr`
- **Query**：`month=2026-07`
- **说明**：聚合人事概览、部门人数分布、入职/离职趋势、部门出勤率。

响应 `data` 结构：

```json
{
  "statMonth": "2026-07",
  "overview": {},
  "deptDistribution": [],
  "hireResignationTrend": [
    {
      "statMonth": "2026-07",
      "activeEmployees": 120,
      "newHires": 8,
      "resignations": 2
    }
  ],
  "deptAttendanceRate": [],
  "refreshTime": "2026-07-21T15:00:00"
}
```

### 12. 审批数据大屏

- **GET** `/api/visual/screen/approval`
- **Query**：`month=2026-07`
- **说明**：聚合审批统计、部门审批效率、审批趋势、申请类型分布、待审批积压量。

响应 `data` 结构：

```json
{
  "statMonth": "2026-07",
  "stats": {},
  "speedRanking": [],
  "approvalTrend": [
    {
      "statMonth": "2026-07",
      "totalApplications": 86,
      "approvedCount": 70,
      "rejectedCount": 6,
      "pendingCount": 10,
      "avgApprovalHours": 5.6
    }
  ],
  "applicationTypeDistribution": [
    {"name": "请假", "value": 50, "unit": "件"},
    {"name": "加班", "value": 24, "unit": "件"},
    {"name": "外出", "value": 12, "unit": "件"}
  ],
  "pendingBacklog": 10,
  "refreshTime": "2026-07-21T15:00:00"
}
```

---

## 六、数据来源

### 1. 统计库

主要读取 `statistics_db`：

| 表 | 用途 |
|----|------|
| `stat_attendance_monthly` | 月度考勤趋势、部门加班、部门迟到率、概览加班时长 |
| `stat_approval_summary` | 审批统计、审批效率、审批趋势 |
| `stat_dept_overview` | 部门人数分布、入职/离职趋势、出勤率 |

### 2. 实时业务库

部分大屏实时指标会跨库只读：

| 库表 | 用途 |
|------|------|
| `user_db.sys_dept` | 部门名称 |
| `user_db.sys_user` | 当前在职员工数 |
| `attendance_db.att_record` | 今日上班/下班打卡人数 |
| `approval_db.app_application` | 待审批积压量、申请类型分布 |

### 3. 缓存

热点统计接口使用 Redis 缓存 60 秒。Redis 不可用时接口会降级为直接查询数据库。

---

## 七、XXL-Job 定时同步

### 1. 执行器配置

`oa-data-visual-service` 默认不启用 XXL-Job 执行器；部署调度中心后设置：

```yaml
xxl:
  job:
    enabled: true
    admin:
      addresses: http://127.0.0.1:8080/xxl-job-admin
    access-token:
    executor:
      appname: oa-data-visual-service
      port: 9997
```

也可以使用环境变量：

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `XXL_JOB_ENABLED` | `false` | 是否启用执行器 |
| `XXL_JOB_ADMIN_ADDRESSES` | `http://127.0.0.1:8080/xxl-job-admin` | 调度中心地址 |
| `XXL_JOB_EXECUTOR_APPNAME` | `oa-data-visual-service` | 执行器名称 |
| `XXL_JOB_EXECUTOR_PORT` | `9997` | 执行器端口 |
| `XXL_JOB_ACCESS_TOKEN` | 空 | 调度中心 accessToken |
| `XXL_JOB_LOG_PATH` | `./logs/xxl-job/jobhandler` | 任务日志目录 |
| `XXL_JOB_LOG_RETENTION_DAYS` | `30` | 日志保留天数 |

### 2. JobHandler

| Handler | 默认行为 | 建议 Cron | 说明 |
|---------|----------|-----------|------|
| `visualStatisticsMonthlyJob` | 无参数时同步上月 | `0 10 1 1 * ?` | 每月 1 日 01:10 生成上月统计 |
| `visualStatisticsCurrentMonthJob` | 无参数时同步当前月 | `0 0/30 * * * ?` | 每 30 分钟刷新当月看板数据 |

两个 Handler 都支持任务参数 `yyyy-MM`，例如 `2026-07`，用于重跑指定月份。

### 3. 聚合口径

| 统计表 | 来源 | 口径 |
|--------|------|------|
| `stat_dept_overview` | `user_db.sys_user`、`attendance_db.att_daily_summary` | 在职人数、新入职、离职、部门出勤率 |
| `stat_attendance_monthly` | `attendance_db.att_daily_summary`、`user_db.sys_user` | 正常、迟到、早退、旷工、请假、加班时长 |
| `stat_approval_summary` | `approval_db.app_application`、`approval_db.app_approval_record` | 审批中、已通过、已驳回、平均审批耗时 |

---

## 八、联调建议

1. 先执行 `sql/oa_init.sql` 初始化数据库。
2. 启动 Nacos、MySQL、Redis。
3. 启动 `oa-data-visual-service`。
4. 通过 `GET /api/visual/health` 确认服务可用。
5. 调用 `POST /api/visual/statistics/sync?month=2026-07` 生成统计数据。
6. 再调用 Dashboard 或大屏接口查看聚合结果。
