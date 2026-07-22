<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">考勤打卡</h2>
        <p class="page-subtitle">今日打卡、本月概览与异常提醒</p>
      </div>
      <span class="mock-banner">考勤后端未完成，当前为界面流程</span>
    </div>

    <div class="grid-3">
      <section class="panel panel-pad punch-panel">
        <CalendarCheck class="punch-icon" />
        <h3>{{ statusText }}</h3>
        <p>{{ now }}</p>
        <div class="toolbar punch-actions">
          <button class="btn primary" @click="punchIn"><LogIn class="icon" />上班打卡</button>
          <button class="btn ghost" @click="punchOut"><LogOut class="icon" />下班打卡</button>
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
        <div><span>打卡地点</span><strong>办公区 Wi-Fi</strong></div>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { CalendarCheck, LogIn, LogOut } from 'lucide-vue-next'
import { computed, reactive, ref } from 'vue'

const punch = reactive({ in: '', out: '' })
const now = ref(new Date().toLocaleString())
setInterval(() => (now.value = new Date().toLocaleString()), 1000)

const statusText = computed(() => punch.out ? '今日考勤完成' : punch.in ? '已打上班卡' : '等待打卡')
const stats = [
  { label: '本月正常', value: 18, meta: '工作日统计' },
  { label: '迟到/早退', value: 2, meta: '待主管确认' }
]

function punchIn() { punch.in = new Date().toLocaleTimeString() }
function punchOut() { punch.out = new Date().toLocaleTimeString() }
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
