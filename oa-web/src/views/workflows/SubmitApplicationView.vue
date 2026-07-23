<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">提交申请</h2>
        <p class="page-subtitle">请假、加班、外出申请表单</p>
      </div>
    </div>

    <p v-if="error" class="error-banner">{{ error }}</p>
    <p v-if="message" class="success-banner">{{ message }}</p>

    <section class="panel panel-pad">
      <div class="form-grid">
        <label class="form-item">
          <span class="form-label">申请类型</span>
          <select v-model.number="form.appType" class="select">
            <option :value="1">请假</option>
            <option :value="2">加班</option>
            <option :value="3">外出</option>
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
          <textarea v-model="form.reason" class="textarea" />
        </label>
      </div>
      <div class="toolbar form-actions">
        <button class="btn" @click="resetForm"><RotateCcw class="icon" />清空</button>
        <button class="btn primary" :disabled="submitting" @click="submit"><Send class="icon" />{{ submitting ? '提交中' : '提交申请' }}</button>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { RotateCcw, Send } from 'lucide-vue-next'
import { reactive, ref } from 'vue'
import { approvalApi } from '@/api/services'

const submitting = ref(false)
const error = ref('')
const message = ref('')
const form = reactive({ appType: 1, leaveType: 2, startTime: '', endTime: '', reason: '' })

function normalizeDateTime(value: string) {
  return value.length === 16 ? `${value}:00` : value
}

function resetForm() {
  Object.assign(form, { appType: 1, leaveType: 2, startTime: '', endTime: '', reason: '' })
}

async function submit() {
  submitting.value = true
  error.value = ''
  message.value = ''
  try {
    const result = await approvalApi.submitApplication({
      appType: form.appType,
      leaveType: form.appType === 1 ? form.leaveType : undefined,
      startTime: normalizeDateTime(form.startTime),
      endTime: normalizeDateTime(form.endTime),
      reason: form.reason,
      attachments: []
    })
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
</style>