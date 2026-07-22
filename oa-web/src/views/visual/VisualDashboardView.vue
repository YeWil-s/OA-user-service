<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">数据可视化</h2>
        <p class="page-subtitle">人事、考勤、审批三类经营指标</p>
      </div>
      <div class="toolbar">
        <span v-if="mocked" class="mock-banner">演示数据</span>
        <input v-model="month" class="field month-field" type="month" />
        <button class="btn" @click="load"><RefreshCw class="icon" />刷新</button>
        <button class="btn primary" @click="sync"><DatabaseZap class="icon" />同步统计</button>
      </div>
    </div>

    <div class="grid-4">
      <article v-for="item in stats" :key="item.label" class="panel stat">
        <span class="stat-label">{{ item.label }}</span>
        <strong class="stat-value">{{ item.value }}</strong>
        <span class="stat-meta">{{ item.meta }}</span>
      </article>
    </div>

    <div class="grid-2">
      <ChartPanel title="部门人数分布" :option="deptPie" />
      <ChartPanel title="月度考勤趋势" :option="attendanceLine" />
      <ChartPanel title="部门加班对比" :option="overtimeBar" />
      <ChartPanel title="审批效率排行" :option="approvalSpeedBar" />
    </div>
  </section>
</template>

<script setup lang="ts">
import { DatabaseZap, RefreshCw } from 'lucide-vue-next'
import { computed, onMounted, ref } from 'vue'
import ChartPanel from '@/components/ChartPanel.vue'
import { withFallback } from '@/api/http'
import { visualApi } from '@/api/services'
import { mockApprovalStats, mockAttendanceTrend, mockDeptDistribution, mockOverview } from '@/api/mock'
import type { ApprovalStats, AttendanceTrend, DeptMetric, VisualOverview } from '@/api/types'

const month = ref('2026-07')
const overview = ref<VisualOverview>(mockOverview)
const depts = ref<DeptMetric[]>(mockDeptDistribution)
const overtime = ref<DeptMetric[]>(mockDeptDistribution.map((item, index) => ({ ...item, value: 80 + index * 36, unit: '小时' })))
const trends = ref<AttendanceTrend[]>(mockAttendanceTrend)
const approval = ref<ApprovalStats>(mockApprovalStats)
const speed = ref([{ deptId: 2, deptName: '技术部', totalApplications: 32, avgApprovalHours: 4.5 }, { deptId: 3, deptName: '人事部', totalApplications: 21, avgApprovalHours: 5.2 }])
const mocked = ref(false)

const stats = computed(() => [
  { label: '总人数', value: overview.value.totalEmployees, meta: `新增 ${overview.value.newHires} / 离职 ${overview.value.resignations}` },
  { label: '出勤率', value: `${overview.value.attendanceRate}%`, meta: `加班 ${overview.value.overtimeHours} 小时` },
  { label: '审批通过率', value: `${overview.value.approvalPassRate}%`, meta: `平均 ${overview.value.avgApprovalHours} 小时` },
  { label: '待审批', value: approval.value.pendingCount, meta: `总申请 ${approval.value.totalApplications}` }
])

const deptPie = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{ type: 'pie', radius: ['42%', '70%'], data: depts.value.map((item) => ({ name: item.deptName, value: item.value })), color: ['#126f83', '#b87716', '#6b5db8', '#1f8a58', '#be3a45'] }]
}))

const attendanceLine = computed(() => ({
  tooltip: { trigger: 'axis' },
  legend: { top: 0 },
  grid: { left: 36, right: 18, top: 42, bottom: 30 },
  xAxis: { type: 'category', data: trends.value.map((item) => item.statMonth) },
  yAxis: { type: 'value' },
  series: [
    { name: '正常', type: 'line', smooth: true, data: trends.value.map((item) => item.normalCount), color: '#126f83' },
    { name: '迟到', type: 'line', smooth: true, data: trends.value.map((item) => item.lateCount), color: '#b87716' },
    { name: '请假', type: 'line', smooth: true, data: trends.value.map((item) => item.leaveCount), color: '#6b5db8' }
  ]
}))

const overtimeBar = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 38, right: 18, top: 26, bottom: 34 },
  xAxis: { type: 'category', data: overtime.value.map((item) => item.deptName) },
  yAxis: { type: 'value' },
  series: [{ type: 'bar', data: overtime.value.map((item) => item.value), color: '#b87716' }]
}))

const approvalSpeedBar = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 42, right: 18, top: 26, bottom: 34 },
  xAxis: { type: 'category', data: speed.value.map((item) => item.deptName) },
  yAxis: { type: 'value' },
  series: [{ type: 'bar', data: speed.value.map((item) => item.avgApprovalHours), color: '#126f83' }]
}))

async function load() {
  const [overviewResult, deptResult, overtimeResult, trendResult, approvalResult, speedResult] = await Promise.all([
    withFallback(visualApi.overview(month.value), mockOverview),
    withFallback(visualApi.deptDistribution(month.value), mockDeptDistribution),
    withFallback(visualApi.deptOvertime(month.value), overtime.value),
    withFallback(visualApi.attendanceTrend(month.value, 6), mockAttendanceTrend),
    withFallback(visualApi.approvalStats(month.value), mockApprovalStats),
    withFallback(visualApi.approvalSpeed(month.value), speed.value)
  ])
  overview.value = overviewResult.data
  depts.value = deptResult.data
  overtime.value = overtimeResult.data
  trends.value = trendResult.data
  approval.value = approvalResult.data
  speed.value = speedResult.data
  mocked.value = [overviewResult, deptResult, overtimeResult, trendResult, approvalResult, speedResult].some((item) => item.mocked)
}

async function sync() {
  if (!mocked.value) await visualApi.sync(month.value)
  await load()
}

onMounted(load)
</script>

<style scoped>
.month-field {
  width: 150px;
}
</style>
