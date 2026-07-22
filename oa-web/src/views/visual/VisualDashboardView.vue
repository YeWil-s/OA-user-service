<template>
  <main class="cockpit-shell">
    <div class="data-grid" aria-hidden="true" />
    <div class="data-stream stream-one" aria-hidden="true" />
    <div class="data-stream stream-two" aria-hidden="true" />
    <div class="data-stream stream-three" aria-hidden="true" />

    <header class="cockpit-header">
      <div class="header-side header-left">
        <RouterLink class="screen-button icon-only" to="/dashboard" aria-label="返回工作台" title="返回工作台">
          <ArrowLeft class="icon" />
        </RouterLink>
        <div class="system-identity">
          <span class="identity-mark"><Activity class="icon" /></span>
          <div>
            <strong>OA INTELLIGENCE</strong>
            <span>Enterprise Operation System</span>
          </div>
        </div>
      </div>

      <div class="screen-title">
        <span class="title-line" />
        <div>
          <p>ENTERPRISE INTELLIGENCE COMMAND CENTER</p>
          <h1>企业智能管理数据驾驶舱</h1>
        </div>
        <span class="title-line reverse" />
      </div>

      <div class="header-side header-right">
        <div class="clock-block">
          <strong>{{ clockTime }}</strong>
          <span>{{ clockDate }}</span>
        </div>
        <button class="screen-button icon-only" type="button" :aria-label="isFullscreen ? '退出全屏' : '进入全屏'" :title="isFullscreen ? '退出全屏' : '进入全屏'" @click="toggleFullscreen">
          <Minimize2 v-if="isFullscreen" class="icon" />
          <Expand v-else class="icon" />
        </button>
      </div>
    </header>

    <section class="control-strip">
      <div class="live-status">
        <span class="status-beacon" :class="{ syncing: loading || syncing, error: Boolean(error) }" />
        <div>
          <strong>{{ error ? '数据链路异常' : loading || syncing ? '数据同步中' : '数据链路正常' }}</strong>
          <span>最近更新 {{ lastUpdated }}</span>
        </div>
      </div>

      <div class="screen-controls">
        <label class="month-control">
          <CalendarClock class="icon" />
          <input v-model="month" type="month" aria-label="统计月份" />
        </label>
        <button class="screen-button" type="button" :disabled="loading" @click="load">
          <RefreshCw class="icon" />
          <span>{{ loading ? '刷新中' : '刷新数据' }}</span>
        </button>
        <button class="screen-button primary" type="button" :disabled="syncing" @click="sync">
          <DatabaseZap class="icon" />
          <span>{{ syncing ? '同步中' : '同步统计' }}</span>
        </button>
      </div>
    </section>

    <p v-if="error" class="error-banner">{{ error }}</p>
    <p v-if="message" class="success-banner">{{ message }}</p>

    <section class="kpi-strip">
      <article v-for="(item, index) in stats" :key="item.label" class="kpi-module" :style="{ '--accent-index': index }">
        <div class="kpi-heading">
          <span class="kpi-icon"><component :is="statIcon(item.label)" class="icon" /></span>
          <span>{{ item.label }}</span>
          <span class="live-tag"><Radio class="icon" />LIVE</span>
        </div>
        <div v-if="loading" class="kpi-loading">
          <span class="skeleton skeleton-value" />
          <span class="skeleton skeleton-line" />
        </div>
        <template v-else>
          <strong class="kpi-value"><AnimatedNumber :value="item.value" /></strong>
          <div class="kpi-footer">
            <span>{{ item.meta }}</span>
            <SparklineBars :values="statSparkline(item.label)" />
          </div>
        </template>
      </article>
    </section>

    <section class="cockpit-main">
      <aside class="left-column">
        <ChartPanel class="screen-chart" title="组织人员分布" subtitle="各部门在职人数占比" :option="deptPie" :height="190" :loading="loading" />

        <section class="screen-panel ranking-panel">
          <header class="module-header">
            <div>
              <span class="module-code">DEPT / RANK</span>
              <h2>部门规模排行</h2>
            </div>
            <Building2 class="module-icon" />
          </header>
          <div class="rank-list">
            <div v-for="(item, index) in deptRanking" :key="item.deptId" class="rank-row">
              <span class="rank-index">{{ String(index + 1).padStart(2, '0') }}</span>
              <div class="rank-content">
                <div><strong>{{ item.deptName }}</strong><span>{{ item.value }} {{ item.unit || '人' }}</span></div>
                <span class="rank-track"><i :style="{ width: deptProgress(item.value) + '%' }" /></span>
              </div>
            </div>
            <div v-if="!loading && deptRanking.length === 0" class="screen-empty">暂无部门统计</div>
          </div>
        </section>
      </aside>

      <section class="core-stage">
        <header class="core-header">
          <div>
            <span class="module-code">CORE OPERATION INDEX</span>
            <h2>企业运营核心态势</h2>
          </div>
          <span class="core-badge"><ShieldCheck class="icon" />实时监测</span>
        </header>

        <div class="core-visual">
          <div class="gauge-frame">
            <div class="gauge-ring" :style="gaugeStyle">
              <div class="gauge-inner">
                <span>综合出勤率</span>
                <strong><AnimatedNumber :value="overview.attendanceRate + '%'" /></strong>
                <small>{{ overview.statMonth || month }}</small>
              </div>
            </div>
            <span class="gauge-scan" aria-hidden="true" />
          </div>

          <div class="core-facts">
            <article>
              <CheckCircle2 class="fact-icon success" />
              <div><span>审批通过</span><strong><AnimatedNumber :value="approval.approvedCount" /></strong></div>
            </article>
            <article>
              <Clock3 class="fact-icon warning" />
              <div><span>等待审批</span><strong><AnimatedNumber :value="approval.pendingCount" /></strong></div>
            </article>
            <article>
              <AlertTriangle class="fact-icon danger" />
              <div><span>审批驳回</span><strong><AnimatedNumber :value="approval.rejectedCount" /></strong></div>
            </article>
            <article>
              <TimerReset class="fact-icon info" />
              <div><span>平均审批耗时</span><strong><AnimatedNumber :value="overview.avgApprovalHours + 'h'" /></strong></div>
            </article>
          </div>
        </div>

        <div class="core-footer">
          <span><i class="signal success" />组织数据 {{ depts.length }} 项</span>
          <span><i class="signal info" />趋势数据 {{ trends.length }} 期</span>
          <span><i class="signal warning" />审批申请 {{ overview.totalApplications }} 件</span>
        </div>
      </section>

      <aside class="right-column">
        <section class="screen-panel speed-panel">
          <header class="module-header">
            <div>
              <span class="module-code">APPROVAL / SPEED</span>
              <h2>审批效率排行</h2>
            </div>
            <TrendingUp class="module-icon" />
          </header>
          <div class="speed-list">
            <div v-for="(item, index) in speedRanking" :key="item.deptId" class="speed-row">
              <span class="rank-index">{{ String(index + 1).padStart(2, '0') }}</span>
              <div>
                <strong>{{ item.deptName }}</strong>
                <span>{{ item.totalApplications }} 件申请</span>
              </div>
              <em>{{ item.avgApprovalHours }}h</em>
            </div>
            <div v-if="!loading && speedRanking.length === 0" class="screen-empty">暂无审批效率数据</div>
          </div>
        </section>

        <section class="screen-panel monitor-panel">
          <header class="module-header">
            <div>
              <span class="module-code">REALTIME / MONITOR</span>
              <h2>实时监控与提醒</h2>
            </div>
            <Radio class="module-icon pulse-icon" />
          </header>
          <div class="monitor-list">
            <div v-for="item in monitorItems" :key="item.label" class="monitor-row">
              <span class="monitor-dot" :class="item.tone" />
              <div><strong>{{ item.label }}</strong><span>{{ item.description }}</span></div>
              <em :class="item.tone">{{ item.value }}</em>
            </div>
          </div>
        </section>
      </aside>
    </section>

    <section class="cockpit-bottom">
      <ChartPanel class="screen-chart trend-chart" title="近六个月考勤运行趋势" subtitle="正常、迟到、请假变化" :option="attendanceLine" :height="172" :loading="loading" />
      <ChartPanel class="screen-chart" title="部门加班对比" subtitle="月度累计小时" :option="overtimeBar" :height="172" :loading="loading" />
      <ChartPanel class="screen-chart" title="审批耗时分布" subtitle="部门平均处理时间" :option="approvalSpeedBar" :height="172" :loading="loading" />
    </section>
  </main>
