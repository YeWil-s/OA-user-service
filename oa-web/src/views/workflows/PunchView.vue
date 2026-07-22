<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">考勤打卡</h2>
        <p class="page-subtitle">今日打卡、本月概览与异常提醒</p>
      </div>
      <button class="btn" :disabled="loading" @click="load"><RefreshCw class="icon" />{{ loading ? '刷新中' : '刷新' }}</button>
    </div>

    <p v-if="error" class="error-banner">{{ error }}</p>
    <p v-if="message" class="success-banner">{{ message }}</p>

    <div class="grid-3">
      <section class="panel panel-pad punch-panel">
        <CalendarCheck class="punch-icon" />
        <h3>{{ statusText }}</h3>
        <p>{{ now }}</p>
        <div class="toolbar punch-actions">
          <button class="btn primary" :disabled="submitting" @click="punchIn"><LogIn class="icon" />上班打卡</button>
          <button class="btn ghost" :disabled="submitting" @click="punchOut"><LogOut class="icon" />下班打卡</button>
        </div>
      </section>
      <article v-for="item in stats" :key="item.label" class="panel stat">
        <span class="stat-label">{{ item.label }}</span>
        <strong class="stat-value">{{ item.value }}</strong>
        <span class="stat-meta">{{ item.meta }}</span>
      </article>
    </div>

    <section class="panel panel-pad">
      <h3 class="section-title">今日记录</h3>
      <div class="record-grid">
        <div><span>上班打卡</span><strong>{{ punch.in || '-' }}</strong></div>
        <div><span>下班打卡</span><strong>{{ punch.out || '-' }}</strong></div>
        <div><span>打卡地点</span><strong>{{ punch.location || '-' }}</strong></div>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { CalendarCheck, LogIn, LogOut, RefreshCw } from 'lucide-vue-next'
import { computed, onMounted, onUnmounted, reactive, ref } from 'vue'
import { attendanceApi } from '@/api/services'
import type { AttendanceRecord } from '@/api/types'

function currentMonth() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
}

function currentDate() {
  const now = new Date()
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
}

function formatTime(value?: string | null) {
  if (!value) return ''
  return value.includes('T') ? value.slice(11, 19) : value
}

const punch = reactive({ in: '', out: '', location: '办公区 Wi-Fi' })
const now = ref(new Date().toLocaleString())
const records = ref<AttendanceRecord[]>([])
const loading = ref(false)
const submitting = ref(false)
const error = ref('')
const message = ref('')
let timer: ReturnType<typeof setInterval> | undefined

const statusText = computed(() => punch.out ? '今日考勤完成' : punch.in ? '已打上班卡' : '等待打卡')
const stats = computed(() => {
  const normal = records.value.filter((item) => item.statusLabel === '正常').length
  const abnormal = records.value.length - normal
  return [
    { label: '本月正常', value: normal, meta: '个人考勤记录' },
    { label: '异常记录', value: abnormal, meta: '迟到、早退、缺卡等' }
  ]
})

const pageRows = <T,>(page: { records?: T[]; list?: T[] }) => page.records || page.list || []

async function load() {
  loading.value = true
  error.value = ''
  try {
    const result = await attendanceApi.myRecords({ month: currentMonth(), pageNum: 1, pageSize: 100 })
    records.value = pageRows(result)
    const today = records.value.find((item) => item.recordDate === currentDate())
    punch.in = formatTime(today?.punchInTime)
    punch.out = formatTime(today?.punchOutTime)
    punch.location = today?.location || '办公区 Wi-Fi'
  } catch (err) {
    error.value = err instanceof Error ? err.message : '考勤记录加载失败'
  } finally {
    loading.value = false
  }
}

async function punchIn() {
  submitting.value = true
  error.value = ''
  message.value = ''
  try {
    const result = await attendanceApi.punchIn({ punchType: 1, deviceInfo: navigator.userAgent, location: punch.location })
    message.value = result.message || '上班打卡成功'
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '上班打卡失败'
  } finally {
    submitting.value = false
  }
}

async function punchOut() {
  submitting.value = true
  error.value = ''
  message.value = ''
  try {
    const result = await attendanceApi.punchOut({ punchType: 1, deviceInfo: navigator.userAgent, location: punch.location })
    message.value = result.message || '下班打卡成功'
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '下班打卡失败'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  load()
  timer = setInterval(() => (now.value = new Date().toLocaleString()), 1000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})
</script>

<style scoped>
.punch-panel {
  min-height: 220px;
  display: grid;
  place-items: center;
  text-align: center;
}

.punch-icon {
  width: 52px;
  height: 52px;
  color: var(--primary);
}

.punch-panel h3 {
  margin: 8px 0 0;
  font-size: 22px;
}

.punch-panel p {
  margin: 0;
  color: var(--muted);
}

.punch-actions {
  justify-content: center;
}

.section-title {
  margin: 0 0 14px;
}

.record-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.record-grid div {
  min-height: 86px;
  display: grid;
  align-content: center;
  gap: 8px;
  padding: 0 14px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--surface-soft);
}

.record-grid span {
  color: var(--muted);
  font-size: 13px;
}

@media (max-width: 760px) {
  .record-grid {
    grid-template-columns: 1fr;
  }
}
</style>