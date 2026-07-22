<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">{{ title }}</h2>
        <p class="page-subtitle">{{ config.subtitle }}</p>
      </div>
      <div class="toolbar">
        <button class="btn" :disabled="loading" @click="load"><RefreshCw class="icon" />{{ loading ? '刷新中' : '刷新' }}</button>
        <button v-if="config.canCreate" class="btn primary" @click="openCreate"><Plus class="icon" />{{ config.createText || '新增' }}</button>
      </div>
    </div>

    <p v-if="error" class="error-banner">{{ error }}</p>
    <p v-if="message" class="success-banner">{{ message }}</p>

    <section class="panel panel-pad">
      <div class="toolbar filters">
        <input v-model.trim="keyword" class="field filter-input" placeholder="关键词" @keyup.enter="load" />
        <input v-if="moduleKey === 'attendanceRecords'" v-model="month" class="field filter-input" type="month" />
        <select v-if="statusOptions.length" v-model="statusFilter" class="select filter-input">
          <option value="">全部状态</option>
          <option v-for="option in statusOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
        </select>
        <button class="btn" @click="load">查询</button>
      </div>
      <div class="table-wrap">
        <table class="data-table">
          <thead><tr><th v-for="column in config.columns" :key="column.key">{{ column.label }}</th><th v-if="hasActions">操作</th></tr></thead>
          <tbody>
            <SkeletonTableRows v-if="loading && filteredRows.length === 0" :columns="config.columns.length + (hasActions ? 1 : 0)" />
            <tr v-for="row in filteredRows" :key="row.id">
              <td v-for="column in config.columns" :key="column.key">{{ rowValue(row, column.key) }}</td>
              <td v-if="hasActions"><div class="row-actions">
                <button v-if="config.canView" class="btn icon-btn" aria-label="查看" @click="openDetail(row)"><Eye class="icon" /></button>
                <button v-if="config.canEdit" class="btn icon-btn" aria-label="编辑" @click="openEdit(row)"><Pencil class="icon" /></button>
                <button v-if="config.canDelete" class="btn icon-btn danger" aria-label="删除" @click="remove(row)"><Trash2 class="icon" /></button>
              </div></td>
            </tr>
            <tr v-if="!loading && filteredRows.length === 0">
              <td :colspan="config.columns.length + (hasActions ? 1 : 0)"><div class="empty">暂无数据</div></td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <ModalDialog v-model="dialogOpen" :title="editingId ? `编辑${title}` : `新增${title}`">
      <div v-if="moduleKey === 'shifts'" class="form-grid">
        <label class="form-item"><span class="form-label">班次名称</span><input v-model="shiftForm.shiftName" class="field" /></label>
        <label class="form-item"><span class="form-label">上班时间</span><input v-model="shiftForm.startTime" class="field" type="time" step="1" /></label>
        <label class="form-item"><span class="form-label">下班时间</span><input v-model="shiftForm.endTime" class="field" type="time" step="1" /></label>
        <label class="form-item"><span class="form-label">弹性开始</span><input v-model="shiftForm.flexStart" class="field" type="time" step="1" /></label>
        <label class="form-item"><span class="form-label">最晚正常打卡</span><input v-model="shiftForm.flexEnd" class="field" type="time" step="1" /></label>
        <label class="form-item"><span class="form-label">状态</span><select v-model.number="shiftForm.status" class="select"><option :value="1">启用</option><option :value="0">停用</option></select></label>
      </div>
      <div v-else-if="moduleKey === 'assets'" class="form-grid">
        <label class="form-item"><span class="form-label">资产编码</span><input v-model="assetForm.assetCode" class="field" /></label>
        <label class="form-item"><span class="form-label">资产名称</span><input v-model="assetForm.assetName" class="field" /></label>
        <label class="form-item"><span class="form-label">分类</span><select v-model="assetForm.category" class="select"><option :value="1">固定资产</option><option :value="2">办公用品</option><option :value="3">电子设备</option></select></label>
        <label class="form-item"><span class="form-label">状态</span><select v-model.number="assetForm.status" class="select"><option :value="1">可领用</option><option :value="2">已领用</option><option :value="0">已报废</option></select></label>
        <label class="form-item"><span class="form-label">品牌</span><input v-model="assetForm.brand" class="field" /></label>
        <label class="form-item"><span class="form-label">型号</span><input v-model="assetForm.model" class="field" /></label>
        <label class="form-item"><span class="form-label">购置日期</span><input v-model="assetForm.purchaseDate" class="field" type="date" /></label>
        <label class="form-item"><span class="form-label">购置金额</span><input v-model.number="assetForm.purchasePrice" class="field" type="number" /></label>
        <label class="form-item full"><span class="form-label">备注</span><textarea v-model="assetForm.remark" class="textarea" /></label>
      </div>
      <template #footer><button class="btn" @click="dialogOpen = false">取消</button><button class="btn primary" :disabled="saving" @click="save">{{ saving ? '保存中' : '保存' }}</button></template>
    </ModalDialog>

    <ModalDialog v-model="detailOpen" title="详情" width="720px">
      <pre class="detail-json">{{ detailText }}</pre>
      <template #footer><button class="btn primary" @click="detailOpen = false">关闭</button></template>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { Eye, Pencil, Plus, RefreshCw, Trash2 } from 'lucide-vue-next'
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ModalDialog from '@/components/ModalDialog.vue'
import SkeletonTableRows from '@/components/SkeletonTableRows.vue'
import { approvalApi, assetApi, attendanceApi } from '@/api/services'
import type { ApprovalApplication, Asset, AttendanceRecord, PageResult, Shift } from '@/api/types'

