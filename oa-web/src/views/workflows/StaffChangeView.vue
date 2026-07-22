<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">人事变动</h2>
        <p class="page-subtitle">入职、转正、调岗、离职记录</p>
      </div>
      <div class="toolbar">
        <button class="btn" :disabled="loading" @click="load"><RefreshCw class="icon" />{{ loading ? '刷新中' : '刷新' }}</button>
        <button class="btn primary" @click="openCreate"><Plus class="icon" />新增</button>
      </div>
    </div>

    <p v-if="error" class="error-banner">{{ error }}</p>
    <p v-if="message" class="success-banner">{{ message }}</p>

    <section class="panel panel-pad">
      <div v-if="!loading && rows.length === 0" class="empty">暂无人事变动记录</div>
      <div v-else class="timeline">
        <article v-for="item in rows" :key="item.id" class="timeline-item">
          <span class="dot" />
          <div>
            <strong>{{ personName(item) }} · {{ changeTypeText(item.changeType) }}</strong>
            <p>{{ changeDesc(item) }}</p>
          </div>
          <span>{{ item.changeDate || item.createTime || '-' }}</span>
          <div class="row-actions">
            <button class="btn icon-btn" aria-label="编辑" @click="openEdit(item)"><Pencil class="icon" /></button>
            <button class="btn icon-btn danger" aria-label="删除" @click="remove(item)"><Trash2 class="icon" /></button>
          </div>
        </article>
      </div>
    </section>

    <ModalDialog v-model="dialogOpen" :title="editing?.id ? '编辑人事变动' : '新增人事变动'">
      <div class="form-grid">
        <label class="form-item"><span class="form-label">员工 ID</span><input v-model.number="form.userId" class="field" type="number" /></label>
        <label class="form-item"><span class="form-label">类型</span><select v-model.number="form.changeType" class="select"><option :value="1">入职</option><option :value="2">转正</option><option :value="3">调岗</option><option :value="4">离职</option></select></label>
        <label class="form-item"><span class="form-label">日期</span><input v-model="form.changeDate" class="field" type="date" /></label>
        <label class="form-item"><span class="form-label">目标部门 ID</span><input v-model.number="form.toDeptId" class="field" type="number" /></label>
        <label class="form-item"><span class="form-label">目标岗位 ID</span><input v-model.number="form.toPositionId" class="field" type="number" /></label>
        <label class="form-item full"><span class="form-label">备注</span><textarea v-model="form.remark" class="textarea" /></label>
      </div>
      <template #footer><button class="btn" @click="dialogOpen = false">取消</button><button class="btn primary" :disabled="saving" @click="save">{{ saving ? '保存中' : '保存' }}</button></template>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { Pencil, Plus, RefreshCw, Trash2 } from 'lucide-vue-next'
import { onMounted, reactive, ref } from 'vue'
import ModalDialog from '@/components/ModalDialog.vue'
import { assetApi } from '@/api/services'
import type { StaffChange } from '@/api/types'

const rows = ref<StaffChange[]>([])
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const message = ref('')
const dialogOpen = ref(false)
const editing = ref<StaffChange | null>(null)
const form = reactive<Partial<StaffChange>>({})
const pageRows = <T,>(page: { records?: T[]; list?: T[] }) => page.records || page.list || []
const changeTypeText = (type?: number) => ({ 1: '入职', 2: '转正', 3: '调岗', 4: '离职' }[type ?? 1])

function personName(item: StaffChange) {
  return item.realName || item.userName || `员工 ${item.userId}`
}

function changeDesc(item: StaffChange) {
  const targetDept = item.toDeptName || (item.toDeptId ? `部门 ${item.toDeptId}` : '')
  const targetPosition = item.toPositionName || (item.toPositionId ? `岗位 ${item.toPositionId}` : '')
  return item.remark || [targetDept, targetPosition].filter(Boolean).join(' / ') || '-'
}

function payload() {
  return {
    userId: form.userId,
    changeType: form.changeType,
    changeDate: form.changeDate,
    toDeptId: form.toDeptId,
    toPositionId: form.toPositionId,
    remark: form.remark
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const result = await assetApi.staffChanges({ pageNum: 1, pageSize: 50 })
    rows.value = pageRows(result)
  } catch (err) {
    error.value = err instanceof Error ? err.message : '人事变动数据加载失败'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = null
  Object.assign(form, { userId: undefined, changeType: 1, changeDate: '', toDeptId: undefined, toPositionId: undefined, remark: '' })
  dialogOpen.value = true
}

function openEdit(item: StaffChange) {
  editing.value = item
  Object.assign(form, { userId: item.userId, changeType: item.changeType, changeDate: item.changeDate || '', toDeptId: item.toDeptId, toPositionId: item.toPositionId, remark: item.remark || '' })
  dialogOpen.value = true
}

async function save() {
  saving.value = true
  error.value = ''
  message.value = ''
  try {
    if (editing.value?.id) {
      await assetApi.updateStaffChange(editing.value.id, payload())
      message.value = '人事变动已更新'
    } else {
      await assetApi.addStaffChange(payload())
      message.value = '人事变动已新增'
    }
    dialogOpen.value = false
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '人事变动保存失败'
  } finally {
    saving.value = false
  }
}

async function remove(item: StaffChange) {
  error.value = ''
  message.value = ''
  try {
    await assetApi.deleteStaffChange(item.id)
    message.value = '人事变动已删除'
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '人事变动删除失败'
  }
}

onMounted(load)
</script>

<style scoped>
.timeline {
  display: grid;
  gap: 12px;
}

.timeline-item {
  min-height: 64px;
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr) 130px auto;
  align-items: center;
  gap: 12px;
  padding: 0 12px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--surface-soft);
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--primary);
}

.timeline-item p,
.timeline-item > span:last-of-type {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.full {
  grid-column: 1 / -1;
}

@media (max-width: 760px) {
  .timeline-item {
    grid-template-columns: 18px minmax(0, 1fr);
    padding: 12px;
  }

  .timeline-item .row-actions {
    grid-column: 2;
  }
}
</style>