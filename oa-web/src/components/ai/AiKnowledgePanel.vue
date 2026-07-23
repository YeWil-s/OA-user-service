<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { BookOpen, DatabaseZap, Edit3, FilePlus2, Plus, RefreshCw, Search, Tag, Trash2, X } from 'lucide-vue-next'
import { aiApi } from '@/api/ai'
import { systemApi } from '@/api/services'
import { useAuthStore } from '@/stores/auth'
import type { DeptNode } from '@/api/types'

type KnowledgeDoc = {
  id: number | string; title?: string; category?: number | string; categoryDesc?: string
  summary?: string; content?: string; tagNames?: string[]
  tagIds?: Array<number | string>; tags?: Array<{ id: number | string; tagName?: string }>
  accessRoles?: string[]; deptId?: number | string
  vectorStatus?: number; vectorError?: string
}

type KnowledgeTag = { id: number | string; tagName?: string; tagCode?: string; tagDesc?: string }

const CATS = [
  { value: 1, label: '公司制度' }, { value: 2, label: '操作流程' },
  { value: 3, label: 'HR政策' }, { value: 4, label: '财务制度' },
  { value: 5, label: 'IT规范' }, { value: 6, label: '其他' }
]

const auth = useAuthStore()
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const docs = ref<KnowledgeDoc[]>([])
const total = ref(0)
const tags = ref<KnowledgeTag[]>([])
const depts = ref<DeptNode[]>([])
const editingId = ref<number | string | null>(null)
const editorOpen = ref(false)
const tagEditorOpen = ref(false)
const tagForm = reactive({ id: null as number | null, tagName: '', tagCode: '', tagDesc: '' })
const filters = reactive({ keyword: '', category: '', tagId: '', pageNum: 1, pageSize: 20 })
const form = reactive({
  title: '', category: 1, summary: '', content: '',
  selectedTagIds: [] as Array<number | string>,
  accessRoles: 'ROLE_ADMIN,ROLE_HR,ROLE_EMPLOYEE',
  deptId: 0 as number
})

const roles = computed(() => auth.user?.roles || [])
const canEdit = computed(() => roles.value.includes('ROLE_ADMIN') || roles.value.includes('ROLE_HR'))
const canDelete = computed(() => roles.value.includes('ROLE_ADMIN'))

function toRecords(page: Record<string, unknown>) { return (page?.records || page?.list || []) as KnowledgeDoc[] }
function catLabel(v: number | string) { return CATS.find(c => c.value === Number(v))?.label || String(v) }
function vsClass(s: number | undefined) { return s === 1 ? 'ok' : s === 2 ? 'fail' : 'pend' }
function vsText(s: number | undefined) { return s === 1 ? '已索引' : s === 2 ? '失败' : '待索引' }

async function load() {
  loading.value = true; error.value = ''
  try {
    const p: Record<string, unknown> = { pageNum: filters.pageNum, pageSize: filters.pageSize }
    if (filters.keyword) p.keyword = filters.keyword
    if (filters.category) p.category = Number(filters.category)
    if (filters.tagId) p.tagId = Number(filters.tagId)
    const page = await aiApi.knowledge.list(p)
    docs.value = toRecords(page)
    total.value = Number(page?.total || docs.value.length)
  } catch (e) { error.value = e instanceof Error ? e.message : '加载失败' }
  finally { loading.value = false }
}

async function loadTags() { try { tags.value = (await aiApi.knowledge.tags()) as KnowledgeTag[] } catch { tags.value = [] } }
async function loadDepts() { try { depts.value = await systemApi.depts() } catch { depts.value = [] } }

function toggleTag(id: number | string) {
  const i = form.selectedTagIds.indexOf(id)
  if (i > -1) form.selectedTagIds.splice(i, 1); else form.selectedTagIds.push(id)
}

function resetForm() {
  editingId.value = null
  Object.assign(form, { title: '', category: 1, summary: '', content: '', selectedTagIds: [], accessRoles: 'ROLE_ADMIN,ROLE_HR,ROLE_EMPLOYEE', deptId: 0 })
}

