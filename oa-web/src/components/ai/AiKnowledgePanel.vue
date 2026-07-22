<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { BookOpen, DatabaseZap, Edit3, FilePlus2, RefreshCw, Search, Tag, Trash2, X } from 'lucide-vue-next'
import { aiApi } from '@/api/ai'
import { useAuthStore } from '@/stores/auth'

type KnowledgeDoc = {
  id: number | string
  title?: string
  category?: number | string
  categoryName?: string
  summary?: string
  content?: string
  tagIds?: Array<number | string>
  tags?: Array<{ id: number | string }>
  accessRoles?: string[]
}

type KnowledgeTag = { id: number | string; tagName?: string; name?: string }

const auth = useAuthStore()
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const docs = ref<KnowledgeDoc[]>([])
const total = ref(0)
const tags = ref<KnowledgeTag[]>([])
const editingId = ref<number | string | null>(null)
const editorOpen = ref(false)
const filters = reactive({ keyword: '', category: '', tagId: '', pageNum: 1, pageSize: 10 })
const form = reactive({
  title: '',
  category: 1,
  summary: '',
  content: '',
  tagIdsText: '',
  accessRolesText: 'ROLE_ADMIN,ROLE_HR,ROLE_EMPLOYEE'
})

const roles = computed(() => auth.user?.roles || [])
const canEdit = computed(() => roles.value.includes('ROLE_ADMIN') || roles.value.includes('ROLE_HR'))
const canDelete = computed(() => roles.value.includes('ROLE_ADMIN'))
const formTitle = computed(() => (editingId.value ? '编辑知识文档' : '新增知识文档'))

function toRecords(page: Record<string, unknown>) {
  return (page?.records || page?.list || []) as KnowledgeDoc[]
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const page = await aiApi.knowledge.list(filters)
    docs.value = toRecords(page)
    total.value = Number(page?.total || docs.value.length)
  } catch (err) {
    error.value = err instanceof Error ? err.message : '知识库加载失败'
  } finally {
    loading.value = false
  }
}

async function loadTags() {
  try {
    tags.value = (await aiApi.knowledge.tags()) as KnowledgeTag[]
  } catch {
    tags.value = []
  }
}

function resetForm() {
  editingId.value = null
  Object.assign(form, {
    title: '',
    category: 1,
    summary: '',
    content: '',
    tagIdsText: '',
    accessRolesText: 'ROLE_ADMIN,ROLE_HR,ROLE_EMPLOYEE'
  })
}

function createDoc() {
  resetForm()
  editorOpen.value = true
}

function editDoc(doc: KnowledgeDoc) {
  editingId.value = doc.id
  Object.assign(form, {
    title: doc.title || '',
    category: doc.category || 1,
    summary: doc.summary || '',
    content: doc.content || '',
    tagIdsText: (doc.tagIds || doc.tags?.map((tag) => tag.id) || []).join(','),
    accessRolesText: (doc.accessRoles || []).join(',')
  })
  editorOpen.value = true
}

function buildPayload() {
  return {
    title: form.title,
    category: Number(form.category),
    summary: form.summary,
    content: form.content,
    tagIds: form.tagIdsText.split(',').map((value) => value.trim()).filter(Boolean).map(Number),
    accessRoles: form.accessRolesText.split(',').map((value) => value.trim()).filter(Boolean)
  }
}

async function save() {
  saving.value = true
  error.value = ''
  try {
    if (editingId.value) {
      await aiApi.knowledge.update(editingId.value, buildPayload())
    } else {
      await aiApi.knowledge.create(buildPayload())
    }
    resetForm()
    editorOpen.value = false
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '知识文档保存失败'
  } finally {
    saving.value = false
  }
}

async function removeDoc(id: number | string) {
  if (!confirm('确认删除该知识文档？')) return
  await aiApi.knowledge.remove(id)
  await load()
}

async function reindex() {
  await aiApi.knowledge.reindex()
  alert('已触发重建向量索引')
}

onMounted(() => {
  load()
  loadTags()
})
</script>

