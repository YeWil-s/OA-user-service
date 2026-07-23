<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">首页工作台</h2>
        <p class="page-subtitle">组织、考勤、审批与通知的运行概览</p>
      </div>
      <div class="toolbar">
        <button class="btn" type="button" :disabled="loading" @click="load">
          <RefreshCw class="icon" />
          {{ loading ? '刷新中' : '刷新' }}
        </button>
      </div>
    </div>

    <p v-if="error" class="error-banner">{{ error }}</p>

    <div v-if="loading" class="grid-4" aria-label="指标加载中">
      <article v-for="index in 4" :key="index" class="panel stat stat-skeleton">
        <span class="skeleton skeleton-line" />
        <strong class="skeleton skeleton-value" />
        <span class="skeleton skeleton-line" />
      </article>
    </div>
    <div v-else class="grid-4">
      <article v-for="item in stats" :key="item.label" class="panel stat">
        <span class="stat-label">{{ item.label }}</span>
        <strong class="stat-value"><AnimatedNumber :value="item.value" /></strong>
        <span class="stat-meta" :class="{ positive: statTrend(item.label) > 0, negative: statTrend(item.label) < 0 }">
          <TrendingUp v-if="statTrend(item.label) > 0" class="trend-icon" />
          <TrendingDown v-else-if="statTrend(item.label) < 0" class="trend-icon" />
          <Minus v-else class="trend-icon" />
          {{ item.meta }}
        </span>
        <SparklineBars :values="statSparkline(item.label)" />
      </article>
    </div>

    <div class="grid-2">
      <ChartPanel title="近 6 个月考勤趋势" :option="attendanceOption" :loading="loading" />
      <ChartPanel title="部门人数分布" :option="deptOption" :loading="loading" />
    </div>

    <div class="grid-2">
      <section class="panel panel-pad">
        <div class="section-head">
          <h3>待办提醒</h3>
          <RouterLink class="btn ghost" to="/approval/pending">
            <ClipboardCheck class="icon" />
            待审批
          </RouterLink>
        </div>
        <div class="todo-list">
          <div v-for="todo in todos" :key="todo.title" class="todo-item">
            <component :is="todo.icon" class="icon" />
            <div>
              <strong>{{ todo.title }}</strong>
              <span>{{ todo.desc }}</span>
            </div>
            <span class="pill" :class="todo.tone">{{ todo.count }}</span>
          </div>
        </div>
      </section>

      <section class="panel panel-pad">
        <div class="section-head">
          <h3>快捷入口</h3>
          <RouterLink class="btn ghost" to="/visual/dashboard">
            <BarChart3 class="icon" />
            看板
          </RouterLink>
        </div>
        <div class="quick-grid">
          <RouterLink v-for="item in quickLinks" :key="item.path" class="quick-link" :to="item.path">
            <component :is="item.icon" class="icon" />
            <span>{{ item.label }}</span>
          </RouterLink>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import {
  BarChart3,
  Bell,
  CalendarCheck,
  ClipboardCheck,
  MessageSquarePlus,
  Minus,
  RefreshCw,
  TrendingDown,
  TrendingUp,
  UserPlus,
  UsersRound
} from 'lucide-vue-next'
import { computed, onMounted, ref } from 'vue'
import AnimatedNumber from '@/components/AnimatedNumber.vue'
import ChartPanel from '@/components/ChartPanel.vue'
import SparklineBars from '@/components/SparklineBars.vue'
import { noticeApi, visualApi } from '@/api/services'
import type { AttendanceTrend, DeptMetric, VisualOverview } from '@/api/types'

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

const overview = ref<VisualOverview>({ ...emptyOverview })
const trends = ref<AttendanceTrend[]>([])
const depts = ref<DeptMetric[]>([])
const unread = ref(0)
const loading = ref(false)
const error = ref('')

const stats = computed(() => [
  { label: '在职员工', value: overview.value.totalEmployees, meta: `本月入职 ${overview.value.newHires}` },
  { label: '出勤率', value: `${overview.value.attendanceRate}%`, meta: `加班 ${overview.value.overtimeHours} 小时` },
  { label: '审批申请', value: overview.value.totalApplications, meta: `待处理 ${overview.value.pendingCount}` },
  { label: '未读消息', value: unread.value, meta: '公告与站内信' }
])

