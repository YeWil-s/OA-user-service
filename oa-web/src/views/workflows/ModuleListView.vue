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
        <input v-if="moduleKey === 'attendanceRecords'" v-model="date" class="field filter-input" type="date" />
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

    <ModalDialog v-model="detailOpen" :title="moduleKey === 'attendanceRecords' ? '考勤记录详情' : '详情'" width="760px">
      <div v-if="moduleKey === 'attendanceRecords'" class="attendance-detail">
        <header class="attendance-profile">
          <span class="attendance-avatar"><UserRound /></span>
          <div>
            <span class="detail-eyebrow">ATTENDANCE RECORD</span>
            <h3>{{ detailValue('realName') }}</h3>
            <p>员工编号 {{ detailValue('userId') }} · {{ detailValue('deptName') }}</p>
          </div>
          <span class="attendance-status"><CheckCircle2 />{{ detailValue('statusLabel') }}</span>
        </header>

        <section class="detail-section">
          <header><BriefcaseBusiness /><div><strong>人员与班次</strong><span>EMPLOYEE & SHIFT</span></div></header>
          <div class="detail-grid">
            <div><span>所属部门</span><strong>{{ detailValue('deptName') }}</strong></div>
            <div><span>执行班次</span><strong>{{ detailValue('shiftName') }}</strong></div>
            <div><span>考勤日期</span><strong>{{ detailValue('recordDate') }}</strong></div>
            <div><span>打卡类型</span><strong>{{ punchTypeLabel(detailData?.punchType) }}</strong></div>
          </div>
        </section>

        <section class="detail-section">
          <header><Clock3 /><div><strong>打卡时间</strong><span>PUNCH TIMELINE</span></div></header>
          <div class="punch-timeline">
            <article><i class="timeline-dot start" /><span>上班打卡</span><strong>{{ detailValue('punchInTimeText') }}</strong><small>{{ detailValue('punchInTime') }}</small></article>
            <span class="timeline-line" />
            <article><i class="timeline-dot end" /><span>下班打卡</span><strong>{{ detailValue('punchOutTimeText') }}</strong><small>{{ detailValue('punchOutTime') }}</small></article>
          </div>
        </section>

        <section class="detail-section">
          <header><Timer /><div><strong>考勤统计</strong><span>ATTENDANCE METRICS</span></div></header>
          <div class="metric-grid">
            <div><span>迟到时长</span><strong class="warning-value">{{ detailValue('lateMinutes', ' 分钟') }}</strong></div>
            <div><span>早退时长</span><strong>{{ detailValue('earlyMinutes', ' 分钟') }}</strong></div>
            <div><span>有效工时</span><strong>{{ detailValue('workHours', ' 小时') }}</strong></div>
          </div>
        </section>

        <section class="detail-section environment-section">
          <header><MapPin /><div><strong>打卡环境</strong><span>LOCATION & DEVICE</span></div></header>
          <div class="environment-grid">
            <div><MapPin /><span><small>位置</small><strong>{{ detailValue('location') }}</strong></span></div>
            <div><Monitor /><span><small>设备信息</small><strong>{{ detailValue('deviceInfo') }}</strong></span></div>
          </div>
        </section>
      </div>
      <div v-else-if="moduleKey === 'applications' || moduleKey === 'processedApplications'" class="approval-detail">
        <header class="approval-profile">
          <span class="approval-avatar"><FileText /></span>
          <div>
            <span class="detail-eyebrow">{{ detailData?.applicationNo || '-' }}</span>
            <h3>{{ detailData?.applicantName || '-' }}</h3>
            <p>{{ detailData?.deptName || '-' }} · {{ detailData?.createTime?.slice(0, 10) || '-' }}</p>
          </div>
          <span class="approval-status" :class="approvalStatusClass(detailData?.status)">{{ detailData?.statusText || '-' }}</span>
        </header>

        <section class="detail-section">
          <header><Info /><div><strong>申请信息</strong><span>APPLICATION DETAILS</span></div></header>
          <div class="detail-grid cols-4">
            <div><span>申请类型</span><strong>{{ detailData?.appTypeText || '-' }}</strong></div>
            <div v-if="detailData?.appType === 1"><span>请假类别</span><strong>{{ detailData?.leaveTypeText || '-' }}</strong></div>
            <div v-if="[1,2,3].includes(detailData?.appType as number)"><span>开始时间</span><strong>{{ formatDate(detailData?.startTime) }}</strong></div>
            <div v-if="[1,2,3].includes(detailData?.appType as number)"><span>结束时间</span><strong>{{ formatDate(detailData?.endTime) }}</strong></div>
            <div v-if="[1,2,3].includes(detailData?.appType as number)"><span>时长</span><strong>{{ detailData?.duration }} 小时</strong></div>
            <div v-if="detailData?.appType === 4"><span>调至部门</span><strong>{{ detailData?.targetDeptName || '-' }}</strong></div>
            <div v-if="detailData?.appType === 4"><span>调至岗位</span><strong>{{ detailData?.targetPositionName || '-' }}</strong></div>
            <div v-if="detailData?.appType === 5"><span>资产名称</span><strong>{{ detailData?.assetName || '-' }}</strong></div>
            <div v-if="detailData?.appType === 5"><span>资产编码</span><strong>{{ detailData?.assetCode || '-' }}</strong></div>
            <div><span>提交时间</span><strong>{{ formatDate(detailData?.createTime) }}</strong></div>
            <div v-if="detailData?.reason"><span>申请原因</span><strong>{{ detailData?.reason }}</strong></div>
          </div>
        </section>

        <section v-if="detailData?.timeline?.length" class="detail-section">
          <header><Clock3 /><div><strong>审批时间线</strong><span>APPROVAL TIMELINE</span></div></header>
          <div class="approval-timeline">
            <div v-for="(item, idx) in detailData.timeline" :key="item.id" class="tl-item" :class="{ 'tl-active': idx === 0 }">
              <span class="tl-node" :class="tlNodeClass(item.action)">{{ tlNodeIcon(item.action) }}</span>
              <div class="tl-body">
                <div class="tl-head">
                  <strong>{{ item.approverName }}</strong>
                  <span class="tl-action">{{ item.actionText }}</span>
                  <time>{{ formatDate(item.actionTime) }}</time>
                </div>
                <p v-if="item.comment" class="tl-comment">{{ item.comment }}</p>
              </div>
            </div>
          </div>
        </section>

        <section v-if="detailData" class="detail-section">
          <header><Hash /><div><strong>详细信息</strong><span>RAW DATA</span></div></header>
          <pre class="detail-json-compact">{{ detailText }}</pre>
        </section>
      </div>
      <pre v-else class="detail-json">{{ detailText }}</pre>
      <template #footer><button class="btn primary" @click="detailOpen = false">关闭</button></template>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { BriefcaseBusiness, CheckCircle2, Clock3, Eye, FileText, Hash, Info, MapPin, Monitor, Pencil, Plus, RefreshCw, Timer, Trash2, UserRound } from 'lucide-vue-next'
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
const date = ref('')
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const message = ref('')
const dialogOpen = ref(false)
const detailOpen = ref(false)
const detailText = ref('')
const detailData = ref<TableRow | null>(null)
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
      { key: 'punchTypeText', label: '类型' },
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
  if (moduleKey.value === 'attendanceRecords') return [
    { value: 'normal', label: '正常' },
    { value: 'late', label: '迟到' },
    { value: 'early', label: '早退' },
    { value: 'late_early', label: '迟到/早退' },
    { value: 'missing', label: '缺卡' },
    { value: 'leave', label: '请假' },
    { value: 'field', label: '外勤' }
  ]
  if (moduleKey.value === 'applications') return [{ value: '1', label: '审批中' }, { value: '2', label: '已通过' }, { value: '3', label: '已驳回' }, { value: '4', label: '已撤销' }]
  if (moduleKey.value === 'assets') return [{ value: '1', label: '可领用' }, { value: '2', label: '已领用' }, { value: '0', label: '已报废' }]
  return []
})
const filteredRows = computed(() => {
  if (!keyword.value) return rows.value
  return rows.value.filter((row) => JSON.stringify(row).includes(keyword.value))
})

