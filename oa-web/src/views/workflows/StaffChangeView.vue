<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">人事变动</h2>
        <p class="page-subtitle">入职、转正、调岗、离职记录</p>
      </div>
      <div class="toolbar">
        <select v-model.number="filterType" class="select" @change="fetchData(1)">
          <option :value="undefined">全部类型</option>
          <option :value="1">入职</option>
          <option :value="2">转正</option>
          <option :value="3">调岗</option>
          <option :value="4">离职</option>
        </select>
        <button class="btn primary" @click="openCreate"><Plus class="icon" />新增</button>
      </div>
    </div>

    <section class="panel panel-pad">
      <div v-if="loading" class="empty">加载中...</div>
      <div v-else-if="rows.length === 0" class="empty">暂无记录</div>
      <div v-else class="table-wrap">
        <table class="data-table">
          <thead>
            <tr><th>员工ID</th><th>变动类型</th><th>变动前部门</th><th>变动后部门</th><th>变动日期</th><th>备注</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="row in rows" :key="row.id">
              <td>{{ row.userId }}</td>
              <td><StatusPill :value="row.changeType" :map="{1:'入职',2:'转正',3:'调岗',4:'离职'}" :tone-map="{1:'success',2:'info',3:'warn',4:'danger'}" /></td>
              <td>{{ row.beforeDept ?? '-' }}</td>
              <td>{{ row.afterDept ?? '-' }}</td>
              <td>{{ row.changeDate ?? '-' }}</td>
              <td>{{ row.remark ?? '-' }}</td>
              <td>
                <div class="row-actions">
                  <button class="btn icon-btn" @click="openEdit(row)"><Pencil class="icon" /></button>
                  <button class="btn icon-btn danger" @click="handleDelete(row.id)"><Trash2 class="icon" /></button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="total > pageSize" class="pager">
        <button class="btn ghost" :disabled="pageNum <= 1" @click="fetchData(pageNum - 1)">上一页</button>
        <span>{{ pageNum }} / {{ Math.ceil(total / pageSize) }}</span>
        <button class="btn ghost" :disabled="pageNum * pageSize >= total" @click="fetchData(pageNum + 1)">下一页</button>
      </div>
    </section>

    <ModalDialog v-model="dialogOpen" :title="editing ? '编辑变动' : '新增变动'">
      <div class="form-grid">
        <label class="form-item"><span>员工ID</span><input v-model.number="form.userId" class="field" type="number" /></label>
        <label class="form-item"><span>类型</span><select v-model.number="form.changeType" class="select"><option :value="1">入职</option><option :value="2">转正</option><option :value="3">调岗</option><option :value="4">离职</option></select></label>
        <label class="form-item"><span>变动前部门</span><input v-model.number="form.beforeDept" class="field" type="number" /></label>
        <label class="form-item"><span>变动后部门</span><input v-model.number="form.afterDept" class="field" type="number" /></label>
        <label class="form-item"><span>变动前岗位</span><input v-model.number="form.beforePosition" class="field" type="number" /></label>
        <label class="form-item"><span>变动后岗位</span><input v-model.number="form.afterPosition" class="field" type="number" /></label>
        <label class="form-item"><span>变动日期</span><input v-model="form.changeDate" class="field" type="date" /></label>
        <label class="form-item"><span>备注</span><input v-model="form.remark" class="field" /></label>
      </div>
      <template #footer>
        <button class="btn" @click="dialogOpen = false">取消</button>
        <button class="btn primary" :disabled="saving" @click="handleSave">{{ saving ? '保存中' : '保存' }}</button>
      </template>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Pencil, Plus, Trash2 } from 'lucide-vue-next'
import { assetApi } from '@/api/services'
import type { StaffChange } from '@/api/types'
import ModalDialog from '@/components/ModalDialog.vue'
import StatusPill from '@/components/StatusPill.vue'

const rows = ref<StaffChange[]>([])
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogOpen = ref(false)
const editing = ref<StaffChange | null>(null)
const filterType = ref<number | undefined>(undefined)

const form = ref<Partial<StaffChange>>({})

const empty = computed(() => rows.value.length === 0)

onMounted(() => fetchData(1))

function fetchData(page: number) {
  loading.value = true
  pageNum.value = page
  const params: Record<string, unknown> = { pageNum: page, pageSize }
  if (filterType.value != null) params.changeType = filterType.value
  assetApi.staffChanges(params)
    .then(res => { rows.value = res.records ?? []; total.value = res.total })
    .finally(() => loading.value = false)
}

function openCreate() {
  editing.value = null
  form.value = { changeType: 1, changeDate: new Date().toISOString().slice(0, 10) }
  dialogOpen.value = true
}

function openEdit(row: StaffChange) {
  editing.value = row
  form.value = { ...row }
  dialogOpen.value = true
}

async function handleSave() {
  saving.value = true
  try {
    if (editing.value) {
      await assetApi.updateStaffChange(editing.value.id, form.value)
    } else {
      await assetApi.createStaffChange(form.value)
    }
    dialogOpen.value = false
    fetchData(pageNum.value)
  } finally { saving.value = false }
}

async function handleDelete(id: number) {
  if (!confirm('确定删除？')) return
  await assetApi.deleteStaffChange(id)
  fetchData(pageNum.value)
}
</script>

<style scoped>
.empty { text-align: center; padding: 40px; color: var(--muted); }
.pager { display: flex; align-items: center; gap: 12px; margin-top: 12px; justify-content: center; }
.danger { color: var(--danger); }
</style>
