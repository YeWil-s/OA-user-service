<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref } from 'vue'
import { Bot, BrainCircuit, FileCheck2, RotateCcw, Send, Sparkles } from 'lucide-vue-next'
import { streamAgent, streamChat, type AiEvent, type AiEventHandlers } from '@/api/ai'
import StreamSources from '@/components/ai/StreamSources.vue'

type ChatMode = 'chat' | 'agent'
type Message = { role: 'assistant' | 'user'; content: string }
type Source = { docId?: string | number; title?: string; snippet?: string; content?: string; score?: number | string }

const mode = ref<ChatMode>('chat')
const input = ref('')
const loading = ref(false)
const sessionId = ref(crypto.randomUUID())
const sources = ref<Source[]>([])
const confirmation = ref<Record<string, unknown> | null>(null)
const error = ref('')
const messages = ref<Message[]>([
  {
    role: 'assistant',
    content: '你好，我是 OA 智能助手。你可以直接问我制度、流程，或者让我帮你发起智能填单。'
  }
])
const typingQueue = ref('')
const typingTimer = ref<ReturnType<typeof setInterval> | null>(null)
const messagesEl = ref<HTMLElement | null>(null)

const placeholder = computed(() =>
  mode.value === 'chat'
    ? '输入知识库问题，例如：请假流程怎么走？'
    : '输入自然语言申请，例如：明天下午请半天假，家里有事'
)

function scrollToLatest() {
  nextTick(() => {
    if (messagesEl.value) messagesEl.value.scrollTop = messagesEl.value.scrollHeight
  })
}

function stopTyping() {
  if (typingTimer.value) {
    clearInterval(typingTimer.value)
    typingTimer.value = null
  }
  typingQueue.value = ''
}

function createAssistantMessage(content = '') {
  const message: Message = { role: 'assistant', content }
  messages.value.push(message)
  scrollToLatest()
  return message
}

function startTyping(answer: Message) {
  stopTyping()
  typingTimer.value = setInterval(() => {
    if (!typingQueue.value.length) {
      stopTyping()
      return
    }
    answer.content += typingQueue.value.slice(0, 1)
    typingQueue.value = typingQueue.value.slice(1)
    scrollToLatest()
  }, 18)
}

function pushToken(answer: Message, content = '') {
  if (!content) return
  typingQueue.value += content
  if (!typingTimer.value) startTyping(answer)
}

function handleAiEvent(answer: Message): AiEventHandlers {
  return {
    thinking(event: AiEvent) {
      answer.content = event.content || '正在处理中...'
    },
    sources(event: AiEvent) {
      sources.value = (event.data || []) as Source[]
    },
    token(event: AiEvent) {
      if (answer.content === '正在处理中...') answer.content = ''
      pushToken(answer, event.content || '')
    },
    confirmation(event: AiEvent) {
      confirmation.value = (event.fields || event.data || {}) as Record<string, unknown>
      answer.content = event.content || '请确认下方表单信息后提交。'
    },
    submitted(event: AiEvent) {
      confirmation.value = null
      answer.content = event.content || `申请已提交：${event.applicationNo || ''}`
    },
    done(event: AiEvent) {
      if (event.sessionId) sessionId.value = event.sessionId
    },
    error(event: AiEvent) {
      error.value = event.content || 'AI 服务暂时不可用'
      answer.content = error.value
    }
  }
}

