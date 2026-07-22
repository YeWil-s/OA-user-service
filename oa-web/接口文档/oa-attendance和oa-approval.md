# OA 考勤与审批前端接口文档

**Base URL**: `http://{gateway-host}:8080`

所有接口均通过网关访问，前端不要直接访问考勤服务或审批服务的端口。

**鉴权方式**: 先调用用户服务的登录接口 `POST /api/user/login`，从响应 `data.accessToken` 取得 Token。除登录接口外，调用本文件中的全部接口时，在 Header 中携带：

```http
Authorization: Bearer {accessToken}
```

发送 JSON 请求体时，额外携带标准请求头：

```http
Content-Type: application/json
```

前端不需要、也不应自行传递用户 ID、部门或角色。网关负责校验并解析 Token，再将当前用户身份在内部传递给对应微服务。

**通用响应格式**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1784682056709
}
```

---

## 1. 登录前置接口

登录接口由 `oa-user` 服务提供，完整字段请查看《oa-user 前端接口文档》。登录成功后保存 `accessToken`，后续请求由前端请求拦截器统一添加 `Authorization` 请求头。

### 登录

```
POST /api/user/login
```

| 请求体 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 账号/工号 |
| password | String | 是 | 密码 |

**响应 data**:

```json
{
  "accessToken": "eyJhbGci...",
  "userId": 2,
  "username": "zhangsan",
  "realName": "张三",
  "avatarUrl": null,
  "roles": ["ROLE_EMPLOYEE"],
  "permissions": []
}
```

---

## 2. 考勤模块

接口前缀：`/api/attendance`

### 班次列表（分页）

```
GET /api/attendance/shifts?pageNum=1&pageSize=20
```

权限：`ROLE_ADMIN` 或 `ROLE_HR`

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| pageNum | Integer | 否 | 1 | 页码，从 1 开始 |
| pageSize | Integer | 否 | 20 | 每页条数 |

**响应 data**:

```json
{
  "records": [
    {
      "id": 1,
      "shiftName": "标准白班",
      "startTime": "09:00:00",
      "endTime": "18:00:00",
      "flexStart": "08:30:00",
      "flexEnd": "09:30:00",
      "status": 1,
      "createTime": "2026-07-22T09:00:00",
      "updateTime": "2026-07-22T09:00:00"
    }
  ],
  "total": 1,
  "size": 20,
  "current": 1
}
```

### 班次详情

```
GET /api/attendance/shifts/{id}
```

权限：`ROLE_ADMIN` 或 `ROLE_HR`

路径参数 `id` 为班次 ID。响应 `data` 为上方列表中的单个班次对象。

### 新增班次

```
POST /api/attendance/shifts
```

权限：`ROLE_ADMIN` 或 `ROLE_HR`

| 请求体 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| shiftName | String | 是 | 班次名称 |
| startTime | String | 是 | 上班时间，格式 `HH:mm:ss` |
| endTime | String | 是 | 下班时间，格式 `HH:mm:ss`，必须晚于 `startTime` |
| flexStart | String | 否 | 弹性上班开始时间，格式 `HH:mm:ss` |
| flexEnd | String | 否 | 最晚正常打卡时间，格式 `HH:mm:ss`；迟到以此时间判断 |
| status | Integer | 否 | `1=启用`，`0=停用`，默认 `1` |

```json
{
  "shiftName": "标准白班",
  "startTime": "09:00:00",
  "endTime": "18:00:00",
  "flexStart": "08:30:00",
  "flexEnd": "09:30:00",
  "status": 1
}
```

响应 `data` 为新建后的班次对象。

### 编辑班次

```
PUT /api/attendance/shifts/{id}
```

权限：`ROLE_ADMIN` 或 `ROLE_HR`

路径参数 `id` 为班次 ID；请求体字段与“新增班次”相同。响应 `data` 为更新后的班次对象。

### 删除班次

```
DELETE /api/attendance/shifts/{id}
```

权限：`ROLE_ADMIN` 或 `ROLE_HR`

已分配给员工的班次不能删除，接口返回 `409`。

### 给员工分配班次

```
POST /api/attendance/user-shifts
```

权限：`ROLE_ADMIN` 或 `ROLE_HR`

| 请求体 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| userId | Long | 是 | 员工 ID |
| shiftId | Long | 是 | 班次 ID |

```json
{
  "userId": 2,
  "shiftId": 1
}
```

同一员工重复分配时会更新为新的班次。员工必须已分配启用状态的班次，才能完成打卡。

### 上班打卡

```
POST /api/attendance/punch/in
```

当前登录用户操作。请求体可不传；如需记录设备和地点，可传：

| 请求体 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| punchType | Integer | 否 | 打卡类型，默认 `1` |
| deviceInfo | String | 否 | 设备信息，例如浏览器名称 |
| location | String | 否 | 打卡地点 |

```json
{
  "punchType": 1,
  "deviceInfo": "Chrome",
  "location": "公司 A 座"
}
```

**响应 data**:

```json
{
  "message": "上班打卡成功",
  "userId": 2,
  "recordDate": "2026-07-22",
  "punchTime": "2026-07-22T09:05:00",
  "shiftId": 1,
  "shiftName": "标准白班",
  "statusLabel": "正常",
  "lateMinutes": 0,
  "earlyMinutes": 0
}
```

同一天重复上班打卡会返回 `20001`；短时间重复点击会返回 `409`。

### 下班打卡

```
POST /api/attendance/punch/out
```

当前登录用户操作。请求体与“上班打卡”相同，也可不传。

**响应 data**:

```json
{
  "message": "下班打卡成功",
  "userId": 2,
  "recordDate": "2026-07-22",
  "punchTime": "2026-07-22T18:05:00",
  "shiftId": 1,
  "shiftName": "标准白班",
  "statusLabel": "正常",
  "lateMinutes": 0,
  "earlyMinutes": 0
}
```

必须先完成当天上班打卡；当天重复下班打卡会返回 `409`。

### 个人考勤记录（分页）

```
GET /api/attendance/records/mine?month=2026-07&pageNum=1&pageSize=20
```

当前登录用户操作，只返回自己的记录。

### 部门考勤记录（分页）

```
GET /api/attendance/records/dept?month=2026-07&pageNum=1&pageSize=20
```

权限：`ROLE_ADMIN`、`ROLE_HR` 或 `ROLE_LEADER`。返回当前用户所在部门及其子部门员工的记录。

### 全公司考勤记录（分页）

```
GET /api/attendance/records/all?month=2026-07&pageNum=1&pageSize=20
```

权限：`ROLE_ADMIN` 或 `ROLE_HR`

上述三个考勤记录接口使用相同查询参数和响应结构：

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| date | String | 否 | - | 指定日期，格式 `yyyy-MM-dd`；传入后优先于 `month` |
| month | String | 否 | 当前月 | 指定月份，格式 `yyyy-MM` |
| pageNum | Integer | 否 | 1 | 页码，从 1 开始 |
| pageSize | Integer | 否 | 20 | 每页条数，最大 `100` |

**响应 data**:

```json
{
  "records": [
    {
      "id": 1,
      "userId": 2,
      "realName": "张三",
      "deptId": 2,
      "deptName": "技术部",
      "shiftId": 1,
      "shiftName": "标准白班",
      "recordDate": "2026-07-22",
      "punchInTime": "2026-07-22T09:05:00",
      "punchOutTime": "2026-07-22T18:05:00",
      "lateMinutes": 0,
      "earlyMinutes": 0,
      "workHours": 9.0,
      "punchType": 1,
      "deviceInfo": "Chrome",
      "location": "公司 A 座",
      "statusLabel": "正常"
    }
  ],
  "total": 1,
  "size": 20,
  "current": 1
}
```

`statusLabel` 可能为：`正常`、`迟到`、`早退`、`迟到/早退`、`缺卡`、`班次未分配`。

---

## 3. 审批模块

接口前缀：`/api/approval`

### 提交申请

```
POST /api/approval/applications
```

当前登录用户操作。服务会使用 Token 对应的用户和部门信息创建申请，并自动把部门负责人设为当前审批人。

| 请求体 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| appType | Integer | 是 | `1=请假`，`2=加班`，`3=外出` |
| leaveType | Integer | 请假时是 | 请假类型，见“枚举说明” |
| startTime | String | 是 | 开始时间，格式 `yyyy-MM-ddTHH:mm:ss` |
| endTime | String | 是 | 结束时间，格式 `yyyy-MM-ddTHH:mm:ss`，必须晚于开始时间 |
| reason | String | 是 | 申请原因，不能为空字符串 |
| attachments | String[] | 否 | 附件 URL 列表 |

请假示例：

```json
{
  "appType": 1,
  "leaveType": 2,
  "startTime": "2026-07-22T09:00:00",
  "endTime": "2026-07-22T18:00:00",
  "reason": "家中有事，请假一天",
  "attachments": []
}
```

加班示例：

```json
{
  "appType": 2,
  "startTime": "2026-07-22T19:00:00",
  "endTime": "2026-07-22T21:00:00",
  "reason": "项目上线支持",
  "attachments": []
}
```

**响应 data**:

```json
{
  "id": 1,
  "applicationNo": "LV202607220001",
  "userId": 2,
  "applicantName": "张三",
  "deptId": 2,
  "deptName": "技术部",
  "appType": 1,
  "appTypeText": "请假",
  "leaveType": 2,
  "leaveTypeText": "事假",
  "startTime": "2026-07-22T09:00:00",
  "endTime": "2026-07-22T18:00:00",
  "duration": 9.0,
  "reason": "家中有事，请假一天",
  "status": 1,
  "statusText": "审批中",
  "currentApproverId": 3,
  "currentApproverName": "李四",
  "latestAction": null,
  "latestActionText": null,
  "latestActionTime": null,
  "createTime": "2026-07-22T08:50:00",
  "attachments": [],
  "timeline": []
}
```

申请单号自动生成：请假以 `LV` 开头、加班以 `OT` 开头、外出以 `OUT` 开头。部门未配置负责人时，提交会失败。

### 我的申请列表（分页）

```
GET /api/approval/applications?status=1&pageNum=1&pageSize=20
```

当前登录用户操作，只返回自己提交的申请。

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| status | Integer | 否 | - | 申请状态，见“枚举说明” |
| pageNum | Integer | 否 | 1 | 页码，从 1 开始 |
| pageSize | Integer | 否 | 20 | 每页条数，最大 `100` |

**响应 data**:

```json
{
  "records": [
    {
      "id": 1,
      "applicationNo": "LV202607220001",
      "userId": 2,
      "applicantName": "张三",
      "deptId": 2,
      "deptName": "技术部",
      "appType": 1,
      "appTypeText": "请假",
      "leaveType": 2,
      "leaveTypeText": "事假",
      "startTime": "2026-07-22T09:00:00",
      "endTime": "2026-07-22T18:00:00",
      "duration": 9.0,
      "reason": "家中有事，请假一天",
      "status": 1,
      "statusText": "审批中",
      "currentApproverId": 3,
      "currentApproverName": "李四",
      "latestAction": null,
      "latestActionText": null,
      "latestActionTime": null,
      "createTime": "2026-07-22T08:50:00"
    }
  ],
  "total": 1,
  "size": 20,
  "current": 1
}
```

### 申请详情

```
GET /api/approval/applications/{id}
```

可访问者：申请人本人、当前审批人、已处理过该申请的审批人、管理员、HR。

路径参数 `id` 为申请 ID。响应 `data` 的基础字段与“我的申请列表”中的单条记录相同，额外包含 `attachments` 和 `timeline`：

```json
{
  "attachments": ["https://example.com/files/proof.png"],
  "timeline": [
    {
      "id": 1,
      "approverId": 3,
      "approverName": "李四",
      "action": 1,
      "actionText": "同意",
      "comment": "同意",
      "actionTime": "2026-07-22T10:00:00"
    }
  ]
}
```

### 撤销申请

```
PUT /api/approval/applications/{id}/cancel
```

可操作者：申请人本人、`ROLE_ADMIN`、`ROLE_HR`。

路径参数 `id` 为申请 ID，无请求体。仅 `status=1`（审批中）的申请可撤销；成功后状态变为 `4`（已撤销）。

### 待审批列表（分页）

```
GET /api/approval/pending?pageNum=1&pageSize=20
```

当前登录用户操作，只返回当前用户是审批人的、状态为“审批中”的申请。

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| pageNum | Integer | 否 | 1 | 页码，从 1 开始 |
| pageSize | Integer | 否 | 20 | 每页条数，最大 `100` |

响应 `data` 为分页对象，单条记录字段与“我的申请列表”相同。

### 审批操作

```
PUT /api/approval/pending/{id}/approve
```

仅当前审批人可操作。路径参数 `id` 为申请 ID。

| 请求体 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| approved | Boolean | 是 | `true=同意`，`false=驳回` |
| comment | String | 是 | 审批意见，不能为空字符串 |

同意示例：

```json
{
  "approved": true,
  "comment": "同意"
}
```

驳回示例：

```json
{
  "approved": false,
  "comment": "资料不完整，驳回"
}
```

同意后状态变为 `2`（已通过）；驳回后状态变为 `3`（已驳回）。成功时 `data` 为 `null`。

### 已办列表（分页）

```
GET /api/approval/processed?pageNum=1&pageSize=20
```

当前登录用户操作，返回该用户已经审批过的申请。

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| pageNum | Integer | 否 | 1 | 页码，从 1 开始 |
| pageSize | Integer | 否 | 20 | 每页条数，最大 `100` |

响应 `data` 为分页对象，单条记录字段与“我的申请列表”相同，并可能包含 `latestAction`、`latestActionText`、`latestActionTime`。

---

## 4. 枚举说明

### 申请类型 `appType`

| 值 | 说明 |
|----|------|
| 1 | 请假 |
| 2 | 加班 |
| 3 | 外出 |

### 请假类型 `leaveType`

| 值 | 说明 |
|----|------|
| 1 | 年假 |
| 2 | 事假 |
| 3 | 病假 |
| 4 | 婚假 |
| 5 | 产假 |

### 申请状态 `status`

| 值 | 说明 |
|----|------|
| 0 | 草稿 |
| 1 | 审批中 |
| 2 | 已通过 |
| 3 | 已驳回 |
| 4 | 已撤销 |

### 审批动作 `action`

| 值 | 说明 |
|----|------|
| 1 | 同意 |
| 2 | 驳回 |

---

## 5. 错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数校验失败或业务参数不合法 |
| 401 | 未登录、Token 无效或 Token 已过期 |
| 403 | 当前用户无接口或数据权限 |
| 404 | 请求的资源不存在 |
| 409 | 业务冲突，例如重复打卡、班次已被分配 |
| 500 | 服务器内部错误 |
| 20001 | 今日已完成上班打卡，请勿重复操作 |
| 20003 | 员工未分配可用班次 |
| 30001 | 申请单不存在 |
| 30002 | 当前申请状态不可撤销 |
| 30003 | 该申请已被审批，不能再次审批 |

---

## 6. 前后端联调说明

1. 前端开发环境中配置的 API 地址应是网关地址，例如 `http://localhost:8080`；所有路径仍使用本文档中的 `/api/...`。
2. 登录成功后保存 `data.accessToken`。除登录外，由请求拦截器统一添加 `Authorization: Bearer {accessToken}`。
3. 网关必须配置路由：`/api/user/**`、`/api/attendance/**`、`/api/approval/**` 分别转发至对应服务，并负责 Token 校验和用户身份传递。
4. 审批提交依赖用户服务的部门负责人配置。申请人所在部门没有负责人时，接口会返回“未找到可用审批人”。
5. 打卡前先由管理员或 HR 为员工分配一个启用的班次，否则打卡会失败。