</template>

<script setup lang="ts">
import {
  Activity,
  AlertTriangle,
  ArrowLeft,
  Building2,
  CalendarClock,
  CheckCircle2,
  Clock3,
  DatabaseZap,
  Expand,
  Minimize2,
  Radio,
  RefreshCw,
  ShieldCheck,
  TimerReset,
  TrendingUp,
  UsersRound
} from 'lucide-vue-next'
import { computed, onMounted, onUnmounted, ref } from 'vue'
import AnimatedNumber from '@/components/AnimatedNumber.vue'
import ChartPanel from '@/components/ChartPanel.vue'
import SparklineBars from '@/components/SparklineBars.vue'
import { visualApi } from '@/api/services'
import type { ApprovalStats, AttendanceTrend, DeptMetric, VisualOverview } from '@/api/types'

function currentMonth() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

const emptyOverview: VisualOverview = {
  statMonth: '',
  totalEmployees: 0,
  newHires: 0,
  resignations: 0,
  attendanceRate: 0,
  overtimeHours: 0,
  totalApplications: 0,
  approvedCount: 0,
  rejectedCount: 0,
  pendingCount: 0,
  approvalPassRate: 0,
  avgApprovalHours: 0,
  refreshTime: ''
}

const emptyApproval: ApprovalStats = {
  statMonth: '',
  totalApplications: 0,
  approvedCount: 0,
  rejectedCount: 0,
  pendingCount: 0,
  passRate: 0,
  rejectRate: 0,
  pendingRate: 0
}

const month = ref(currentMonth())
const overview = ref<VisualOverview>({ ...emptyOverview })
const depts = ref<DeptMetric[]>([])
const overtime = ref<DeptMetric[]>([])
const trends = ref<AttendanceTrend[]>([])
const approval = ref<ApprovalStats>({ ...emptyApproval })
const speed = ref<Array<{ deptId: number; deptName: string; totalApplications: number; avgApprovalHours: number }>>([])
const loading = ref(false)
const syncing = ref(false)
const error = ref('')
const message = ref('')

const currentTime = ref(new Date())
const isFullscreen = ref(Boolean(document.fullscreenElement))
let clockTimer: ReturnType<typeof setInterval> | undefined

