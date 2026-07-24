<script setup lang="ts">
import { computed, nextTick, onBeforeUnmount, ref, watch } from 'vue'
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
const messagesEl = ref<HTMLElement | null>(null)
const rafPending = ref(false)
let lastScrollTs = 0

const placeholder = computed(() =>
  mode.value === 'chat' ? '输入问题，例如：请假流程怎么走？' : '输入申请，例如：明天下午请半天年假'
)

function scrollToBottom(force = false) {
  const now = Date.now()
  if (!force && now - lastScrollTs < 50) return
  lastScrollTs = now
  nextTick(() => {
    if (messagesEl.value) {
      messagesEl.value.scrollTop = messagesEl.value.scrollHeight
    }
  })
}

function createAssistantMessage(content = '') {
  const msg: Message = { role: 'assistant', content }
  messages.value.push(msg)
  scrollToBottom(true)
  return msg
}

function handleAiEvent(answer: Message): AiEventHandlers {
  return {
    thinking(event: AiEvent) {
      answer.content = event.content || '思考中...'
      scrollToBottom()
    },
    sources(event: AiEvent) {
      sources.value = (event.data || []) as Source[]
      if (sources.value.length) sourcesOpen.value = true
    },
    token(event: AiEvent) {
      answer.content += event.content || ''
      if (!rafPending.value) {
        rafPending.value = true
        requestAnimationFrame(() => {
          rafPending.value = false
          scrollToBottom()
        })
      }
    },
    confirmation(event: AiEvent) {
      confirmation.value = (event.fields || event.data || {}) as Record<string, unknown>
      answer.content = event.content || '请确认下方表单信息后提交。'
    },
    submitted(event: AiEvent) {
      confirmation.value = null
      answer.content = event.applicationNo ? `已提交：${event.applicationNo}` : '已提交'
    },
    done(event: AiEvent) {
      if (event.sessionId) sessionId.value = event.sessionId
    },
    clarification(event: AiEvent) {
      answer.content = event.content || ''
      scrollToBottom()
    },
    message(event: AiEvent) {
      answer.content = event.content || ''
      scrollToBottom()
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
    scrollToBottom(true)
  }
}

function resetSession() {
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

watch(messages, () => scrollToBottom(true), { deep: true })
onBeforeUnmount(() => {})
</script>

<template>
  <div class="chat-root">
    <!-- header -->
    <div class="chat-top">
      <div class="chat-brand">
        <span class="brand-dot"><Sparkles /></span>
        <div>
          <strong>OA 智能助手</strong>
          <span :class="['status', { busy: loading }]">{{ loading ? '回复中...' : '在线' }}</span>
        </div>
      </div>
      <div class="top-right">
        <div class="mode-switch">
          <button :class="{ on: mode === 'chat' }" title="知识问答" @click="mode = 'chat'"><BrainCircuit /></button>
          <button :class="{ on: mode === 'agent' }" title="智能填单" @click="mode = 'agent'"><FileCheck2 /></button>
        </div>
        <button class="new-btn" title="新会话" @click="resetSession"><RotateCcw /></button>
      </div>
    </div>

    <!-- messages -->
    <div ref="messagesEl" class="chat-msgs">
      <div v-for="(msg, idx) in messages" :key="idx" :class="['msg', msg.role]">
        <div class="msg-avatar">
          <Sparkles v-if="msg.role === 'assistant'" />
          <span v-else>YOU</span>
        </div>
        <div class="msg-body">
          <div class="msg-bubble">{{ msg.content }}</div>
        </div>
      </div>

      <!-- sources -->
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

/* header */
.chat-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
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

/* messages */
.chat-msgs {
  flex: 1;
  min-height: 0;
  overflow-y: auto;
  padding: 24px 20px;
}

.msg {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}
.msg.user { flex-direction: row-reverse; }

.msg-avatar {
  width: 30px; height: 30px; flex-shrink: 0;
  border-radius: 6px;
  display: grid; place-items: center;
  font-size: 9px; font-weight: 700;
}
.assistant .msg-avatar {
  background: color-mix(in srgb, var(--primary) 12%, transparent);
  color: var(--primary);
}
.user .msg-avatar {
  background: var(--brand-gradient);
  color: #fff;
}
.msg-avatar svg { width: 14px; height: 14px; }

.msg-body { max-width: 78%; min-width: 0; }

.msg-bubble {
  padding: 10px 14px;
  border-radius: 10px;
  font-size: 13.5px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
}
.assistant .msg-bubble {
  background: var(--surface);
  border: 1px solid var(--border);
  color: var(--text);
  border-top-left-radius: 4px;
}
.user .msg-bubble {
  background: var(--brand-gradient);
  color: #fff;
  border-top-right-radius: 4px;
}

/* sources */
.sources-inline { margin-left: 40px; margin-top: -8px; margin-bottom: 16px; }
.src-toggle {
  display: inline-flex; align-items: center; gap: 4px;
  border: 0; background: transparent;
  color: var(--muted); font-size: 11px; cursor: pointer;
  padding: 4px 0;
}
.src-toggle:hover { color: var(--primary-soft); }
.src-toggle svg { width: 14px; }
.src-list { display: grid; gap: 8px; margin-top: 8px; }
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

/* confirmation */
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
.confirm-info pre { margin: 0; overflow: auto; max-height: 100px; font-size: 10px; white-space: pre-wrap; color: var(--text); }

/* error */
.err-bar {
  margin: 0 16px 8px;
  padding: 8px 12px;
  border-radius: 6px;
  background: color-mix(in srgb, var(--danger) 10%, transparent);
  color: var(--danger);
  font-size: 11px;
}

/* composer */
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
.send-btn { width: 42px; height: 42px; flex-shrink: 0; border-radius: 10px; }

/* buttons */
.btn.primary {
  display: grid; place-items: center;
  border: 0;
  background: var(--brand-gradient);
  color: #fff;
  cursor: pointer;
  border-radius: 8px;
  transition: opacity .16s;
  font-size: 13px;
  padding: 8px 16px;
}
.btn.primary:disabled { opacity: .4; cursor: not-allowed; }
</style>
