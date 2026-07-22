# 项目问题记录

## 已解决

### 1. SQL 初始化密码哈希不匹配

**描述**：`oa_init.sql` 和 `user_ai_init.sql` 中的 BCrypt 哈希无法通过应用的 `BCryptPasswordEncoder` 验证。

**根因**：原始哈希由外部工具生成，与 Spring Security BCrypt 不兼容。

**修复**：通过运行中应用的 `reset-pwd` 接口生成正确哈希，已更新两个 SQL 文件。所有用户默认密码统一为 `123456`。

---

### 2. oa-ai 与 oa-data-visual 端口冲突

**描述**：两个模块均配置 `server.port: 8087`。

**修复**：`oa-data-visual` 改为 `8086`。

### 当前端口分配

| 模块 | 端口 |
|------|------|
| oa-gateway | 8080 |
| oa-user | 8081 |
| oa-notice | 8084 |
| oa-data-visual | 8086 |
| oa-ai | 8087 |

---

### 3. oa-common 合并冲突

**描述**：合并 `OA-user-service-feature-OA-web` 时 `JwtUtils`、`PermissionAspect`、`ResultCode` 冲突。

**修复**：
| 文件 | 策略 |
|------|------|
| `JwtUtils.java` | 双向合并：对方 deptId 支持 + 我方 getRemainingTtl() |
| `PermissionAspect.java` | 采用对方 UserContextHolder 模式 |
| `ResultCode.java` | 保留我方版本（含 AI 错误码） |
| 新增 | constant/、context/、page/ 三个包 |

**验证**：所有 ResultCode 引用（NOTICE_NOT_FOUND、BAD_REQUEST 等）经跨模块检查均兼容。

---

### 4. Redis Stack 向量索引降级

**描述**：无 Redis Stack 时启动报 ERROR。

**修复**：启动日志降为 WARN，`VectorStoreService.search()` 已有 try-catch 兜底 + MySQL 关键词搜索降级。

---

### 5. Gateway+微服务双重鉴权

**结论**：非 bug，是架构设计。

```
Gateway (无 Redis) → 无状态 JWT 签名验证 → 传递用户头
微服务 (有 Redis) → 查黑名单（退出登录检测）
```

Gateway 主动排除了 `spring-boot-starter-data-redis`，设计上就是轻量无状态的，黑名单检查只能在各微服务做。

---

## 待解决

### 6. `/api/user/menus/routers` 未实现

**描述**：`MenuController.routers()` 返回空壳。未根据用户角色查询权限内菜单并组装路由树。

**需要做**：
- 后端：用户角色 → 关联菜单 → 过滤权限 → 组装 RouterVO 树
- 前端：登录后调用此接口，动态生成菜单和路由

---

### 7. 前端菜单/路由未对接权限

**描述**：`oa-web` 路由全量写死，所有登录用户看到相同菜单，无权限区分。

**需要做**（依赖问题 6）：
- 调 `/api/user/menus/routers` 获取权限内路由
- `MainLayout` 侧边栏按返回的菜单树渲染
- 路由守卫根据权限过滤

---

### 8. 部分微服务模块仅有骨架

| 模块 | 状态 |
|------|------|
| oa-attendance | 空壳 |
| oa-approval | 空壳 |
| oa-asset | ✅ 已合并（资产 CRUD + 领用归还 + 人事档案 + 合同预警） |

---

### 9. XXL-JOB 调度未配置

`oa-data-visual` 依赖 XXL-JOB，配置中 `xxl.job.enabled: false`（默认关闭）。需部署调度中心并开启。
