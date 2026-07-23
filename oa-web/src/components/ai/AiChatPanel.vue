<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref } from 'vue'
import { Bot, BrainCircuit, FileCheck2, RotateCcw, Send, Sparkles, ChevronDown, ChevronRight } from 'lucide-vue-next'
import { streamAgent, streamChat, type AiEvent, type AiEventHandlers } from '@/api/ai'

type ChatMode = 'chat' | 'agent'
type Message = { role: 'assistant' | 'user'; content: string }
type Source = { docId?: string | number; title?: string; snippet?: string; content?: string; score?: number | string }

const mode = ref<ChatMode>('chat')
const input = ref('')
const loading = ref(false)
const sessionId = ref(crypto.randomUUID())
const sources = ref<Source[]>([])
const sourcesOpen = ref(false)
const confirmation = ref<Record<string, unknown> | null>(null)
const error = ref('')
const messages = ref<Message[]>([
  { role: 'assistant', content: '你好，我是 OA 智能助手。你可以问我制度流程，或者让我帮你发起智能填单。' }
])
const typingQueue = ref('')
const typingTimer = ref<ReturnType<typeof setInterval> | null>(null)
const messagesEl = ref<HTMLElement | null>(null)

const placeholder = computed(() =>
  mode.value === 'chat' ? '输入问题，例如：请假流程怎么走？' : '输入申请，例如：明天下午请半天年假'
)

function scrollToLatest() {
  nextTick(() => {
    if (messagesEl.value) messagesEl.value.scrollTop = messagesEl.value.scrollHeight
  })
}

function stopTyping() {
  if (typingTimer.value) { clearInterval(typingTimer.value); typingTimer.value = null }
  typingQueue.value = ''
}

function createAssistantMessage(content = '') {
  const msg: Message = { role: 'assistant', content }
  messages.value.push(msg)
  scrollToLatest()
  return msg
}

function startTyping(answer: Message) {
  stopTyping()
  typingTimer.value = setInterval(() => {
    if (!typingQueue.value.length) { stopTyping(); return }
    answer.content += typingQueue.value.slice(0, 1)
    typingQueue.value = typingQueue.value.slice(1)
    scrollToLatest()
  }, 20)
}

function pushToken(answer: Message, content = '') {
  if (!content) return
  typingQueue.value += content
  if (!typingTimer.value) startTyping(answer)
}

function handleAiEvent(answer: Message): AiEventHandlers {
  return {
    thinking(event: AiEvent) { answer.content = event.content || '思考中...' },
    sources(event: AiEvent) {
      sources.value = (event.data || []) as Source[]
      if (sources.value.length) sourcesOpen.value = true
    },
    token(event: AiEvent) {
      if (answer.content === '思考中...') answer.content = ''
      pushToken(answer, event.content || '')
    },
    confirmation(event: AiEvent) {
      confirmation.value = (event.fields || event.data || {}) as Record<string, unknown>
      answer.content = event.content || '请确认下方表单信息后提交。'
    },
    submitted(event: AiEvent) {
      confirmation.value = null
      answer.content = `已提交：${event.applicationNo || ''}`
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
  sources.value = []
  sourcesOpen.value = false
  if (action !== 'confirm') {
    messages.value.push({ role: 'user', content: text })
    input.value = ''
  }
  const answer = createAssistantMessage('思考中...')
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
  sourcesOpen.value = false
  confirmation.value = null
  messages.value = [{ role: 'assistant', content: '新的会话已开始。' }]
  error.value = ''
}

function handleKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); void send() }
}

onBeforeUnmount(stopTyping)
</script>

