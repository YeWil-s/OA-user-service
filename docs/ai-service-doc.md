# oa-ai 智能助手服务 技术文档

> 版本: v1.1 | 日期: 2026-07-21

---

## 一、模块概述

`oa-ai` 是 OA 管理系统的 AI 智能助手微服务，提供两大核心能力：

1. **RAG 知识问答** — 基于 Redis Stack KNN 向量相似度检索 + LLM 流式生成
2. **Agent 智能填单** — 识别用户意图，从自然语言中提取表单字段，确认后提交审批申请

### 技术栈

| 组件 | 选型 | 说明 |
|------|------|------|
| 框架 | Spring Boot 3.2.5 | |
| ORM | MyBatis-Plus 3.5.7 | |
| LLM API | OkHttp 4.12 + OpenAI 兼容协议 | DeepSeek Chat |
| Embedding | 硅基流动 `BAAI/bge-large-zh-v1.5` | 1024 维，余弦距离 |
| 向量存储 & 检索 | Redis Stack (RediSearch) + Jedis 5.1.3 | FT.CREATE / FT.SEARCH KNN |
| 流式输出 | Spring WebFlux (SSE) | |
| Redis 基础操作 | Lettuce (Spring Data Redis) | HSET / DELETE 等 |
| 服务注册 | Nacos | |

---

## 二、模块结构

```
oa-ai/src/main/java/com/oa/ai/
├── OaAiApplication.java
├── config/
│   ├── AiConfig.java            # LLM/Embedding 配置属性 + OkHttpClient Bean
│   ├── SecurityConfig.java      # JwtUtils Bean
│   ├── RedisVectorConfig.java   # Jedis 连接池 + 启动时自动建索引 (sendCommand)
│   ├── MyBatisPlusConfig.java   # 分页插件、自动填充
│   └── WebMvcConfig.java        # CORS + JwtInterceptor 注册
├── interceptor/
│   └── JwtInterceptor.java      # JWT 解析 → request attributes
├── controller/
│   ├── AiChatController.java    # GET|POST /api/ai/chat/stream — RAG SSE
│   ├── AiAgentController.java   # GET|POST /api/ai/agent/stream — Agent SSE
│   ├── KnowledgeController.java # /api/ai/knowledge — 知识库 CRUD
│   └── ConversationController.java # /api/ai/conversations — 对话历史
├── service/
│   ├── EmbeddingService.java    # 调用 Embedding API 返回 float[1024]
│   ├── LlmService.java          # 非流式 String + 流式 Flux<String>
│   ├── PromptService.java       # 严格反幻觉提示词构建
│   ├── RagService.java          # embed → KNN search → prompt → LLM stream
│   ├── AgentService.java        # 意图识别 → 提取 → 确认 → 提交
│   ├── VectorStoreService.java  # Redis Embedding 存储 + KNN 检索 + MySQL fallback
│   ├── IKnowledgeDocService.java
│   ├── IKnowledgeTagService.java
│   ├── IAiConversationService.java
│   ├── IApprovalService.java    # 提交审批单
│   └── impl/                    # 所有实现类
├── mapper/
│   ├── KnowledgeDocMapper.java  # 含 n-gram 关键词搜索 SQL (fallback)
│   └── ...
├── entity/                      # KnowledgeDoc, AiConversation, AppApplication...
├── dto/                         # ChatRequestDTO, AgentRequestDTO...
├── vo/                          # KnowledgeDocVO, SourceRefVO...
└── enums/                       # KnowledgeCategory, AgentIntent, AgentState
```

---

## 三、数据库

### 3.1 ai_db（oa-ai 自有库）

| 表名 | 用途 |
|------|------|
| `ai_knowledge_doc` | 知识文档（title, content, category, access_roles） |
| `ai_knowledge_tag` | 知识标签 |
| `ai_knowledge_doc_tag` | 文档-标签 N:N 关联 |
| `ai_conversation` | AI 对话记录 |
| `ai_analysis_report` | 分析报告（预留） |

建表脚本：`sql/ai_knowledge_init.sql`

### 3.2 审批申请单（临时本地表）

| 表名 | 用途 |
|------|------|
| `app_application` | 审批申请单（当前存于 ai_db，oa-ai 直接写入） |

> **TODO**: 待 oa-approval 服务开发完成后，迁移至 approval_db，oa-ai 改为通过 Nacos + HTTP 调用。

---

## 四、API 端点

