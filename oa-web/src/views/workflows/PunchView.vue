<script setup lang="ts">
import { CalendarCheck, LogIn, LogOut, RefreshCw, ClockAlert, Timer } from 'lucide-vue-next'
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

const punch = reactive({ in: '', out: '', location: '办公区 Wi-Fi', lateMinutes: 0, earlyMinutes: 0, statusLabel: '' })
const gps = ref<{ lat: number; lng: number } | null>(null)
const gpsError = ref('')

function getPosition(): Promise<{ lat: number; lng: number } | null> {
  return new Promise((resolve) => {
    if (!navigator.geolocation) {
      gpsError.value = '浏览器不支持GPS定位'
      resolve(null)
      return
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        gps.value = { lat: pos.coords.latitude, lng: pos.coords.longitude }
        gpsError.value = ''
        resolve(gps.value)
      },
      (err) => {
        gpsError.value = '定位失败: ' + err.message
        resolve(null)
      },
      { enableHighAccuracy: true, timeout: 8000, maximumAge: 60000 }
    )
  })
}
const now = ref(new Date().toLocaleString())
const records = ref<AttendanceRecord[]>([])
const loading = ref(false)
const submitting = ref(false)
const error = ref('')
const message = ref('')
let timer: ReturnType<typeof setInterval> | undefined

const hasPunchedIn = computed(() => !!punch.in)
const hasPunchedOut = computed(() => !!punch.out)
const canPunchIn = computed(() => !hasPunchedIn.value && !hasPunchedOut.value)
const canPunchOut = computed(() => hasPunchedIn.value && !hasPunchedOut.value)

const statusText = computed(() => {
  if (hasPunchedOut.value) return punch.statusLabel || '今日考勤完成'
  if (hasPunchedIn.value) return '已打上班卡，等待下班打卡'
  return '等待打卡'
})