const clockTime = computed(() => new Intl.DateTimeFormat('zh-CN', {
  hour: '2-digit',
  minute: '2-digit',
  second: '2-digit',
  hour12: false
}).format(currentTime.value))

const clockDate = computed(() => new Intl.DateTimeFormat('zh-CN', {
  year: 'numeric',
  month: '2-digit',
  day: '2-digit',
  weekday: 'short'
}).format(currentTime.value))

const lastUpdated = computed(() => overview.value.refreshTime
  ? overview.value.refreshTime.replace('T', ' ').slice(0, 19)
  : '--')

const deptRanking = computed(() => [...depts.value].sort((a, b) => b.value - a.value).slice(0, 5))
const speedRanking = computed(() => [...speed.value].sort((a, b) => a.avgApprovalHours - b.avgApprovalHours).slice(0, 6))
const maxDeptValue = computed(() => Math.max(...deptRanking.value.map((item) => item.value), 1))

const monitorItems = computed(() => [
  {
    label: '统计数据链路',
    description: error.value ? '最近一次请求返回异常' : '全部数据接口连接正常',
    value: error.value ? '异常' : loading.value || syncing.value ? '同步中' : '正常',
    tone: error.value ? 'danger' : loading.value || syncing.value ? 'warning' : 'success'
  },
  {
    label: '待审批积压',
    description: '当前等待处理的业务申请',
    value: approval.value.pendingCount + ' 件',
    tone: approval.value.pendingCount > 0 ? 'warning' : 'success'
  },
  {
    label: '本月人员变动',
    description: '入职 ' + overview.value.newHires + ' / 离职 ' + overview.value.resignations,
    value: (overview.value.newHires - overview.value.resignations >= 0 ? '+' : '') + (overview.value.newHires - overview.value.resignations),
    tone: overview.value.newHires - overview.value.resignations >= 0 ? 'info' : 'danger'
  },
  {
    label: '加班运行负载',
    description: '本月企业累计加班时长',
    value: overview.value.overtimeHours + 'h',
    tone: overview.value.overtimeHours > 0 ? 'info' : 'success'
  }
])

const stats = computed(() => [
  { label: '总人数', value: overview.value.totalEmployees, meta: `新增 ${overview.value.newHires} / 离职 ${overview.value.resignations}` },
  { label: '出勤率', value: `${overview.value.attendanceRate}%`, meta: `加班 ${overview.value.overtimeHours} 小时` },
  { label: '审批通过率', value: `${overview.value.approvalPassRate}%`, meta: `平均 ${overview.value.avgApprovalHours} 小时` },
  { label: '待审批', value: approval.value.pendingCount, meta: `总申请 ${approval.value.totalApplications}` }
])

function statSparkline(label: string) {
  if (label === '总人数') return depts.value.map((item) => item.value)
  if (label === '出勤率') return trends.value.map((item) => item.attendanceRate)
  if (label === '审批通过率') {
    return [approval.value.approvedCount, approval.value.rejectedCount, approval.value.pendingCount]
  }
  return speed.value.map((item) => item.totalApplications)
}

function statIcon(label: string) {
  if (label === '总人数') return UsersRound
  if (label === '出勤率') return Activity
  if (label === '审批通过率') return CheckCircle2
  return Clock3
}

function deptProgress(value: number) {
  return Math.max(8, Math.round((value / maxDeptValue.value) * 100))
}

const gaugeStyle = computed(() => ({
  '--gauge-angle': String(Math.max(0, Math.min(100, overview.value.attendanceRate)) * 3.6) + 'deg'
}))

async function toggleFullscreen() {
  if (document.fullscreenElement) {
    await document.exitFullscreen()
  } else {
    await document.documentElement.requestFullscreen()
  }
}

function syncFullscreenState() {
  isFullscreen.value = Boolean(document.fullscreenElement)
}

const deptPie = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{ type: 'pie', radius: ['42%', '70%'], data: depts.value.map((item) => ({ name: item.deptName, value: item.value })), color: ['#3b82f6', '#8b5cf6', '#06b6d4', '#22c55e', '#ec4899'] }]
}))

const attendanceLine = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { top: 0 },
  grid: { left: 36, right: 18, top: 42, bottom: 30 },
  xAxis: { type: 'category', data: trends.value.map((item) => item.statMonth) },
  yAxis: { type: 'value' },
  series: [
    { name: '正常', type: 'line', smooth: true, data: trends.value.map((item) => item.normalCount), color: '#3b82f6' },
    { name: '迟到', type: 'line', smooth: true, data: trends.value.map((item) => item.lateCount), color: '#f97316' },
    { name: '请假', type: 'line', smooth: true, data: trends.value.map((item) => item.leaveCount), color: '#8b5cf6' }
  ]
}))

const overtimeBar = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 38, right: 18, top: 26, bottom: 34 },
  xAxis: { type: 'category', data: overtime.value.map((item) => item.deptName) },
  yAxis: { type: 'value' },
  series: [{ type: 'bar', data: overtime.value.map((item) => item.value), color: '#f97316' }]
}))

const approvalSpeedBar = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 42, right: 18, top: 26, bottom: 34 },
  xAxis: { type: 'category', data: speed.value.map((item) => item.deptName) },
  yAxis: { type: 'value' },
  series: [{ type: 'bar', data: speed.value.map((item) => item.avgApprovalHours), color: '#3b82f6' }]
}))

