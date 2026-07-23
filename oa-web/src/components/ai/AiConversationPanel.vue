<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Clock3, MessageSquareText, Search, Trash2 } from 'lucide-vue-next'
import { aiApi } from '@/api/ai'

type Conv = { sessionId: string; question?: string; title?: string; answer?: string; content?: string; createTime?: string }
type DetailMsg = { id?: string | number; question?: string; answer?: string; content?: string }

const loading = ref(false)
const detailLoading = ref(false)
const conversations = ref<Conv[]>([])
const detail = ref<DetailMsg[]>([])
const selected = ref('')
const error = ref('')
const keyword = ref('')

const filtered = computed(() => {
  const v = keyword.value.trim().toLowerCase()
  if (!v) return conversations.value
  return conversations.value.filter(c =>
    [c.question, c.title, c.answer, c.content, c.sessionId].some(t => String(t || '').toLowerCase().includes(v))
  )
})

async function load() {
  loading.value = true; error.value = ''
  try {
    const page = await aiApi.conversations.list({ pageNum: 1, pageSize: 50 })
    conversations.value = (page?.records || []) as Conv[]
  } catch (e) { error.value = e instanceof Error ? e.message : '加载失败' }
  finally { loading.value = false }
}

async function openSession(sid: string) {
  selected.value = sid; detailLoading.value = true; error.value = ''
  try { detail.value = await aiApi.conversations.detail(sid) }
  catch (e) { error.value = e instanceof Error ? e.message : '加载失败' }
  finally { detailLoading.value = false }
}

async function removeSession(sid: string) {
  if (!confirm('确认删除该会话？')) return
  await aiApi.conversations.remove(sid)
  if (selected.value === sid) { selected.value = ''; detail.value = [] }
  await load()
}

onMounted(load)
</script>

<template>
  <div class="conv-root">
    <!-- left list -->
    <div class="conv-list panel">
      <div class="list-head">
        <span>会话历史</span>
        <em>{{ conversations.length }}</em>
      </div>
      <label class="search-box">
        <Search />
        <input v-model="keyword" placeholder="搜索会话..." />
      </label>
      <div class="list-body">
        <template v-if="loading">
          <div v-for="n in 6" :key="n" class="sk-item"><span class="sk w" /><span class="sk m" /></div>
        </template>
        <button
          v-for="c in filtered" :key="c.sessionId"
          :class="['item', { sel: selected === c.sessionId }]"
          @click="openSession(c.sessionId)"
        >
          <MessageSquareText class="item-icon" />
          <div class="item-copy">
            <b>{{ c.question || c.title || c.sessionId }}</b>
            <small>{{ c.answer || c.content || '点击查看详情' }}</small>
            <time v-if="c.createTime"><Clock3 />{{ c.createTime }}</time>
          </div>
          <span class="del" title="删除" @click.stop="removeSession(c.sessionId)"><Trash2 /></span>
        </button>
        <div v-if="!conversations.length && !loading" class="empty">暂无会话记录</div>
      </div>
    </div>

    <!-- right detail -->
    <div class="conv-detail panel">
      <div class="detail-head">
        <span>{{ selected ? '对话详情' : '选择会话查看' }}</span>
        <code v-if="selected">{{ selected }}</code>
      </div>
      <div class="detail-body">
        <template v-if="detailLoading">
          <div v-for="n in 4" :key="n" class="sk-msg"><span class="sk w" /><span class="sk m" /></div>
        </template>
        <template v-else-if="!detail.length">
          <div class="empty">点击左侧会话查看完整上下文</div>
        </template>
        <div v-for="(m, i) in detail" :key="i" :class="['d-msg', i % 2 === 0 ? 'user' : 'ai']">
          <em>{{ i % 2 === 0 ? 'YOU' : 'AI' }}</em>
          <p>{{ m.question || m.answer || m.content }}</p>
        </div>
      </div>
      <div v-if="error" class="err">{{ error }}</div>
    </div>
  </div>
</template>

<style scoped>
.conv-root { height: 100%; display: grid; grid-template-columns: 300px 1fr; gap: 0; }
.panel { border-right: 1px solid var(--border); }
.conv-detail.panel { border-right: 0; }

