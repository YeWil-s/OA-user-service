# user-service API 测试用例

> 导入 Apifox：`http://localhost:8081/v3/api-docs`

---

## 一、认证模块 AuthController

### 1. 用户登录
- **POST** `/api/user/login`
- **Body**:
```json
{"username":"admin","password":"123456"}
```
- **预期**: code=200，data.accessToken 非空，roles 含 "ROLE_ADMIN"

- ![image-20260721095957728](C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721095957728.png)

- **异常**:
  
  - 密码错 → code=10002 "密码错误"
  
  - <img src="C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721094856945.png" alt="image-20260721094856945" />
  
  - 用户不存在 → code=10001 "用户不存在"
  
    ![image-20260721094931220](C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721094931220.png)

### 2. 用户登出
- **POST** `/api/user/logout`
- **Body**: 无
- **预期**: code=200
- ![image-20260721100040801](C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721100040801.png)

### 3. 获取当前用户信息
- **GET** `/api/user/current`
- **Header**: `Authorization: Bearer {token}`
- **预期**: code=200（当前仅返回空占位，后续从 JWT 解析）
- ![image-20260721100110427](C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721100110427.png)

---

## 二、员工管理 EmployeeController

### 4. 新增员工
- **POST** `/api/user/employees`
- **Body**:
```json
{
  "username": "witon",
  "password": "123456",
  "realName": "witon",
  "phone": "13800000005",
  "email": "witon@oa.com",
  "gender": 0,
  "deptId": 1,
  "positionId": 1,
  "entryDate": "2025-01-01",
  "status": 1
}
```
- **预期**: code=200
- ![image-20260721100300942](C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721100300942.png)
- **异常**: username 重复 → code=10004 "用户已存在"
- ![image-20260721100407345](C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721100407345.png)

### 5. 员工列表（分页+多条件筛选）
- **GET** `/api/user/employees`
- **Params**:
  - pageNum=1, pageSize=10
  - deptId=1, status=1 (可选筛选)
  - realName=张 (模糊搜索)
- **预期**: code=200，data.records 数组含 "张三"
- ![image-20260721100427494](C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721100427494.png)

![image-20260721100554689](C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721100554689.png)

### 6. 员工详情

- **GET** `/api/user/employees/{id}`
- **Path**: id=2
- **预期**: code=200，data.realName="张三"，password 字段为 null（已脱敏）
- ![image-20260721100647899](C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721100647899.png)

### 7. 编辑员工
- **PUT** `/api/user/employees/{id}`
- **Path**: id=2
- **Body**:
```json
{
  "username": "zhangsan",
  "realName": "张三三",
  "phone": "13900000001",
  "email": "zhangsan_new@oa.com",
  "gender": 1,
  "deptId": 2,
  "positionId": 2,
  "entryDate": "2025-01-01",
  "status": 1
}
```
- **预期**: code=200，重新查详情确认 realName 已变为 "张三三"
- ![image-20260721100718343](C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721100718343.png)

### 8. 删除员工（逻辑删除）
- **DELETE** `/api/user/employees/{id}`
- **Path**: id=2
- **预期**: code=200，再查详情 status=0
- ![image-20260721100746331](C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721100746331.png)
- ![image-20260721100848285](C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721100848285.png)

### 9. 管理员重置密码
- **PUT** `/api/user/employees/{id}/reset-pwd`
- **Path**: id=2
- **预期**: code=200，密码恢复为默认 "123456"，用新密码可成功登录

### 10. 修改自己的密码
- **PUT** `/api/user/employees/{id}/update-pwd`
- **Path**: id=1
- **Body**:
```json
{"oldPassword":"123456","newPassword":"654321"}
```
- **预期**: code=200，用新密码 654321 可登录
- ![image-20260721100959823](C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721100959823.png)
- **异常**: 旧密码错 → code=10010 "旧密码错误"
- ![image-20260721101008488](C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721101008488.png)

---

## 三、部门管理 DeptController

### 11. 新增一级部门
- **POST** `/api/user/depts`
- **Body**:
```json
{
  "deptName": "技术部",
  "deptCode": "DEPT_TECH",
  "parentId": 0,
  "sortOrder": 1,
  "status": 1
}
```
- **预期**: code=200
- ![image-20260721101200476](C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721101200476.png)

### 12. 新增子部门
- **POST** `/api/user/depts`
- **Body**:
```json
{
  "deptName": "后端组",
  "deptCode": "DEPT_TECH_BACKEND",
  "parentId": {技术部的id},
  "sortOrder": 1,
  "status": 1
}
```
- **预期**: code=200

