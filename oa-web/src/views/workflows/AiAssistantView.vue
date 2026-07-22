<template>
  <section class="page ai-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">AI 助手</h2>
        <p class="page-subtitle">智能填单、数据分析、知识问答</p>
      </div>
      <button v-if="messages.length > 1" class="btn ghost" @click="clearChat">新对话</button>
    </div>

    <section class="panel chat-panel">
      <div class="chat-messages" ref="chatList">
        <div v-if="messages.length === 0" class="chat-welcome">
          <p>我可以帮你：</p>
          <ul>
            <li>智能填单 — "帮我请个假，从明天开始三天"</li>
            <li>知识问答 — "请假流程是什么？"</li>
            <li>数据分析 — "本月考勤异常有哪些？"</li>
          </ul>
        </div>

        <template v-for="(item, idx) in messages" :key="idx">
          <div class="chat-row" :class="item.role">
            <template v-if="item.role === 'assistant'">
              <p v-if="item.text" class="chat-text">{{ item.text }}</p>
              <span v-if="item.thinking" class="thinking-dot" />
              <span v-if="item.streaming" class="cursor">|</span>
            </template>
            <p v-else>{{ item.text }}</p>
          </div>

          <!-- Confirmation card -->
          <div v-if="item.confirmation && !streaming" class="confirm-card">
            <div class="confirm-fields">
              <div v-for="(val, key) in item.fields" :key="key" class="confirm-field">
                <span class="cf-label">{{ fieldLabel(key) }}</span>
                <span class="cf-value">{{ formatFieldVal(key, val) }}</span>
              </div>
            </div>
            <div class="confirm-actions">
              <button class="btn primary" @click="confirmForm">确认提交</button>
              <button class="btn" @click="modifyForm">修改信息</button>
            </div>
          </div>
        </template>
      </div>

      <form class="chat-input" @submit.prevent="send" v-if="!streaming">
        <input v-model="input" class="field" placeholder="输入问题或办公需求" :disabled="streaming" />
        <button class="btn primary" :disabled="!input.trim()"><Send class="icon" />发送</button>
      </form>
      <div v-else class="chat-input"><span class="thinking-text">AI 正在思考...</span></div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { nextTick, ref } from 'vue'
import { Send } from 'lucide-vue-next'

interface ChatItem {
  role: 'user' | 'assistant'
  text: string
  streaming?: boolean
  thinking?: boolean
  confirmation?: boolean
  fields?: Record<string, unknown>
}

const input = ref('')
const messages = ref<ChatItem[]>([])
const streaming = ref(false)
const chatList = ref<HTMLElement>()
const sessionId = ref('')
let abortCtrl: AbortController | null = null

const fieldLabels: Record<string, string> = {
  appType: '申请类型', leaveType: '请假类型', startTime: '开始时间', endTime: '结束时间', duration: '时长', reason: '原因'
}

function fieldLabel(key: string) { return fieldLabels[key] || key }

function formatFieldVal(key: string, val: unknown) {
  if (key === 'appType') return ({ '1': '请假', '2': '加班', '3': '外出' } as Record<string, string>)[String(val)] || val
  if (key === 'leaveType') return ({ '1': '年假', '2': '事假', '3': '病假', '4': '婚假', '5': '产假' } as Record<string, string>)[String(val)] || val
  if (key === 'duration') return val + '天'
  return val
}

function scrollToBottom() {
  nextTick(() => { if (chatList.value) chatList.value.scrollTop = chatList.value.scrollHeight })
}

function clearChat() { messages.value = []; sessionId.value = '' }

function send() {
  const text = input.value.trim()
  if (!text || streaming.value) return
  input.value = ''
  messages.value.push({ role: 'user', text })
  startStream(text, '')
  scrollToBottom()
}

function confirmForm() {
  const lastUser = [...messages.value].reverse().find(m => m.role === 'user')
  if (!lastUser) return
  startStream(lastUser.text, 'confirm')
}

function modifyForm() {
  startStream('需要修改申请信息', 'modify')
}

