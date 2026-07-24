<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">岗位管理</h2>
        <p class="page-subtitle">岗位编码、所属部门、角色绑定与启用状态</p>
      </div>
      <div class="toolbar">
        <button class="btn" :disabled="loading" @click="load"><RefreshCw class="icon" />{{ loading ? '刷新中' : '刷新' }}</button>
        <button class="btn primary" @click="openCreate"><Plus class="icon" />新增</button>
      </div>
    </div>

    <p v-if="error" class="error-banner">{{ error }}</p>
    <p v-if="message" class="success-banner">{{ message }}</p>

    <section class="panel panel-pad">
      <div class="table-wrap">
        <table class="data-table">
          <thead><tr><th>岗位名称</th><th>编码</th><th>所属部门</th><th>排序</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <SkeletonTableRows v-if="loading && rows.length === 0" :columns="6" />
            <tr v-for="row in rows" :key="row.id">
              <td>{{ row.positionName }}</td>
              <td>{{ row.positionCode }}</td>
              <td>{{ row.deptName || deptName(row.deptId) }}</td>
              <td>{{ row.sortOrder ?? 0 }}</td>
              <td><StatusPill :value="row.status" /></td>
              <td><div class="row-actions">
                <button class="btn icon-btn" aria-label="编辑" @click="openEdit(row)"><Pencil class="icon" /></button>
                <button class="btn icon-btn danger" aria-label="删除" @click="remove(row)"><Trash2 class="icon" /></button>
              </div></td>
            </tr>
            <tr v-if="!loading && rows.length === 0"><td colspan="6"><div class="empty">暂无岗位数据</div></td></tr>
          </tbody>
        </table>
      </div>
    </section>

    <ModalDialog v-model="dialogOpen" :title="editing?.id ? '编辑岗位' : '新增岗位'">
      <div class="form-grid">
        <label class="form-item"><span class="form-label">岗位名称</span><input v-model="form.positionName" class="field" /></label>
        <label class="form-item"><span class="form-label">岗位编码</span><input v-model="form.positionCode" class="field" /></label>
        <label class="form-item"><span class="form-label">所属部门</span><select v-model.number="form.deptId" class="select"><option v-for="dept in flatDepts" :key="dept.id" :value="dept.id">{{ dept.deptName }}</option></select></label>
        <label class="form-item"><span class="form-label">排序</span><input v-model.number="form.sortOrder" class="field" type="number" /></label>
        <label class="form-item"><span class="form-label">状态</span><select v-model.number="form.status" class="select"><option :value="1">启用</option><option :value="0">停用</option></select></label>
      </div>
      <div class="role-section">
        <span class="form-label">绑定角色</span>
        <div class="role-checkboxes">
          <label v-for="role in allRoles" :key="role.id" class="checkbox-label">
            <input type="checkbox" :value="role.id" v-model="selectedRoleIds" />
            {{ role.roleName }}
          </label>
          <p v-if="allRoles.length === 0" class="hint">暂无可用角色</p>
        </div>
      </div>
      <template #footer><button class="btn" @click="dialogOpen = false">取消</button><button class="btn primary" :disabled="saving" @click="save">{{ saving ? '保存中' : '保存' }}</button></template>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { Pencil, Plus, RefreshCw, Trash2 } from 'lucide-vue-next'
import { computed, onMounted, reactive, ref } from 'vue'
import ModalDialog from '@/components/ModalDialog.vue'
import SkeletonTableRows from '@/components/SkeletonTableRows.vue'
import StatusPill from '@/components/StatusPill.vue'
import { systemApi } from '@/api/services'
import type { DeptNode, Position, Role } from '@/api/types'

const rows = ref<Position[]>([])
const depts = ref<DeptNode[]>([])
const allRoles = ref<Role[]>([])
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const message = ref('')
const dialogOpen = ref(false)
const editing = ref<Position | null>(null)
const form = reactive<Partial<Position>>({})
const selectedRoleIds = ref<number[]>([])
const flatDepts = computed(() => flatten(depts.value))
const flatten = (nodes: DeptNode[]): DeptNode[] => nodes.flatMap((node) => [node, ...flatten(node.children || [])])
const deptName = (id?: number) => flatDepts.value.find((item) => item.id === id)?.deptName || '-'
const pageRows = <T,>(page: { records?: T[]; list?: T[] }) => page.records || page.list || []

function positionPayload() {
  return {
    positionName: form.positionName,
    positionCode: form.positionCode,
    deptId: form.deptId,
    sortOrder: form.sortOrder ?? 0,
    status: form.status ?? 1,
    roleIds: selectedRoleIds.value
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [positionResult, deptResult, roleResult] = await Promise.all([
      systemApi.positions({ pageNum: 1, pageSize: 100 }),
      systemApi.depts(),
      systemApi.roles({ pageNum: 1, pageSize: 100 })
    ])
    rows.value = pageRows(positionResult)
    depts.value = deptResult
    allRoles.value = pageRows(roleResult).filter((r) => r.status === 1)
  } catch (err) {
    error.value = err instanceof Error ? err.message : '岗位数据加载失败'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = null
  Object.assign(form, { positionName: '', positionCode: '', deptId: flatDepts.value[0]?.id, sortOrder: 0, status: 1 })
  selectedRoleIds.value = []
  dialogOpen.value = true
}

async function openEdit(row: Position) {
  editing.value = row
  Object.assign(form, { positionName: row.positionName, positionCode: row.positionCode, deptId: row.deptId, sortOrder: row.sortOrder ?? 0, status: row.status ?? 1 })
  try {
    selectedRoleIds.value = await systemApi.positionRoles(row.id)
  } catch {
    selectedRoleIds.value = []
  }
  dialogOpen.value = true
}

async function save() {
  if (selectedRoleIds.value.length === 0) {
    error.value = '岗位必须绑定至少一个角色'
    return
  }
  saving.value = true
  error.value = ''
  message.value = ''
  try {
    if (editing.value?.id) {
      await systemApi.updatePosition(editing.value.id, positionPayload())
      message.value = '岗位已更新'
    } else {
      await systemApi.addPosition(positionPayload())
      message.value = '岗位已新增'
    }
    dialogOpen.value = false
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '岗位保存失败'
  } finally {
    saving.value = false
  }
}

async function remove(row: Position) {
  error.value = ''
  message.value = ''
  try {
    await systemApi.deletePosition(row.id)
    message.value = '岗位已删除'
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '岗位删除失败'
  }
}

onMounted(load)
</script>

<style scoped>
.role-section {
  margin-top: 16px;
}

.role-checkboxes {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-top: 8px;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  cursor: pointer;
}

.hint {
  color: var(--muted);
  font-size: 13px;
  margin: 0;
}
</style>
