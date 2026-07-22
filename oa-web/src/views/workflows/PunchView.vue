<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">考勤打卡</h2>
        <p class="page-subtitle">{{ today }}</p>
      </div>
    </div>

    <div class="punch-center">
      <div class="punch-card">
        <div class="punch-time">{{ now }}</div>
        <div class="punch-status">
          <span v-if="punchedIn && !punchedOut" class="pill success">已打上班卡</span>
          <span v-else-if="punchedOut" class="pill success">今日打卡完成</span>
          <span v-else class="pill muted">未打卡</span>
        </div>
        <div class="punch-info" v-if="lastPunch">
          <p v-if="lastPunch.punchInTime">上班: {{ lastPunch.punchInTime }}</p>
          <p v-if="lastPunch.punchOutTime">下班: {{ lastPunch.punchOutTime }}</p>
        </div>
        <div class="punch-actions">
          <button class="btn primary punch-btn" :disabled="punchedIn" @click="doPunchIn">上班打卡</button>
          <button class="btn punch-btn" :disabled="!punchedIn || punchedOut" @click="doPunchOut">下班打卡</button>
        </div>
        <p v-if="punchMsg" class="punch-msg">{{ punchMsg }}</p>
      </div>
    </div>

    <section class="panel panel-pad">
      <h3>本月考勤记录</h3>
      <div v-if="loading" class="empty">加载中...</div>
      <div v-else-if="records.length === 0" class="empty">暂无记录</div>
      <div v-else class="table-wrap">
        <table class="data-table">
          <thead><tr><th>日期</th><th>上班</th><th>下班</th><th>迟到(分)</th><th>早退(分)</th><th>工时</th><th>状态</th></tr></thead>
          <tbody>
            <tr v-for="r in records" :key="r.id">
              <td>{{ r.recordDate }}</td>
              <td>{{ r.punchInTime ?? '-' }}</td>
              <td>{{ r.punchOutTime ?? '-' }}</td>
              <td>{{ r.lateMinutes ?? '-' }}</td>
              <td>{{ r.earlyMinutes ?? '-' }}</td>
              <td>{{ r.workHours ?? '-' }}</td>
              <td>{{ r.statusLabel }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onMounted, onUnmounted, ref } from 'vue'
import { attendanceApi } from '@/api/services'
import type { AttendanceRecord, PunchVO } from '@/api/types'

const now = ref('')
const today = new Date().toISOString().slice(0, 10)
let timer: ReturnType<typeof setInterval>

const punchedIn = ref(false)
const punchedOut = ref(false)
const punchMsg = ref('')
const lastPunch = ref<Partial<AttendanceRecord>>({})
const records = ref<AttendanceRecord[]>([])
const loading = ref(false)

onMounted(() => {
  updateTime()
  timer = setInterval(updateTime, 1000)
  loadRecords()
  loadTodayStatus()
})

onUnmounted(() => clearInterval(timer))

function updateTime() { now.value = new Date().toLocaleTimeString() }

async function loadTodayStatus() {
  try {
    const res = await attendanceApi.myRecords({ pageNum: 1, pageSize: 1 })
    const todayRecord = (res.records ?? [])[0]
    if (todayRecord) {
      lastPunch.value = todayRecord
      punchedIn.value = !!todayRecord.punchInTime
      punchedOut.value = !!todayRecord.punchOutTime
    }
  } catch { /* ignore */ }
}

async function loadRecords() {
  loading.value = true
  const month = today.slice(0, 7)
  try {
    const res = await attendanceApi.myRecords({ month, pageNum: 1, pageSize: 31 })
    records.value = res.records ?? []
  } catch { /* ignore */ }
  loading.value = false
}

async function doPunchIn() {
  try {
    const res: PunchVO = await attendanceApi.punchIn()
    punchMsg.value = res.message
    punchedIn.value = true
    loadTodayStatus()
    loadRecords()
  } catch (e: unknown) { punchMsg.value = (e as Error).message }
}

async function doPunchOut() {
  try {
    const res: PunchVO = await attendanceApi.punchOut()
    punchMsg.value = res.message
    punchedOut.value = true
    loadTodayStatus()
    loadRecords()
  } catch (e: unknown) { punchMsg.value = (e as Error).message }
}
</script>

<style scoped>
.punch-center { display: grid; place-items: center; padding: 24px; }
.punch-card { text-align: center; padding: 32px; border-radius: 12px; background: var(--surface-soft); border: 1px solid var(--border); min-width: 320px; }
.punch-time { font-size: 36px; font-weight: 700; margin-bottom: 8px; }
.punch-status { margin-bottom: 12px; }
.punch-info p { margin: 4px 0; color: var(--muted); }
.punch-actions { display: flex; gap: 12px; justify-content: center; margin: 16px 0; }
.punch-btn { width: 120px; height: 44px; }
.punch-msg { margin-top: 8px; color: var(--primary); font-weight: 500; }
.empty { text-align: center; padding: 24px; color: var(--muted); }
</style>
