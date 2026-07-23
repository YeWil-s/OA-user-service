<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">公告列表</h2>
        <p class="page-subtitle">公司公告、部门通知与系统通知</p>
      </div>
      <div class="toolbar">
        <button class="btn" :disabled="loading" @click="load"><RefreshCw class="icon" />{{ loading ? '刷新中' : '刷新' }}</button>
        <button class="btn primary" @click="openCreate"><Megaphone class="icon" />发布</button>
      </div>
    </div>

    <p v-if="error" class="error-banner">{{ error }}</p>
    <p v-if="message" class="success-banner">{{ message }}</p>

    <section class="panel panel-pad">
      <div class="toolbar filters">
        <input v-model.trim="keyword" class="field filter-input" placeholder="标题/内容关键词" @keyup.enter="load" />
        <select v-model="noticeTypeFilter" class="select filter-input">
          <option value="">全部类型</option>
          <option value="1">公司公告</option>
          <option value="2">部门通知</option>
          <option value="3">系统通知</option>
        </select>
        <button class="btn" @click="load">查询</button>
      </div>
      <div class="table-wrap">
        <table class="data-table">
          <thead><tr><th>标题</th><th>类型</th><th>范围</th><th>发布时间</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <SkeletonTableRows v-if="loading && rows.length === 0" :columns="6" />
            <tr v-for="row in rows" :key="row.id">
              <td>{{ row.title }}</td>
              <td>{{ noticeType(row.noticeType) }}</td>
              <td>{{ targetType(row.targetType) }}</td>
              <td>{{ row.createTime || '-' }}</td>
              <td><StatusPill :value="row.status" :map="{ '1': '已发布', '0': '草稿', '2': '已下架' }" :tone-map="{ '1': 'success', '0': 'warn', '2': 'danger' }" /></td>
              <td><div class="row-actions">
                <button class="btn icon-btn" aria-label="编辑" @click="openEdit(row)"><Pencil class="icon" /></button>
                <button class="btn icon-btn danger" aria-label="下架" @click="offline(row)"><ArchiveX class="icon" /></button>
              </div></td>
            </tr>
            <tr v-if="!loading && rows.length === 0"><td colspan="6"><div class="empty">暂无公告数据</div></td></tr>
          </tbody>
        </table>
      </div>
    </section>

    <ModalDialog v-model="dialogOpen" :title="editing?.id ? '编辑公告' : '发布公告'">
      <div class="form-grid">
        <label class="form-item full"><span class="form-label">标题</span><input v-model="form.title" class="field" /></label>
        <label class="form-item"><span class="form-label">类型</span><select v-model.number="form.noticeType" class="select"><option :value="1">公司公告</option><option :value="2">部门通知</option><option :value="3">系统通知</option></select></label>
        <label class="form-item"><span class="form-label">范围</span><select v-model.number="form.targetType" class="select"><option :value="1">全公司</option><option :value="2">指定部门</option><option :value="3">指定人员</option></select></label>
        <label v-if="form.targetType === 2" class="form-item full"><span class="form-label">目标部门</span>
          <div class="dept-pick-list">
            <label v-for="d in flatDepts()" :key="d.id" class="dept-check">
              <input type="checkbox" :checked="selectedDeptIds.includes(d.id)" @change="toggleDept(d.id)" />
              <span :style="{ paddingLeft: (d.parentId ? 16 : 0) + 'px' }">{{ d.deptName }}</span>
            </label>
          </div>
        </label>
        <label v-if="form.targetType === 3" class="form-item"><span class="form-label">目标人员 ID</span><input v-model="form.targetIdsText" class="field" placeholder="多个 ID 用英文逗号分隔" /></label>
        <label class="form-item"><span class="form-label">状态</span><select v-model.number="form.status" class="select"><option :value="1">发布</option><option :value="0">草稿</option></select></label>
        <label class="form-item full"><span class="form-label">内容</span><textarea v-model="form.content" class="textarea" /></label>
      </div>
      <template #footer><button class="btn" @click="dialogOpen = false">取消</button><button class="btn primary" :disabled="saving" @click="save">{{ saving ? '保存中' : '保存' }}</button></template>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { ArchiveX, Megaphone, Pencil, RefreshCw } from 'lucide-vue-next'
import { onMounted, reactive, ref } from 'vue'
import ModalDialog from '@/components/ModalDialog.vue'
import SkeletonTableRows from '@/components/SkeletonTableRows.vue'
import StatusPill from '@/components/StatusPill.vue'
import { noticeApi, systemApi } from '@/api/services'
import type { DeptNode, Notice } from '@/api/types'