function editDoc(doc: KnowledgeDoc) {
  editingId.value = doc.id
  Object.assign(form, {
    title: doc.title || '', category: Number(doc.category) || 1,
    summary: doc.summary || '', content: doc.content || '',
    selectedTagIds: (doc.tagIds || doc.tags?.map(t => t.id) || []).map(Number),
    accessRoles: (doc.accessRoles || []).join(','),
    deptId: Number(doc.deptId) || 0
  })
  editorOpen.value = true
}

async function save() {
  saving.value = true; error.value = ''
  const payload = {
    title: form.title, category: Number(form.category), summary: form.summary, content: form.content,
    tagIds: form.selectedTagIds.map(Number),
    accessRoles: form.accessRoles.split(',').map(v => v.trim()).filter(Boolean),
    deptId: form.deptId || undefined
  }
  try {
    if (editingId.value) await aiApi.knowledge.update(editingId.value, payload)
    else await aiApi.knowledge.create(payload)
    resetForm(); editorOpen.value = false; await load()
  } catch (e) { error.value = e instanceof Error ? e.message : '保存失败' }
  finally { saving.value = false }
}

async function removeDoc(id: number | string) {
  if (!confirm('确认删除？')) return
  try { await aiApi.knowledge.remove(id); await load() }
  catch (e) { error.value = e instanceof Error ? e.message : '删除失败' }
}

async function reindex() {
  try { await aiApi.knowledge.reindex(); alert('已触发重建索引') }
  catch (e) { error.value = e instanceof Error ? e.message : '重建失败' }
}

// tag crud
function openTagEditor(tag?: KnowledgeTag) {
  if (tag) Object.assign(tagForm, { id: Number(tag.id), tagName: tag.tagName || '', tagCode: tag.tagCode || '', tagDesc: tag.tagDesc || '' })
  else Object.assign(tagForm, { id: null, tagName: '', tagCode: '', tagDesc: '' })
  tagEditorOpen.value = true
}

async function saveTag() {
  try {
    const body = { tagName: tagForm.tagName, tagCode: tagForm.tagCode, tagDesc: tagForm.tagDesc }
    if (tagForm.id) await aiApi.knowledge.updateTag(tagForm.id, body)
    else await aiApi.knowledge.createTag(body)
    tagEditorOpen.value = false; await loadTags()
  } catch (e) { error.value = e instanceof Error ? e.message : '标签保存失败' }
}

async function removeTag(id: number | string) {
  if (!confirm('确认删除该标签？')) return
  try { await aiApi.knowledge.deleteTag(id); await loadTags() }
  catch (e) { error.value = e instanceof Error ? e.message : '删除失败' }
}

onMounted(() => { load(); loadTags(); loadDepts() })
</script>