const pageRows = <T,>(page: PageResult<T>) => page.records || page.list || []
const appTypeText = (value?: number) => ({ 1: '请假', 2: '加班', 3: '外出', 4: '调岗', 5: '资产领用' }[value ?? 1])
const punchTypeLabel = (value?: number) => ({ 1: '现场', 2: '外勤', 3: '请假' }[value ?? 1] || '-')
const appStatusText = (value?: number) => ({ 0: '草稿', 1: '审批中', 2: '已通过', 3: '已驳回', 4: '已撤销' }[value ?? 1])
const assetCategoryText = (value?: number | string) => ({ 1: '固定资产', 2: '办公用品', 3: '电子设备' }[Number(value)] || String(value || '-'))
const assetStatusText = (value?: number) => ({ 0: '已报废', 1: '可领用', 2: '已领用' }[value ?? 1])
const timeText = (value?: string | null) => value ? (value.includes('T') ? value.slice(11, 19) : value) : '-'

function formatDate(value?: string | null) {
  if (!value) return '-'
  if (value.includes('T')) return value.slice(0, 16).replace('T', ' ')
  return value
}

function approvalStatusClass(status?: unknown) {
  return { 1: 'status-pending', 2: 'status-approved', 3: 'status-rejected', 4: 'status-cancelled' }[String(status)] || ''
}

