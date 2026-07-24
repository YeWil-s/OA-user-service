<script setup lang="ts">
import { Calendar, ChevronLeft, ChevronRight, Users, Table2, User } from 'lucide-vue-next'
import { ref, computed, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { attendanceApi } from '@/api/services'
import type { Schedule, Shift, UserShift } from '@/api/types'

const auth = useAuthStore()
const isAdmin = computed(() => {
  const roles = auth.user?.roles || []
  return roles.some((r: string) => r === 'ROLE_ADMIN' || r === 'ROLE_HR')
})

type Tab = 'mine' | 'people' | 'master'
const activeTab = ref<Tab>('mine')
const currentMonday = ref(getMonday(new Date()))
const schedules = ref<Schedule[]>([])
const shifts = ref<Shift[]>([])
const people = ref<UserShift[]>([])
const loading = ref(false)
const editingItem = ref<Schedule | null>(null)
const editShiftId = ref<number | null>(null)
const selectedUserId = ref<number | null>(null)
const selectedUserName = ref('')

const weekDays = computed(() => {
  const days = []
  const labels = ['一', '二', '三', '四', '五', '六', '日']
  for (let i = 0; i < 7; i++) {
    const d = new Date(currentMonday.value)
    d.setDate(d.getDate() + i)
    days.push({ date: d, label: labels[i], dateStr: fmt(d) })
  }
  return days
})
const weekLabel = computed(() => {
  const end = new Date(currentMonday.value)
  end.setDate(end.getDate() + 6)
  return `${fmt(currentMonday.value)} ~ ${fmt(end)}`
})

function getMonday(d: Date): Date {
  const dt = new Date(d)
  const day = dt.getDay()
  dt.setDate(dt.getDate() - (day === 0 ? 6 : day - 1))
  dt.setHours(0, 0, 0, 0)
  return dt
}
function fmt(d: Date): string { return d.toISOString().split('T')[0] }
function prevWeek() { const d = new Date(currentMonday.value); d.setDate(d.getDate() - 7); currentMonday.value = d }
function nextWeek() { const d = new Date(currentMonday.value); d.setDate(d.getDate() + 7); currentMonday.value = d }
function goToday() { currentMonday.value = getMonday(new Date()) }
function isToday(d: Date): boolean { return fmt(d) === fmt(new Date()) }
function isWeekend(d: Date): boolean { return d.getDay() === 0 || d.getDay() === 6 }
function isPast(d: Date): boolean { const now = new Date(); now.setHours(23,59,59,999); return d < now }
function getSched(dateStr: string): Schedule | undefined { return schedules.value.find(s => s.scheduleDate === dateStr) }

async function fetchData() {
  loading.value = true
  try {
    const [start, end] = [weekDays.value[0].dateStr, weekDays.value[6].dateStr]
    if (activeTab.value === 'mine') {
      schedules.value = await attendanceApi.mySchedules(start, end)
    } else if (activeTab.value === 'people' && selectedUserId.value) {
      schedules.value = await attendanceApi.userSchedules(selectedUserId.value, start, end)
    } else if (activeTab.value === 'master') {
      const all: Schedule[] = []
      for (const p of people.value) {
        try { const s = await attendanceApi.userSchedules(p.userId, start, end); all.push(...s) } catch {}
      }
      schedules.value = all
    }
  } catch { schedules.value = [] } finally { loading.value = false }
}

async function fetchShifts() {
  try { const r = await attendanceApi.shifts({ pageNum: 1, pageSize: 50 }); shifts.value = r.records || r.list || [] } catch { shifts.value = [] }
}
async function fetchPeople() {
  try { people.value = await attendanceApi.allUserShifts() } catch { people.value = [] }
}
function selectPerson(p: UserShift) { selectedUserId.value = p.userId; selectedUserName.value = p.userName; activeTab.value = 'people' }
function backToPeople() { selectedUserId.value = null; selectedUserName.value = '' }
function startEdit(s: Schedule) { editingItem.value = s; editShiftId.value = s.shiftId }
function cancelEdit() { editingItem.value = null; editShiftId.value = null }
async function saveEdit() {
  if (!editingItem.value || editShiftId.value == null) return
  try {
    await attendanceApi.batchSchedule([{ userId: editingItem.value.userId, scheduleDate: editingItem.value.scheduleDate, shiftId: editShiftId.value }])
    editingItem.value = null
    await fetchData()
  } catch { alert('保存失败') }
}
function shiftColor(name: string): string {
  if (name?.includes('早')) return '#f59e0b'
  if (name?.includes('晚')) return '#6366f1'
  if (name?.includes('夜')) return '#8b5cf6'
  return '#10b981'
}

onMounted(async () => { await Promise.all([fetchShifts(), fetchPeople()]); fetchData() })
watch(currentMonday, () => fetchData())
watch(activeTab, () => { selectedUserId.value = null; fetchData() })

const masterDays = computed(() => weekDays.value)
const masterRows = computed(() => {
  if (activeTab.value !== 'master') return []
  const map = new Map<number, Map<string, Schedule>>()
  for (const s of schedules.value) {
    if (!map.has(s.userId)) map.set(s.userId, new Map())
    map.get(s.userId)!.set(s.scheduleDate, s)
  }
  return people.value.map(p => ({ person: p, cells: masterDays.value.map(d => map.get(p.userId)?.get(d.dateStr) || null) }))
})
</script>

<template>
  <div class="schedule-root">
    <div class="top-bar">
      <div class="top-left">
        <h2 class="page-title">排班管理</h2>
        <div class="tabs">
          <button :class="['tab', { active: activeTab === 'mine' }]" @click="activeTab = 'mine'"><User class="tab-icon" />我的排班</button>
          <button v-if="isAdmin" :class="['tab', { active: activeTab === 'people' }]" @click="activeTab = 'people'"><Users class="tab-icon" />人员排班</button>
          <button v-if="isAdmin" :class="['tab', { active: activeTab === 'master' }]" @click="activeTab = 'master'"><Table2 class="tab-icon" />总排班表</button>
        </div>
      </div>
      <div class="week-nav">
        <button class="nav-btn" @click="prevWeek"><ChevronLeft /></button>
        <button class="today-btn" @click="goToday">今天</button>
        <span class="week-text">{{ weekLabel }}</span>
        <button class="nav-btn" @click="nextWeek"><ChevronRight /></button>
      </div>
    </div>

    <div v-if="loading" class="loading-row"><div v-for="i in 7" :key="i" class="sk-day" /></div>

    <div v-else-if="activeTab === 'mine' && !schedules.length" class="empty-hint">
      <Calendar class="empty-icon" />
      <p>暂未分配班次，请联系管理员设置</p>
    </div>

    <!-- 我的排班 / 人员查看 -->
    <template v-else-if="activeTab !== 'master'">
      <div v-if="activeTab === 'people'" class="people-nav">
        <template v-if="selectedUserId">
          <button class="back-btn" @click="backToPeople"><ChevronLeft class="icon-sm" />返回列表</button>
          <span class="person-title">{{ selectedUserName }} 的排班</span>
        </template>
        <div v-else class="people-grid">
          <div v-for="p in people" :key="p.userId" class="person-card" @click="selectPerson(p)">
            <div class="person-avatar" :style="{ background: shiftColor(p.shiftName) }">{{ p.userName?.charAt(0) }}</div>
            <div class="person-body">
              <strong>{{ p.userName }}</strong>
              <small>{{ p.deptName }} · {{ p.shiftName }}</small>
            </div>
          </div>
        </div>
      </div>

      <div v-if="selectedUserId || activeTab === 'mine'" class="week-row">
        <div v-for="day in weekDays" :key="day.dateStr" :class="['day-cell', {
          today: isToday(day.date),
          past: isPast(day.date),
          weekend: isWeekend(day.date),
          leave: getSched(day.dateStr)?.status === 2
        }]">
          <div class="cell-head">
            <span class="cell-week">周{{ day.label }}</span>
            <span class="cell-date">{{ day.date.getMonth() + 1 }}/{{ day.date.getDate() }}</span>
          </div>
          <div class="cell-body">
            <template v-if="getSched(day.dateStr)">
              <div v-if="getSched(day.dateStr)!.status === 2" class="leave-badge">请假</div>
              <template v-else>
                <div class="shift-tag" :style="{ background: shiftColor(getSched(day.dateStr)!.shiftName) + '14', color: shiftColor(getSched(day.dateStr)!.shiftName), borderColor: shiftColor(getSched(day.dateStr)!.shiftName) + '30' }">
                  {{ getSched(day.dateStr)!.shiftName }}
                </div>
                <div class="shift-time">{{ getSched(day.dateStr)!.startTime?.substring(0, 5) }} - {{ getSched(day.dateStr)!.endTime?.substring(0, 5) }}</div>
                <div v-if="getSched(day.dateStr)!.overtimeHours > 0" class="overtime-badge">加班 {{ getSched(day.dateStr)!.overtimeHours }}h</div>
                <div v-if="getSched(day.dateStr)!.statusText" class="default-tag">{{ getSched(day.dateStr)!.statusText }}</div>
              </template>
            </template>
            <span v-else class="no-data">-</span>
          </div>
          <div v-if="isAdmin" class="cell-foot">
            <button class="edit-btn" @click="startEdit(getSched(day.dateStr) || { userId: selectedUserId || auth.user?.userId || 0, scheduleDate: day.dateStr, shiftId: shifts[0]?.id || 0, shiftName: '', startTime: '', endTime: '', userName: '', deptName: '', status: 1 } as Schedule)">
              {{ getSched(day.dateStr)?.id ? '修改' : '设置' }}
            </button>
          </div>
        </div>
      </div>
    </template>

    <!-- 总排班表 -->
    <div v-else class="master-wrap">
      <table class="master-table">
        <thead>
          <tr>
            <th class="th-name">姓名</th>
            <th v-for="d in masterDays" :key="d.dateStr" :class="{ today: isToday(d.date), past: isPast(d.date), wkend: isWeekend(d.date) }">
              {{ d.date.getMonth() + 1 }}/{{ d.date.getDate() }}<br><span>周{{ d.label }}</span>
            </th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="row in masterRows" :key="row.person.userId">
            <td class="td-name">{{ row.person.userName }}</td>
            <td v-for="(s, idx) in row.cells" :key="masterDays[idx].dateStr" :class="{ today: isToday(masterDays[idx].date), past: isPast(masterDays[idx].date), wkend: isWeekend(masterDays[idx].date) }">
              <span v-if="s?.status === 2" class="leave-cell">请假</span>
              <span v-else-if="s" class="sched-pill" :style="{ background: shiftColor(s.shiftName) + '14', color: shiftColor(s.shiftName) }">{{ s.shiftName }}<span v-if="s.overtimeHours > 0" class="ot-dot" title="加班 {{ s.overtimeHours }}h"></span></span>
              <span v-else class="none-cell">-</span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Modal -->
    <Teleport to="body">
      <div v-if="editingItem" class="modal-mask" @click.self="cancelEdit">
        <div class="modal-card">
          <h3>{{ editingItem.id ? '修改排班' : '设置排班' }}</h3>
          <p class="modal-date">{{ editingItem.scheduleDate }}</p>
          <div class="field">
            <label>选择班次</label>
            <select v-model="editShiftId" class="input">
              <option v-for="s in shifts" :key="s.id" :value="s.id">{{ s.shiftName }} ({{ s.startTime }}-{{ s.endTime }})</option>
            </select>
          </div>
          <div class="modal-btns">
            <button class="btn-cancel" @click="cancelEdit">取消</button>
            <button class="btn-save" @click="saveEdit">保存</button>
          </div>
        </div>
      </div>
    </Teleport>
  </div>
