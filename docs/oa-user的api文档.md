# oa-user 前端接口文档

**Base URL**: `http://{host}:8081`

**鉴权方式**: 除 `/api/user/login` 外，所有接口需在 Header 中携带 `Authorization: Bearer {token}`

**通用响应格式**:

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "timestamp": 1700000000000
}
```

---

## 1. 认证模块

### 登录

```
POST /api/user/login
```

| 请求体 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 工号/账号 |
| password | String | 是 | 密码 |

**响应 data** (LoginVO):

```json
{
  "accessToken": "eyJhbGci...",
  "userId": 1,
  "username": "admin",
  "realName": "管理员",
  "avatarUrl": null,
  "roles": ["admin"],
  "permissions": ["sys:user:list", "sys:user:add"]
}
```

### 退出登录

```
POST /api/user/logout
Header: Authorization: Bearer {token}
```

JWT 加入 Redis 黑名单，有效期内不可再用。

### 获取当前用户

```
GET /api/user/current
Header: Authorization: Bearer {token}
```

**响应 data** (CurrentUserVO):

```json
{
  "userId": 1,
  "username": "admin",
  "realName": "管理员",
  "deptId": 1,
  "avatarUrl": null,
  "roles": ["admin"],
  "permissions": ["sys:user:list", "sys:user:add"]
}
```

---

## 2. 员工管理

### 员工列表（分页）

```
GET /api/user/employees?pageNum=1&pageSize=10&deptId=1&realName=张三&status=1
```

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| pageNum | int | 否 | 1 | 页码 |
| pageSize | int | 否 | 10 | 每页条数 |
| deptId | Long | 否 | - | 部门筛选 |
| positionId | Long | 否 | - | 岗位筛选 |
| realName | String | 否 | - | 姓名模糊搜索 |
| status | Integer | 否 | - | 1=在职, 0=禁用 |

**响应 data**:

```json
{
  "records": [
    {
      "id": 1,
      "username": "admin",
      "realName": "管理员",
      "phone": "13800000000",
      "email": "admin@oa.com",
      "gender": 1,
      "deptId": 1,
      "positionId": 1,
      "avatarUrl": null,
      "entryDate": "2024-01-01",
      "status": 1,
      "lastLoginTime": "2026-07-22T09:00:00",
      "createTime": "2024-01-01T00:00:00"
    }
  ],
  "total": 1,
  "size": 10,
  "current": 1
}
```

### 员工详情

```
GET /api/user/employees/{id}
```

### 新增员工

```
POST /api/user/employees
```

| 请求体 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| username | String | 是 | 工号 |
| password | String | 否 | 密码，默认 "123456" |
| realName | String | 是 | 姓名 |
| phone | String | 否 | 手机号 |
| email | String | 否 | 邮箱 |
| gender | Integer | 否 | 1=男, 2=女 |
| deptId | Long | 是 | 部门ID |
| positionId | Long | 是 | 岗位ID |
| entryDate | String | 否 | 入职日期，格式 `yyyy-MM-dd` |
| status | Integer | 否 | 1=在职, 0=禁用，默认1 |

### 编辑员工

```
PUT /api/user/employees/{id}
```

请求体同新增（不含 password，不可通过此接口修改密码）。

### 删除员工（逻辑删除）

```
DELETE /api/user/employees/{id}
```

### 重置密码（管理员操作）

```
PUT /api/user/employees/{id}/reset-pwd
```

重置为默认密码 "123456"。

### 修改密码（本人操作）

```
PUT /api/user/employees/{id}/update-pwd
```

| 请求体 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| oldPassword | String | 是 | 旧密码 |
| newPassword | String | 是 | 新密码 |

---

## 3. 部门管理

### 部门树

```
GET /api/user/depts
```

**响应 data** (DeptTreeVO[]):

```json
[
  {
    "id": 1,
    "parentId": 0,
    "deptName": "总公司",
    "deptCode": "HQ",
    "leaderId": 1,
    "leaderName": "张三",
    "sortOrder": 1,
    "status": 1,
    "children": [
      {
        "id": 2,
        "parentId": 1,
        "deptName": "技术部",
        "deptCode": "TECH",
        "leaderId": 2,
        "leaderName": null,
        "sortOrder": 1,
        "status": 1,
        "children": []
      }
    ]
  }
]
```

### 新增部门

```
POST /api/user/depts
```

| 请求体 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| deptName | String | 是 | 部门名称 |
| deptCode | String | 是 | 部门编码（唯一） |
| parentId | Long | 否 | 上级部门ID |
| leaderId | Long | 否 | 部门负责人ID |
| sortOrder | Integer | 否 | 排序号 |
| status | Integer | 否 | 1=启用, 0=禁用 |

### 编辑部门

```
PUT /api/user/depts/{id}
```

请求体同新增。

### 删除部门

```
DELETE /api/user/depts/{id}
```

有子部门或关联员工时拒绝删除（code: 10010 或 10011）。

---

## 4. 岗位管理

### 岗位列表

```
GET /api/user/positions?pageNum=1&pageSize=10&deptId=1
```

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| pageNum | int | 否 | 1 | |
| pageSize | int | 否 | 10 | |
| deptId | Long | 否 | - | 部门筛选 |

**响应 data**:

```json
{
  "records": [
    {
      "id": 1,
      "positionName": "Java开发工程师",
      "positionCode": "JAVA_DEV",
      "deptId": 2,
      "sortOrder": 1,
      "status": 1,
      "createTime": "2024-01-01T00:00:00"
    }
  ],
  "total": 1,
  "size": 10,
  "current": 1
}
```

### 新增岗位

```
POST /api/user/positions
```

| 请求体 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| positionName | String | 是 | 岗位名称 |
| positionCode | String | 是 | 岗位编码（唯一） |
| deptId | Long | 是 | 所属部门ID |
| sortOrder | Integer | 否 | 排序号 |
| status | Integer | 否 | 1=启用, 0=禁用 |

### 编辑岗位

```
PUT /api/user/positions/{id}
```

请求体同新增。

### 删除岗位

```
DELETE /api/user/positions/{id}
```

---

## 5. 角色管理

### 角色列表

```
GET /api/user/roles?pageNum=1&pageSize=10
```

### 新增角色

```
POST /api/user/roles
```

| 请求体 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| roleName | String | 是 | 角色名称 |
| roleCode | String | 是 | 角色编码（唯一） |
| roleDesc | String | 否 | 角色描述 |
| dataScope | Integer | 否 | 数据范围 |
| sortOrder | Integer | 否 | 排序号 |
| status | Integer | 否 | 1=启用, 0=禁用 |

### 编辑角色

```
PUT /api/user/roles/{id}
```

### 删除角色

```
DELETE /api/user/roles/{id}
```

### 角色菜单分配

```
PUT  /api/user/roles/{id}/menus       Body: [1, 2, 3]   // 分配菜单ID列表
GET  /api/user/roles/{id}/menus                           // 获取角色的菜单ID列表
```

---

## 6. 菜单管理

### 菜单树

```
GET /api/user/menus
```

### 新增菜单

```
POST /api/user/menus
```

### 编辑菜单

```
PUT /api/user/menus/{id}
```

### 删除菜单

```
DELETE /api/user/menus/{id}
```

> `/api/user/menus/routers` 目前为占位接口，返回空数据。

---

## 错误码

| code | 说明 |
|------|------|
| 200 | 成功 |
| 401 | 未登录或 token 过期/已登出 |
| 500 | 服务器内部错误 |
| 10001 | 用户不存在 |
| 10002 | 密码错误 |
| 10003 | 账号已被禁用 |
| 10004 | 用户已存在 |
| 10010 | 部门有子部门，无法删除 |
| 10011 | 部门有员工，无法删除 |
| 10012 | 旧密码错误 |