所有接口前缀 `/api/ai`，需 `Authorization: Bearer <token>` 头。

### 4.1 RAG 问答

```
GET  /api/ai/chat/stream?question=请假流程是怎样的&sessionId=xxx
POST /api/ai/chat/stream  { "question": "...", "sessionId": "..." }
```

响应：SSE 事件流
```json
{"type":"thinking","content":"正在检索相关知识..."}
{"type":"sources","data":[{"docId":1,"title":"请假制度与流程","snippet":"...","score":0.9}]}
{"type":"token","content":"根据"}
{"type":"done","sessionId":"uuid","tokensUsed":0}
{"type":"error","content":"AI服务暂时不可用"}
```

### 4.2 Agent 智能填单

```
GET /api/ai/agent/stream?message=明天不舒服想去医院，帮我请个病假&sessionId=xxx
```

响应：两阶段 SSE
```
# 阶段1 — 字段提取
{"type":"intent","intent":"FORM_FILLING","confidence":0.95}
{"type":"confirmation","fields":{...},"content":"确认无误请回复「确认」..."}

# 阶段2 — 用户确认后（同一 sessionId，action=confirm）
{"type":"submitted","applicationNo":"LV20260721001","content":"申请已提交成功！..."}
```

### 4.3 知识管理

| Method | Path | 权限 |
|--------|------|------|
| `GET` | `/knowledge` | 所有用户 (角色过滤) |
| `GET` | `/knowledge/{id}` | 所有用户 |
| `POST` | `/knowledge` | ROLE_ADMIN, ROLE_HR |
| `PUT` | `/knowledge/{id}` | ROLE_ADMIN, ROLE_HR |
| `DELETE` | `/knowledge/{id}` | ROLE_ADMIN |
| `POST` | `/knowledge/reindex` | ROLE_ADMIN |
| `GET` | `/tags` | 所有用户 |
| `POST/PUT/DELETE` | `/tags[/{id}]` | ROLE_ADMIN |

### 4.4 对话历史

| Method | Path | 说明 |
|--------|------|------|
| `GET` | `/conversations` | 分页查询 |
| `GET` | `/conversations/{sessionId}` | 会话详情 |
| `DELETE` | `/conversations/{sessionId}` | 删除（本人或 admin） |

---

## 五、数据流

### 5.1 RAG 管道（KNN 向量检索）

```
用户问题
  → EmbeddingService.embed(question)       # 硅基流动 bge-large-zh-v1.5 → float[1024]
  → VectorStoreService.search(vec, roles)  # Jedis FT.SEARCH KNN @embedding, 余弦距离
  → PromptService.buildRagSystemPrompt()   # 系统提示词 + 检索到的文档片段
  → LlmService.chatStream()               # DeepSeek Chat，SSE 流式输出
  → IAiConversationService.save()         # 持久化对话记录
```

### 5.2 Agent 管道

```
用户消息
  → AgentService.processMessage()
  → 意图分类 (LLM 非流式)
  ├── FORM_FILLING → 字段提取 (LLM) → 验证 → 确认 SSE → 提交 → ApprovalService
  ├── KNOWLEDGE_QA → 委托 RagService
  └── UNKNOWN      → 引导提示
```

### 5.3 知识入库

```
KnowledgeController.create()
  → KnowledgeDocServiceImpl.createDoc()
  → MySQL INSERT (ai_knowledge_doc + tags)
  → EmbeddingService.embed(title + content)  # 生成向量
  → VectorStoreService.store(id, ..., vec)   # Redis Hash (text fields + binary embedding)
```

---

## 六、向量检索机制

### 6.1 索引结构

```
FT.CREATE idx:knowledge ON HASH PREFIX 1 doc:knowledge: SCHEMA
  doc_id       NUMERIC SORTABLE
  title        TEXT WEIGHT 2.0
  content      TEXT WEIGHT 1.0
  category     TAG SEPARATOR ","
  tags         TAG SEPARATOR ","
  access_roles TAG SEPARATOR ","
  embedding    VECTOR HNSW 6 TYPE FLOAT32 DIM 1024 DISTANCE_METRIC COSINE
```

### 6.2 检索流程

1. 用户提问 → 硅基流动 `bge-large-zh-v1.5` 生成 1024 维向量
2. Jedis `sendCommand(SEARCH)` 执行 KNN 余弦相似度检索，同时按 `access_roles` 过滤
3. 按余弦距离升序返回 Top 5 文档
4. 返回 `SourceRefVO`（含 docId, title, snippet, cosine similarity score）