### 13. 部门树
- **GET** `/api/user/depts`
- **预期**: code=200，data 为数组，技术部 children 包含后端组

### 14. 编辑部门
- **PUT** `/api/user/depts/{id}`
- **Path**: id={技术部id}
- **Body**:
```json
{
  "deptName": "技术中心",
  "deptCode": "DEPT_TECH",
  "parentId": 0,
  "sortOrder": 1,
  "status": 1
}
```
- **预期**: code=200，部门树中名称变为 "技术中心"

### 15. 删除部门
- **DELETE** `/api/user/depts/{id}`
- **Path**: id={后端组id}（必须先删子部门）
- **预期**: code=200
- **异常**: 部门下有员工 → code=10005；有子部门 → code=10006

---

## 四、岗位管理 PositionController

### 16. 新增岗位
- **POST** `/api/user/positions`
- **Body**:
```json
{"positionName":"Java工程师","positionCode":"POS_JAVA","deptId":1,"sortOrder":1,"status":1}
```
- **预期**: code=200
- **异常**: positionCode 重复 → code=10009

### 17. 岗位列表
- **GET** `/api/user/positions?pageNum=1&pageSize=10&deptId=1`
- **预期**: code=200，data.records 含 Java工程师

### 18. 编辑岗位
- **PUT** `/api/user/positions/{id}`
- **Body**:
```json
{"positionName":"高级Java工程师","positionCode":"POS_JAVA","deptId":1,"sortOrder":1,"status":1}
```
- **预期**: code=200

### 19. 删除岗位
- **DELETE** `/api/user/positions/{id}`
- **预期**: code=200

---

## 五、角色管理 RoleController

### 20. 新增角色
- **POST** `/api/user/roles`
- **Body**:
```json
{"roleName":"测试角色","roleCode":"ROLE_TEST","roleDesc":"测试用","dataScope":3,"sortOrder":5,"status":1}
```
- **预期**: code=200
- **异常**: roleCode 重复 → code=10007

### 21. 角色列表
- **GET** `/api/user/roles?pageNum=1&pageSize=10`
- **预期**: code=200，data.records 含 5 条（4 预设 + 1 新增）

### 22. 编辑角色
- **PUT** `/api/user/roles/{id}`
- **Path**: id={测试角色id}
- **Body**:
```json
{"roleName":"测试角色改名","roleCode":"ROLE_TEST","roleDesc":"改名后的描述","dataScope":2,"sortOrder":5,"status":1}
```
- **预期**: code=200

### 23. 给角色分配菜单
- **PUT** `/api/user/roles/{id}/menus`
- **Path**: id={测试角色id}
- **Body**: `[1, 2, 3, 4, 5, 6]`（菜单ID数组）
- **预期**: code=200

### 24. 查询角色拥有的菜单
- **GET** `/api/user/roles/{id}/menus`
- **Path**: id={测试角色id}
- **预期**: code=200，data=[1,2,3,4,5,6]

### 25. 删除角色
- **DELETE** `/api/user/roles/{id}`
- **Path**: id={测试角色id}
- **预期**: code=200

---

## 六、菜单管理 MenuController

### 26. 所有菜单树
- **GET** `/api/user/menus`
- **预期**: code=200，data 为树形数组，含 "系统管理" 目录及其下菜单
- ![image-20260721101911946](C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721101911946.png)

### 27. 新增菜单
- **POST** `/api/user/menus`
- **Body**:
```json
{"menuName":"测试菜单","menuType":2,"parentId":1,"path":"/system/test","component":"system/test/index","permissionCode":"test:list","icon":"Test","sortOrder":99,"visible":1,"status":1}
```
- **预期**: code=200

### 28. 编辑菜单
- **PUT** `/api/user/menus/{id}`
- **Path**: id={测试菜单id}
- **Body**:
```json
{"menuName":"测试菜单改名","menuType":2,"parentId":1,"path":"/system/test","component":"system/test/index","permissionCode":"test:list","icon":"Test","sortOrder":99,"visible":1,"status":1}
```
- **预期**: code=200

### 29. 删除菜单
- **DELETE** `/api/user/menus/{id}`
- **Path**: id={测试菜单id}
- **预期**: code=200
- **异常**: 有子菜单 → code=10008

### 30. 用户动态路由
- **GET** `/api/user/menus/routers`
- **Header**: `Authorization: Bearer {token}`
- **预期**: code=200（当前返回空占位）
- ![image-20260721101949731](C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721101949731.png)

---

## nacos服务列表

![image-20260721102512438](C:\Users\Ye\AppData\Roaming\Typora\typora-user-images\image-20260721102512438.png)