const stats = computed(() => {
  const normal = records.value.filter((item) => item.statusLabel === '正常').length
  const abnormal = records.value.length - normal
  return [
    { label: '本月正常', value: normal, meta: '正常考勤天数' },
    { label: '异常记录', value: abnormal, meta: '迟到、早退、缺卡' }
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
    punch.lateMinutes = today?.lateMinutes || 0
    punch.earlyMinutes = today?.earlyMinutes || 0
    punch.statusLabel = today?.statusLabel || ''
  } catch (err) {
    error.value = err instanceof Error ? err.message : '考勤记录加载失败'
  } finally {
    loading.value = false
  }
}

async function doPunch(isIn: boolean) {
  submitting.value = true
  error.value = ''
  message.value = ''
  try {
    const pos = await getPosition()
    const api = isIn ? attendanceApi.punchIn : attendanceApi.punchOut
    const result = await api({
      deviceInfo: navigator.userAgent,
      location: punch.location,
      latitude: pos?.lat,
      longitude: pos?.lng
    })
    message.value = result.message || (isIn ? '上班打卡成功' : '下班打卡成功')
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : (isIn ? '上班' : '下班') + '打卡失败'
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  load()
  timer = setInterval(() => (now.value = new Date().toLocaleString()), 1000)
})
onUnmounted(() => { if (timer) clearInterval(timer) })
</script>

<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">考勤打卡</h2>
        <p class="page-subtitle">今日打卡、本月概览与异常提醒</p>
      </div>
      <button class="btn" :disabled="loading" @click="load"><RefreshCw :class="['icon', loading && 'spin']" />{{ loading ? '刷新中' : '刷新' }}</button>
    </div>

    <p v-if="error" class="error-banner">{{ error }}</p>
    <p v-if="message" class="success-banner">{{ message }}</p>

    <div class="grid-3">
      <!-- 打卡区域 -->
      <section class="panel punch-panel">
        <CalendarCheck class="punch-icon" />
        <h3>{{ statusText }}</h3>
        <p class="current-time">{{ now }}</p>

        <div v-if="punch.lateMinutes" class="alert-tag late"><ClockAlert class="icon-sm" />迟到 {{ punch.lateMinutes }} 分钟</div>
        <div v-if="punch.earlyMinutes" class="alert-tag early"><Timer class="icon-sm" />早退 {{ punch.earlyMinutes }} 分钟</div>

        <p v-if="gps" class="gps-ok">GPS: {{ gps.lat.toFixed(4) }}, {{ gps.lng.toFixed(4) }}</p>
        <p v-if="gpsError" class="gps-err">{{ gpsError }}</p>

        <div class="punch-btns">
          <button class="btn primary" :disabled="submitting || !canPunchIn" @click="doPunch(true)">
            <LogIn class="icon" />上班打卡
          </button>
          <button class="btn ghost" :disabled="submitting || !canPunchOut" @click="doPunch(false)">
            <LogOut class="icon" />下班打卡
          </button>
        </div>
      </section>

      <!-- 统计卡片 -->
      <article v-for="item in stats" :key="item.label" class="panel stat-card">
        <span class="stat-label">{{ item.label }}</span>
        <strong class="stat-value">{{ item.value }}</strong>
        <span class="stat-meta">{{ item.meta }}</span>
      </article>
    </div>

    <!-- 今日记录 -->
    <section class="panel record-panel">
      <h3 class="section-title">今日记录</h3>
      <div class="record-grid">
        <div class="record-item">
          <LogIn class="icon-sm icon-in" />
          <span>上班打卡</span>
          <strong>{{ punch.in || '-' }}</strong>
        </div>
        <div class="record-item">
          <LogOut class="icon-sm icon-out" />
          <span>下班打卡</span>
          <strong>{{ punch.out || '-' }}</strong>
        </div>
        <div class="record-item">
          <span>打卡地点</span>
          <strong>{{ punch.location || '-' }}</strong>
        </div>
      </div>
    </section>
  </section>
</template>

<style scoped>
.punch-panel {
  min-height: 240px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  text-align: center;
  padding: 28px 20px;
}
.punch-icon { width: 48px; height: 48px; color: var(--primary); }
.punch-panel h3 { margin: 4px 0 0; font-size: 20px; font-weight: 700; }
.current-time { margin: 0; color: var(--text-muted); font-size: 14px; font-variant-numeric: tabular-nums; }
.punch-btns { display: flex; gap: 10px; margin-top: 6px; }
.btn.primary:disabled, .btn.ghost:disabled { opacity: 0.4; cursor: not-allowed; }

.alert-tag { display: flex; align-items: center; gap: 4px; padding: 3px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; }
.alert-tag.late { background: #fef3c7; color: #92400e; }
.alert-tag.early { background: #fee2e2; color: #991b1b; }
.gps-ok { margin: 2px 0; font-size: 11px; color: var(--text-muted); }
.gps-err { margin: 2px 0; font-size: 11px; color: #ef4444; }

.stat-card { background: var(--bg-card); border: 1px solid var(--border-color); border-radius: 14px; padding: 20px; display: flex; flex-direction: column; gap: 4px; justify-content: center; }
.stat-card .stat-label { font-size: 13px; color: var(--text-secondary); }
.stat-card .stat-value { font-size: 28px; font-weight: 800; color: var(--text-primary); }
.stat-card .stat-meta { font-size: 12px; color: var(--text-muted); }

.section-title { margin: 0 0 14px; font-size: 16px; font-weight: 600; }
.record-panel { padding: 20px; border: 1px solid var(--border-color); border-radius: 14px; background: var(--bg-card); margin-top: 20px; }
.record-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; }
.record-item { padding: 14px; border: 1px solid var(--border-color); border-radius: 10px; background: var(--bg-subtle); display: flex; flex-direction: column; gap: 4px; }
.record-item span { color: var(--text-muted); font-size: 12px; }
.record-item strong { font-size: 16px; font-weight: 700; }

.icon-sm { width: 16px; height: 16px; }
.icon-in { color: var(--primary); }
.icon-out { color: #f59e0b; }
.spin { animation: spin 1s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

@media (max-width: 760px) {
  .record-grid { grid-template-columns: 1fr; }
}
</style>
