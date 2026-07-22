# oa-ai 前端接口文档

**Base URL**: `http://{host}:8087`

**鉴权方式**: 所有接口需在 Header 中携带 `Authorization: Bearer {token}`（通过 oa-user JWT 鉴权）

---

## 1. AI 智能问答（RAG）

基于知识库的智能问答，SSE 流式返回。

### GET

```
GET /api/ai/chat/stream?question=请假流程是什么？&sessionId=xxx
```

### POST

```
POST /api/ai/chat/stream
Content-Type: application/json

{
  "question": "请假流程是什么？",
  "sessionId": "xxx"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| question | String | 是 | 用户问题 |
| sessionId | String | 否 | 会话ID，不传则自动生成。用于关联多轮对话上下文 |

### SSE 事件类型

响应为 `text/event-stream`，每行格式 `data: {json}`：

| type | 触发时机 | content/data 说明 |
|------|----------|-------------------|
| `thinking` | 思考中 | content: 阶段提示文字，如"正在检索相关知识..."、"正在生成回答..." |
| `sources` | 检索完成 | data: SourceRefVO[]，参考文档列表 `[{docId, title, snippet, score}]` |
| `token` | LLM 生成中 | content: 增量文本片段（逐 token 推送） |
| `done` | 完成 | sessionId: 本次会话ID |
| `error` | 出错 | content: 错误信息 |

### 示例响应流

```
data: {"type":"thinking","content":"正在检索相关知识..."}
data: {"type":"sources","data":[{"docId":1,"title":"请假制度与流程","snippet":"...","score":0.95}]}
data: {"type":"thinking","content":"正在生成回答..."}
data: {"type":"token","content":"根据"}
data: {"type":"token","content":"【请假制度与流程】"}
data: {"type":"token","content":"，请假流程如下：..."}
data: {"type":"done","sessionId":"2b6140ad-a65c-4b3e-a372-d1939e334335"}
```

---

## 2. AI 智能助手 Agent

含意图识别和智能填单，SSE 流式返回。支持表单确认/修改。

### GET

```
GET /api/ai/agent/stream?message=帮我请个假&sessionId=xxx&action=
```

### POST

```
POST /api/ai/agent/stream
Content-Type: application/json

