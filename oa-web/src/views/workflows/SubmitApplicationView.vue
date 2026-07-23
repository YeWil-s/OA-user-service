<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">提交申请</h2>
        <p class="page-subtitle">选择申请类别，填写相关信息后提交</p>
      </div>
    </div>

    <p v-if="error" class="error-banner">{{ error }}</p>
    <p v-if="message" class="success-banner">{{ message }}</p>

    <!-- 类别选择 -->
    <section class="category-grid">
      <button
        class="category-card"
        :class="{ active: activeCategory === 1 }"
        type="button"
        @click="switchCategory(1)"
      >
        <span class="category-icon leave"><CalendarCheck /></span>
        <span class="category-copy">
          <strong>考勤相关</strong>
          <small>请假 / 加班 / 外勤</small>
        </span>
      </button>
      <button
        class="category-card"
        :class="{ active: activeCategory === 2 }"
        type="button"
        @click="switchCategory(2)"
      >
        <span class="category-icon transfer"><Briefcase /></span>
        <span class="category-copy">
          <strong>调岗申请</strong>
          <small>部门 / 岗位调整</small>
        </span>
      </button>
      <button
        class="category-card"
        :class="{ active: activeCategory === 3 }"
        type="button"
        @click="switchCategory(3)"
      >
        <span class="category-icon asset"><Package /></span>
        <span class="category-copy">
          <strong>资产领用</strong>
          <small>领用公司资产</small>
        </span>
      </button>
    </section>

    <!-- 表单区域 -->
    <section class="panel panel-pad">
      <!-- 类别1：请假/加班/外勤 -->
      <template v-if="activeCategory === 1">
        <div class="form-grid">
          <label class="form-item">
            <span class="form-label">申请类型</span>
            <select v-model.number="form.appType" class="select">
              <option :value="1">请假</option>
              <option :value="2">加班</option>
              <option :value="3">外勤</option>
            </select>
          </label>
          <label v-if="form.appType === 1" class="form-item">
            <span class="form-label">请假类型</span>
            <select v-model.number="form.leaveType" class="select">
              <option :value="1">年假</option>
              <option :value="2">事假</option>
              <option :value="3">病假</option>
              <option :value="4">婚假</option>
              <option :value="5">产假</option>
            </select>
          </label>
          <label class="form-item">
            <span class="form-label">开始时间</span>
            <input v-model="form.startTime" class="field" type="datetime-local" />
          </label>
          <label class="form-item">
            <span class="form-label">结束时间</span>
            <input v-model="form.endTime" class="field" type="datetime-local" />
          </label>
          <label class="form-item full">
            <span class="form-label">申请原因</span>
            <textarea v-model="form.reason" class="textarea" placeholder="请描述申请原因" />
          </label>
        </div>
      </template>

      <!-- 类别2：调岗 -->
      <template v-if="activeCategory === 2">
        <div class="form-grid">
          <label class="form-item">
            <span class="form-label">目标部门</span>
            <select v-model.number="form.targetDeptId" class="select">
              <option :value="0" disabled>请选择部门</option>
              <option v-for="dept in depts" :key="dept.id" :value="dept.id">{{ dept.deptName }}</option>
            </select>
          </label>
          <label class="form-item">
            <span class="form-label">目标岗位</span>
            <select v-model.number="form.targetPositionId" class="select">
              <option :value="0" disabled>请选择岗位</option>
              <option v-for="pos in positions" :key="pos.id" :value="pos.id">{{ pos.positionName }}</option>
            </select>
          </label>
          <label class="form-item full">
            <span class="form-label">申请原因</span>
            <textarea v-model="form.reason" class="textarea" placeholder="请描述调岗原因" />
          </label>
        </div>
      </template>

      <!-- 类别3：资产领用 -->
      <template v-if="activeCategory === 3">
        <div class="form-grid">
          <label class="form-item">
            <span class="form-label">选择资产</span>
            <select v-model.number="form.assetId" class="select">
              <option :value="0" disabled>请选择资产</option>
              <option v-for="asset in availableAssets" :key="asset.id" :value="asset.id">
                {{ asset.assetName || asset.name }} ({{ asset.assetCode || asset.code }})
              </option>
            </select>
          </label>
          <label class="form-item">
            <span class="form-label">预计归还日期</span>
            <input v-model="form.expectReturnDate" class="field" type="date" />
          </label>
          <label class="form-item full">
            <span class="form-label">申请原因</span>
            <textarea v-model="form.reason" class="textarea" placeholder="请描述领用原因" />
          </label>
        </div>
      </template>

      <div class="toolbar form-actions">
        <button class="btn" @click="resetForm"><RotateCcw class="icon" />清空</button>
        <button class="btn primary" :disabled="submitting" @click="submit">
          <Send class="icon" />{{ submitting ? '提交中' : '提交申请' }}
        </button>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { Briefcase, CalendarCheck, Package, RotateCcw, Send } from 'lucide-vue-next'
import { onMounted, reactive, ref } from 'vue'
import { approvalApi, assetApi, systemApi } from '@/api/services'
import type { DeptNode, Position } from '@/api/types'