</template>

<style scoped>
.schedule-root { padding: 24px 28px; max-width: 1300px; margin: 0 auto; }
.top-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 28px; gap: 12px; flex-wrap: wrap; }
.top-left { display: flex; align-items: center; gap: 20px; }
.page-title { font-size: 22px; font-weight: 700; margin: 0; letter-spacing: -0.3px; }

/* ---- Tabs ---- */
.tabs { display: flex; gap: 2px; background: var(--bg-subtle); border-radius: 12px; padding: 4px; }
.tab { display: flex; align-items: center; gap: 6px; padding: 8px 16px; border: none; background: transparent; border-radius: 10px; font-size: 13px; font-weight: 500; color: var(--text-secondary); cursor: pointer; transition: all 0.2s; }
.tab:hover { color: var(--text-primary); }
.tab.active { background: var(--bg-card); color: var(--primary); box-shadow: 0 1px 3px rgba(0,0,0,0.08); }
.tab-icon { width: 16px; height: 16px; }

/* ---- Week nav ---- */
.week-nav { display: flex; align-items: center; gap: 6px; }
.nav-btn { width: 34px; height: 34px; border: 1px solid var(--border-color); border-radius: 10px; background: var(--bg-card); cursor: pointer; display: flex; align-items: center; justify-content: center; color: var(--text-primary); transition: all 0.15s; }
.nav-btn:hover { border-color: var(--primary); background: color-mix(in srgb, var(--primary) 6%, var(--bg-card)); }
.today-btn { padding: 6px 16px; border: 1px solid var(--primary); border-radius: 20px; background: transparent; color: var(--primary); font-size: 12px; font-weight: 600; cursor: pointer; transition: all 0.15s; }
.today-btn:hover { background: var(--primary); color: #fff; }
.week-text { font-size: 13px; font-weight: 500; color: var(--text-secondary); min-width: 210px; text-align: center; }

/* ---- Loading / Empty ---- */
.loading-row { display: grid; grid-template-columns: repeat(7, 1fr); gap: 10px; }
.sk-day { height: 160px; border-radius: 14px; background: var(--bg-subtle); animation: shimmer 1.5s infinite; }
@keyframes shimmer { 0%, 100% { opacity: 0.4 } 50% { opacity: 0.75 } }
.empty-hint { display: flex; flex-direction: column; align-items: center; gap: 12px; padding: 80px 0; color: var(--text-muted); }
.empty-icon { width: 48px; height: 48px; opacity: 0.35; }

/* ========== Week grid (我的排班 / 人员排班) ========== */
.week-row { display: grid; grid-template-columns: repeat(7, 1fr); gap: 10px; }

.day-cell {
  background: var(--bg-card); border: 1px solid var(--border-color); border-radius: 14px;
  padding: 16px 10px 12px; display: flex; flex-direction: column; align-items: center; gap: 10px;
  min-height: 160px; transition: all 0.2s;
}
.day-cell:hover { box-shadow: 0 2px 8px rgba(0,0,0,0.06); }

/* today */
.day-cell.today {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px color-mix(in srgb, var(--primary) 15%, transparent);
  background: color-mix(in srgb, var(--primary) 4%, var(--bg-card));
}

/* weekend */
.day-cell.weekend { background: color-mix(in srgb, var(--bg-subtle) 80%, var(--bg-card)); }

/* leave */
.day-cell.leave { background: #fef2f2; border-color: #fecaca; }

/* past days -- still has border but muted */
.day-cell.past {
  background: color-mix(in srgb, var(--bg-subtle) 60%, var(--bg-card));
  opacity: 0.65;
}
.day-cell.past.today { opacity: 0.85; }
.day-cell.past.leave { opacity: 0.75; }

.cell-head { text-align: center; }
.cell-week { font-weight: 600; font-size: 13px; color: var(--text-secondary); }
.cell-date { display: block; font-size: 11px; color: var(--text-muted); margin-top: 2px; }
.today .cell-date { background: var(--primary); color: #fff; border-radius: 50%; width: 24px; height: 24px; line-height: 24px; margin: 3px auto 0; font-weight: 600; }

.cell-body { flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px; }
.no-data { color: var(--text-muted); font-size: 15px; font-weight: 300; }
.shift-tag { padding: 4px 12px; border-radius: 20px; font-size: 13px; font-weight: 700; border: 1px solid; }
.shift-time { font-size: 11px; color: var(--text-muted); }
.leave-badge { background: #fecaca; color: #991b1b; padding: 4px 16px; border-radius: 20px; font-size: 13px; font-weight: 600; }
.overtime-badge { background: #fef3c7; color: #92400e; padding: 3px 12px; border-radius: 20px; font-size: 11px; font-weight: 600; }
.ot-dot { display: inline-block; width: 7px; height: 7px; border-radius: 50%; background: #f59e0b; margin-left: 4px; vertical-align: middle; }
.default-tag { font-size: 10px; color: var(--text-muted); background: var(--bg-subtle); padding: 2px 8px; border-radius: 8px; }

.cell-foot { margin-top: auto; padding-top: 4px; }
.edit-btn { padding: 4px 14px; border-radius: 8px; border: 1px solid var(--border-color); background: transparent; cursor: pointer; font-size: 12px; color: var(--text-secondary); transition: all 0.15s; }
.edit-btn:hover { border-color: var(--primary); color: var(--primary); background: color-mix(in srgb, var(--primary) 6%, transparent); }

/* ========== People grid ========== */
.people-grid {
  display: grid; grid-template-columns: repeat(auto-fill, minmax(260px, 1fr)); gap: 10px;
  max-height: 420px; overflow-y: auto; margin-bottom: 18px; padding: 2px;
}
.person-card {
  display: flex; align-items: center; gap: 14px; padding: 14px 16px;
  border-radius: 12px; border: 1px solid var(--border-color); cursor: pointer;
  transition: all 0.2s; background: var(--bg-card);
}
.person-card:hover {
  border-color: var(--primary);
  box-shadow: 0 4px 14px rgba(0,0,0,0.08);
  transform: translateY(-1px);
}
.person-avatar {
  width: 40px; height: 40px; border-radius: 50%; color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-weight: 700; font-size: 15px; flex-shrink: 0;
  box-shadow: 0 2px 6px rgba(0,0,0,0.15);
}
.person-body { display: flex; flex-direction: column; gap: 2px; font-size: 13px; min-width: 0; }
.person-body strong { color: var(--text-primary); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.person-body small { color: var(--text-muted); font-size: 12px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }

.back-btn { display: inline-flex; align-items: center; gap: 4px; padding: 6px 14px; border: 1px solid var(--border-color); border-radius: 8px; background: var(--bg-card); cursor: pointer; font-size: 13px; color: var(--text-secondary); transition: all 0.15s; margin-bottom: 14px; }
.back-btn:hover { border-color: var(--primary); color: var(--primary); }
.person-title { font-size: 16px; font-weight: 600; margin-left: 10px; vertical-align: middle; }
.icon-sm { width: 15px; height: 15px; }

/* ========== Master table ========== */
.master-wrap {
  overflow-x: auto; border: 1px solid var(--border-color); border-radius: 14px;
  background: var(--bg-card); box-shadow: 0 1px 4px rgba(0,0,0,0.04);
}
.master-table { width: 100%; border-collapse: collapse; font-size: 13px; }

.master-table thead { position: sticky; top: 0; z-index: 2; }

.master-table th, .master-table td {
  padding: 11px 8px; text-align: center; border-bottom: 1px solid var(--border-color);
  min-width: 70px; transition: background 0.15s;
}

.master-table th {
  font-weight: 600; color: var(--text-secondary); font-size: 12px;
  background: var(--bg-subtle); border-bottom: 2px solid var(--border-color);
}
.master-table th span { font-weight: 400; color: var(--text-muted); display: block; font-size: 10px; margin-top: 1px; }

.master-table th.today { color: var(--primary); background: color-mix(in srgb, var(--primary) 8%, var(--bg-subtle)); }
.master-table th.past { color: var(--text-muted); }

/* row stripes */
.master-table tbody tr:nth-child(even) td { background: color-mix(in srgb, var(--bg-subtle) 40%, transparent); }
.master-table tbody tr:hover td { background: color-mix(in srgb, var(--primary) 4%, var(--bg-card)); }

.master-table td.today { background: color-mix(in srgb, var(--primary) 5%, var(--bg-card)) !important; }
.master-table td.past { opacity: 0.55; }
.master-table td.wkend { background: color-mix(in srgb, var(--bg-subtle) 50%, var(--bg-card)); }

.th-name, .td-name {
  text-align: left !important; font-weight: 600; white-space: nowrap;
  min-width: 70px; padding-left: 20px !important;
  position: sticky; left: 0; z-index: 1;
  background: var(--bg-card) !important;
}
.th-name { background: var(--bg-subtle) !important; }

.sched-pill { padding: 4px 12px; border-radius: 20px; font-size: 12px; font-weight: 600; white-space: nowrap; }
.leave-cell { color: #991b1b; font-weight: 600; font-size: 12px; }
.none-cell { color: var(--text-muted); font-weight: 300; }

/* ========== Modal ========== */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.35); display: flex; align-items: center; justify-content: center; z-index: 1000; backdrop-filter: blur(2px); }
.modal-card { background: var(--bg-card); border-radius: 16px; padding: 28px; width: 380px; max-width: 92vw; box-shadow: 0 20px 60px rgba(0,0,0,0.15); }
.modal-card h3 { margin: 0; font-size: 17px; font-weight: 700; }
.modal-date { font-size: 13px; color: var(--text-muted); margin: 6px 0 20px; }
.field { margin-bottom: 18px; }
.field label { display: block; font-size: 12px; font-weight: 600; color: var(--text-secondary); margin-bottom: 6px; }
.input { width: 100%; padding: 9px 12px; border-radius: 10px; border: 1px solid var(--border-color); font-size: 14px; background: var(--bg-card); color: var(--text-primary); outline: none; transition: border-color 0.15s; }
.input:focus { border-color: var(--primary); box-shadow: 0 0 0 3px color-mix(in srgb, var(--primary) 12%, transparent); }
.modal-btns { display: flex; justify-content: flex-end; gap: 8px; }
.btn-cancel { padding: 8px 20px; border-radius: 10px; border: 1px solid var(--border-color); background: transparent; cursor: pointer; font-size: 13px; color: var(--text-secondary); transition: all 0.15s; }
.btn-cancel:hover { border-color: #d1d5db; background: var(--bg-subtle); }
.btn-save { padding: 8px 20px; border-radius: 10px; border: none; background: var(--primary); color: #fff; cursor: pointer; font-size: 13px; font-weight: 600; transition: all 0.15s; }
.btn-save:hover { opacity: 0.9; }

@media (max-width: 900px) {
  .week-row { grid-template-columns: repeat(4, 1fr); }
  .loading-row { grid-template-columns: repeat(4, 1fr); }
  .top-bar { flex-direction: column; align-items: flex-start; }
}
@media (max-width: 500px) {
  .week-row { grid-template-columns: repeat(2, 1fr); }
  .loading-row { grid-template-columns: repeat(2, 1fr); }
}
</style>
