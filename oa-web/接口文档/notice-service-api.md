# notice-service 公告通知服务接口文档

## 1. 基本信息

| 项目 | 内容 |
| --- | --- |
| 服务名 | `notice-service` |
| 服务端口 | `9104` |
| 网关访问前缀 | `http://localhost:9000/api/notice` |
| 服务直连前缀 | `http://localhost:9104/api/notice` |
| 数据库 | `notice_db` |
| 相关表 | `ntc_notice`、`ntc_read_status`、`ntc_message` |

建议前端统一通过 Gateway 访问，不直接访问 `9104` 业务服务端口。

## 2. 鉴权说明

除健康检查外，业务接口需要通过 Gateway 携带 JWT：

```http
Authorization: Bearer <token>
```

Gateway 校验通过后，会向下游服务写入用户上下文请求头：

| 请求头 | 说明 |
| --- | --- |
| `X-User-Id` | 当前用户 ID |
| `X-Dept-Id` | 当前用户部门 ID |
| `X-Username` | 当前用户名 |
| `X-Roles` | 当前用户角色，逗号分隔 |
| `X-Permissions` | 当前用户权限，逗号分隔 |

公告发布、编辑、下架、创建站内消息需要满足以下任一条件：

| 条件类型 | 值 |
| --- | --- |
| 角色 | `ROLE_ADMIN` |
| 角色 | `ROLE_HR` |
| 权限 | `notice:publish` |
| 权限 | `notice:manage` |

当前演示账号 `admin / 123456` 已包含公告维护权限。

## 3. 统一返回格式

所有接口统一返回：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1720000000000
}
```

常见状态码：

| code | message | 说明 |
| --- | --- | --- |
| 200 | 操作成功 | 请求成功 |
| 400 | 参数校验失败 | 请求参数错误 |
| 401 | 未登录或Token过期 | JWT 无效、过期或缺少用户上下文 |
| 403 | 无权限 | 当前用户无操作权限 |
| 404 | 资源不存在 | 数据不存在或不可访问 |
| 500 | 服务器内部错误 | 未处理系统异常 |

分页返回结构：

```json
{
  "records": [],
  "total": 0,
  "current": 1,
  "size": 10
}
```

分页参数：

| 参数 | 类型 | 默认值 | 说明 |
| --- | --- | --- | --- |
| `current` | number | `1` | 当前页，从 1 开始 |
| `size` | number | `10` | 每页条数，最大 200 |

## 4. 枚举说明

公告类型 `noticeType`：

| 值 | 说明 |
| --- | --- |
| `1` | 公司公告 |
| `2` | 部门通知 |
| `3` | 系统通知 |

公告发布范围 `targetType`：

| 值 | 说明 |
| --- | --- |
| `1` | 全公司 |
| `2` | 指定部门 |
| `3` | 指定用户 |

公告状态 `status`：

| 值 | 说明 |
| --- | --- |
| `0` | 草稿 |
| `1` | 已发布 |
| `2` | 已下架 |

消息类型 `msgType`：

| 值 | 说明 |
| --- | --- |
| `1` | 审批通知 |
| `2` | 考勤通知 |
| `3` | 系统通知 |

时间字段使用 ISO-8601 格式：

```text
2026-07-21T10:30:00
```

## 5. 接口总览

| 功能 | 方法 | 路径 | 权限 |
| --- | --- | --- | --- |
| 健康检查 | `GET` | `/api/notice/health` | 白名单或普通访问 |
| 公告发布 | `POST` | `/api/notice/list` | 管理员/HR/公告权限 |
| 公告分页 | `GET` | `/api/notice/list` | 登录用户 |
| 公告详情并标记已读 | `GET` | `/api/notice/list/{id}` | 登录用户 |
| 公告编辑 | `PUT` | `/api/notice/list/{id}` | 管理员/HR/公告权限 |
| 公告下架 | `DELETE` | `/api/notice/list/{id}` | 管理员/HR/公告权限 |
| 未读数量 | `GET` | `/api/notice/unread-count` | 登录用户 |
| 站内消息分页 | `GET` | `/api/notice/messages` | 登录用户 |
| 创建站内消息 | `POST` | `/api/notice/messages` | 管理员/HR/公告权限 |
| 标记消息已读 | `PUT` | `/api/notice/messages/{id}/read` | 登录用户 |

## 6. 接口详情

### 6.1 健康检查

```http
GET /api/notice/health
```

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "service": "notice-service",
    "status": "UP"
  },
  "timestamp": 1720000000000
}
```

