<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">员工管理</h2>
        <p class="page-subtitle">员工档案、部门岗位、账号状态维护</p>
      </div>
      <div class="toolbar">
        <button class="btn" type="button" :disabled="loading" @click="load">
          <RefreshCw class="icon" />
          {{ loading ? '刷新中' : '刷新' }}
        </button>
        <button class="btn primary" type="button" @click="openCreate">
          <UserPlus class="icon" />
          新增
        </button>
      </div>
    </div>

    <p v-if="error" class="error-banner">{{ error }}</p>
    <p v-if="message" class="success-banner">{{ message }}</p>

    <section class="panel panel-pad">
      <div class="toolbar filters">
        <input v-model.trim="query.realName" class="field filter-input" placeholder="姓名" @keyup.enter="load" />
        <select v-model="query.status" class="select filter-input">
          <option value="">全部状态</option>
          <option value="1">在职</option>
          <option value="0">离职</option>
          <option value="2">冻结</option>
        </select>
        <button class="btn" type="button" @click="load">查询</button>
      </div>

      <div class="table-wrap">
        <table class="data-table">
          <thead>
            <tr>
              <th>工号</th>
              <th>姓名</th>
              <th>手机号</th>
              <th>部门</th>
              <th>岗位</th>
              <th>入职日期</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <SkeletonTableRows v-if="loading && rows.length === 0" :columns="8" />
            <tr v-for="row in rows" :key="row.id">
              <td>{{ row.username }}</td>
              <td>{{ row.realName }}</td>
              <td>{{ row.phone || '-' }}</td>
              <td>{{ row.deptName || deptName(row.deptId) }}</td>
              <td>{{ row.positionName || positionName(row.positionId) }}</td>
              <td>{{ row.entryDate || '-' }}</td>
              <td>
                <StatusPill
                  :value="row.status"
                  :map="{ '1': '在职', '0': '离职', '2': '冻结' }"
                  :tone-map="{ '1': 'success', '0': 'danger', '2': 'warn' }"
                />
              </td>
              <td>
                <div class="row-actions">
                  <button class="btn icon-btn" type="button" aria-label="编辑" @click="openEdit(row)">
                    <Pencil class="icon" />
                  </button>
                  <button class="btn icon-btn" type="button" aria-label="重置密码" @click="resetPassword(row)">
                    <KeyRound class="icon" />
                  </button>
                  <button class="btn icon-btn danger" type="button" aria-label="删除" @click="remove(row)">
                    <Trash2 class="icon" />
                  </button>
                </div>
              </td>
            </tr>
            <tr v-if="!loading && rows.length === 0">
              <td colspan="8"><div class="empty">暂无员工数据</div></td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <ModalDialog v-model="dialogOpen" :title="editing?.id ? '编辑员工' : '新增员工'">
      <div class="form-grid">
        <label class="form-item">
          <span class="form-label">工号</span>
          <input v-model="form.username" class="field" />
        </label>
        <label class="form-item">
          <span class="form-label">姓名</span>
          <input v-model="form.realName" class="field" />
        </label>
        <label class="form-item">
          <span class="form-label">初始密码</span>
          <input v-model="form.password" class="field" type="password" :placeholder="editing?.id ? '不修改请留空' : '请输入初始密码'" />
        </label>
        <label class="form-item">
          <span class="form-label">手机号</span>
          <input v-model="form.phone" class="field" />
        </label>
        <label class="form-item">
          <span class="form-label">邮箱</span>
          <input v-model="form.email" class="field" />
        </label>
        <label class="form-item">
          <span class="form-label">性别</span>
          <select v-model.number="form.gender" class="select">
            <option :value="1">男</option>
            <option :value="0">女</option>
          </select>
        </label>
        <label class="form-item">
          <span class="form-label">部门</span>
          <select v-model.number="form.deptId" class="select">
            <option v-for="dept in flatDepts" :key="dept.id" :value="dept.id">{{ dept.deptName }}</option>
          </select>
        </label>
        <label class="form-item">
          <span class="form-label">岗位</span>
          <select v-model.number="form.positionId" class="select">
            <option v-for="position in positions" :key="position.id" :value="position.id">{{ position.positionName }}</option>
          </select>
        </label>
        <label class="form-item">
          <span class="form-label">入职日期</span>
          <input v-model="form.entryDate" class="field" type="date" />
        </label>
        <label class="form-item">
          <span class="form-label">状态</span>
          <select v-model.number="form.status" class="select">
            <option :value="1">在职</option>
            <option :value="0">离职</option>
            <option :value="2">冻结</option>
          </select>
        </label>
      </div>
      <template #footer>
        <button class="btn" type="button" @click="dialogOpen = false">取消</button>
        <button class="btn primary" type="button" :disabled="saving" @click="save">{{ saving ? '保存中' : '保存' }}</button>
      </template>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { KeyRound, Pencil, RefreshCw, Trash2, UserPlus } from 'lucide-vue-next'