type ModuleKey = 'attendanceRecords' | 'shifts' | 'applications' | 'processedApplications' | 'assets'
type TableRow = Record<string, unknown> & { id: number }

type Config = {
  subtitle: string
  columns: Array<{ key: string; label: string }>
  canCreate?: boolean
  canEdit?: boolean
  canDelete?: boolean
  canView?: boolean
  createText?: string
}

const route = useRoute()
const router = useRouter()
const title = computed(() => String(route.meta.title || '业务管理'))
const moduleKey = computed<ModuleKey>(() => {
  if (route.name === 'approval-processed') return 'processedApplications'
  return String(route.meta.module || 'applications') as ModuleKey
})

function currentMonth() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

const rows = ref<TableRow[]>([])
const keyword = ref('')
const statusFilter = ref('')
const month = ref(currentMonth())
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const message = ref('')
const dialogOpen = ref(false)
const detailOpen = ref(false)
const detailText = ref('')
const editingId = ref<number | null>(null)
const shiftForm = reactive<Partial<Shift>>({})
const assetForm = reactive<Partial<Asset>>({})

const configs: Record<ModuleKey, Config> = {
  attendanceRecords: {
    subtitle: '个人、部门、全公司考勤记录查询',
    columns: [
      { key: 'realName', label: '姓名' },
      { key: 'deptName', label: '部门' },
      { key: 'recordDate', label: '日期' },
      { key: 'punchInTimeText', label: '上班' },
      { key: 'punchOutTimeText', label: '下班' },
      { key: 'statusLabel', label: '状态' }
    ],
    canView: true
  },
  shifts: {
    subtitle: '班次模板、弹性时间与分配规则',
    columns: [
      { key: 'shiftName', label: '班次' },
      { key: 'startTime', label: '上班' },
      { key: 'endTime', label: '下班' },
      { key: 'flexText', label: '弹性' },
      { key: 'statusText', label: '状态' }
    ],
    canCreate: true,
    canEdit: true,
    canDelete: true
  },
  applications: {
    subtitle: '我的申请单状态、时长与审批链路',
    columns: [
      { key: 'applicationNo', label: '单号' },
      { key: 'appTypeText', label: '类型' },
      { key: 'durationText', label: '时长' },
      { key: 'statusText', label: '状态' },
      { key: 'createTime', label: '提交时间' }
    ],
    canCreate: true,
    canView: true,
    createText: '提交申请'
  },
  processedApplications: {
    subtitle: '已经处理过的审批申请',
    columns: [
      { key: 'applicationNo', label: '单号' },
      { key: 'appTypeText', label: '类型' },
      { key: 'durationText', label: '时长' },
      { key: 'latestActionText', label: '处理结果' },
      { key: 'latestActionTime', label: '处理时间' }
    ],
    canView: true
  },
  assets: {
    subtitle: '资产登记、领用、归还与报废',
    columns: [
      { key: 'assetCodeText', label: '资产编码' },
      { key: 'assetNameText', label: '资产名称' },
      { key: 'categoryText', label: '分类' },
      { key: 'ownerText', label: '领用人' },
      { key: 'statusText', label: '状态' }
    ],
    canCreate: true,
    canEdit: true,
    canDelete: true,
    canView: true
  }
}

