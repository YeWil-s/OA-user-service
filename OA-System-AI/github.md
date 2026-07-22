```markdown
# GitHub团队开发规范

## 1. 分支管理

采用 **Git Flow 简化版**：
```

main
|
develop
|
feature/*

```
### 分支说明

| 分支 | 用途 |
|---|---|
| main | 正式发布版本，禁止直接提交 |
| develop | 日常开发分支 |
| feature/* | 个人功能开发分支 |

---

## 2. 分支命名规范

格式：
```

类型/功能名称

```
示例：

```bash
feature/user-login       # 新功能
feature/rbac-permission

bugfix/login-error        # Bug修复

docs/api-document        # 文档修改

refactor/security        # 重构
```

------

# 3. 开发流程

## 创建功能分支

```bash
git checkout develop

git pull origin develop

git checkout -b feature/xxx
```

## 开发提交

```bash
git add .

git commit -m "feat: add user login"
```

## 推送代码

```bash
git push origin feature/xxx
```

## 创建 Pull Request

流程：

```
feature分支

↓

Pull Request

↓

Code Review

↓

Merge到develop
```

------

# 4. Commit提交规范

格式：

```
type: description
```

## 类型

| 类型     | 说明     |
| -------- | -------- |
| feat     | 新功能   |
| fix      | Bug修复  |
| docs     | 文档     |
| style    | 格式调整 |
| refactor | 重构     |
| test     | 测试     |
| chore    | 配置修改 |

示例：

```bash
feat: implement JWT login

fix: solve redis cache error

docs: update README
```

禁止：

```bash
修改代码

update

test
```

------

# 5. Pull Request规范

提交PR必须包含：

```markdown
## 功能说明

本次实现的功能


## 修改内容

- xxx
- xxx


## 测试情况

- [x] 后端启动成功
- [x] 接口测试通过
- [x] 页面测试通过


## 影响模块

例如：

- 用户模块
- 权限模块
```

------

# 6. Code Review规范

审核重点：

## 代码规范

- 命名是否规范
- 是否存在重复代码
- 是否符合项目结构

## 后端检查

Controller：

```
只负责请求处理
```

Service：

```
负责业务逻辑
```

Mapper：

```
负责数据库操作
```

## 安全检查

检查：

- JWT认证
- 权限控制
- 参数校验
- 敏感信息泄露

------

# 7. Issue任务管理

所有任务创建 Issue。

格式：

```
[模块] 功能名称
```

示例：

```
[用户模块] 实现登录功能
[AI模块] 集成RAG问答
```

Issue内容：

```markdown
## 需求

实现xxx功能


## 任务

- xxx
- xxx


## 负责人

@xxx
```

------

# 8. GitHub权限管理

推荐：

| 角色       | 权限     |
| ---------- | -------- |
| 项目负责人 | Admin    |
| 模块负责人 | Maintain |
| 普通成员   | Write    |

规则：

- 禁止直接push main
- 禁止直接修改develop
- 必须通过PR合并

------

# 9. .gitignore规范

禁止提交：

```
.idea/

target/

node_modules/

*.log

.env

application-local.yml
```

禁止上传：

```
数据库密码

服务器地址

API Key

个人配置文件
```

------

# 10. 每日开发流程

开始开发：

```bash
git checkout develop

git pull
```

创建分支：

```bash
git checkout -b feature/xxx
```

提交：

```bash
git add .

git commit -m "feat: xxx"

git push
```

完成：

```
创建PR
 ↓
Review
 ↓
Merge
 ↓
删除feature分支
```

------

# 团队开发核心规则

✅ 不直接提交main/develop
✅ 一个功能一个分支
✅ 一个功能一个PR
✅ Commit信息规范
✅ 合并前必须Review
✅ 提交前完成测试
✅ 不提交敏感配置

```

```