type NoticeForm = {
  title: string
  content: string
  noticeType: number
  targetType: number
  targetIdsText: string
  status: number
}

const rows = ref<Notice[]>([])
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const message = ref('')
const dialogOpen = ref(false)
const editing = ref<Notice | null>(null)
const keyword = ref('')
const noticeTypeFilter = ref('')
const deptList = ref<DeptNode[]>([])
const selectedDeptIds = ref<number[]>([])

const form = reactive<NoticeForm>({ title: '', content: '', noticeType: 1, targetType: 1, targetIdsText: '', status: 1 })
const noticeType = (value?: number) => ({ 1: '公司公告', 2: '部门通知', 3: '系统通知' }[value ?? 1])
const targetType = (value?: number) => ({ 1: '全公司', 2: '指定部门', 3: '指定人员' }[value ?? 1])
const pageRows = <T,>(page: { records?: T[]; list?: T[] }) => page.records || page.list || []

function stringifyIds(ids?: number[]) {
  return Array.isArray(ids) ? ids.join(',') : ''
}

function parseIds(value: string) {
  return value
    .split(',')
    .map((item) => Number(item.trim()))
    .filter((item) => Number.isFinite(item))
}

function flattenDepts(tree: DeptNode[]): DeptNode[] {
  const result: DeptNode[] = []
  for (const d of tree) {
    result.push(d)
    if (d.children) result.push(...flattenDepts(d.children))
  }
  return result
}

function flatDepts() { return flattenDepts(deptList.value) }

function toggleDept(id: number) {
  const idx = selectedDeptIds.value.indexOf(id)
  if (idx >= 0) selectedDeptIds.value.splice(idx, 1)
  else selectedDeptIds.value.push(id)
}

function noticePayload(): Partial<Notice> {
  let targetIds: number[] = []
  if (form.targetType === 2) {
    targetIds = [...selectedDeptIds.value]
  } else if (form.targetType === 3) {
    targetIds = parseIds(form.targetIdsText)
  }
  return {
    title: form.title,
    content: form.content,
    noticeType: form.noticeType,
    targetType: form.targetType,
    targetIds,
    status: form.status
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const result = await noticeApi.notices({ current: 1, size: 20, keyword: keyword.value, noticeType: noticeTypeFilter.value })
    rows.value = pageRows(result)
  } catch (err) {
    error.value = err instanceof Error ? err.message : '公告数据加载失败'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = null
  Object.assign(form, { title: '', noticeType: 1, targetType: 1, targetIdsText: '', status: 1, content: '' })
  selectedDeptIds.value = []
  dialogOpen.value = true
}

async function openEdit(row: Notice) {
  error.value = ''
  try {
    editing.value = row
    const detail = await noticeApi.detail(row.id)
    Object.assign(form, {
      title: detail.title,
      noticeType: detail.noticeType ?? 1,
      targetType: detail.targetType ?? 1,
      targetIdsText: stringifyIds(detail.targetIds),
      status: detail.status ?? 1,
      content: detail.content || ''
    })
    selectedDeptIds.value = detail.targetType === 2 ? (detail.targetIds || []) : []
    dialogOpen.value = true
  } catch (err) {
    error.value = err instanceof Error ? err.message : '公告详情加载失败'
  }
}

async function save() {
  saving.value = true
  error.value = ''
  message.value = ''
  try {
    if (editing.value?.id) {
      await noticeApi.update(editing.value.id, noticePayload())
      message.value = '公告已更新'
    } else {
      await noticeApi.publish(noticePayload())
      message.value = '公告已发布'
    }
    dialogOpen.value = false
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '公告保存失败'
  } finally {
    saving.value = false
  }
}

async function offline(row: Notice) {
  error.value = ''
  message.value = ''
  try {
    await noticeApi.offline(row.id)
    message.value = '公告已下架'
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '公告下架失败'
  }
}

async function loadDepts() {
  try { deptList.value = await systemApi.depts() } catch { deptList.value = [] }
}

onMounted(() => { load(); loadDepts() })
</script>

<style scoped>
.full {
  grid-column: 1 / -1;
}

.filters {
  margin-bottom: 14px;
}

.filter-input { width: 190px; }
.dept-pick-list { max-height: 200px; overflow-y: auto; border: 1px solid var(--border-color); border-radius: 8px; padding: 8px; }
.dept-check { display: flex; align-items: center; gap: 6px; padding: 4px 8px; border-radius: 4px; font-size: 13px; cursor: pointer; }
.dept-check:hover { background: var(--bg-subtle); }
.dept-check input { accent-color: var(--primary); }
</style>