# 数据库脚本说明

当前目录根路径已有 `oa_init.sql`，该文件是本项目现阶段的全量数据库初始化脚本。

`docker-compose.infra.yml` 会将根目录的 `oa_init.sql` 挂载到 MySQL 容器：

```text
/docker-entrypoint-initdb.d/oa_init.sql
```

因此首次启动 MySQL 容器时会自动创建：

- `user_db`
- `attendance_db`
- `approval_db`
- `notice_db`
- `asset_db`
- `ai_db`
- `statistics_db`

