<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">提交申请</h2>
        <p class="page-subtitle">请假、加班、外出申请表单</p>
      </div>
      <span class="mock-banner">审批后端未完成，当前为界面流程</span>
    </div>

    <section class="panel panel-pad">
      <div class="form-grid">
        <label class="form-item">
          <span class="form-label">申请类型</span>
          <select v-model="form.type" class="select">
            <option>请假</option>
            <option>加班</option>
            <option>外出</option>
          </select>
        </label>
        <label class="form-item">
          <span class="form-label">时长</span>
          <input v-model="form.duration" class="field" placeholder="1 天 / 3 小时" />
        </label>
        <label class="form-item">
          <span class="form-label">开始时间</span>
          <input v-model="form.start" class="field" type="datetime-local" />
        </label>
        <label class="form-item">
          <span class="form-label">结束时间</span>
          <input v-model="form.end" class="field" type="datetime-local" />
        </label>
        <label class="form-item full">
          <span class="form-label">申请原因</span>
          <textarea v-model="form.reason" class="textarea" />
        </label>
      </div>
      <div class="toolbar form-actions">
        <button class="btn" @click="saveDraft"><FileClock class="icon" />保存草稿</button>
        <button class="btn primary" @click="submit"><Send class="icon" />提交申请</button>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { FileClock, Send } from 'lucide-vue-next'
import { reactive } from 'vue'

const form = reactive({ type: '请假', duration: '', start: '', end: '', reason: '' })
function saveDraft() {}
function submit() {}
</script>

<style scoped>
.full {
  grid-column: 1 / -1;
}

.form-actions {
  margin-top: 16px;
}
</style>
