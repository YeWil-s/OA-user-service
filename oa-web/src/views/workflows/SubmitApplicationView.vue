<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">提交申请</h2>
        <p class="page-subtitle">请假、加班、外出申请</p>
      </div>
    </div>

    <section class="panel panel-pad">
      <div class="form-grid" style="max-width:600px">
        <label class="form-item"><span>申请类型</span><select v-model.number="form.appType" class="select"><option :value="1">请假</option><option :value="2">加班</option><option :value="3">外出</option></select></label>
        <label class="form-item" v-if="form.appType === 1"><span>请假类型</span><select v-model.number="form.leaveType" class="select"><option :value="1">年假</option><option :value="2">事假</option><option :value="3">病假</option><option :value="4">婚假</option><option :value="5">产假</option></select></label>
        <label class="form-item"><span>开始时间</span><input v-model="form.startTime" class="field" type="datetime-local" /></label>
        <label class="form-item"><span>结束时间</span><input v-model="form.endTime" class="field" type="datetime-local" /></label>
        <label class="form-item full"><span>申请原因</span><textarea v-model="form.reason" class="textarea" rows="3" placeholder="请填写申请原因" /></label>
        <div class="form-actions">
          <button class="btn primary" :disabled="submitting" @click="submit">{{ submitting ? '提交中' : '提交申请' }}</button>
        </div>
        <p v-if="msg" :class="msgOk ? 'msg-ok' : 'msg-err'">{{ msg }}</p>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { approvalApi } from '@/api/services'

const form = reactive({ appType: 1, leaveType: 1, startTime: '', endTime: '', reason: '' })
const submitting = ref(false)
const msg = ref('')
const msgOk = ref(false)

async function submit() {
  if (!form.startTime || !form.endTime || !form.reason) {
    msg.value = '请填写完整信息'
    msgOk.value = false
    return
  }
  submitting.value = true
  msg.value = ''
  try {
    const res = await approvalApi.submit({
      appType: form.appType,
      leaveType: form.appType === 1 ? form.leaveType : undefined,
      startTime: form.startTime,
      endTime: form.endTime,
      reason: form.reason
    })
    msg.value = '提交成功！申请单号: ' + res.applicationNo
    msgOk.value = true
    form.reason = ''
  } catch (e: unknown) {
    msg.value = (e as Error).message
    msgOk.value = false
  }
  submitting.value = false
}
</script>

<style scoped>
.form-actions { display: flex; gap: 12px; margin-top: 12px; }
.msg-ok { color: var(--success); margin: 8px 0 0; }
.msg-err { color: var(--danger); margin: 8px 0 0; }
.full { grid-column: 1 / -1; }
</style>