const config = computed(() => configs[moduleKey.value])
const hasActions = computed(() => Boolean(config.value.canView || config.value.canEdit || config.value.canDelete))
const statusOptions = computed(() => {
  if (moduleKey.value === 'applications') return [{ value: '1', label: '审批中' }, { value: '2', label: '已通过' }, { value: '3', label: '已驳回' }, { value: '4', label: '已撤销' }]
  if (moduleKey.value === 'assets') return [{ value: '1', label: '可领用' }, { value: '2', label: '已领用' }, { value: '0', label: '已报废' }]
  return []
})
const filteredRows = computed(() => {
  if (!keyword.value) return rows.value
  return rows.value.filter((row) => JSON.stringify(row).includes(keyword.value))
})

const pageRows = <T,>(page: PageResult<T>) => page.records || page.list || []
const appTypeText = (value?: number) => ({ 1: '请假', 2: '加班', 3: '外出' }[value ?? 1])
const appStatusText = (value?: number) => ({ 0: '草稿', 1: '审批中', 2: '已通过', 3: '已驳回', 4: '已撤销' }[value ?? 1])
const assetCategoryText = (value?: number | string) => ({ 1: '固定资产', 2: '办公用品', 3: '电子设备' }[Number(value)] || String(value || '-'))
const assetStatusText = (value?: number) => ({ 0: '已报废', 1: '可领用', 2: '已领用' }[value ?? 1])
const timeText = (value?: string | null) => value ? (value.includes('T') ? value.slice(11, 19) : value) : '-'

function rowValue(row: TableRow, key: string) {
  return row[key] ?? '-'
}

function mapAttendance(row: AttendanceRecord): TableRow {
  return {
    ...row,
    id: row.id,
    realName: row.realName || `用户 ${row.userId}`,
    deptName: row.deptName || '-',
    punchInTimeText: timeText(row.punchInTime),
    punchOutTimeText: timeText(row.punchOutTime),
    statusLabel: row.statusLabel || '-'
  }
}

function mapShift(row: Shift): TableRow {
  return {
    ...row,
    id: row.id,
    flexText: `${row.flexStart || '-'} / ${row.flexEnd || '-'}`,
    statusText: row.status === 0 ? '停用' : '启用'
  }
}

function mapApplication(row: ApprovalApplication): TableRow {
  return {
    ...row,
    id: row.id,
    appTypeText: row.appTypeText || appTypeText(row.appType),
    statusText: row.statusText || appStatusText(row.status),
    durationText: row.duration == null ? '-' : `${row.duration} 小时`,
    latestActionText: row.latestActionText || '-'
  }
}

function mapAsset(row: Asset): TableRow {
  return {
    ...row,
    id: row.id,
    assetCodeText: row.assetCode || row.code || '-',
    assetNameText: row.assetName || row.name || '-',
    categoryText: row.categoryName || assetCategoryText(row.category),
    ownerText: row.ownerName || row.userName || (row.userId ? `用户 ${row.userId}` : '-'),
    statusText: assetStatusText(row.status)
  }
}

