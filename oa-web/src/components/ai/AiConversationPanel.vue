<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Clock3, History, MessageSquareText, Search, Trash2 } from 'lucide-vue-next'
import { aiApi } from '@/api/ai'

type Conversation = {
  sessionId: string
  question?: string
  title?: string
  answer?: string
  content?: string
  createTime?: string
}

type ConversationMessage = {
  id?: string | number
  question?: string
  answer?: string
  content?: string
}

const loading = ref(false)
const detailLoading = ref(false)
const conversations = ref<Conversation[]>([])
const detail = ref<ConversationMessage[]>([])
const selected = ref('')
const error = ref('')
const keyword = ref('')
const filteredConversations = computed(() => {
  const value = keyword.value.trim().toLowerCase()
  if (!value) return conversations.value
  return conversations.value.filter((item) =>
    [item.question, item.title, item.answer, item.content, item.sessionId]
      .some((text) => String(text || '').toLowerCase().includes(value))
  )
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    const page = await aiApi.conversations.list({ pageNum: 1, pageSize: 30 })
    conversations.value = (page?.records || []) as Conversation[]
  } catch (err) {
    error.value = err instanceof Error ? err.message : '会话历史加载失败'
  } finally {
    loading.value = false
  }
}

async function openSession(sessionId: string) {
  selected.value = sessionId
  detailLoading.value = true
  error.value = ''
  try {
    detail.value = await aiApi.conversations.detail(sessionId)
  } catch (err) {
    error.value = err instanceof Error ? err.message : '会话详情加载失败'
  } finally {
    detailLoading.value = false
  }
}

async function removeSession(sessionId: string) {
  if (!confirm('确认删除该会话？')) return
  await aiApi.conversations.remove(sessionId)
  if (selected.value === sessionId) {
    selected.value = ''
    detail.value = []
  }
  await load()
}

onMounted(load)
</script>

<template>
  <section class="conversation-layout">
    <aside class="session-panel glass-panel">
      <header>
        <div><span class="eyebrow">CONVERSATIONS</span><h2>会话历史</h2></div>
        <span class="session-count">{{ conversations.length }}</span>
      </header>
      <label class="session-search"><Search /><input v-model="keyword" placeholder="筛选会话" /></label>
      <div class="session-list">
        <template v-if="loading">
          <article v-for="index in 6" :key="index" class="session-item skeleton-card"><span class="skeleton wide" /><span class="skeleton medium" /></article>
        </template>
        <article v-for="item in filteredConversations" v-else :key="item.sessionId" class="session-item" :class="{ active: selected === item.sessionId }" @click="openSession(item.sessionId)">
          <span class="session-icon"><MessageSquareText /></span>
          <div><strong>{{ item.question || item.title || item.sessionId }}</strong><p>{{ item.answer || item.content || '点击查看详情' }}</p><small v-if="item.createTime"><Clock3 />{{ item.createTime }}</small></div>
          <button type="button" title="删除会话" aria-label="删除会话" @click.stop="removeSession(item.sessionId)"><Trash2 /></button>
        </article>
        <div v-if="!conversations.length && !loading" class="empty-list"><History /><strong>暂无会话</strong><span>AI 对话将在这里留存</span></div>
      </div>
    </aside>

    <section class="detail-panel glass-panel">
      <header><div><span class="eyebrow">SESSION TRACE</span><h2>{{ selected ? '对话详情' : '会话查看器' }}</h2></div><span v-if="selected" class="session-id">{{ selected }}</span></header>
      <div class="detail-messages">
        <template v-if="detailLoading">
          <article v-for="index in 4" :key="index" class="detail-message skeleton-message"><span class="skeleton wide" /><span class="skeleton medium" /></article>
        </template>
        <div v-else-if="!detail.length" class="empty-detail"><span class="orbit"><MessageSquareText /></span><strong>选择一条会话查看完整上下文</strong><p>历史消息将按照交互顺序呈现</p></div>
        <article v-for="(message, index) in detail" v-else :key="message.id || index" class="detail-message" :class="index % 2 === 0 ? 'user' : 'assistant'">
          <span class="role-label">{{ index % 2 === 0 ? 'YOU' : 'AI' }}</span>
          <p>{{ message.question || message.answer || message.content || JSON.stringify(message) }}</p>
        </article>
      </div>
      <div v-if="error" class="error-banner">{{ error }}</div>
    </section>
  </section>
</template>