async function send(action = '') {
  const text = action === 'confirm' ? '确认' : input.value.trim()
  if (!text || loading.value) return

  error.value = ''
  loading.value = true
  if (action !== 'confirm') {
    messages.value.push({ role: 'user', content: text })
    input.value = ''
  }
  const answer = createAssistantMessage('正在处理中...')

  try {
    if (mode.value === 'chat') {
      await streamChat({ question: text, sessionId: sessionId.value }, handleAiEvent(answer))
    } else {
      await streamAgent({ message: text, sessionId: sessionId.value, action }, handleAiEvent(answer))
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : 'AI 服务暂时不可用'
    answer.content = error.value
  } finally {
    loading.value = false
    scrollToLatest()
  }
}

function resetSession() {
  stopTyping()
  sessionId.value = crypto.randomUUID()
  sources.value = []
  confirmation.value = null
  messages.value = [{ role: 'assistant', content: '新的会话已开始。' }]
  error.value = ''
}

function handleComposerKeydown(event: KeyboardEvent) {
  if (event.key === 'Enter' && !event.shiftKey) {
    event.preventDefault()
    void send()
  }
}

onBeforeUnmount(stopTyping)
</script>

<template>
  <section class="chat-layout">
    <div class="chat-main glass-panel">
      <header class="panel-header">
        <div class="panel-title">
          <span class="title-icon"><Sparkles /></span>
          <div>
            <h2>智能对话</h2>
            <p>基于企业知识与业务流程实时响应</p>
          </div>
        </div>
        <span class="runtime-status" :class="{ busy: loading }">
          <i />{{ loading ? '正在生成' : '随时待命' }}
        </span>
      </header>

      <div ref="messagesEl" class="messages" aria-live="polite">
        <article v-for="(message, index) in messages" :key="index" class="message-row" :class="message.role">
          <span class="message-avatar">
            <Bot v-if="message.role === 'assistant'" />
            <span v-else>我</span>
          </span>
          <div class="message-bubble">
            {{ message.content }}
            <span v-if="message.role === 'assistant' && loading && index === messages.length - 1" class="typing-cursor" />
          </div>
        </article>
      </div>

      <div v-if="error" class="error-banner">{{ error }}</div>

      <form class="composer" @submit.prevent="send()">
        <textarea v-model="input" :placeholder="placeholder" rows="2" @keydown="handleComposerKeydown" />
        <div class="composer-actions">
          <span>Enter 发送 · Shift + Enter 换行</span>
          <button class="icon-button ghost" type="button" title="新会话" aria-label="新会话" @click="resetSession">
            <RotateCcw />
          </button>
          <button class="send-button" type="submit" :disabled="loading || !input.trim()">
            <Send />
            <span>{{ loading ? '生成中' : '发送' }}</span>
          </button>
        </div>
      </form>
    </div>

    <aside class="context-rail">
      <section class="glass-panel mode-panel">
        <p class="section-label">工作模式</p>
        <div class="mode-tabs">
          <button type="button" :class="{ active: mode === 'chat' }" @click="mode = 'chat'">
            <BrainCircuit />
            <span><strong>知识问答</strong><small>制度与流程检索</small></span>
          </button>
          <button type="button" :class="{ active: mode === 'agent' }" @click="mode = 'agent'">
            <FileCheck2 />
            <span><strong>智能填单</strong><small>自然语言发起申请</small></span>
          </button>
        </div>
      </section>

      <section v-if="confirmation" class="glass-panel confirmation-panel">
        <div class="confirmation-title"><FileCheck2 /><strong>待确认表单</strong></div>
        <pre>{{ JSON.stringify(confirmation, null, 2) }}</pre>
        <button class="confirm-button" :disabled="loading" @click="send('confirm')">确认提交</button>
      </section>

      <section class="glass-panel source-panel">
        <div class="source-title">
          <div><span class="section-label">RAG CONTEXT</span><h3>引用来源</h3></div>
          <span class="source-count">{{ sources.length }}</span>
        </div>
        <StreamSources :sources="sources" :loading="loading && !sources.length" />
      </section>
    </aside>
  </section>
</template>

<style scoped>
.chat-layout {
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  gap: 12px;
}

.glass-panel {
  border: 1px solid rgba(148, 163, 184, 0.14);
  border-radius: 8px;
  background: linear-gradient(145deg, rgba(15, 23, 42, 0.74), rgba(8, 15, 29, 0.64));
  box-shadow: 0 22px 58px rgba(2, 6, 23, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.045);
  backdrop-filter: blur(14px) saturate(125%);
}

.chat-main {
  min-width: 0;
  min-height: 0;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto auto;
  overflow: hidden;
}

.panel-header {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 0 18px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.11);
  background: linear-gradient(90deg, rgba(59, 130, 246, 0.07), transparent 48%);
}

.panel-title,
.confirmation-title,
.source-title {
  display: flex;
  align-items: center;
  gap: 11px;
}

.title-icon {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(96, 165, 250, 0.24);
  border-radius: 7px;
  background: linear-gradient(145deg, rgba(59, 130, 246, 0.18), rgba(139, 92, 246, 0.12));
  color: #7dd3fc;
  box-shadow: 0 0 20px rgba(59, 130, 246, 0.12);
}