### 6.3 存储方式

- **文本字段**（title, content, tags 等）→ Lettuce `StringRedisTemplate.opsForHash().putAll()`
- **二进制向量**（float[1024] × 4 bytes = 4096 bytes）→ Lettuce raw `conn.hSet(key, "embedding", bytes)` 避免 String 序列化损坏

---

## 七、提示词设计

### 7.1 反幻觉规则（核心）

```
你是OA办公系统的智能助手。你**只能**根据下方"参考资料"中的内容回答问题。

## 严格规则
1. 只使用参考资料中的信息，不得使用任何外部知识。
2. 如果参考资料中没有答案，必须回答："抱歉，我目前的知识库中没有
   相关信息，请联系HR或管理员获取帮助。"
3. 不要猜测、推测、或编造任何信息。
4. 回答时注明引用的文档标题，例如【请假制度】。
5. 对于流程类问题，按步骤列出。
```

### 7.2 Agent 意图分类 & 字段提取

- 意图分类：FORM_FILLING / KNOWLEDGE_QA / DATA_ANALYSIS / UNKNOWN
- 字段提取：JSON 输出 `{appType, leaveType, startTime, endTime, duration, reason, missingFields, needClarification}`

---

## 八、配置

### 8.1 application.yml 关键项

```yaml
server.port: 8087
spring.data.redis.database: 0           # Redis Stack 仅支持 db 0 建索引
oa.ai.llm.base-url: https://api.deepseek.com
oa.ai.llm.api-key: ${DEEPSEEK_API_KEY}
oa.ai.llm.model: deepseek-chat
oa.ai.embedding.base-url: https://api.siliconflow.cn
oa.ai.embedding.api-key: ${EMBEDDING_API_KEY}
oa.ai.embedding.model: BAAI/bge-large-zh-v1.5   # 1024 维
oa.ai.vector.top-k: 5
```

### 8.2 环境依赖

- MySQL 8.0+（ai_db、approval_db）
- Redis Stack（Docker: `redis/redis-stack-server`，需含 RediSearch 模块）
- Nacos（服务注册与发现）
- DeepSeek API Key + 硅基流动 API Key

---

## 九、TODO List

### 高优先级

- [x] **向量检索**：已通过 Jedis `sendCommand(SEARCH)` + 硅基流动 bge-large-zh-v1.5 实现 Redis Stack KNN 余弦相似度检索。
- [x] **索引自动创建**：已通过 Jedis `sendCommand(CREATE)` 在启动时自动建索引，稳定运行。
- [x] **Agent 表单填写**：意图识别 → 字段提取 → 确认 → 提交 → 数据库写入全流程跑通。`app_application` 暂存于 `ai_db`。
- [x] **对话记录存储**：从原始 SSE JSON token 改为提取纯文本后存储，提高可读性。
- [ ] **审批服务对接**：`ApprovalServiceImpl` 当前直接写 `ai_db.app_application` 表。待 `oa-approval` 服务开发完成后，替换为 Nacos 服务发现 + HTTP 调用。

### 中优先级

- [ ] **多轮对话上下文**：当前 RAG 每轮独立检索。应加载同 session 历史对话，辅助理解上下文（如追问、补充问题）。
- [ ] **Token 统计**：`LlmService.chatStream()` 返回的 `done` 事件中 `tokensUsed` 当前写死为 0。应从 LLM API 的 `usage.total_tokens` 字段提取。
- [ ] **速率限制**：基于 Redis 的 Token Bucket 或滑动窗口，限制单用户单日 AI 调用次数。
- [ ] **知识文档分块**：长文档（>2000 字）应拆分为多个 chunk 逐条索引，提高检索精度。
- [ ] **对话记录导出**：管理员可导出指定用户/时间范围的对话记录。

### 低优先级

- [ ] **Agent 多步推理**：支持"我还有多少年假？帮我请 3 天年假"这类复合意图。
- [ ] **数据分析模块**：考勤异常检测、审批效能分析、风险预警等（`ai_analysis_report` 表已建，待开发）。
- [ ] **知识文档自动摘要**：创建文档时调用 LLM 自动生成 `summary` 字段（当前可选填）。
- [ ] **反馈机制**：答案点赞/踩，用于后续提示词优化和检索排序调优。
- [ ] **Knife4j 文档完善**：补充各接口的请求/响应示例和错误码说明。
