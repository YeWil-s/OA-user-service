# Nacos 配置说明

当前工程在各服务 `application.yml` 中已配置：

```yaml
spring:
  config:
    import:
      - optional:nacos:${spring.application.name}.yaml
```

服务启动时会优先读取本地配置，同时尝试从 Nacos 加载同名配置，例如：

- `gateway-service.yaml`
- `user-service.yaml`
- `attendance-service.yaml`
- `approval-service.yaml`
- `notice-service.yaml`
- `asset-service.yaml`
- `ai-service.yaml`
- `visual-service.yaml`

本目录用于后续维护 Nacos 配置备份。