const actionIconMap: Record<number, string> = { 1: '✓', 2: '✓', 3: '✗' }

function tlNodeClass(action?: unknown) {
  const v = Number(action)
  return { 1: 'tl-submit', 2: 'tl-approved', 3: 'tl-rejected' }[v] || ''
}

function tlNodeIcon(action?: unknown) {
  return actionIconMap[Number(action)] || '●'
}

function rowValue(row: TableRow, key: string) {
  return row[key] ?? '-'
}

function detailValue(key: string, suffix = '') {
  const value = detailData.value?.[key]
  if (value === undefined || value === null || value === '') return '-'
  return `${String(value)}${suffix}`
}

function mapAttendance(row: AttendanceRecord): TableRow {
  const punchTypeMap: Record<number, string> = { 1: '现场', 2: '外勤', 3: '请假' }
  return {
    ...row,
    id: row.id,
    realName: row.realName || `用户 ${row.userId}`,
    deptName: row.deptName || '-',
    punchInTimeText: timeText(row.punchInTime),
    punchOutTimeText: timeText(row.punchOutTime),
    punchTypeText: punchTypeMap[row.punchType ?? 1] || '现场',
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
  let summaryText = '-'
  if (row.appType === 4) {
    summaryText = row.targetDeptName || row.targetDeptId ? `调至 ${row.targetDeptName || row.targetDeptId}` : '-'
  } else if (row.appType === 5) {
    summaryText = row.assetName || row.assetCode || (row.assetId ? `资产 #${row.assetId}` : '-')
  } else if (row.duration != null) {
    summaryText = `${row.duration} 小时`
  }
  return {
    ...row,
    id: row.id,
    appTypeText: row.appTypeText || appTypeText(row.appType),
    statusText: row.statusText || appStatusText(row.status),
    durationText: summaryText,
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
      const params: Record<string, string | number> = { pageNum: 1, pageSize: 100 }
      if (date.value) params.date = date.value
      if (month.value) params.month = month.value
      if (statusFilter.value) params.status = statusFilter.value
      const result = await attendanceApi.allRecords(params)
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
    detailData.value = detail && typeof detail === 'object' ? detail as TableRow : row
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

.attendance-detail {
  display: grid;
  gap: 22px;
}

.attendance-profile {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 14px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--border);
}

.attendance-avatar {
  width: 50px;
  height: 50px;
  display: grid;
  place-items: center;
  border: 1px solid color-mix(in srgb, var(--primary) 32%, var(--border));
  border-radius: 8px;
  background: linear-gradient(145deg, color-mix(in srgb, var(--primary) 18%, transparent), color-mix(in srgb, var(--violet) 12%, transparent));
  color: var(--primary-soft);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.06), 0 10px 24px var(--primary-glow);
}

.attendance-avatar svg {
  width: 23px;
  height: 23px;
}

.detail-eyebrow {
  color: var(--faint);
  font-size: 9px;
  font-weight: 780;
  letter-spacing: 1.2px;
}

.attendance-profile h3 {
  margin: 5px 0 0;
  color: var(--text);
  font-size: 19px;
}

.attendance-profile p {
  margin: 5px 0 0;
  color: var(--muted);
  font-size: 11px;
}

.attendance-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 10px;
  border: 1px solid color-mix(in srgb, var(--warning) 24%, var(--border));
  border-radius: 6px;
  background: color-mix(in srgb, var(--warning) 10%, transparent);
  color: var(--warning);
  font-size: 11px;
  font-weight: 700;
}