<style scoped>
.conversation-layout { min-height:0;display:grid;grid-template-columns:340px minmax(0,1fr);gap:12px; }
.glass-panel { min-height:0;border:1px solid rgba(148,163,184,.14);border-radius:8px;background:linear-gradient(145deg,rgba(15,23,42,.74),rgba(8,15,29,.64));box-shadow:0 22px 58px rgba(2,6,23,.3),inset 0 1px 0 rgba(255,255,255,.045);backdrop-filter:blur(14px) saturate(125%); }
.session-panel,.detail-panel { display:grid;grid-template-rows:auto auto minmax(0,1fr);overflow:hidden; }
.detail-panel { grid-template-rows:auto minmax(0,1fr) auto; }
.session-panel>header,.detail-panel>header { min-height:62px;display:flex;align-items:center;justify-content:space-between;gap:10px;padding:0 16px;border-bottom:1px solid rgba(148,163,184,.11);background:linear-gradient(90deg,rgba(59,130,246,.06),transparent); }
.eyebrow { color:#50627a;font-size:8px;font-weight:800;letter-spacing:1.4px; }
h2 { margin:3px 0 0;color:#edf4ff;font-size:14px; }
.session-count { min-width:27px;height:22px;display:grid;place-items:center;border-radius:4px;background:rgba(59,130,246,.1);color:#7dd3fc;font-size:9px;font-variant-numeric:tabular-nums; }
.session-search { position:relative;margin:10px 12px; }
.session-search svg { position:absolute;top:10px;left:10px;width:14px;color:#53657c; }
.session-search input { width:100%;height:34px;border:1px solid rgba(148,163,184,.12);border-radius:6px;outline:0;background:rgba(2,6,23,.35);color:#dbe7f7;padding:0 10px 0 31px;font:inherit;font-size:10px; }
.session-search input:focus { border-color:rgba(96,165,250,.5);box-shadow:0 0 0 3px rgba(59,130,246,.09); }
.session-list,.detail-messages { min-height:0;overflow-y:auto;overscroll-behavior:contain; }
.session-list { padding:0 8px 10px; }
.session-item { position:relative;display:grid;grid-template-columns:30px minmax(0,1fr) auto;gap:9px;align-items:start;margin-bottom:5px;padding:10px 8px;border:1px solid transparent;border-radius:6px;cursor:pointer;transition:background .18s ease,border-color .18s ease,transform .16s ease; }
.session-item:hover { border-color:rgba(96,165,250,.16);background:rgba(59,130,246,.07);transform:translateX(3px); }
.session-item.active { border-color:rgba(34,211,238,.22);background:linear-gradient(90deg,rgba(37,99,235,.16),rgba(139,92,246,.07));box-shadow:inset 2px 0 0 #22d3ee; }
.session-icon { width:30px;height:30px;display:grid;place-items:center;border-radius:6px;background:rgba(59,130,246,.09);color:#7dd3fc; }.session-icon svg{width:14px}
.session-item>div { min-width:0; }.session-item strong { display:block;overflow:hidden;color:#cfd9e8;font-size:10px;text-overflow:ellipsis;white-space:nowrap; }.session-item p { margin:5px 0 0;overflow:hidden;color:#64748b;font-size:9px;text-overflow:ellipsis;white-space:nowrap; }.session-item small { display:flex;align-items:center;gap:4px;margin-top:6px;color:#46566c;font-size:8px; }.session-item small svg{width:9px;height:9px}
.session-item button { width:26px;height:26px;display:grid;place-items:center;border:0;border-radius:5px;background:transparent;color:#56667b;cursor:pointer;opacity:0;transition:opacity .18s ease,color .18s ease,background .18s ease; }.session-item:hover button{opacity:1}.session-item button:hover{background:rgba(244,63,94,.1);color:#fb7185}.session-item button svg{width:12px}
.session-id { max-width:45%;overflow:hidden;color:#53647a;font-family:ui-monospace,SFMono-Regular,Consolas,monospace;font-size:8px;text-overflow:ellipsis;white-space:nowrap; }
.detail-messages { display:grid;align-content:start;gap:15px;padding:20px; }
.detail-message { max-width:min(700px,86%);display:grid;gap:6px;padding:11px 13px;border:1px solid rgba(148,163,184,.12);border-radius:4px 8px 8px 8px;background:rgba(30,41,59,.5);animation:message-in .24s ease-out both; }
.detail-message.user { justify-self:end;border-color:rgba(96,165,250,.2);border-radius:8px 4px 8px 8px;background:linear-gradient(135deg,rgba(37,99,235,.72),rgba(109,40,217,.62)); }
.role-label { color:#67e8f9;font-size:7px;font-weight:850;letter-spacing:1.3px; }.user .role-label{color:#d8b4fe}.detail-message p{margin:0;color:#d7e1ef;font-size:11px;line-height:1.7;white-space:pre-wrap}
.empty-list,.empty-detail { min-height:220px;display:grid;place-items:center;align-content:center;gap:9px;color:#53647a;text-align:center; }.empty-list svg{width:24px}.empty-list strong,.empty-detail strong{color:#8493a7;font-size:11px}.empty-list span,.empty-detail p{margin:0;font-size:9px}
.orbit { width:58px;height:58px;display:grid;place-items:center;margin-bottom:5px;border:1px solid rgba(34,211,238,.18);border-radius:50%;background:radial-gradient(circle,rgba(59,130,246,.12),transparent 68%);color:#67e8f9;box-shadow:0 0 0 10px rgba(59,130,246,.025),0 0 34px rgba(34,211,238,.08); }.orbit svg{width:22px}
.skeleton-card { display:grid;min-height:58px;cursor:default }.skeleton-message{width:62%;min-height:62px}.skeleton { display:block;position:relative;height:9px;overflow:hidden;border-radius:999px;background:rgba(148,163,184,.1) }.skeleton::after{position:absolute;inset:0;background:linear-gradient(90deg,transparent,rgba(125,211,252,.2),transparent);content:"";transform:translateX(-100%);animation:shimmer 1.3s infinite}.skeleton.wide{width:90%}.skeleton.medium{width:62%}
.error-banner { margin:0 14px 12px;padding:9px 11px;border:1px solid rgba(244,63,94,.24);border-radius:6px;background:rgba(159,18,57,.12);color:#fda4af;font-size:10px; }
@keyframes shimmer{to{transform:translateX(100%)}}@keyframes message-in{from{opacity:0;transform:translateY(7px)}}
@media(max-width:760px){.conversation-layout{display:block;overflow-y:auto}.session-panel{height:360px;margin-bottom:10px}.detail-panel{min-height:520px}.session-item button{opacity:1}}
@media(prefers-reduced-motion:reduce){.skeleton::after,.detail-message{animation:none}}
</style>
