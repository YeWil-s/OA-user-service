<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">首页工作台</h2>
        <p class="page-subtitle">组织、考勤、审批与通知的运行概览</p>
      </div>
      <div class="toolbar">
        <span v-if="mocked" class="mock-banner">
          <Info class="icon" />
          当前显示演示数据
        </span>
        <button class="btn" type="button" @click="load">
          <RefreshCw class="icon" />
          刷新
        </button>
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
      <ChartPanel title="近 6 个月考勤趋势" :option="attendanceOption" />
      <ChartPanel title="部门人数分布" :option="deptOption" />
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
  Info,
  MessageSquarePlus,
  RefreshCw,
  UserPlus,
  UsersRound
} from 'lucide-vue-next'
import { computed, onMounted, ref } from 'vue'
import ChartPanel from '@/components/ChartPanel.vue'
import { withFallback } from '@/api/http'
import { visualApi, noticeApi } from '@/api/services'
import { mockAttendanceTrend, mockDeptDistribution, mockOverview } from '@/api/mock'
import type { AttendanceTrend, DeptMetric, VisualOverview } from '@/api/types'

const overview = ref<VisualOverview>(mockOverview)
const trends = ref<AttendanceTrend[]>(mockAttendanceTrend)
const depts = ref<DeptMetric[]>(mockDeptDistribution)
const unread = ref(3)
const mocked = ref(false)

const stats = computed(() => [
  { label: '在职员工', value: overview.value.totalEmployees, meta: `本月入职 ${overview.value.newHires}` },
  { label: '出勤率', value: `${overview.value.attendanceRate}%`, meta: `加班 ${overview.value.overtimeHours} 小时` },
  { label: '审批申请', value: overview.value.totalApplications, meta: `待处理 ${overview.value.pendingCount}` },
  { label: '未读消息', value: unread.value, meta: '公告与站内信' }
])

const attendanceOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: 36, right: 18, top: 32, bottom: 30 },
  xAxis: { type: 'category', data: trends.value.map((item) => item.statMonth) },
  yAxis: { type: 'value' },
  series: [
    { name: '正常', type: 'line', smooth: true, data: trends.value.map((item) => item.normalCount), color: '#126f83' },
    { name: '迟到', type: 'line', smooth: true, data: trends.value.map((item) => item.lateCount), color: '#b87716' },
    { name: '旷工', type: 'bar', data: trends.value.map((item) => item.absentCount), color: '#be3a45' }
  ]
}))

const deptOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [
    {
      type: 'pie',
      radius: ['46%', '72%'],
      data: depts.value.map((item) => ({ name: item.deptName, value: item.value })),
      color: ['#126f83', '#b87716', '#6b5db8', '#1f8a58', '#be3a45']
    }
  ]
}))

const todos = computed(() => [
  { title: '待审批申请', desc: '请假、加班、外出申请', count: overview.value.pendingCount, tone: 'warn', icon: ClipboardCheck },
  { title: '未读消息', desc: '公告和站内通知', count: unread.value, tone: '', icon: Bell },
  { title: '考勤异常', desc: '迟到、早退、缺卡待确认', count: Math.max(2, Math.round(overview.value.totalEmployees * 0.03)), tone: 'danger', icon: CalendarCheck }
])

const quickLinks = [
  { label: '新增员工', path: '/system/employee', icon: UserPlus },
  { label: '发布公告', path: '/notice/list', icon: MessageSquarePlus },
  { label: '员工列表', path: '/system/employee', icon: UsersRound },
  { label: '考勤打卡', path: '/attendance/punch', icon: CalendarCheck }
]

async function load() {
  const [overviewResult, trendResult, deptResult, unreadResult] = await Promise.all([
    withFallback(visualApi.overview(), mockOverview),
    withFallback(visualApi.attendanceTrend(undefined, 6), mockAttendanceTrend),
    withFallback(visualApi.deptDistribution(), mockDeptDistribution),
    withFallback(noticeApi.unreadCount(), { noticeUnread: 1, messageUnread: 2, totalUnread: 3 })
  ])
  overview.value = overviewResult.data
  trends.value = trendResult.data
  depts.value = deptResult.data
  unread.value = unreadResult.data.totalUnread
  mocked.value = overviewResult.mocked || trendResult.mocked || deptResult.mocked || unreadResult.mocked
}

onMounted(load)
</script>

<style scoped>
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