<template>
  <div class="chat-root">
    <!-- header -->
    <div class="chat-top">
      <div class="chat-brand">
        <span class="brand-dot"><Sparkles /></span>
        <div>
          <strong>OA 智能助手</strong>
          <span :class="['status', { busy: loading }]">{{ loading ? '正在回复...' : '在线' }}</span>
        </div>
      </div>
      <div class="top-right">
        <div class="mode-switch">
          <button :class="{ on: mode === 'chat' }" @click="mode = 'chat'"><BrainCircuit /></button>
          <button :class="{ on: mode === 'agent' }" @click="mode = 'agent'"><FileCheck2 /></button>
        </div>
        <button class="new-btn" title="新会话" @click="resetSession"><RotateCcw /></button>
      </div>
    </div>

    <!-- messages -->
    <div ref="messagesEl" class="chat-msgs">
      <div v-for="(msg, idx) in messages" :key="idx" :class="['msg', msg.role]">
        <div class="msg-bubble" v-text="msg.content" />
        <span v-if="msg.role === 'assistant' && loading && idx === messages.length - 1" class="cursor" />
      </div>

      <!-- sources inline -->
      <div v-if="sources.length" class="sources-inline">
        <button class="src-toggle" @click="sourcesOpen = !sourcesOpen">
          <component :is="sourcesOpen ? ChevronDown : ChevronRight" />
          引用来源 ({{ sources.length }})
        </button>
        <div v-if="sourcesOpen" class="src-list">
          <div v-for="(s, i) in sources" :key="i" class="src-item">
            <span class="src-score">{{ s.score ? (Number(s.score) * 100).toFixed(0) + '%' : '-' }}</span>
            <div>
              <strong>{{ s.title || '未命名' }}</strong>
              <p>{{ s.snippet || s.content || '-' }}</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- confirmation -->
    <div v-if="confirmation" class="confirm-bar">
      <div class="confirm-info">
        <FileCheck2 />
        <pre>{{ JSON.stringify(confirmation, null, 2) }}</pre>
      </div>
      <button class="btn primary" :disabled="loading" @click="send('confirm')">确认提交</button>
    </div>

    <!-- error -->
    <div v-if="error" class="err-bar">{{ error }}</div>

    <!-- composer -->
    <div class="composer">
      <textarea
        v-model="input"
        :placeholder="placeholder"
        rows="1"
        @keydown="handleKeydown"
      />
      <button class="btn primary send-btn" :disabled="loading || !input.trim()" @click="send()">
        <Send />
      </button>
    </div>
  </div>
</template>

<style scoped>
.chat-root {
  height: 100%;
  display: flex;
  flex-direction: column;
  background: var(--bg);
}

/* ---- header ---- */
.chat-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 52px;
  padding: 0 20px;
  border-bottom: 1px solid var(--border);
  background: var(--bg-subtle);
  flex-shrink: 0;
}

.chat-brand { display: flex; align-items: center; gap: 10px; }
.brand-dot {
  width: 34px; height: 34px;
  display: grid; place-items: center;
  border-radius: 8px;
  background: var(--brand-gradient);
  color: #fff;
  box-shadow: 0 4px 14px var(--primary-glow);
}
.brand-dot svg { width: 17px; height: 17px; }
.chat-brand strong { font-size: 14px; color: var(--text); }
.status { font-size: 11px; color: var(--success); display: block; margin-top: 1px; }
.status.busy { color: var(--primary-soft); }

.top-right { display: flex; align-items: center; gap: 8px; }

.mode-switch {
  display: flex;
  border: 1px solid var(--border);
  border-radius: 7px;
  overflow: hidden;
  background: var(--surface);
}
.mode-switch button {
  width: 34px; height: 32px;
  display: grid; place-items: center;
  border: 0; background: transparent;
  color: var(--muted); cursor: pointer;
  transition: background .16s, color .16s;
}
.mode-switch button:hover { color: var(--text); }
.mode-switch button.on {
  background: color-mix(in srgb, var(--primary) 12%, transparent);
  color: var(--primary);
}
.mode-switch svg { width: 15px; height: 15px; }

.new-btn {
  width: 32px; height: 32px;
  display: grid; place-items: center;
  border: 1px solid var(--border);
  border-radius: 7px;
  background: var(--surface);
  color: var(--muted);
  cursor: pointer;
  transition: color .16s, border-color .16s;
}
.new-btn:hover { color: var(--text); border-color: var(--border-strong); }
.new-btn svg { width: 14px; height: 14px; }