<template>
  <section class="knowledge-layout">
    <header class="knowledge-header glass-panel">
      <div class="heading-copy">
        <span class="heading-icon"><BookOpen /></span>
        <div>
          <span class="eyebrow">ENTERPRISE KNOWLEDGE</span>
          <h2>企业知识中枢</h2>
          <p>维护制度、流程与业务文档，为 AI 检索提供可信上下文</p>
        </div>
      </div>
      <div class="header-actions">
        <div class="document-metric"><span>已收录文档</span><strong>{{ total }}</strong></div>
        <button v-if="canDelete" class="secondary-button" type="button" @click="reindex"><DatabaseZap />重建索引</button>
        <button v-if="canEdit" class="primary-button" type="button" @click="createDoc"><FilePlus2 />新增文档</button>
      </div>
    </header>

    <form class="filter-bar glass-panel" @submit.prevent="load">
      <label class="search-field"><Search /><input v-model.trim="filters.keyword" placeholder="搜索标题或内容" /></label>
      <label><span>分类</span><select v-model="filters.category"><option value="">全部分类</option><option value="1">制度流程</option><option value="2">人事行政</option><option value="3">财务资产</option><option value="4">其他</option></select></label>
      <label><span>标签</span><select v-model="filters.tagId"><option value="">全部标签</option><option v-for="tagItem in tags" :key="tagItem.id" :value="tagItem.id">{{ tagItem.tagName || tagItem.name }}</option></select></label>
      <button class="search-button" :disabled="loading"><RefreshCw :class="{ spinning: loading }" />{{ loading ? '查询中' : '查询' }}</button>
    </form>

    <div class="table-panel glass-panel">
      <div class="table-scroll">
        <table>
          <thead><tr><th>知识文档</th><th>分类</th><th>内容摘要</th><th>访问角色</th><th>操作</th></tr></thead>
          <tbody v-if="loading">
            <tr v-for="index in 6" :key="index" class="skeleton-row">
              <td><span class="skeleton medium" /></td><td><span class="skeleton short" /></td><td><span class="skeleton wide" /></td><td><span class="skeleton medium" /></td><td><span class="skeleton action" /></td>
            </tr>
          </tbody>
          <tbody v-else>
            <tr v-for="doc in docs" :key="doc.id">
              <td><div class="doc-title"><span><BookOpen /></span><strong>{{ doc.title }}</strong></div></td>
              <td><span class="category-chip">{{ doc.categoryName || doc.category }}</span></td>
              <td><p class="summary">{{ doc.summary || doc.content?.slice(0, 80) }}</p></td>
              <td><div class="role-list"><span v-for="role in doc.accessRoles || []" :key="role">{{ role.replace('ROLE_', '') }}</span></div></td>
              <td><div class="row-actions"><button v-if="canEdit" type="button" title="编辑" @click="editDoc(doc)"><Edit3 /></button><button v-if="canDelete" class="danger" type="button" title="删除" @click="removeDoc(doc.id)"><Trash2 /></button></div></td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="!docs.length && !loading" class="empty-state"><BookOpen /><strong>暂无知识文档</strong><span>新增文档后可为 AI 提供业务知识</span></div>
      <div v-if="error" class="error-banner">{{ error }}</div>
      <footer class="table-footer"><span>当前展示 {{ docs.length }} 条，共 {{ total }} 条</span><span class="index-status"><i />向量索引就绪</span></footer>
    </div>

    <Transition name="drawer">
      <div v-if="editorOpen && canEdit" class="drawer-layer" @click.self="editorOpen = false">
        <aside class="editor-drawer">
          <header><div><span class="eyebrow">KNOWLEDGE EDITOR</span><h3>{{ formTitle }}</h3></div><button type="button" title="关闭" @click="editorOpen = false"><X /></button></header>
          <form class="editor-form" @submit.prevent="save">
            <label><span>文档标题</span><input v-model.trim="form.title" required placeholder="输入文档标题" /></label>
            <div class="form-row"><label><span>分类</span><select v-model="form.category"><option :value="1">制度流程</option><option :value="2">人事行政</option><option :value="3">财务资产</option><option :value="4">其他</option></select></label><label><span>标签 ID</span><input v-model="form.tagIdsText" placeholder="多个 ID 用英文逗号分隔" /></label></div>
            <label><span>可访问角色</span><input v-model="form.accessRolesText" /></label>
            <label><span>摘要</span><textarea v-model="form.summary" rows="3" placeholder="简要描述文档内容" /></label>
            <label class="content-field"><span>正文内容</span><textarea v-model="form.content" rows="12" required placeholder="输入知识文档正文" /></label>
            <footer><button class="secondary-button" type="button" @click="resetForm">清空</button><button class="primary-button" :disabled="saving">{{ saving ? '保存中...' : '保存文档' }}</button></footer>
          </form>
        </aside>
      </div>
    </Transition>
  </section>