.title-icon svg,
.composer svg,
.mode-tabs svg,
.confirmation-title svg { width: 17px; height: 17px; }

.panel-title h2,
.source-title h3 {
  margin: 0;
  color: #f1f5f9;
  font-size: 14px;
  letter-spacing: 0;
}

.panel-title p {
  margin: 4px 0 0;
  color: #718198;
  font-size: 10px;
}

.runtime-status {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  color: #86efac;
  font-size: 10px;
}

.runtime-status i {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 10px rgba(34, 197, 94, 0.75);
}

.runtime-status.busy { color: #7dd3fc; }
.runtime-status.busy i { background: #38bdf8; animation: status-pulse 1.1s infinite; }

.messages {
  min-height: 0;
  display: grid;
  align-content: start;
  gap: 18px;
  padding: 22px 24px;
  overflow-y: auto;
  overscroll-behavior: contain;
}

.message-row {
  max-width: min(760px, 88%);
  display: flex;
  align-items: flex-start;
  gap: 10px;
  animation: message-enter 0.26s ease-out both;
}

.message-row.user {
  justify-self: end;
  flex-direction: row-reverse;
}

.message-avatar {
  width: 30px;
  height: 30px;
  flex: 0 0 30px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(96, 165, 250, 0.2);
  border-radius: 7px;
  background: rgba(59, 130, 246, 0.1);
  color: #7dd3fc;
  font-size: 10px;
  font-weight: 750;
}

.message-avatar svg { width: 15px; height: 15px; }
.user .message-avatar { border-color: rgba(167, 139, 250, 0.24); background: rgba(139, 92, 246, 0.12); color: #c4b5fd; }

.message-bubble {
  padding: 11px 14px;
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 3px 8px 8px 8px;
  background: rgba(30, 41, 59, 0.58);
  color: #d9e3f1;
  font-size: 12px;
  line-height: 1.75;
  white-space: pre-wrap;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.03);
}

.user .message-bubble {
  border-color: rgba(96, 165, 250, 0.2);
  border-radius: 8px 3px 8px 8px;
  background: linear-gradient(135deg, rgba(37, 99, 235, 0.88), rgba(109, 40, 217, 0.78));
  color: #fff;
}

.typing-cursor {
  display: inline-block;
  width: 5px;
  height: 13px;
  margin-left: 3px;
  background: #67e8f9;
  vertical-align: -2px;
  animation: cursor-blink 0.8s steps(1) infinite;
}

.error-banner {
  margin: 0 18px 10px;
  padding: 9px 11px;
  border: 1px solid rgba(244, 63, 94, 0.25);
  border-radius: 6px;
  background: rgba(159, 18, 57, 0.12);
  color: #fda4af;
  font-size: 11px;
  animation: error-shake 0.28s ease;
}

.composer {
  margin: 0 16px 16px;
  padding: 10px 12px;
  border: 1px solid rgba(96, 165, 250, 0.18);
  border-radius: 8px;
  background: rgba(15, 23, 42, 0.82);
  box-shadow: 0 12px 30px rgba(2, 6, 23, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.035);
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
}

.composer:focus-within {
  border-color: rgba(96, 165, 250, 0.55);
  box-shadow: 0 0 0 1px rgba(139, 92, 246, 0.28), 0 0 24px rgba(59, 130, 246, 0.13);
}

.composer textarea {
  width: 100%;
  min-height: 48px;
  resize: none;
  border: 0;
  outline: 0;
  background: transparent;
  color: #eef4ff;
  font: inherit;
  font-size: 12px;
  line-height: 1.6;
}

.composer textarea::placeholder { color: #536176; }

.composer-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 7px;
}

.composer-actions > span {
  margin-right: auto;
  color: #536176;
  font-size: 9px;
}

.icon-button,
.send-button,
.confirm-button {
  min-height: 34px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 7px;
  border: 1px solid transparent;
  border-radius: 6px;
  color: #fff;
  cursor: pointer;
  font: inherit;
  font-size: 11px;
  font-weight: 680;
  transition: transform 0.15s ease, filter 0.18s ease, box-shadow 0.18s ease;
}

.icon-button:active,
.send-button:active,
.confirm-button:active { transform: scale(0.97); }

.icon-button { width: 34px; padding: 0; }
.icon-button.ghost { border-color: rgba(148, 163, 184, 0.14); background: rgba(148, 163, 184, 0.06); color: #94a3b8; }
.send-button { min-width: 82px; padding: 0 13px; background: linear-gradient(135deg, #2563eb, #7c3aed); box-shadow: 0 8px 18px rgba(37, 99, 235, 0.22); }
.send-button:hover { filter: brightness(1.12); box-shadow: 0 10px 24px rgba(59, 130, 246, 0.3); }
.send-button:disabled { cursor: not-allowed; opacity: 0.42; transform: none; }

.context-rail {
  min-height: 0;
  display: grid;
  grid-template-rows: auto auto minmax(0, 1fr);
  gap: 12px;
  overflow-y: auto;
}

.mode-panel,
.source-panel,
.confirmation-panel { padding: 14px; }

.section-label {
  display: block;
  margin: 0 0 9px;
  color: #52647d;
  font-size: 8px;
  font-weight: 800;
  letter-spacing: 1.4px;
}

.mode-tabs { display: grid; grid-template-columns: 1fr 1fr; gap: 7px; }

.mode-tabs button {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 9px;
  border: 1px solid rgba(148, 163, 184, 0.12);
  border-radius: 6px;
  background: rgba(30, 41, 59, 0.46);
  color: #64748b;
  cursor: pointer;
  text-align: left;
  transition: transform 0.15s ease, color 0.18s ease, border-color 0.18s ease, background 0.18s ease;
}

.mode-tabs button:hover { color: #cbd5e1; transform: translateY(-2px); }
.mode-tabs button:active { transform: scale(0.97); }
.mode-tabs button.active { border-color: rgba(96, 165, 250, 0.32); background: linear-gradient(145deg, rgba(37, 99, 235, 0.2), rgba(124, 58, 237, 0.12)); color: #7dd3fc; box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04); }
.mode-tabs svg { flex: 0 0 17px; }
.mode-tabs span { min-width: 0; display: grid; gap: 3px; }
.mode-tabs strong { color: inherit; font-size: 10px; white-space: nowrap; }
.mode-tabs small { overflow: hidden; color: #607087; font-size: 8px; text-overflow: ellipsis; white-space: nowrap; }

.confirmation-panel { border-color: rgba(245, 158, 11, 0.24); }
.confirmation-title { color: #fbbf24; font-size: 11px; }
.confirmation-panel pre { max-height: 160px; margin: 12px 0; padding: 10px; overflow: auto; border-radius: 6px; background: rgba(2, 6, 23, 0.55); color: #cbd5e1; font-size: 10px; line-height: 1.55; white-space: pre-wrap; }
.confirm-button { width: 100%; background: linear-gradient(135deg, #d97706, #ea580c); }

.source-panel { min-height: 0; overflow-y: auto; }
.source-title { justify-content: space-between; margin-bottom: 10px; }
.source-title .section-label { margin-bottom: 4px; }
.source-count { min-width: 24px; height: 20px; display: grid; place-items: center; border-radius: 4px; background: rgba(34, 211, 238, 0.08); color: #67e8f9; font-size: 9px; font-variant-numeric: tabular-nums; }

@keyframes message-enter { from { opacity: 0; transform: translateY(8px); } }
@keyframes cursor-blink { 50% { opacity: 0; } }
@keyframes status-pulse { 50% { opacity: 0.4; transform: scale(1.25); } }
@keyframes error-shake { 25% { transform: translateX(-3px); } 75% { transform: translateX(3px); } }

@media (max-width: 1040px) {
  .chat-layout { grid-template-columns: minmax(0, 1fr) 260px; }
  .mode-tabs { grid-template-columns: 1fr; }
}

@media (max-width: 760px) {
  .chat-layout { display: block; overflow-y: auto; }
  .chat-main { min-height: 72vh; }
  .context-rail { margin-top: 10px; overflow: visible; }
  .mode-tabs { grid-template-columns: 1fr 1fr; }
  .messages { padding: 18px 12px; }
  .message-row { max-width: 96%; }
  .composer-actions > span { display: none; }
}

@media (prefers-reduced-motion: reduce) {
  .message-row,
  .runtime-status.busy i,
  .typing-cursor { animation: none; }
}
</style>