{
  "message": "帮我请个假",
  "sessionId": "xxx",
  "action": ""
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| message | String | 是 | 用户消息 |
| sessionId | String | 否 | 会话ID，不传则自动生成 |
| action | String | 否 | `""` = 正常对话，`"confirm"` = 确认提交表单，`"modify"` = 修改表单信息 |

### action 说明

| action | 用途 |
|--------|------|
| 空字符串 | 正常对话，AI 进行意图识别后路由到知识问答或填单流程 |
| `confirm` | 确认当前待提交的申请单，AI 执行提交并返回结果 |
| `modify` | 拒绝当前表单，AI 清除暂存数据并询问需要修改哪些信息 |

### SSE 事件类型

| type | 触发时机 | 额外字段 |
|------|----------|----------|
| `thinking` | 思考中 | content: 阶段提示 |
| `intent` | 意图识别完成 | intent: `FORM_FILLING` / `KNOWLEDGE_QA` / `DATA_ANALYSIS` / `UNKNOWN`, confidence: 置信度 |
| `clarification` | 需要追问 | content: 追问内容（如"什么类型的假期？"） |
| `confirmation` | 表单提取完成，待确认 | content: 确认信息文本，fields: 提取的字段JSON，sessionId |
| `submitted` | 提交成功 | applicationNo: 申请单号，content: 结果文本 |
| `token` | LLM 生成中 | content: 增量文本 |
| `message` | 纯文本消息 | content: 完整文本 |
| `done` | 完成 | sessionId |
| `error` | 出错 | content: 错误信息 |

### 示例：填单完整流程

```
// 1. 用户发起
GET /api/ai/agent/stream?message=帮我请个假&sessionId=

// SSE 响应：
data: {"type":"thinking","content":"正在理解您的意图..."}
data: {"type":"intent","intent":"FORM_FILLING","confidence":0.95}
data: {"type":"thinking","content":"正在提取申请信息..."}
data: {"type":"clarification","content":"什么类型的假期？"}
data: {"type":"done","sessionId":"abc123"}

// 2. 用户回答
GET /api/ai/agent/stream?message=年假&sessionId=abc123

// SSE 响应：
data: {"type":"thinking","content":"正在提取申请信息..."}
data: {"type":"clarification","content":"请问您想从什么时候开始请假？需要请几天？"}
data: {"type":"done","sessionId":"abc123"}

// 3. 用户补充日期
GET /api/ai/agent/stream?message=从明天开始，请3天&sessionId=abc123

// SSE 响应：
data: {"type":"thinking","content":"正在提取申请信息..."}
data: {"type":"confirmation","content":"我理解您想要：\n- 类型：请假（年假）\n- 开始时间：2026-07-23 09:00\n- 结束时间：2026-07-25 18:00\n- 时长：3.0天\n\n确认无误请回复「确认」或「提交」，如需修改请重新描述。","sessionId":"abc123","fields":{"appType":1,"leaveType":1,"startTime":"2026-07-23 09:00","endTime":"2026-07-25 18:00","duration":3.0,"reason":""}}
data: {"type":"done","sessionId":"abc123"}

// 此时前端显示 [确认提交] 和 [修改信息] 两个按钮

// 4a. 用户点击 [确认提交] → 前端发 action=confirm
GET /api/ai/agent/stream?message=从明天开始，请3天&sessionId=abc123&action=confirm

// SSE 响应：
data: {"type":"thinking","content":"正在提交申请..."}
data: {"type":"submitted","applicationNo":"LV20260723001","content":"申请已提交成功！\n- 申请单号：LV20260723001\n- 状态：待审批\n\n请等待审批人处理"}
data: {"type":"done","sessionId":"abc123"}

// 4b. 用户点击 [修改信息] → 前端发 action=modify
GET /api/ai/agent/stream?message=需要修改申请信息&sessionId=abc123&action=modify

// SSE 响应：
data: {"type":"message","content":"请问需要修改哪些信息？请描述您需要调整的内容（例如：改成请假3天、开始时间改成明天等）。"}
data: {"type":"done","sessionId":"abc123"}
```

## 3. 对话历史

### 历史列表（分页）

```
GET /api/ai/conversations?pageNum=1&pageSize=20
```

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| pageNum | int | 否 | 1 | |
| pageSize | int | 否 | 20 | |

**响应 data** (ConversationVO[]):

```json
{
  "records": [
    {
      "id": 29,
      "sessionId": "ed0b8025-1f95-40e6-9e34-c1a68614d2d7",
      "question": "请假流程是什么？",
      "answer": "根据【请假制度与流程】，请假流程如下：...",
      "category": 3,
      "tokensUsed": 0,
      "createTime": "2026-07-21T20:42:19"
    }
  ],
  "total": 1,
  "size": 20,
  "current": 1
}
```

### 会话详情

```
GET /api/ai/conversations/{sessionId}
```

返回某次会话的所有 Q&A 轮次，按时间升序排列。

### 删除会话

```
DELETE /api/ai/conversations/{sessionId}
```

管理员可删除任意用户的会话，普通用户仅可删除自己的会话。

---

## 4. 知识库管理

### 知识文档列表

```
GET /api/ai/knowledge?keyword=请假&tagId=1&category=3&pageNum=1&pageSize=10
```

| 参数 | 类型 | 必填 | 默认 | 说明 |
|------|------|------|------|------|
| keyword | String | 否 | - | 标题/内容关键词搜索 |
| tagId | Long | 否 | - | 标签筛选 |
| category | Integer | 否 | - | 分类筛选 |
| pageNum | int | 否 | 1 | |
| pageSize | int | 否 | 10 | |

**category 枚举**:

| 值 | 说明 |
|----|------|
| 1 | 公司制度 |
| 2 | 操作规范 |
| 3 | 人事政策 |
| 4 | 财务制度 |
| 5 | IT规范 |
| 6 | 其他 |

**响应 data** (KnowledgeDocVO):

```json
{
  "records": [
    {
      "id": 1,
      "title": "请假制度与流程",
      "summary": "本文档规定了公司员工请假的类型、流程和审批权限",
      "category": 3,
      "categoryDesc": "人事政策",
      "tagNames": ["考勤", "请假"],
      "accessRoles": ["ROLE_ADMIN", "ROLE_HR", "ROLE_EMPLOYEE"],
      "status": 1,
      "createTime": "2026-07-01T00:00:00",
      "updateTime": "2026-07-15T00:00:00"
    }
  ],
  "total": 1
}
```

### 知识文档详情

```
GET /api/ai/knowledge/{id}
```

### 创建知识文档

```
POST /api/ai/knowledge
权限: ROLE_ADMIN / ROLE_HR
```

| 请求体 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| title | String | 是 | 标题 |
| content | String | 是 | 正文内容 |
| summary | String | 否 | 摘要 |
| category | Integer | 是 | 分类，见上表 |
| tagIds | Long[] | 否 | 关联标签ID列表 |
| accessRoles | String[] | 否 | 可访问角色列表 |

### 更新知识文档

```
PUT /api/ai/knowledge/{id}
权限: ROLE_ADMIN / ROLE_HR
```

请求体同创建。

### 删除知识文档

```
DELETE /api/ai/knowledge/{id}
权限: ROLE_ADMIN
```

### 重建向量索引

```
POST /api/ai/knowledge/reindex
权限: ROLE_ADMIN
```

全量重新生成所有文档的向量嵌入并写入 Redis Stack。

### 标签列表

```
GET /api/ai/knowledge/tags
```

### 创建 / 更新 / 删除标签

```
POST   /api/ai/knowledge/tags       权限: ROLE_ADMIN
PUT    /api/ai/knowledge/tags/{id}  权限: ROLE_ADMIN
DELETE /api/ai/knowledge/tags/{id}  权限: ROLE_ADMIN
```

---

## 错误码参考

| code | 说明 |
|------|------|
| 200 | 成功 |
| 401 | 未登录或 token 过期 |
| 403 | 无权限（角色不满足 `@RequiresRole` 要求） |
| 500 | 服务器内部错误 |
| 10000 | AI服务错误 |