async function load() {
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    if (moduleKey.value === 'attendanceRecords') {
      const result = await attendanceApi.allRecords({ month: month.value, pageNum: 1, pageSize: 100 })
      rows.value = pageRows(result).map(mapAttendance)
    } else if (moduleKey.value === 'shifts') {
      const result = await attendanceApi.shifts({ pageNum: 1, pageSize: 100 })
      rows.value = pageRows(result).map(mapShift)
    } else if (moduleKey.value === 'applications') {
      const result = await approvalApi.applications({ status: statusFilter.value, pageNum: 1, pageSize: 100 })
      rows.value = pageRows(result).map(mapApplication)
    } else if (moduleKey.value === 'processedApplications') {
      const result = await approvalApi.processed({ pageNum: 1, pageSize: 100 })
      rows.value = pageRows(result).map(mapApplication)
    } else {
      const result = await assetApi.assets({ keyword: keyword.value, status: statusFilter.value, pageNum: 1, pageSize: 100 })
      rows.value = pageRows(result).map(mapAsset)
    }
  } catch (err) {
    error.value = err instanceof Error ? err.message : '数据加载失败'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  if (moduleKey.value === 'applications') {
    router.push('/approval/submit')
    return
  }
  editingId.value = null
  if (moduleKey.value === 'shifts') {
    Object.assign(shiftForm, { shiftName: '', startTime: '09:00:00', endTime: '18:00:00', flexStart: '08:30:00', flexEnd: '09:30:00', status: 1 })
  } else if (moduleKey.value === 'assets') {
    Object.assign(assetForm, { assetCode: '', assetName: '', category: 3, status: 1, brand: '', model: '', purchaseDate: '', purchasePrice: undefined, remark: '' })
  }
  dialogOpen.value = true
}

async function openEdit(row: TableRow) {
  editingId.value = row.id
  if (moduleKey.value === 'shifts') {
    Object.assign(shiftForm, row)
    dialogOpen.value = true
  } else if (moduleKey.value === 'assets') {
    try {
      const detail = await assetApi.assetDetail(row.id)
      Object.assign(assetForm, detail)
    } catch {
      Object.assign(assetForm, row)
    }
    dialogOpen.value = true
  }
}

async function openDetail(row: TableRow) {
  error.value = ''
  try {
    let detail: unknown = row
    if (moduleKey.value === 'applications' || moduleKey.value === 'processedApplications') {
      detail = await approvalApi.detail(row.id)
    } else if (moduleKey.value === 'assets') {
      detail = await assetApi.assetDetail(row.id)
    }
    detailText.value = JSON.stringify(detail, null, 2)
    detailOpen.value = true
  } catch (err) {
    error.value = err instanceof Error ? err.message : '详情加载失败'
  }
}

function shiftPayload() {
  return {
    shiftName: shiftForm.shiftName,
    startTime: shiftForm.startTime,
    endTime: shiftForm.endTime,
    flexStart: shiftForm.flexStart,
    flexEnd: shiftForm.flexEnd,
    status: shiftForm.status ?? 1
  }
}

function assetPayload() {
  return {
    assetCode: assetForm.assetCode,
    assetName: assetForm.assetName,
    category: assetForm.category,
    status: assetForm.status ?? 1,
    brand: assetForm.brand,
    model: assetForm.model,
    purchaseDate: assetForm.purchaseDate,
    purchasePrice: assetForm.purchasePrice,
    remark: assetForm.remark
  }
}

async function save() {
  saving.value = true
  error.value = ''
  message.value = ''
  try {
    if (moduleKey.value === 'shifts') {
      if (editingId.value) await attendanceApi.updateShift(editingId.value, shiftPayload())
      else await attendanceApi.addShift(shiftPayload())
      message.value = editingId.value ? '班次已更新' : '班次已新增'
    } else if (moduleKey.value === 'assets') {
      if (editingId.value) await assetApi.updateAsset(editingId.value, assetPayload())
      else await assetApi.addAsset(assetPayload())
      message.value = editingId.value ? '资产已更新' : '资产已登记'
    }
    dialogOpen.value = false
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '保存失败'
  } finally {
    saving.value = false
  }
}

async function remove(row: TableRow) {
  error.value = ''
  message.value = ''
  try {
    if (moduleKey.value === 'shifts') {
      await attendanceApi.deleteShift(row.id)
      message.value = '班次已删除'
    } else if (moduleKey.value === 'assets') {
      await assetApi.deleteAsset(row.id)
      message.value = '资产已报废'
    }
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '删除失败'
  }
}

watch(moduleKey, () => {
  statusFilter.value = ''
  keyword.value = ''
  load()
})

onMounted(load)
</script>

<style scoped>
.filters {
  margin-bottom: 14px;
}

.filter-input {
  width: 180px;
}

.full {
  grid-column: 1 / -1;
}

.detail-json {
  max-height: 460px;
  overflow: auto;
  margin: 0;
  padding: 12px;
  border-radius: 6px;
  background: var(--surface-soft);
  color: var(--text);
  white-space: pre-wrap;
}
</style>