/* ---- messages ---- */
.chat-msgs {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 24px 20px;
  scroll-behavior: smooth;
}

.msg {
  display: flex;
  margin-bottom: 20px;
  animation: fade-up .25s ease both;
}

.msg.user { justify-content: flex-end; }

.msg-bubble {
  max-width: 72%;
  padding: 12px 16px;
  border-radius: 12px;
  font-size: 13.5px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}

.assistant .msg-bubble {
  background: var(--surface);
  border: 1px solid var(--border);
  color: var(--text);
  border-bottom-left-radius: 4px;
}

.user .msg-bubble {
  background: var(--brand-gradient);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.cursor {
  width: 2px; height: 18px;
  background: var(--primary);
  margin-left: 6px;
  align-self: center;
  animation: blink .7s steps(1) infinite;
}

/* ---- sources inline ---- */
.sources-inline {
  margin-top: 8px;
  max-width: 72%;
}

.src-toggle {
  display: inline-flex; align-items: center; gap: 4px;
  border: 0; background: transparent;
  color: var(--muted); font: inherit; font-size: 11px; cursor: pointer;
  padding: 4px 0;
}
.src-toggle:hover { color: var(--primary-soft); }
.src-toggle svg { width: 14px; }

.src-list {
  display: grid; gap: 8px;
  margin-top: 8px;
}

.src-item {
  display: flex; gap: 10px;
  padding: 10px 12px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--surface);
  font-size: 11px;
}

.src-score {
  width: 36px; height: 22px; flex-shrink: 0;
  display: grid; place-items: center;
  border-radius: 4px;
  background: color-mix(in srgb, var(--primary) 10%, transparent);
  color: var(--primary-soft);
  font-size: 10px; font-weight: 700;
}

.src-item strong { display: block; color: var(--text); font-size: 11px; margin-bottom: 3px; }
.src-item p { margin: 0; color: var(--muted); font-size: 10px; line-height: 1.5;
  display: -webkit-box; -webkit-line-clamp: 2; -webkit-box-orient: vertical; overflow: hidden; }

/* ---- confirmation ---- */
.confirm-bar {
  display: flex; align-items: flex-start; gap: 12px;
  margin: 0 16px 8px;
  padding: 12px 14px;
  border: 1px solid color-mix(in srgb, var(--warning) 30%, transparent);
  border-radius: 8px;
  background: color-mix(in srgb, var(--warning) 6%, transparent);
}
.confirm-info { flex: 1; min-width: 0; display: flex; gap: 8px; color: var(--warning); font-size: 12px; }
.confirm-info svg { width: 16px; flex-shrink: 0; margin-top: 2px; }
.confirm-info pre { margin: 0; overflow: auto; max-height: 100px; font: inherit; font-size: 10px; white-space: pre-wrap; color: var(--text); }

/* ---- error ---- */
.err-bar {
  margin: 0 16px 8px;
  padding: 8px 12px;
  border-radius: 6px;
  background: color-mix(in srgb, var(--danger) 10%, transparent);
  color: var(--danger);
  font-size: 11px;
}

/* ---- composer ---- */
.composer {
  display: flex; align-items: flex-end; gap: 10px;
  padding: 12px 20px;
  border-top: 1px solid var(--border);
  background: var(--bg-subtle);
  flex-shrink: 0;
}
.composer textarea {
  flex: 1;
  min-height: 42px; max-height: 120px;
  padding: 10px 14px;
  border: 1px solid var(--border);
  border-radius: 10px;
  outline: 0; resize: none;
  background: var(--surface);
  color: var(--text);
  font: inherit; font-size: 13px; line-height: 1.5;
  transition: border-color .18s;
}
.composer textarea:focus { border-color: var(--primary); box-shadow: 0 0 0 3px var(--primary-glow); }
.composer textarea::placeholder { color: var(--faint); }

.send-btn {
  width: 42px; height: 42px; flex-shrink: 0;
  border-radius: 10px;
}

@keyframes fade-up { from { opacity: 0; transform: translateY(8px); } }
@keyframes blink { 50% { opacity: 0; } }
</style>