const submitting = ref(false)
const error = ref('')
const message = ref('')
const activeCategory = ref(1)
const depts = ref<DeptNode[]>([])
const positions = ref<Position[]>([])
const availableAssets = ref<Asset[]>([])
const form = reactive({
  appType: 1,
  leaveType: 2,
  startTime: '',
  endTime: '',
  targetDeptId: 0 as number,
  targetPositionId: 0 as number,
  assetId: 0 as number,
  expectReturnDate: '',
  reason: ''
})

function normalizeDateTime(value: string) {
  return value.length === 16 ? `${value}:00` : value
}

function switchCategory(cat: number) {
  activeCategory.value = cat
  if (cat === 1) {
    form.appType = 1
    form.leaveType = 2
    form.targetDeptId = 0
    form.targetPositionId = 0
    form.assetId = 0
    form.expectReturnDate = ''
  } else if (cat === 2) {
    form.appType = 4
    form.leaveType = 0 as any
    form.startTime = ''
    form.endTime = ''
    form.assetId = 0
    form.expectReturnDate = ''
  } else if (cat === 3) {
    form.appType = 5
    form.leaveType = 0 as any
    form.startTime = ''
    form.endTime = ''
    form.targetDeptId = 0
    form.targetPositionId = 0
  }
}

function resetForm() {
  Object.assign(form, {
    appType: 1, leaveType: 2, startTime: '', endTime: '',
    targetDeptId: 0, targetPositionId: 0, assetId: 0, expectReturnDate: '',
    reason: ''
  })
  activeCategory.value = 1
}

async function loadOptions() {
  try {
    depts.value = await systemApi.depts()
  } catch { /* keep default empty */ }
  try {
    const posResult = await systemApi.positions({ pageNum: 1, pageSize: 100 })
    positions.value = (posResult as any).records || (posResult as any).list || []
  } catch { /* keep default empty */ }
  try {
    const assetResult = await assetApi.assets({ pageNum: 1, pageSize: 100, status: 1 })
    availableAssets.value = (assetResult as any).records || (assetResult as any).list || []
  } catch {
    error.value = '无法加载可用资产列表，请确认网络或资产服务是否正常'
  }
}

onMounted(loadOptions)

async function submit() {
  submitting.value = true
  error.value = ''
  message.value = ''
  try {
    const payload: Record<string, unknown> = {
      appType: form.appType,
      leaveType: form.appType === 1 ? form.leaveType : undefined,
      reason: form.reason,
      attachments: []
    }
    if (form.appType >= 1 && form.appType <= 3) {
      payload.startTime = normalizeDateTime(form.startTime)
      payload.endTime = normalizeDateTime(form.endTime)
    }
    if (form.appType === 4) {
      payload.targetDeptId = form.targetDeptId || undefined
      payload.targetPositionId = form.targetPositionId || undefined
    }
    if (form.appType === 5) {
      payload.assetId = form.assetId || undefined
      payload.expectReturnDate = form.expectReturnDate || undefined
    }
    const result = await approvalApi.submitApplication(payload as any)
    message.value = result.applicationNo ? `申请已提交：${result.applicationNo}` : '申请已提交'
    resetForm()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '申请提交失败'
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.full {
  grid-column: 1 / -1;
}

.form-actions {
  margin-top: 16px;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.category-card {
  position: relative;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 18px;
  border: 1px solid var(--border);
  border-radius: 8px;
  background: var(--surface);
  color: var(--text);
  cursor: pointer;
  text-align: left;
  box-shadow: var(--shadow);
  backdrop-filter: blur(14px);
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease, background 0.18s ease;
}

.category-card:hover {
  transform: translateY(-2px);
  border-color: var(--border-strong);
  box-shadow: var(--shadow-hover);
}

.category-card:active {
  transform: scale(0.98);
}

.category-card.active {
  border-color: var(--primary-soft);
  background: linear-gradient(135deg, color-mix(in srgb, var(--primary) 12%, transparent), color-mix(in srgb, var(--accent) 6%, transparent));
  box-shadow: var(--shadow-hover), 0 0 24px var(--primary-glow);
}

.category-card.active::before {
  position: absolute;
  top: 0;
  right: 0;
  bottom: 0;
  left: 0;
  width: 3px;
  height: 40px;
  margin: auto 0;
  border-radius: 0 4px 4px 0;
  background: linear-gradient(180deg, var(--primary-soft), var(--primary));
  box-shadow: 0 0 14px var(--primary-glow);
  content: "";
}

.category-icon {
  width: 44px;
  height: 44px;
  flex: 0 0 44px;
  display: grid;
  place-items: center;
  border-radius: 8px;
}

.category-icon svg {
  width: 22px;
  height: 22px;
}

.category-icon.leave {
  background: color-mix(in srgb, var(--primary) 12%, transparent);
  color: var(--primary-soft);
}

.category-icon.transfer {
  background: color-mix(in srgb, var(--accent) 12%, transparent);
  color: var(--accent);
}

.category-icon.asset {
  background: color-mix(in srgb, var(--success) 12%, transparent);
  color: var(--success);
}

.category-copy {
  display: grid;
  gap: 4px;
}

.category-copy strong {
  font-size: 14px;
  font-weight: 720;
}

.category-copy small {
  color: var(--muted);
  font-size: 11px;
}

@media (max-width: 760px) {
  .category-grid {
    grid-template-columns: 1fr;
  }
}
</style>