async function startStream(message: string, action: string) {
  if (streaming.value) return
  streaming.value = true
  abortCtrl = new AbortController()

  const assistantMsg: ChatItem = { role: 'assistant', text: '', streaming: true }
  messages.value.push(assistantMsg)
  scrollToBottom()

  try {
    const token = localStorage.getItem('oa_token') || ''
    const body: Record<string, string> = { message }
    if (sessionId.value) body.sessionId = sessionId.value
    if (action) body.action = action

    const resp = await fetch('/api/ai/agent/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
      body: JSON.stringify(body),
      signal: abortCtrl.signal
    })

    if (!resp.ok) {
      appendToLast('\n[服务异常: ' + resp.status + ']')
      finishStream()
      return
    }

    const reader = resp.body!.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const lines = buffer.split('\n')
      buffer = lines.pop() || ''
      for (const line of lines) {
        if (!line.startsWith('data:')) continue
        const data = line.slice(5).trim()
        if (!data || data === '[DONE]') continue
        try {
          handleEvent(JSON.parse(data))
        } catch {
          assistantMsg.text += data
          assistantMsg.text = assistantMsg.text
        }
      }
    }
    finishStream()
  } catch (err: unknown) {
    if (err instanceof DOMException && err.name === 'AbortError') return
    const msg = err instanceof Error ? err.message : '连接失败'
    appendToLast('\n[' + msg + ']')
    finishStream()
  }
}

function handleEvent(event: Record<string, unknown>) {
  const type = String(event.type || '')
  const content = String(event.content || '')

  switch (type) {
    case 'thinking':
      setThinking(true)
      break
    case 'intent':
    case 'sources':
      break
    case 'token':
      setThinking(false)
      appendToLast(content)
      scrollToBottom()
      break
    case 'message':
    case 'clarification':
      setThinking(false)
      appendToLast(content)
      scrollToBottom()
      break
    case 'confirmation': {
      setThinking(false)
      appendToLast(content)
      const fields = event.fields as Record<string, unknown> | undefined
      const last = lastAssistant()
      if (last) {
        last.confirmation = true
        last.fields = fields
        last.text += '\n\n字段已提取完毕，请在下方确认或修改。'
      }
      const sid = event.sessionId as string
      if (sid) sessionId.value = sid
      break
    }
    case 'submitted':
      appendToLast('\n\n' + content)
      break
    case 'error':
      appendToLast('\n[错误: ' + content + ']')
      break
    case 'done': {
      const sid = event.sessionId as string
      if (sid) sessionId.value = sid
      break
    }
    default:
      if (content) appendToLast(content)
  }
}

function lastAssistant(): ChatItem | undefined {
  const msgs = messages.value
  for (let i = msgs.length - 1; i >= 0; i--) {
    if (msgs[i].role === 'assistant') return msgs[i]
  }
}

function appendToLast(text: string) {
  const last = lastAssistant()
  if (last) last.text += text
}

function setThinking(v: boolean) {
  const last = lastAssistant()
  if (last) last.thinking = v
}

function finishStream() {
  streaming.value = false
  const last = lastAssistant()
  if (last) { last.streaming = false; last.thinking = false }
  abortCtrl = null
}
</script>

<style scoped>
.chat-panel {
  min-height: calc(100vh - 180px);
  display: grid;
  grid-template-rows: minmax(0, 1fr) auto;
  overflow: hidden;
}

.chat-messages {
  display: grid;
  align-content: start;
  gap: 12px;
  padding: 18px;
  overflow: auto;
}

.chat-welcome {
  padding: 24px;
  color: var(--muted);
  line-height: 1.8;
}

.chat-row {
  max-width: min(680px, 86%);
  padding: 12px 14px;
  border-radius: 8px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
  line-height: 1.65;
}

.chat-row.user {
  justify-self: end;
  background: #eaf5f7;
  border-color: #cce5eb;
}

.chat-row p { margin: 0; }

.chat-text { white-space: pre-wrap; word-break: break-word; }

.cursor { animation: blink 1s infinite; color: var(--primary); font-weight: bold; }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0; } }

.thinking-dot {
  display: inline-block;
  width: 8px; height: 8px;
  border-radius: 50%;
  background: var(--primary);
  margin-left: 2px;
  animation: pulse 0.6s infinite alternate;
}
@keyframes pulse { from { opacity: 0.3; } to { opacity: 1; } }

.thinking-text { color: var(--muted); padding: 14px; }

.chat-input {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  padding: 14px;
  border-top: 1px solid var(--border);
}

.confirm-card {
  max-width: min(680px, 86%);
  padding: 14px;
  border-radius: 8px;
  background: #f0faf5;
  border: 1px solid #b7e4cf;
  display: grid;
  gap: 12px;
}

.confirm-fields { display: grid; gap: 6px; }
.confirm-field { display: grid; grid-template-columns: 90px minmax(0, 1fr); gap: 8px; font-size: 14px; }
.cf-label { color: var(--muted); }
.cf-value { font-weight: 600; }

.confirm-actions { display: flex; gap: 10px; justify-content: flex-end; }
</style>