/* list */
.conv-list { display: flex; flex-direction: column; background: var(--bg-subtle); }
.list-head { display: flex; justify-content: space-between; align-items: center; padding: 14px 16px; font-size: 13px; font-weight: 680; color: var(--text); }
.list-head em { font-style: normal; font-size: 11px; color: var(--muted); background: var(--surface); padding: 2px 8px; border-radius: 10px; }
.search-box { display: flex; align-items: center; gap: 6px; margin: 0 12px 8px; padding: 7px 10px; border: 1px solid var(--border); border-radius: 8px; background: var(--surface); }
.search-box svg { width: 14px; color: var(--faint); }
.search-box input { flex: 1; border: 0; outline: 0; background: transparent; color: var(--text); font-size: 12px; }
.list-body { flex: 1; overflow-y: auto; padding: 0 8px 8px; }

.item {
  display: grid; grid-template-columns: 28px 1fr auto; gap: 8px;
  width: 100%; padding: 10px 8px; margin-bottom: 2px;
  border: 1px solid transparent; border-radius: 8px;
  background: transparent; color: inherit; font: inherit; text-align: left;
  cursor: pointer; transition: background .16s, border-color .16s;
}
.item:hover { background: var(--surface); border-color: var(--border); }
.item.sel { background: color-mix(in srgb, var(--primary) 8%, transparent); border-color: color-mix(in srgb, var(--primary) 20%, transparent); }
.item-icon { width: 28px; height: 28px; padding: 5px; border-radius: 6px; background: var(--surface); color: var(--primary-soft); }
.item-copy { min-width: 0; }
.item-copy b { display: block; overflow: hidden; font-size: 12px; text-overflow: ellipsis; white-space: nowrap; color: var(--text); }
.item-copy small { display: block; overflow: hidden; margin-top: 3px; font-size: 10px; color: var(--muted); text-overflow: ellipsis; white-space: nowrap; }
.item-copy time { display: flex; align-items: center; gap: 3px; margin-top: 4px; font-size: 9px; color: var(--faint); }
.item-copy time svg { width: 10px; }
.del { width: 26px; height: 26px; display: grid; place-items: center; border-radius: 5px; color: var(--faint); opacity: 0; transition: opacity .16s, background .16s, color .16s; }
.item:hover .del { opacity: 1; }
.del:hover { background: color-mix(in srgb, var(--danger) 12%, transparent); color: var(--danger); }
.del svg { width: 12px; }

/* detail */
.conv-detail { display: flex; flex-direction: column; }
.detail-head { display: flex; justify-content: space-between; align-items: center; padding: 14px 20px; border-bottom: 1px solid var(--border); font-size: 13px; font-weight: 680; color: var(--text); }
.detail-head code { font-size: 10px; color: var(--faint); max-width: 40%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.detail-body { flex: 1; overflow-y: auto; padding: 20px; display: flex; flex-direction: column; gap: 16px; }
.d-msg { max-width: 76%; padding: 12px 16px; border-radius: 12px; animation: fade-up .22s ease both; }
.d-msg.ai { align-self: flex-start; background: var(--surface); border: 1px solid var(--border); border-bottom-left-radius: 4px; }
.d-msg.user { align-self: flex-end; background: var(--brand-gradient); color: #fff; border-bottom-right-radius: 4px; }
.d-msg em { display: block; font-size: 9px; font-weight: 700; margin-bottom: 4px; opacity: .6; font-style: normal; }
.d-msg p { margin: 0; font-size: 13px; line-height: 1.65; white-space: pre-wrap; word-break: break-word; }

.empty { padding: 40px 20px; text-align: center; color: var(--faint); font-size: 12px; }
.err { margin: 10px 16px; padding: 8px 12px; border-radius: 6px; background: color-mix(in srgb, var(--danger) 10%, transparent); color: var(--danger); font-size: 11px; }

.sk { display: block; height: 9px; border-radius: 4px; background: color-mix(in srgb, var(--muted) 12%, transparent); margin-bottom: 6px; }
.sk.w { width: 85%; } .sk.m { width: 55%; }
.sk-item { padding: 10px 8px; }
.sk-msg { padding: 12px 16px; max-width: 60%; }

@keyframes fade-up { from { opacity: 0; transform: translateY(6px); } }
</style>