<template>
  <div class="kb-root">
    <!-- toolbar -->
    <div class="kb-bar">
      <div class="kb-bar-row">
        <label class="kb-search">
          <Search /><input v-model.trim="filters.keyword" placeholder="搜索标题或摘要..." @keyup.enter="filters.pageNum=1;load()" />
        </label>
        <select v-model="filters.category" class="kb-sel" @change="filters.pageNum=1;load()">
          <option value="">全部分类</option>
          <option v-for="c in CATS" :key="c.value" :value="c.value">{{ c.label }}</option>
        </select>
        <select v-model="filters.tagId" class="kb-sel" @change="filters.pageNum=1;load()">
          <option value="">全部标签</option>
          <option v-for="t in tags" :key="t.id" :value="t.id">{{ t.tagName }}</option>
        </select>
        <button class="btn" :disabled="loading" @click="filters.pageNum=1;load()"><RefreshCw :class="{ spin: loading }" />查询</button>
        <span class="kb-count">共 {{ total }} 条</span>
      </div>
      <div class="kb-bar-row">
        <div class="tag-row">
          <Tag />
          <span v-for="t in tags" :key="t.id" class="tag-chip">
            {{ t.tagName }}
            <button v-if="canDelete" @click="openTagEditor(t)"><Edit3 /></button>
            <button v-if="canDelete" @click="removeTag(t.id)"><X /></button>
          </span>
          <button v-if="canDelete" class="btn" @click="openTagEditor()"><Plus />标签</button>
        </div>
        <div class="kb-actions">
          <button v-if="canDelete" class="btn" @click="reindex"><DatabaseZap />重建索引</button>
          <button v-if="canEdit" class="btn primary" @click="resetForm();editorOpen=true"><FilePlus2 />新增文档</button>
        </div>
      </div>
    </div>

    <!-- table -->
    <div class="kb-table-wrap">
      <table>
        <thead>
          <tr><th>文档标题</th><th>分类</th><th>标签</th><th>索引</th><th style="width:80px">操作</th></tr>
        </thead>
        <tbody v-if="loading">
          <tr v-for="n in 6" :key="n"><td><span class="sk w" /></td><td><span class="sk m" /></td><td><span class="sk m" /></td><td><span class="sk s" /></td><td><span class="sk s" /></td></tr>
        </tbody>
        <tbody v-else>
          <tr v-for="doc in docs" :key="doc.id">
            <td><div class="doc-t"><BookOpen /> <b>{{ doc.title }}</b></div></td>
            <td><span class="chip">{{ doc.categoryDesc || catLabel(doc.category!) }}</span></td>
            <td><div class="tags-inline"><span v-for="n in (doc.tagNames || [])" :key="n">{{ n }}</span></div></td>
            <td><span :class="['vs', vsClass(doc.vectorStatus)]">{{ vsText(doc.vectorStatus) }}</span></td>
            <td>
              <div class="row-act">
                <button v-if="canEdit" @click="editDoc(doc)"><Edit3 /></button>
                <button v-if="canDelete" class="dang" @click="removeDoc(doc.id)"><Trash2 /></button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
      <div v-if="!docs.length && !loading" class="empty">暂无知识文档</div>
    </div>

    <div v-if="error" class="err">{{ error }}</div>

    <!-- tag modal -->
    <Teleport to="body">
      <div v-if="tagEditorOpen && canDelete" class="modal-overlay" @click.self="tagEditorOpen=false">
        <div class="modal-box">
          <h3>{{ tagForm.id ? '编辑标签' : '新增标签' }}</h3>
          <form @submit.prevent="saveTag">
            <label>名称 <input v-model.trim="tagForm.tagName" class="field" required placeholder="标签名称" /></label>
            <label>编码 <input v-model.trim="tagForm.tagCode" class="field" placeholder="英文编码" /></label>
            <label>描述 <input v-model.trim="tagForm.tagDesc" class="field" placeholder="简要描述" /></label>
            <footer><button type="button" class="btn" @click="tagEditorOpen=false">取消</button><button class="btn primary">保存</button></footer>
          </form>
        </div>
      </div>
    </Teleport>

    <!-- editor drawer -->
    <Teleport to="body">
      <div v-if="editorOpen && canEdit" class="drawer-overlay" @click.self="editorOpen=false">
        <aside class="drawer">
          <header><h3>{{ editingId ? '编辑文档' : '新增文档' }}</h3><button @click="editorOpen=false"><X /></button></header>
          <form class="drawer-form" @submit.prevent="save">
            <label>标题 <input v-model.trim="form.title" class="field" required placeholder="文档标题" /></label>
            <div class="row2">
              <label>分类
                <select v-model="form.category" class="select">
                  <option v-for="c in CATS" :key="c.value" :value="c.value">{{ c.label }}</option>
                </select>
              </label>
              <label>归属部门
                <select v-model.number="form.deptId" class="select">
                  <option :value="0">不限</option>
                  <option v-for="d in depts" :key="d.id" :value="d.id">{{ d.deptName }}</option>
                </select>
              </label>
            </div>
            <label>标签
              <div class="tag-pick">
                <button v-for="t in tags" :key="t.id" type="button" :class="{ on: form.selectedTagIds.includes(t.id) }" @click="toggleTag(t.id)">{{ t.tagName }}</button>
              </div>
            </label>
            <label>访问角色 <input v-model="form.accessRoles" class="field" placeholder="多个角色逗号分隔" /></label>
            <label>摘要 <textarea v-model="form.summary" class="textarea" rows="2" placeholder="简要描述" /></label>
            <label>正文 <textarea v-model="form.content" class="textarea" rows="14" required placeholder="知识文档正文" /></label>
            <footer><button type="button" class="btn" @click="resetForm">清空</button><button class="btn primary" :disabled="saving">{{ saving ? '保存中...' : '保存' }}</button></footer>
          </form>
        </aside>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.kb-root { height: 100%; display: flex; flex-direction: column; overflow: hidden; }