.attendance-status svg {
  width: 14px;
  height: 14px;
}

.detail-section {
  display: grid;
  gap: 13px;
}

.detail-section > header {
  display: flex;
  align-items: center;
  gap: 9px;
  color: var(--primary-soft);
}

.detail-section > header > svg {
  width: 17px;
  height: 17px;
}

.detail-section > header div {
  display: grid;
  gap: 2px;
}

.detail-section > header strong {
  color: var(--text);
  font-size: 12px;
}

.detail-section > header span {
  color: var(--faint);
  font-size: 8px;
  letter-spacing: 1px;
}

.detail-grid,
.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  border: 1px solid var(--border);
  border-radius: 7px;
  background: color-mix(in srgb, var(--surface-soft) 72%, transparent);
  overflow: hidden;
}

.detail-grid > div,
.metric-grid > div {
  min-width: 0;
  display: grid;
  gap: 7px;
  padding: 13px;
  border-right: 1px solid var(--border);
}

.detail-grid > div:last-child,
.metric-grid > div:last-child {
  border-right: 0;
}

.detail-grid span,
.metric-grid span {
  color: var(--muted);
  font-size: 10px;
}

.detail-grid strong,
.metric-grid strong {
  overflow: hidden;
  color: var(--text);
  font-size: 12px;
  font-variant-numeric: tabular-nums;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.metric-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.metric-grid .warning-value {
  color: var(--warning);
}

.punch-timeline {
  display: grid;
  grid-template-columns: minmax(0, 1fr) minmax(40px, 0.3fr) minmax(0, 1fr);
  align-items: center;
  padding: 14px 16px;
  border: 1px solid var(--border);
  border-radius: 7px;
  background: color-mix(in srgb, var(--surface-soft) 72%, transparent);
}

.punch-timeline article {
  position: relative;
  display: grid;
  gap: 4px;
  padding-left: 18px;
}

.punch-timeline article > span {
  color: var(--muted);
  font-size: 10px;
}

.punch-timeline article > strong {
  color: var(--text);
  font-size: 20px;
  font-variant-numeric: tabular-nums;
}

.punch-timeline article > small {
  overflow: hidden;
  color: var(--faint);
  font-size: 9px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.timeline-dot {
  position: absolute;
  top: 5px;
  left: 0;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: var(--success);
  box-shadow: 0 0 10px color-mix(in srgb, var(--success) 60%, transparent);
}

.timeline-dot.end {
  background: var(--info);
  box-shadow: 0 0 10px color-mix(in srgb, var(--info) 60%, transparent);
}

.timeline-line {
  height: 1px;
  margin: 0 12px;
  background: linear-gradient(90deg, var(--success), var(--info));
  opacity: 0.45;
}

.environment-grid {
  display: grid;
  grid-template-columns: minmax(180px, 0.8fr) minmax(0, 1.4fr);
  gap: 10px;
}

.environment-grid > div {
  min-width: 0;
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px;
  border: 1px solid var(--border);
  border-radius: 7px;
  background: color-mix(in srgb, var(--surface-soft) 72%, transparent);
  color: var(--cyan);
}

.environment-grid > div > svg {
  width: 16px;
  height: 16px;
  flex: 0 0 16px;
  margin-top: 2px;
}

.environment-grid span {
  min-width: 0;
  display: grid;
  gap: 5px;
}

.environment-grid small {
  color: var(--muted);
  font-size: 9px;
}

.environment-grid strong {
  overflow-wrap: anywhere;
  color: var(--text);
  font-size: 10px;
  font-weight: 600;
  line-height: 1.55;
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

/* ---- approval detail ---- */

.approval-detail {
  display: grid;
  gap: 22px;
}

.approval-profile {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  align-items: center;
  gap: 14px;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--border);
}

.approval-avatar {
  width: 50px;
  height: 50px;
  display: grid;
  place-items: center;
  border: 1px solid color-mix(in srgb, var(--primary) 32%, var(--border));
  border-radius: 8px;
  background: linear-gradient(145deg, color-mix(in srgb, var(--primary) 18%, transparent), color-mix(in srgb, var(--violet) 12%, transparent));
  color: var(--primary-soft);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.06), 0 10px 24px var(--primary-glow);
}

.approval-avatar svg {
  width: 23px;
  height: 23px;
}

.approval-profile h3 {
  margin: 5px 0 0;
  color: var(--text);
  font-size: 19px;
}

.approval-profile p {
  margin: 5px 0 0;
  color: var(--muted);
  font-size: 11px;
}

.approval-status {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 7px 14px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
  white-space: nowrap;
}

.status-pending {
  border: 1px solid color-mix(in srgb, var(--warning) 24%, var(--border));
  background: color-mix(in srgb, var(--warning) 10%, transparent);
  color: var(--warning);
}

.status-approved {
  border: 1px solid color-mix(in srgb, var(--success) 24%, var(--border));
  background: color-mix(in srgb, var(--success) 10%, transparent);
  color: var(--success);
}

.status-rejected {
  border: 1px solid color-mix(in srgb, var(--error) 24%, var(--border));
  background: color-mix(in srgb, var(--error) 10%, transparent);
  color: var(--error);
}

.status-cancelled {
  border: 1px solid color-mix(in srgb, var(--muted) 24%, var(--border));
  background: color-mix(in srgb, var(--muted) 10%, transparent);
  color: var(--muted);
}

.cols-4 {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

/* approval timeline */

.approval-timeline {
  position: relative;
  display: grid;
  gap: 0;
  border: 1px solid var(--border);
  border-radius: 7px;
  background: color-mix(in srgb, var(--surface-soft) 72%, transparent);
  overflow: hidden;
}

.tl-item {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr);
  gap: 12px;
  padding: 13px 14px;
  border-bottom: 1px solid var(--border);
}

.tl-item:last-child {
  border-bottom: 0;
}

.tl-active {
  background: color-mix(in srgb, var(--primary) 6%, transparent);
}

.tl-node {
  width: 26px;
  height: 26px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  font-size: 12px;
  font-weight: 700;
  background: var(--surface-soft);
  border: 1px solid var(--border);
  color: var(--muted);
  flex-shrink: 0;
}

.tl-active .tl-node {
  border-color: var(--primary-soft);
  color: var(--primary-soft);
  box-shadow: 0 0 10px var(--primary-glow);
}

.tl-submit {
  border-color: var(--info) !important;
  color: var(--info) !important;
}

.tl-approved {
  border-color: var(--success) !important;
  color: var(--success) !important;
}

.tl-rejected {
  border-color: var(--error) !important;
  color: var(--error) !important;
}

.tl-body {
  min-width: 0;
  display: grid;
  gap: 6px;
}

.tl-head {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 8px;
}

.tl-head strong {
  color: var(--text);
  font-size: 13px;
}

.tl-action {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 10px;
  font-weight: 600;
  background: color-mix(in srgb, var(--primary) 12%, transparent);
  color: var(--primary-soft);
}

.tl-head time {
  color: var(--faint);
  font-size: 10px;
  margin-left: auto;
}

.tl-comment {
  margin: 0;
  padding: 8px 10px;
  border-radius: 5px;
  background: var(--surface-soft);
  color: var(--muted);
  font-size: 11px;
  line-height: 1.55;
}

.detail-json-compact {
  max-height: 260px;
  overflow: auto;
  margin: 0;
  padding: 10px;
  border-radius: 5px;
  background: var(--surface-soft);
  color: var(--faint);
  font-size: 10px;
  white-space: pre-wrap;
}

@media (max-width: 640px) {
  .attendance-profile {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .attendance-status {
    grid-column: 1 / -1;
    width: max-content;
  }

  .detail-grid,
  .metric-grid,
  .environment-grid {
    grid-template-columns: 1fr 1fr;
  }

  .detail-grid > div:nth-child(2),
  .metric-grid > div:nth-child(2) {
    border-right: 0;
  }

  .metric-grid > div:last-child {
    grid-column: 1 / -1;
    border-top: 1px solid var(--border);
  }

  .environment-grid {
    grid-template-columns: 1fr;
  }

  .punch-timeline {
    grid-template-columns: 1fr;
    gap: 12px;
  }

  .timeline-line {
    width: 1px;
    height: 18px;
    margin: 0 0 0 4px;
  }

  .approval-profile {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .approval-status {
    grid-column: 1 / -1;
    width: max-content;
  }

  .cols-4 {
    grid-template-columns: 1fr 1fr;
  }

  .tl-head time {
    margin-left: 0;
    width: 100%;
  }
}
</style>