### 6.2 公告发布

```http
POST /api/notice/list
Content-Type: application/json
Authorization: Bearer <token>
```

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `title` | string | 是 | 公告标题，最大 200 字符 |
| `content` | string | 否 | 公告内容，支持富文本 HTML |
| `noticeType` | number | 否 | 公告类型，默认 `1` |
| `targetType` | number | 否 | 发布范围，默认 `1` |
| `targetIds` | number[] | 否 | 指定部门或指定用户 ID 列表；`targetType` 为 `2` 或 `3` 时必填 |
| `startTime` | string | 否 | 生效时间 |
| `endTime` | string | 否 | 失效时间 |
| `status` | number | 否 | `0` 草稿，`1` 已发布，默认 `1` |

请求示例：

```json
{
  "title": "五一假期安排通知",
  "content": "<p>五一假期为 5 月 1 日至 5 月 5 日。</p>",
  "noticeType": 1,
  "targetType": 1,
  "targetIds": [],
  "startTime": "2026-07-21T10:30:00",
  "endTime": "2026-08-01T18:00:00",
  "status": 1
}
```

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 2,
  "timestamp": 1720000000000
}
```

### 6.3 公告分页

```http
GET /api/notice/list?current=1&size=10&keyword=假期&noticeType=1&unreadOnly=false
Authorization: Bearer <token>
```

查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `current` | number | 否 | 当前页 |
| `size` | number | 否 | 每页条数 |
| `keyword` | string | 否 | 标题或内容关键字 |
| `noticeType` | number | 否 | 公告类型 |
| `unreadOnly` | boolean | 否 | 是否只查未读公告 |

可见范围规则：

| 公告范围 | 可见条件 |
| --- | --- |
| 全公司 | 所有登录用户可见 |
| 指定部门 | 当前用户 `X-Dept-Id` 在 `targetIds` 中 |
| 指定用户 | 当前用户 `X-User-Id` 在 `targetIds` 中 |

只返回 `status = 1`，且在有效时间范围内的公告。

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "title": "欢迎使用OA办公管理系统",
        "content": null,
        "summary": "系统已正式上线运行，请大家及时完善个人信息，并熟悉各项功能模块。",
        "publisherId": 1,
        "noticeType": 1,
        "targetType": 1,
        "targetIds": [],
        "startTime": "2026-07-21T10:30:00",
        "endTime": null,
        "status": 1,
        "read": false,
        "createTime": "2026-07-21T10:30:00",
        "updateTime": "2026-07-21T10:30:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10
  },
  "timestamp": 1720000000000
}
```

### 6.4 公告详情并标记已读

```http
GET /api/notice/list/{id}
Authorization: Bearer <token>
```

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | number | 是 | 公告 ID |

说明：

访问详情成功后，服务会向 `ntc_read_status` 写入或更新当前用户的已读记录。

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "title": "欢迎使用OA办公管理系统",
    "content": "<p>系统已正式上线运行，请大家及时完善个人信息，并熟悉各项功能模块。</p>",
    "summary": "系统已正式上线运行，请大家及时完善个人信息，并熟悉各项功能模块。",
    "publisherId": 1,
    "noticeType": 1,
    "targetType": 1,
    "targetIds": [],
    "startTime": "2026-07-21T10:30:00",
    "endTime": null,
    "status": 1,
    "read": true,
    "createTime": "2026-07-21T10:30:00",
    "updateTime": "2026-07-21T10:30:00"
  },
  "timestamp": 1720000000000
}
```

### 6.5 公告编辑

```http
PUT /api/notice/list/{id}
Content-Type: application/json
Authorization: Bearer <token>
```

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | number | 是 | 公告 ID |

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `title` | string | 否 | 公告标题，最大 200 字符 |
| `content` | string | 否 | 公告内容 |
| `noticeType` | number | 否 | 公告类型 |
| `targetType` | number | 否 | 发布范围 |
| `targetIds` | number[] | 否 | 目标 ID 列表 |
| `startTime` | string | 否 | 生效时间 |
| `endTime` | string | 否 | 失效时间 |
| `status` | number | 否 | 公告状态，允许 `0`、`1`、`2` |

请求示例：

```json
{
  "title": "五一假期安排通知（更新）",
  "content": "<p>假期安排已更新，请以本公告为准。</p>",
  "status": 1
}
```

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1720000000000
}
```