/* bar */
.kb-bar { flex-shrink: 0; padding: 12px 16px; border-bottom: 1px solid var(--border); background: var(--bg-subtle); display: flex; flex-direction: column; gap: 8px; }
.kb-bar-row { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.kb-search { display: flex; align-items: center; gap: 6px; padding: 6px 10px; border: 1px solid var(--border); border-radius: 6px; background: var(--surface); min-width: 200px; }
.kb-search svg { width: 14px; color: var(--faint); }
.kb-search input { flex: 1; border: 0; outline: 0; background: transparent; color: var(--text); font-size: 12px; }
.kb-sel { height: 34px; padding: 0 8px; border: 1px solid var(--border); border-radius: 6px; background: var(--surface); color: var(--text); font-size: 12px; }
.kb-count { margin-left: auto; font-size: 12px; color: var(--muted); }

.tag-row { display: flex; align-items: center; gap: 6px; flex-wrap: wrap; flex: 1; min-width: 0; }
.tag-row > svg { width: 14px; color: var(--faint); flex-shrink: 0; }
.tag-chip { display: inline-flex; align-items: center; gap: 3px; padding: 3px 8px; border: 1px solid var(--border); border-radius: 4px; background: var(--surface); color: var(--text); font-size: 11px; }
.tag-chip button { width: 16px; height: 16px; display: grid; place-items: center; border: 0; background: transparent; color: var(--faint); cursor: pointer; border-radius: 2px; }
.tag-chip button:hover { color: var(--danger); background: color-mix(in srgb, var(--danger) 10%, transparent); }
.tag-chip svg { width: 9px; height: 9px; }

.kb-actions { display: flex; gap: 6px; margin-left: auto; }

/* table */
.kb-table-wrap { flex: 1; overflow: auto; }
table { width: 100%; min-width: 700px; border-collapse: collapse; }
th { position: sticky; top: 0; z-index: 2; height: 38px; padding: 0 14px; background: var(--surface); border-bottom: 1px solid var(--border); font-size: 11px; font-weight: 680; color: var(--muted); text-align: left; }
td { height: 46px; padding: 8px 14px; border-bottom: 1px solid var(--border); font-size: 12px; }
tr:hover td { background: color-mix(in srgb, var(--primary) 4%, transparent); }

.doc-t { display: flex; align-items: center; gap: 8px; }
.doc-t svg { width: 14px; color: var(--primary-soft); }
.doc-t b { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; color: var(--text); font-weight: 620; }

.chip { display: inline-block; padding: 2px 8px; border-radius: 4px; background: color-mix(in srgb, var(--primary) 8%, transparent); color: var(--primary-soft); font-size: 10px; font-weight: 650; }

.tags-inline { display: flex; gap: 3px; flex-wrap: wrap; }
.tags-inline span { padding: 2px 7px; border-radius: 3px; background: color-mix(in srgb, var(--accent) 6%, transparent); color: var(--accent); font-size: 10px; }

.vs { display: inline-block; padding: 2px 8px; border-radius: 3px; font-size: 10px; font-weight: 650; }
.vs.ok { background: color-mix(in srgb, var(--success) 8%, transparent); color: var(--success); }
.vs.fail { background: color-mix(in srgb, var(--danger) 8%, transparent); color: var(--danger); }
.vs.pend { background: color-mix(in srgb, var(--warning) 8%, transparent); color: var(--warning); }

.row-act { display: flex; gap: 4px; }
.row-act button { width: 28px; height: 28px; display: grid; place-items: center; border: 1px solid var(--border); border-radius: 5px; background: var(--surface); color: var(--muted); cursor: pointer; transition: color .16s, border-color .16s; }
.row-act button:hover { color: var(--primary-soft); border-color: var(--primary-soft); }
.row-act button.dang:hover { color: var(--danger); border-color: var(--danger); }
.row-act svg { width: 12px; }

.empty { padding: 60px 20px; text-align: center; color: var(--faint); font-size: 13px; }
.err { padding: 8px 16px; background: color-mix(in srgb, var(--danger) 8%, transparent); color: var(--danger); font-size: 11px; }

.sk { display: block; height: 9px; border-radius: 4px; background: color-mix(in srgb, var(--muted) 12%, transparent); }
.sk.w { width: 75%; } .sk.m { width: 50%; } .sk.s { width: 40%; }

.spin { animation: spin .8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

/* modal */
.modal-overlay { position: fixed; inset: 0; z-index: 300; display: grid; place-items: center; background: var(--overlay); backdrop-filter: blur(4px); }
.modal-box { width: min(400px, 94vw); padding: 20px; border: 1px solid var(--border); border-radius: 10px; background: var(--surface-elevated); box-shadow: var(--shadow-hover); }
.modal-box h3 { margin: 0 0 14px; font-size: 15px; color: var(--text); }
.modal-box form { display: grid; gap: 10px; }
.modal-box label { display: grid; gap: 4px; font-size: 11px; color: var(--muted); font-weight: 650; }
.modal-box footer { display: flex; justify-content: flex-end; gap: 8px; margin-top: 4px; }

/* drawer */
.drawer-overlay { position: fixed; inset: 0; z-index: 300; display: flex; justify-content: flex-end; background: var(--overlay); backdrop-filter: blur(6px); }
.drawer { width: min(480px, 100%); height: 100%; display: flex; flex-direction: column; border-left: 1px solid var(--border); background: var(--bg); box-shadow: -20px 0 40px rgba(0,0,0,.15); }
.drawer header { display: flex; align-items: center; justify-content: space-between; height: 56px; padding: 0 18px; border-bottom: 1px solid var(--border); flex-shrink: 0; }
.drawer header h3 { margin: 0; font-size: 15px; color: var(--text); }
.drawer header button { width: 30px; height: 30px; display: grid; place-items: center; border: 1px solid var(--border); border-radius: 6px; background: var(--surface); color: var(--muted); cursor: pointer; }
.drawer-form { flex: 1; overflow-y: auto; padding: 18px; display: grid; gap: 12px; align-content: start; }
.drawer-form label { display: grid; gap: 4px; font-size: 11px; color: var(--muted); font-weight: 650; }
.drawer-form footer { position: sticky; bottom: -18px; display: flex; justify-content: flex-end; gap: 8px; padding: 14px 0 0; background: linear-gradient(transparent, var(--bg) 28%); }
.row2 { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.tag-pick { display: flex; flex-wrap: wrap; gap: 5px; }
.tag-pick button { min-height: 28px; padding: 0 10px; border: 1px solid var(--border); border-radius: 5px; background: var(--surface); color: var(--muted); cursor: pointer; font-size: 11px; transition: all .16s; }
.tag-pick button:hover { border-color: var(--border-strong); }
.tag-pick button.on { border-color: var(--primary-soft); background: color-mix(in srgb, var(--primary) 12%, transparent); color: var(--primary-soft); }
</style>