</template>

<style scoped>
.knowledge-layout { min-height: 0; display: grid; grid-template-rows: auto auto minmax(0, 1fr); gap: 12px; }
.glass-panel { border: 1px solid rgba(148, 163, 184, 0.14); border-radius: 8px; background: linear-gradient(145deg, rgba(15, 23, 42, 0.74), rgba(8, 15, 29, 0.64)); box-shadow: 0 22px 58px rgba(2, 6, 23, 0.28), inset 0 1px 0 rgba(255, 255, 255, 0.045); backdrop-filter: blur(14px) saturate(125%); }
.knowledge-header { min-height: 86px; display: flex; align-items: center; justify-content: space-between; gap: 20px; padding: 14px 18px; }
.heading-copy,.header-actions,.doc-title,.row-actions { display: flex; align-items: center; }
.heading-copy { gap: 13px; }
.heading-icon { width: 40px; height: 40px; display: grid; place-items: center; border: 1px solid rgba(34, 211, 238, 0.24); border-radius: 7px; background: rgba(34, 211, 238, 0.08); color: #67e8f9; box-shadow: 0 0 22px rgba(34, 211, 238, 0.09); }
.heading-icon svg { width: 20px; }
.eyebrow { color: #4f637d; font-size: 8px; font-weight: 800; letter-spacing: 1.5px; }
h2,h3,p { margin: 0; }
h2 { margin-top: 3px; color: #f1f5f9; font-size: 17px; }
.heading-copy p { margin-top: 4px; color: #718198; font-size: 10px; }
.header-actions { gap: 8px; }
.document-metric { min-width: 92px; display: grid; gap: 3px; padding-right: 13px; border-right: 1px solid rgba(148, 163, 184, 0.12); }
.document-metric span { color: #64748b; font-size: 9px; }
.document-metric strong { color: #e2e8f0; font-size: 22px; font-variant-numeric: tabular-nums; }
button { font: inherit; }
.primary-button,.secondary-button,.search-button { min-height: 36px; display: inline-flex; align-items: center; justify-content: center; gap: 7px; padding: 0 12px; border: 1px solid transparent; border-radius: 6px; color: #fff; cursor: pointer; font-size: 10px; font-weight: 700; transition: transform .15s ease, filter .18s ease, box-shadow .18s ease; }
.primary-button { background: linear-gradient(135deg,#2563eb,#7c3aed); box-shadow: 0 8px 20px rgba(37,99,235,.2); }
.secondary-button { border-color: rgba(148,163,184,.16); background: rgba(148,163,184,.07); color: #aebdd0; }
.primary-button:hover,.secondary-button:hover,.search-button:hover { filter: brightness(1.12); transform: translateY(-1px); }
.primary-button:active,.secondary-button:active,.search-button:active { transform: scale(.97); }
.primary-button svg,.secondary-button svg,.search-button svg { width: 14px; height: 14px; }
.filter-bar { min-height: 58px; display: grid; grid-template-columns: minmax(220px,1fr) 170px 170px auto; align-items: end; gap: 10px; padding: 10px 12px; }
.filter-bar label,.editor-form label { display: grid; gap: 5px; color: #708198; font-size: 9px; font-weight: 650; }
.filter-bar input,.filter-bar select,.editor-form input,.editor-form select,.editor-form textarea { width: 100%; min-height: 36px; border: 1px solid rgba(148,163,184,.14); border-radius: 6px; outline: 0; background: rgba(2,6,23,.42); color: #dce6f5; padding: 8px 10px; font: inherit; font-size: 11px; transition: border-color .18s ease,box-shadow .18s ease; }
.filter-bar input:focus,.filter-bar select:focus,.editor-form input:focus,.editor-form select:focus,.editor-form textarea:focus { border-color: rgba(96,165,250,.55); box-shadow: 0 0 0 3px rgba(59,130,246,.1),0 0 18px rgba(139,92,246,.08); }
.search-field { position: relative; }
.search-field > svg { position: absolute; left: 10px; bottom: 10px; z-index: 1; width: 14px; color: #53657e; }
.search-field input { padding-left: 32px; }
.search-button { background: rgba(14,165,233,.14); border-color: rgba(56,189,248,.2); color: #7dd3fc; }
.spinning { animation: spin .8s linear infinite; }
.table-panel { min-height: 0; display: grid; grid-template-rows: minmax(0,1fr) auto; overflow: hidden; }
.table-scroll { min-height: 0; overflow: auto; }
table { width: 100%; min-width: 800px; border-collapse: collapse; table-layout: fixed; }
th { position: sticky; top: 0; z-index: 2; height: 42px; padding: 0 14px; border-bottom: 1px solid rgba(148,163,184,.14); background: rgba(8,15,29,.94); color: #62738a; font-size: 9px; font-weight: 780; text-align: left; }
th:nth-child(1){width:23%} th:nth-child(2){width:13%} th:nth-child(3){width:32%} th:nth-child(4){width:22%} th:nth-child(5){width:10%}
td { height: 54px; padding: 8px 14px; border-bottom: 1px solid rgba(148,163,184,.08); color: #a9b6c8; font-size: 10px; vertical-align: middle; }
tbody tr { position: relative; transition: background .18s ease; }
tbody tr:hover { background: linear-gradient(90deg,rgba(37,99,235,.09),rgba(139,92,246,.035),transparent); box-shadow: inset 3px 0 0 #38bdf8; }
.doc-title { min-width: 0; gap: 9px; }
.doc-title > span { width: 28px; height: 28px; flex: 0 0 28px; display:grid;place-items:center;border-radius:6px;background:rgba(59,130,246,.09);color:#7dd3fc; }
.doc-title svg { width:13px; }
.doc-title strong { overflow:hidden;color:#dbe7f7;font-size:11px;text-overflow:ellipsis;white-space:nowrap; }
.category-chip,.role-list span { display:inline-flex;padding:3px 7px;border-radius:4px;background:rgba(139,92,246,.09);color:#c4b5fd;font-size:8px; }
.summary { display:-webkit-box;overflow:hidden;color:#7d8da3;line-height:1.55;-webkit-box-orient:vertical;-webkit-line-clamp:2; }
.role-list { display:flex;flex-wrap:wrap;gap:4px; }
.role-list span { background:rgba(34,211,238,.07);color:#88ddec; }
.row-actions { gap:5px; }
.row-actions button { width:30px;height:30px;display:grid;place-items:center;border:1px solid rgba(148,163,184,.12);border-radius:5px;background:rgba(148,163,184,.05);color:#8ea0b7;cursor:pointer;transition:transform .15s ease,color .18s ease,border-color .18s ease; }
.row-actions button:hover { border-color:rgba(96,165,250,.3);color:#7dd3fc;transform:translateY(-1px); }
.row-actions button:active { transform:scale(.97); }
.row-actions button.danger:hover { border-color:rgba(244,63,94,.3);color:#fb7185; }
.row-actions svg { width:13px; }
.table-footer { height:38px;display:flex;align-items:center;justify-content:space-between;padding:0 14px;border-top:1px solid rgba(148,163,184,.1);color:#53647b;font-size:9px; }
.index-status { display:flex;align-items:center;gap:6px;color:#72c991; }
.index-status i { width:5px;height:5px;border-radius:50%;background:#22c55e;box-shadow:0 0 8px rgba(34,197,94,.6); }
.skeleton { display:block;position:relative;height:9px;overflow:hidden;border-radius:999px;background:rgba(148,163,184,.1); }
.skeleton::after { position:absolute;inset:0;background:linear-gradient(90deg,transparent,rgba(125,211,252,.2),transparent);content:"";transform:translateX(-100%);animation:shimmer 1.3s infinite; }
.skeleton.wide{width:90%}.skeleton.medium{width:65%}.skeleton.short{width:42%}.skeleton.action{width:54px;height:28px;border-radius:5px}
.empty-state { min-height:220px;display:grid;place-items:center;align-content:center;gap:8px;color:#53647b; }
.empty-state svg { width:28px; }
.empty-state strong { color:#8292a8;font-size:12px; }.empty-state span{font-size:9px}
.error-banner { margin:10px;padding:9px 11px;border:1px solid rgba(244,63,94,.24);border-radius:6px;background:rgba(159,18,57,.12);color:#fda4af;font-size:10px; }
.drawer-layer { position:fixed;inset:0;z-index:240;display:flex;justify-content:flex-end;background:rgba(2,6,23,.7);backdrop-filter:blur(10px); }
.editor-drawer { width:min(540px,100%);height:100%;display:grid;grid-template-rows:auto minmax(0,1fr);border-left:1px solid rgba(96,165,250,.2);background:linear-gradient(155deg,#0f172a,#070d19);box-shadow:-30px 0 70px rgba(2,6,23,.55); }
.editor-drawer > header { height:76px;display:flex;align-items:center;justify-content:space-between;padding:0 20px;border-top:2px solid transparent;border-bottom:1px solid rgba(148,163,184,.12);border-image:linear-gradient(90deg,#3b82f6,#8b5cf6) 1; }
.editor-drawer h3 { margin-top:5px;color:#f1f5f9;font-size:16px; }
.editor-drawer header button { width:34px;height:34px;display:grid;place-items:center;border:1px solid rgba(148,163,184,.14);border-radius:6px;background:rgba(148,163,184,.06);color:#94a3b8;cursor:pointer; }
.editor-drawer header svg { width:16px; }
.editor-form { min-height:0;display:grid;align-content:start;gap:14px;padding:20px;overflow-y:auto; }
.editor-form textarea { resize:vertical;line-height:1.55; }
.form-row { display:grid;grid-template-columns:1fr 1fr;gap:10px; }
.editor-form footer { position:sticky;bottom:-20px;display:flex;justify-content:flex-end;gap:8px;margin-top:4px;padding:14px 0 0;background:linear-gradient(transparent,#070d19 28%); }
.drawer-enter-active,.drawer-leave-active { transition:opacity .22s ease; }.drawer-enter-active .editor-drawer,.drawer-leave-active .editor-drawer{transition:transform .25s ease-out}.drawer-enter-from,.drawer-leave-to{opacity:0}.drawer-enter-from .editor-drawer,.drawer-leave-to .editor-drawer{transform:translateX(100%)}
@keyframes spin{to{transform:rotate(360deg)}}@keyframes shimmer{to{transform:translateX(100%)}}
@media(max-width:860px){.knowledge-header{align-items:flex-start}.header-actions{flex-wrap:wrap;justify-content:flex-end}.filter-bar{grid-template-columns:1fr 1fr}.search-field{grid-column:1/-1}.table-panel{min-height:480px}}
@media(max-width:620px){.knowledge-header{display:grid}.header-actions{justify-content:flex-start}.document-metric{margin-right:auto}.filter-bar{grid-template-columns:1fr}.search-field{grid-column:auto}.form-row{grid-template-columns:1fr}.editor-drawer{height:min(88vh,760px);margin-top:auto;border-top:1px solid rgba(96,165,250,.22);border-left:0}.drawer-layer{align-items:flex-end}.drawer-enter-from .editor-drawer,.drawer-leave-to .editor-drawer{transform:translateY(100%)}}
@media(prefers-reduced-motion:reduce){.spinning,.skeleton::after{animation:none}.drawer-enter-active,.drawer-leave-active,.drawer-enter-active .editor-drawer,.drawer-leave-active .editor-drawer{transition:none}}
</style>