import { computed, onMounted, reactive, ref } from 'vue'
import ModalDialog from '@/components/ModalDialog.vue'
import SkeletonTableRows from '@/components/SkeletonTableRows.vue'
import StatusPill from '@/components/StatusPill.vue'
import { systemApi } from '@/api/services'
import type { DeptNode, Employee, Position } from '@/api/types'

const rows = ref<Employee[]>([])
const depts = ref<DeptNode[]>([])
const positions = ref<Position[]>([])
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const message = ref('')
const dialogOpen = ref(false)
const editing = ref<Employee | null>(null)
const query = reactive({ realName: '', status: '' })
const form = reactive<Partial<Employee> & { password?: string }>({})

const flatDepts = computed(() => flatten(depts.value))

function flatten(nodes: DeptNode[]): DeptNode[] {
  return nodes.flatMap((node) => [node, ...flatten(node.children || [])])
}

function deptName(id?: number) {
  return flatDepts.value.find((item) => item.id === id)?.deptName || '-'
}

function positionName(id?: number) {
  return positions.value.find((item) => item.id === id)?.positionName || '-'
}

function pageRows<T>(page: { records?: T[]; list?: T[] }) {
  return page.records || page.list || []
}

function employeePayload() {
  const payload: Partial<Employee> = {
    username: form.username,
    realName: form.realName,
    phone: form.phone,
    email: form.email,
    gender: form.gender,
    deptId: form.deptId,
    positionId: form.positionId,
    entryDate: form.entryDate,
    status: form.status
  }
  if (form.password) {
    payload.password = form.password
  }
  return payload
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const [employeeResult, deptResult, positionResult] = await Promise.all([
      systemApi.employees({ pageNum: 1, pageSize: 20, realName: query.realName, status: query.status }),
      systemApi.depts(),
      systemApi.positions({ pageNum: 1, pageSize: 100 })
    ])
    rows.value = pageRows(employeeResult)
    depts.value = deptResult
    positions.value = pageRows(positionResult)
  } catch (err) {
    error.value = err instanceof Error ? err.message : '员工数据加载失败'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  editing.value = null
  Object.assign(form, { username: '', realName: '', password: '', phone: '', email: '', gender: 1, deptId: flatDepts.value[0]?.id, positionId: positions.value[0]?.id, entryDate: '', status: 1 })
  dialogOpen.value = true
}

function openEdit(row: Employee) {
  editing.value = row
  Object.assign(form, row, { password: '' })
  dialogOpen.value = true
}

async function save() {
  saving.value = true
  error.value = ''
  message.value = ''
  try {
    if (editing.value?.id) {
      await systemApi.updateEmployee(editing.value.id, employeePayload())
      message.value = '员工信息已更新'
    } else {
      await systemApi.addEmployee(employeePayload())
      message.value = '员工已新增'
    }
    dialogOpen.value = false
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '员工保存失败'
  } finally {
    saving.value = false
  }
}

async function remove(row: Employee) {
  error.value = ''
  message.value = ''
  try {
    await systemApi.deleteEmployee(row.id)
    message.value = '员工已删除'
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '员工删除失败'
  }
}

async function resetPassword(row: Employee) {
  error.value = ''
  message.value = ''
  try {
    await systemApi.resetPassword(row.id)
    message.value = '密码已重置为默认密码'
  } catch (err) {
    error.value = err instanceof Error ? err.message : '密码重置失败'
  }
}

onMounted(load)
</script>

<style scoped>
.filters {
  margin-bottom: 14px;
}

.filter-input {
  width: 180px;
}
</style>