function statTrend(label: string) {
  if (label === '在职员工') return overview.value.newHires - overview.value.resignations
  if (label === '出勤率' && trends.value.length > 1) {
    return trends.value[trends.value.length - 1].attendanceRate - trends.value[0].attendanceRate
  }
  if (label === '审批申请') return overview.value.approvedCount - overview.value.rejectedCount
  return 0
}

function statSparkline(label: string) {
  if (label === '在职员工') {
    return [Math.max(0, overview.value.totalEmployees - overview.value.newHires), overview.value.totalEmployees]
  }
  if (label === '出勤率') return trends.value.map((item) => item.attendanceRate)
  if (label === '审批申请') {
    return [overview.value.approvedCount, overview.value.rejectedCount, overview.value.pendingCount]
  }
  return [0, unread.value]
}

const attendanceOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 36, right: 18, top: 32, bottom: 30 },
  xAxis: { type: 'category', data: trends.value.map((item) => item.statMonth) },
  yAxis: { type: 'value' },
  series: [
    { name: '正常', type: 'line', smooth: true, data: trends.value.map((item) => item.normalCount), color: '#3b82f6' },
    { name: '迟到', type: 'line', smooth: true, data: trends.value.map((item) => item.lateCount), color: '#f97316' },
    { name: '旷工', type: 'bar', data: trends.value.map((item) => item.absentCount), color: '#ef4444' }
  ]
}))

const deptOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'pie',
      radius: ['46%', '72%'],
      data: depts.value.map((item) => ({ name: item.deptName, value: item.value })),
      color: ['#3b82f6', '#8b5cf6', '#06b6d4', '#22c55e', '#ec4899']
    }
  ]
}))

const todos = computed(() => [
  { title: '待审批申请', desc: '请假、加班、外出申请', count: overview.value.pendingCount, tone: 'warn', icon: ClipboardCheck },
  { title: '未读消息', desc: '公告和站内通知', count: unread.value, tone: '', icon: Bell },
  { title: '考勤异常', desc: '迟到、早退、缺卡待确认', count: Math.round(overview.value.totalEmployees * 0.03), tone: 'danger', icon: CalendarCheck }
])

const quickLinks = [
  { label: '新增员工', path: '/system/employee', icon: UserPlus },
  { label: '发布公告', path: '/notice/list', icon: MessageSquarePlus },
  { label: '员工列表', path: '/system/employee', icon: UsersRound },
  { label: '考勤打卡', path: '/attendance/punch', icon: CalendarCheck }
]

async function load() {
  loading.value = true
  error.value = ''
  const errors: string[] = []

  const results = await Promise.allSettled([
    visualApi.overview(),
    visualApi.attendanceTrend(undefined, 6),
    visualApi.deptDistribution(),
    noticeApi.unreadCount()
  ])

  if (results[0].status === 'fulfilled') { overview.value = results[0].value } else { errors.push('概览') }
  if (results[1].status === 'fulfilled') { trends.value = results[1].value } else { errors.push('考勤趋势') }
  if (results[2].status === 'fulfilled') { depts.value = results[2].value } else { errors.push('部门分布') }
  if (results[3].status === 'fulfilled') { unread.value = (results[3].value).totalUnread } else { errors.push('未读消息') }

  if (errors.length > 0) {
    error.value = '部分数据加载失败: ' + errors.join('、')
  }

  loading.value = false
}

onMounted(load)
</script>

<style scoped>
.stat-meta {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.stat-meta.positive {
  color: var(--success);
}

.stat-meta.negative {
  color: var(--danger);
}

.trend-icon {
  width: 13px;
  height: 13px;
}

.stat-skeleton {
  grid-template-columns: 1fr;
}

.stat-skeleton .skeleton-line {
  width: 72%;
}

.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.section-head h3 {
  margin: 0;
  font-size: 16px;
}

.todo-list,
.quick-grid {
  display: grid;
  gap: 10px;
}

.todo-item {
  min-height: 58px;
  display: grid;
  grid-template-columns: 28px minmax(0, 1fr) auto;
  align-items: center;
  gap: 12px;
  padding: 0 10px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--surface-soft);
}

.todo-item strong,
.todo-item span {
  display: block;
}

.todo-item strong {
  font-size: 14px;
}

.todo-item span:not(.pill) {
  margin-top: 3px;
  color: var(--muted);
  font-size: 12px;
}

.quick-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.quick-link {
  min-height: 74px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 14px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--surface-soft);
  font-weight: 700;
}

.quick-link:hover {
  border-color: #aeb9c8;
}

@media (max-width: 640px) {
  .quick-grid {
    grid-template-columns: 1fr;
  }
}
</style>