async function load() {
  loading.value = true
  error.value = ''
  message.value = ''
  try {
    const [overviewResult, deptResult, overtimeResult, trendResult, approvalResult, speedResult] = await Promise.all([
      visualApi.overview(month.value),
      visualApi.deptDistribution(month.value),
      visualApi.deptOvertime(month.value),
      visualApi.attendanceTrend(month.value, 6),
      visualApi.approvalStats(month.value),
      visualApi.approvalSpeed(month.value)
    ])
    overview.value = overviewResult
    depts.value = deptResult
    overtime.value = overtimeResult
    trends.value = trendResult
    approval.value = approvalResult
    speed.value = speedResult
  } catch (err) {
    error.value = err instanceof Error ? err.message : '数据加载失败'
  } finally {
    loading.value = false
  }
}

async function sync() {
  syncing.value = true
  error.value = ''
  message.value = ''
  try {
    await visualApi.sync(month.value)
    message.value = '统计同步完成'
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '统计同步失败'
  } finally {
    syncing.value = false
  }
}

onMounted(load)

onMounted(() => {
  clockTimer = setInterval(() => {
    currentTime.value = new Date()
  }, 1000)
  document.addEventListener('fullscreenchange', syncFullscreenState)
})

onUnmounted(() => {
  if (clockTimer) clearInterval(clockTimer)
  document.removeEventListener('fullscreenchange', syncFullscreenState)
})
</script>