### 6.6 公告下架

```http
DELETE /api/notice/list/{id}
Authorization: Bearer <token>
```

说明：

该接口不是物理删除，而是将公告状态更新为 `2` 已下架。下架后的公告不会在普通公告列表中展示。

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1720000000000
}
```

### 6.7 未读数量

```http
GET /api/notice/unread-count
Authorization: Bearer <token>
```

响应字段：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `noticeUnread` | number | 当前用户可见公告中未读数量 |
| `messageUnread` | number | 当前用户站内消息未读数量 |
| `totalUnread` | number | 未读总数 |

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "noticeUnread": 3,
    "messageUnread": 2,
    "totalUnread": 5
  },
  "timestamp": 1720000000000
}
```

### 6.8 站内消息分页

```http
GET /api/notice/messages?current=1&size=10&msgType=1&read=false
Authorization: Bearer <token>
```

查询参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `current` | number | 否 | 当前页 |
| `size` | number | 否 | 每页条数 |
| `msgType` | number | 否 | 消息类型 |
| `read` | boolean | 否 | 是否已读 |

说明：

仅返回当前登录用户自己的站内消息。

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "records": [
      {
        "id": 1,
        "userId": 2,
        "title": "审批结果通知",
        "content": "你的请假申请已通过。",
        "msgType": 1,
        "relatedId": 1001,
        "read": false,
        "createTime": "2026-07-21T10:30:00"
      }
    ],
    "total": 1,
    "current": 1,
    "size": 10
  },
  "timestamp": 1720000000000
}
```

### 6.9 创建站内消息

```http
POST /api/notice/messages
Content-Type: application/json
Authorization: Bearer <token>
```

请求参数：

| 字段 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `userId` | number | 是 | 接收人用户 ID |
| `title` | string | 是 | 消息标题，最大 200 字符 |
| `content` | string | 否 | 消息内容，最大 500 字符 |
| `msgType` | number | 是 | 消息类型 |
| `relatedId` | number | 否 | 关联业务 ID |

请求示例：

```json
{
  "userId": 2,
  "title": "审批结果通知",
  "content": "你的请假申请已通过。",
  "msgType": 1,
  "relatedId": 1001
}
```

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": 1,
  "timestamp": 1720000000000
}
```

### 6.10 标记消息已读

```http
PUT /api/notice/messages/{id}/read
Authorization: Bearer <token>
```

路径参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| `id` | number | 是 | 消息 ID |

说明：

只能标记当前登录用户自己的消息。

响应示例：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": null,
  "timestamp": 1720000000000
}
```

## 7. 联调流程示例

### 7.1 登录获取 Token

```http
POST /api/user/login
Content-Type: application/json
```

```json
{
  "username": "admin",
  "password": "123456"
}
```

从响应 `data.token` 中取出 JWT。

### 7.2 发布公告

```http
POST /api/notice/list
Authorization: Bearer <token>
Content-Type: application/json
```

```json
{
  "title": "系统维护通知",
  "content": "<p>今晚 22:00 至 23:00 系统维护。</p>",
  "noticeType": 3,
  "targetType": 1,
  "status": 1
}
```

### 7.3 普通用户查询公告

```http
GET /api/notice/list?current=1&size=10
Authorization: Bearer <token>
```

### 7.4 查看详情并自动已读

```http
GET /api/notice/list/1
Authorization: Bearer <token>
```

### 7.5 查询未读数量

```http
GET /api/notice/unread-count
Authorization: Bearer <token>
```

## 8. 业务规则补充

- 公告列表不会返回正文 `content`，只返回 `summary`，详情接口返回完整 `content`。
- 查看公告详情后，会幂等写入已读记录，重复查看不会重复插入。
- 指定部门或指定用户公告要求 `targetIds` 不能为空。
- 公告 `endTime` 不能早于 `startTime`。
- 公告下架后不再进入普通用户公告列表。
- 站内消息列表只查询当前用户自己的消息。
- 创建站内消息接口当前供后台管理或后续审批、考勤服务联动使用。