<style scoped>
.cockpit-shell {
  position: fixed;
  inset: 0;
  z-index: 100;
  min-width: 320px;
  min-height: 100vh;
  display: grid;
  grid-template-rows: 70px 48px 126px minmax(360px, 1fr) 230px;
  gap: 12px;
  overflow: auto;
  padding: 12px;
  background:
    linear-gradient(135deg, rgba(59, 130, 246, 0.08), transparent 30%, rgba(6, 182, 212, 0.045) 64%, transparent),
    #020617;
  color: #e2e8f0;
  font-variant-numeric: tabular-nums;
  isolation: isolate;
  animation: screen-enter 0.5s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.cockpit-shell::before {
  position: fixed;
  inset: 0;
  z-index: -3;
  pointer-events: none;
  background:
    repeating-linear-gradient(90deg, rgba(59, 130, 246, 0.025) 0 1px, transparent 1px 80px),
    repeating-linear-gradient(0deg, rgba(6, 182, 212, 0.022) 0 1px, transparent 1px 80px);
  background-position: center;
  content: "";
}

.cockpit-shell::after {
  position: fixed;
  inset: 0;
  z-index: -2;
  pointer-events: none;
  background: linear-gradient(90deg, transparent, rgba(59, 130, 246, 0.04), transparent);
  background-size: 42% 100%;
  content: "";
  animation: background-flow 14s linear infinite;
}

.data-grid {
  position: fixed;
  inset: 0;
  z-index: -1;
  pointer-events: none;
  opacity: 0.36;
  background-image:
    linear-gradient(rgba(148, 163, 184, 0.018) 1px, transparent 1px),
    linear-gradient(90deg, rgba(148, 163, 184, 0.018) 1px, transparent 1px);
  background-size: 20px 20px;
  mask-image: linear-gradient(to bottom, #000, transparent 92%);
}

.data-stream {
  position: fixed;
  z-index: -1;
  width: 34vw;
  height: 1px;
  pointer-events: none;
  background: linear-gradient(90deg, transparent, rgba(6, 182, 212, 0.58), transparent);
  filter: drop-shadow(0 0 6px rgba(6, 182, 212, 0.45));
  opacity: 0;
  animation: stream-flow 9s linear infinite;
}

.stream-one {
  top: 24%;
  left: -34vw;
}

.stream-two {
  top: 57%;
  left: -34vw;
  animation-delay: 2.7s;
  animation-duration: 12s;
}

.stream-three {
  top: 84%;
  left: -34vw;
  animation-delay: 5.4s;
  animation-duration: 10s;
}

.cockpit-header {
  position: relative;
  display: grid;
  grid-template-columns: minmax(260px, 1fr) auto minmax(260px, 1fr);
  align-items: center;
  gap: 20px;
  border: 1px solid rgba(96, 165, 250, 0.15);
  background: linear-gradient(180deg, rgba(15, 23, 42, 0.84), rgba(15, 23, 42, 0.48));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.045), 0 14px 44px rgba(2, 6, 23, 0.3);
  backdrop-filter: blur(14px);
}

.cockpit-header::before,
.cockpit-header::after {
  position: absolute;
  bottom: -1px;
  width: 28%;
  height: 1px;
  background: linear-gradient(90deg, transparent, #3b82f6);
  content: "";
}

.cockpit-header::before {
  left: 0;
}

.cockpit-header::after {
  right: 0;
  background: linear-gradient(90deg, #8b5cf6, transparent);
}

.header-side {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 14px;
}

.header-right {
  justify-content: flex-end;
}

.system-identity {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.identity-mark {
  width: 34px;
  height: 34px;
  display: grid;
  place-items: center;
  flex: 0 0 34px;
  border: 1px solid rgba(6, 182, 212, 0.3);
  background: rgba(6, 182, 212, 0.1);
  color: #22d3ee;
  box-shadow: inset 0 0 16px rgba(6, 182, 212, 0.1), 0 0 18px rgba(6, 182, 212, 0.08);
}

.system-identity strong,
.system-identity span,
.clock-block strong,
.clock-block span {
  display: block;
}

.system-identity strong {
  color: #dbeafe;
  font-size: 12px;
  letter-spacing: 0;
}

.system-identity span {
  margin-top: 3px;
  overflow: hidden;
  color: #64748b;
  font-size: 9px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.screen-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 18px;
  text-align: center;
}

.screen-title h1 {
  margin: 3px 0 0;
  background: linear-gradient(90deg, #93c5fd, #e2e8f0 44%, #c4b5fd);
  background-clip: text;
  color: transparent;
  font-size: 24px;
  font-weight: 800;
  letter-spacing: 0;
  white-space: nowrap;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.screen-title p {
  margin: 0;
  color: #3b82f6;
  font-size: 8px;
  letter-spacing: 0;
}

.title-line {
  width: 70px;
  height: 1px;
  background: linear-gradient(90deg, transparent, #3b82f6);
  box-shadow: 0 0 8px rgba(59, 130, 246, 0.5);
}

.title-line.reverse {
  transform: scaleX(-1);
}

.clock-block {
  min-width: 118px;
  text-align: right;
}

.clock-block strong {
  color: #f8fafc;
  font-size: 18px;
  font-weight: 720;
  line-height: 1;
}

.clock-block span {
  margin-top: 5px;
  color: #64748b;
  font-size: 9px;
}

.control-strip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 0 12px;
  border: 1px solid rgba(148, 163, 184, 0.1);
  background: rgba(15, 23, 42, 0.52);
  backdrop-filter: blur(12px);
}

.live-status {
  display: flex;
  align-items: center;
  gap: 9px;
}

.live-status strong,
.live-status span {
  display: block;
}

.live-status strong {
  color: #cbd5e1;
  font-size: 11px;
}

.live-status span {
  margin-top: 2px;
  color: #64748b;
  font-size: 9px;
}

.status-beacon {
  width: 9px;
  height: 9px;
  flex: 0 0 9px;
  border-radius: 50%;
  background: #22c55e;
  box-shadow: 0 0 0 4px rgba(34, 197, 94, 0.08), 0 0 13px rgba(34, 197, 94, 0.7);
  animation: beacon-pulse 1.8s ease-in-out infinite;
}

.status-beacon.syncing {
  background: #eab308;
  box-shadow: 0 0 0 4px rgba(234, 179, 8, 0.08), 0 0 13px rgba(234, 179, 8, 0.65);
}

.status-beacon.error {
  background: #ef4444;
  box-shadow: 0 0 0 4px rgba(239, 68, 68, 0.08), 0 0 13px rgba(239, 68, 68, 0.7);
}

.screen-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}

.screen-button,
.month-control {
  min-height: 31px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 0 10px;
  border: 1px solid rgba(96, 165, 250, 0.17);
  background: rgba(30, 41, 59, 0.62);
  color: #94a3b8;
  font-size: 10px;
  cursor: pointer;
  transition: transform 0.15s ease, border-color 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
}

.screen-button:hover:not(:disabled) {
  border-color: rgba(96, 165, 250, 0.45);
  color: #dbeafe;
  box-shadow: 0 0 18px rgba(59, 130, 246, 0.13);
  transform: translateY(-1px);
}

.screen-button:active:not(:disabled) {
  transform: scale(0.96);
}

.screen-button:disabled {
  cursor: not-allowed;
  opacity: 0.48;
}

.screen-button.primary {
  border-color: rgba(139, 92, 246, 0.4);
  background: linear-gradient(135deg, rgba(59, 130, 246, 0.72), rgba(139, 92, 246, 0.72));
  color: #fff;
}

.screen-button.icon-only {
  width: 32px;
  padding: 0;
}

.month-control input {
  width: 108px;
  border: 0;
  outline: 0;
  background: transparent;
  color: #cbd5e1;
  font-size: 10px;
}

.kpi-strip {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.kpi-module,
.screen-panel,
.core-stage {
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(96, 165, 250, 0.14);
  background:
    linear-gradient(145deg, rgba(30, 41, 59, 0.52), transparent 70%),
    rgba(15, 23, 42, 0.62);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.035), 0 18px 44px rgba(2, 6, 23, 0.18);
  backdrop-filter: blur(13px);
}

.kpi-module::before,
.screen-panel::before,
.core-stage::before {
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  height: 1px;
  background: linear-gradient(90deg, transparent, rgba(59, 130, 246, 0.68), transparent);
  content: "";
}

.kpi-module {
  display: grid;
  grid-template-rows: auto 1fr auto;
  padding: 13px 15px;
  transition: transform 0.22s ease, border-color 0.22s ease, box-shadow 0.22s ease;
}

.kpi-module:nth-child(2)::before {
  background: linear-gradient(90deg, transparent, rgba(6, 182, 212, 0.72), transparent);
}

.kpi-module:nth-child(3)::before {
  background: linear-gradient(90deg, transparent, rgba(139, 92, 246, 0.72), transparent);
}

.kpi-module:nth-child(4)::before {
  background: linear-gradient(90deg, transparent, rgba(236, 72, 153, 0.68), transparent);
}

.kpi-module:hover {
  z-index: 2;
  border-color: rgba(96, 165, 250, 0.36);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.06), 0 22px 54px rgba(2, 6, 23, 0.28), 0 0 26px rgba(59, 130, 246, 0.08);
  transform: translateY(-5px);
}

.kpi-heading,
.kpi-footer {
  display: flex;
  align-items: center;
}

.kpi-heading {
  gap: 8px;
  color: #94a3b8;
  font-size: 10px;
  font-weight: 700;
}

.kpi-icon {
  width: 24px;
  height: 24px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(96, 165, 250, 0.2);
  background: rgba(59, 130, 246, 0.1);
  color: #60a5fa;
}

.live-tag {
  display: inline-flex;
  align-items: center;
  gap: 3px;
  margin-left: auto;
  color: #475569;
  font-size: 8px;
}

.live-tag .icon {
  width: 9px;
  height: 9px;
  color: #22c55e;
}

.kpi-value {
  align-self: center;
  width: fit-content;
  background: linear-gradient(90deg, #f8fafc, #93c5fd 65%, #c4b5fd);
  background-clip: text;
  color: transparent;
  font-size: 31px;
  font-weight: 820;
  line-height: 1;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.kpi-footer {
  justify-content: space-between;
  gap: 8px;
  color: #64748b;
  font-size: 9px;
}

.kpi-footer :deep(.sparkline) {
  width: 64px;
  height: 24px;
}

.kpi-loading {
  display: grid;
  align-content: center;
  gap: 10px;
}

.cockpit-main {
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(250px, 0.82fr) minmax(420px, 1.34fr) minmax(250px, 0.82fr);
  gap: 12px;
}

.left-column,
.right-column {
  min-height: 0;
  display: grid;
  grid-template-rows: minmax(0, 1fr) minmax(0, 1fr);
  gap: 12px;
}

.cockpit-shell :deep(.screen-chart) {
  min-height: 0;
  height: 100%;
  padding: 13px;
  border-color: rgba(96, 165, 250, 0.14);
  background:
    linear-gradient(145deg, rgba(30, 41, 59, 0.52), transparent 70%),
    rgba(15, 23, 42, 0.62);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.035), 0 18px 44px rgba(2, 6, 23, 0.18);
}

.cockpit-shell :deep(.screen-chart:hover) {
  transform: none;
}

.cockpit-shell :deep(.chart-head) {
  min-height: 30px;
  margin-bottom: 4px;
}

.cockpit-shell :deep(.chart-head h3) {
  color: #cbd5e1;
  font-size: 12px;
}

.cockpit-shell :deep(.chart-head p) {
  margin-top: 2px;
  color: #475569;
  font-size: 8px;
}

.screen-panel {
  min-height: 0;
  padding: 13px;
}

.module-header,
.core-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.module-header h2,
.core-header h2 {
  margin: 3px 0 0;
  color: #cbd5e1;
  font-size: 12px;
}

.module-code {
  color: #3b82f6;
  font-size: 7px;
  font-weight: 760;
  letter-spacing: 0;
}

.module-icon {
  width: 17px;
  height: 17px;
  color: #475569;
}

.pulse-icon {
  color: #22d3ee;
  animation: icon-pulse 1.8s ease-in-out infinite;
}

.rank-list,
.speed-list,
.monitor-list {
  margin-top: 10px;
  display: grid;
  gap: 7px;
}

.rank-row,
.speed-row,
.monitor-row {
  min-height: 32px;
  display: flex;
  align-items: center;
  gap: 9px;
}

.rank-index {
  width: 22px;
  color: #475569;
  font-size: 9px;
  font-weight: 760;
}

.rank-row:first-child .rank-index,
.speed-row:first-child .rank-index {
  color: #f97316;
}

.rank-content,
.speed-row > div,
.monitor-row > div {
  min-width: 0;
  flex: 1;
}

.rank-content > div {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.rank-content strong,
.speed-row strong,
.monitor-row strong {
  color: #cbd5e1;
  font-size: 9px;
  font-weight: 650;
}

.rank-content span,
.speed-row span,
.monitor-row span {
  color: #526178;
  font-size: 8px;
}

.rank-track {
  height: 3px;
  display: block;
  overflow: hidden;
  margin-top: 5px;
  background: rgba(71, 85, 105, 0.22);
}

.rank-track i {
  height: 100%;
  display: block;
  background: linear-gradient(90deg, #3b82f6, #06b6d4);
  box-shadow: 0 0 8px rgba(6, 182, 212, 0.35);
  transition: width 0.5s ease-out;
}

.speed-row {
  padding-bottom: 6px;
  border-bottom: 1px solid rgba(148, 163, 184, 0.07);
}

.speed-row > div strong,
.speed-row > div span,
.monitor-row > div strong,
.monitor-row > div span {
  display: block;
}

.speed-row > div span,
.monitor-row > div span {
  margin-top: 2px;
}

.speed-row em {
  color: #22d3ee;
  font-size: 10px;
  font-style: normal;
  font-weight: 760;
}

.monitor-dot,
.signal {
  width: 6px;
  height: 6px;
  flex: 0 0 6px;
  border-radius: 50%;
  background: #64748b;
  box-shadow: 0 0 8px currentColor;
}

.monitor-dot.success,
.signal.success {
  background: #22c55e;
  color: #22c55e;
}

.monitor-dot.warning,
.signal.warning {
  background: #eab308;
  color: #eab308;
}

.monitor-dot.danger,
.signal.danger {
  background: #ef4444;
  color: #ef4444;
}

.monitor-dot.info,
.signal.info {
  background: #0ea5e9;
  color: #0ea5e9;
}

.monitor-row em {
  font-size: 9px;
  font-style: normal;
  font-weight: 700;
}

.monitor-row em.success {
  color: #22c55e;
}

.monitor-row em.warning {
  color: #eab308;
}

.monitor-row em.danger {
  color: #ef4444;
}

.monitor-row em.info {
  color: #38bdf8;
}

.core-stage {
  min-height: 0;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  padding: 16px;
  border-color: rgba(59, 130, 246, 0.25);
  background:
    linear-gradient(145deg, rgba(30, 41, 59, 0.58), transparent 62%),
    rgba(7, 14, 31, 0.82);
  box-shadow: inset 0 0 70px rgba(59, 130, 246, 0.035), 0 20px 54px rgba(2, 6, 23, 0.32);
}

.core-stage::after {
  position: absolute;
  inset: 8px;
  pointer-events: none;
  border: 1px solid rgba(6, 182, 212, 0.055);
  content: "";
}

.core-header {
  position: relative;
  z-index: 2;
}

.core-header h2 {
  font-size: 14px;
}

.core-badge {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  color: #22d3ee;
  font-size: 8px;
}

.core-badge .icon {
  width: 13px;
  height: 13px;
}

.core-visual {
  position: relative;
  z-index: 2;
  min-height: 0;
  display: grid;
  grid-template-columns: minmax(180px, 0.9fr) minmax(190px, 1.1fr);
  align-items: center;
  gap: 16px;
}

.gauge-frame {
  position: relative;
  display: grid;
  place-items: center;
}

.gauge-ring {
  position: relative;
  width: min(23vh, 218px);
  aspect-ratio: 1;
  display: grid;
  place-items: center;
  border-radius: 50%;
  background: conic-gradient(from -90deg, #3b82f6 0deg, #06b6d4 var(--gauge-angle), rgba(30, 41, 59, 0.72) var(--gauge-angle), rgba(30, 41, 59, 0.72) 360deg);
  box-shadow: 0 0 44px rgba(59, 130, 246, 0.13), inset 0 0 34px rgba(6, 182, 212, 0.1);
  transition: background 0.5s ease-out;
}

.gauge-ring::before {
  position: absolute;
  inset: 10px;
  border: 1px solid rgba(147, 197, 253, 0.14);
  border-radius: 50%;
  background: #071020;
  box-shadow: inset 0 0 32px rgba(59, 130, 246, 0.09);
  content: "";
}

.gauge-inner {
  position: relative;
  z-index: 2;
  text-align: center;
}

.gauge-inner span,
.gauge-inner small {
  display: block;
}

.gauge-inner span {
  color: #64748b;
  font-size: 9px;
}

.gauge-inner strong {
  display: block;
  margin: 8px 0 6px;
  background: linear-gradient(90deg, #f8fafc, #67e8f9);
  background-clip: text;
  color: transparent;
  font-size: 31px;
  font-weight: 820;
  line-height: 1;
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.gauge-inner small {
  color: #334155;
  font-size: 8px;
}

.gauge-scan {
  position: absolute;
  width: min(25vh, 238px);
  aspect-ratio: 1;
  border: 1px dashed rgba(139, 92, 246, 0.22);
  border-radius: 50%;
  animation: gauge-rotate 18s linear infinite;
}

.gauge-scan::before,
.gauge-scan::after {
  position: absolute;
  width: 4px;
  height: 4px;
  border-radius: 50%;
  background: #8b5cf6;
  box-shadow: 0 0 10px #8b5cf6;
  content: "";
}

.gauge-scan::before {
  top: -2px;
  left: 50%;
}

.gauge-scan::after {
  right: -2px;
  top: 50%;
}

.core-facts {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 8px;
}

.core-facts article {
  min-height: 66px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 9px;
  border: 1px solid rgba(148, 163, 184, 0.08);
  background: rgba(15, 23, 42, 0.5);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.025);
  transition: border-color 0.2s ease, transform 0.2s ease;
}

.core-facts article:hover {
  border-color: rgba(96, 165, 250, 0.24);
  transform: translateY(-2px);
}

.fact-icon {
  width: 19px;
  height: 19px;
}

.fact-icon.success {
  color: #22c55e;
}

.fact-icon.warning {
  color: #eab308;
}

.fact-icon.danger {
  color: #ef4444;
}

.fact-icon.info {
  color: #38bdf8;
}

.core-facts span,
.core-facts strong {
  display: block;
}

.core-facts span {
  color: #64748b;
  font-size: 8px;
}

.core-facts strong {
  margin-top: 5px;
  color: #e2e8f0;
  font-size: 17px;
}

.core-footer {
  position: relative;
  z-index: 2;
  display: flex;
  justify-content: center;
  flex-wrap: wrap;
  gap: 18px;
  padding-top: 10px;
  border-top: 1px solid rgba(148, 163, 184, 0.08);
  color: #526178;
  font-size: 8px;
}

.core-footer span {
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.screen-empty {
  min-height: 70px;
  display: grid;
  place-items: center;
  color: #475569;
  font-size: 9px;
}

.cockpit-bottom {
  min-height: 0;
  display: grid;
  grid-template-columns: 1.45fr 1fr 1fr;
  gap: 12px;
}

.cockpit-shell .error-banner,
.cockpit-shell .success-banner {
  top: 78px;
  z-index: 220;
}

@keyframes screen-enter {
  from {
    opacity: 0;
    transform: translateY(10px) scale(0.992);
  }
}

@keyframes background-flow {
  from { background-position: -70% 0; }
  to { background-position: 170% 0; }
}

@keyframes stream-flow {
  0% {
    opacity: 0;
    transform: translateX(0);
  }
  12%, 82% {
    opacity: 0.45;
  }
  100% {
    opacity: 0;
    transform: translateX(140vw);
  }
}

@keyframes beacon-pulse {
  50% {
    opacity: 0.65;
    transform: scale(0.78);
  }
}

@keyframes icon-pulse {
  50% {
    opacity: 0.5;
    filter: drop-shadow(0 0 8px rgba(6, 182, 212, 0.7));
  }
}

@keyframes gauge-rotate {
  to { transform: rotate(360deg); }
}

@media (max-width: 1450px) {
  .cockpit-shell {
    gap: 9px;
    padding: 9px;
  }

  .screen-title h1 {
    font-size: 20px;
  }

  .title-line {
    width: 42px;
  }

  .cockpit-main {
    grid-template-columns: minmax(230px, 0.78fr) minmax(390px, 1.22fr) minmax(230px, 0.78fr);
    gap: 9px;
  }

  .left-column,
  .right-column,
  .cockpit-bottom,
  .kpi-strip {
    gap: 9px;
  }

  .kpi-value {
    font-size: 27px;
  }

  .gauge-ring {
    width: min(21vh, 190px);
  }

  .gauge-scan {
    width: min(23vh, 208px);
  }
}

@media (max-height: 850px) and (min-width: 1101px) {
  .cockpit-shell {
    grid-template-rows: 58px 42px 104px minmax(300px, 1fr) 188px;
    gap: 8px;
    padding: 8px;
  }

  .cockpit-header {
    grid-template-columns: minmax(230px, 1fr) auto minmax(230px, 1fr);
  }

  .screen-title h1 {
    font-size: 18px;
  }

  .screen-title p,
  .system-identity span {
    display: none;
  }

  .kpi-module {
    padding: 10px 12px;
  }

  .kpi-value {
    font-size: 24px;
  }

  .cockpit-shell :deep(.left-column .chart-canvas) {
    height: 118px !important;
  }

  .cockpit-shell :deep(.cockpit-bottom .chart-canvas) {
    height: 128px !important;
  }

  .cockpit-shell :deep(.chart-skeleton) {
    height: 118px;
  }

  .core-stage {
    padding: 12px;
  }

  .gauge-ring {
    width: min(21vh, 160px);
  }

  .gauge-scan {
    width: min(23vh, 178px);
  }

  .core-facts article {
    min-height: 52px;
    padding: 7px;
  }

  .rank-list,
  .speed-list,
  .monitor-list {
    margin-top: 6px;
    gap: 4px;
  }

  .rank-row,
  .speed-row,
  .monitor-row {
    min-height: 25px;
  }
}

@media (max-width: 1100px) {
  .cockpit-shell {
    position: fixed;
    display: block;
    padding: 10px;
  }

  .cockpit-header {
    min-height: 64px;
    grid-template-columns: 1fr auto;
  }

  .screen-title {
    grid-column: 1 / -1;
    grid-row: 1;
  }

  .header-side {
    grid-row: 2;
    min-height: 48px;
  }

  .cockpit-header {
    grid-template-rows: auto auto;
  }

  .control-strip,
  .kpi-strip,
  .cockpit-main,
  .cockpit-bottom {
    margin-top: 10px;
  }

  .kpi-strip {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .kpi-module {
    min-height: 118px;
  }

  .cockpit-main {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .core-stage {
    grid-column: 1 / -1;
    grid-row: 1;
    min-height: 420px;
  }

  .left-column,
  .right-column {
    min-height: 520px;
  }

  .cockpit-bottom {
    grid-template-columns: 1fr;
  }

  .cockpit-shell :deep(.cockpit-bottom .screen-chart) {
    min-height: 240px;
  }
}

@media (max-width: 680px) {
  .cockpit-shell {
    padding: 8px;
  }

  .cockpit-header {
    display: flex;
    min-height: 118px;
    flex-wrap: wrap;
    justify-content: space-between;
    padding: 8px 0;
  }

  .screen-title {
    order: -1;
    width: 100%;
  }

  .screen-title h1 {
    font-size: 17px;
  }

  .screen-title p,
  .title-line,
  .system-identity {
    display: none;
  }

  .header-side {
    min-height: auto;
    padding: 0 8px;
  }

  .clock-block {
    min-width: 92px;
  }

  .clock-block strong {
    font-size: 15px;
  }

  .control-strip {
    align-items: stretch;
    flex-direction: column;
    padding: 9px;
  }

  .screen-controls {
    display: grid;
    grid-template-columns: 1fr 1fr;
  }

  .month-control {
    grid-column: 1 / -1;
  }

  .month-control input {
    width: 100%;
  }

  .kpi-strip,
  .cockpit-main {
    grid-template-columns: 1fr;
  }

  .core-stage,
  .left-column,
  .right-column {
    grid-column: auto;
    min-height: auto;
  }

  .core-stage {
    min-height: 620px;
  }

  .core-visual {
    grid-template-columns: 1fr;
  }

  .gauge-ring {
    width: 210px;
  }

  .gauge-scan {
    width: 230px;
  }

  .left-column,
  .right-column {
    grid-template-rows: auto;
  }

  .cockpit-shell :deep(.screen-chart),
  .screen-panel {
    min-height: 250px;
  }
}

@media (prefers-reduced-motion: reduce) {
  .cockpit-shell,
  .cockpit-shell::after,
  .data-stream,
  .status-beacon,
  .pulse-icon,
  .gauge-scan {
    animation: none !important;
  }
}
</style>