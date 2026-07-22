<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">待审批</h2>
        <p class="page-subtitle">处理待审批的申请</p>
      </div>
    </div>

    <section class="panel panel-pad">
      <div v-if="loading" class="empty">加载中...</div>
      <div v-else-if="rows.length === 0" class="empty">暂无待审批申请</div>
      <div v-else class="table-wrap">
        <table class="data-table">
          <thead><tr><th>申请单号</th><th>申请人</th><th>类型</th><th>时间</th><th>原因</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="row in rows" :key="row.id">
              <td>{{ row.applicationNo }}</td>
              <td>{{ row.applicantName ?? '-' }}</td>
              <td>{{ row.appTypeText }}{{ row.leaveTypeText ? ' (' + row.leaveTypeText + ')' : '' }}</td>
              <td>{{ row.startTime?.slice(0,16) }} ~ {{ row.endTime?.slice(0,16) }}</td>
              <td>{{ row.reason?.slice(0, 30) }}{{ row.reason?.length > 30 ? '...' : '' }}</td>
              <td>
                <div class="row-actions">
                  <button class="btn primary btn-sm" @click="openApprove(row, true)">同意</button>
                  <button class="btn danger btn-sm" @click="openApprove(row, false)">驳回</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="total > pageSize" class="pager">
        <button class="btn ghost" :disabled="pageNum <= 1" @click="load(pageNum - 1)">上一页</button>
        <span>{{ pageNum }} / {{ Math.ceil(total / pageSize) }}</span>
        <button class="btn ghost" :disabled="pageNum * pageSize >= total" @click="load(pageNum + 1)">下一页</button>
      </div>
    </section>

    <ModalDialog v-model="dialogOpen" :title="approveAction ? '同意申请' : '驳回申请'">
      <label class="form-item"><span>审批意见</span><textarea v-model="comment" class="textarea" rows="3" placeholder="请输入审批意见" /></label>
      <template #footer>
        <button class="btn" @click="dialogOpen = false">取消</button>
        <button class="btn primary" :disabled="saving" @click="doApprove">{{ saving ? '提交中' : '确认' }}</button>
      </template>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { approvalApi } from '@/api/services'
import type { ApplicationVO } from '@/api/types'
import ModalDialog from '@/components/ModalDialog.vue'

const rows = ref<ApplicationVO[]>([])
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const dialogOpen = ref(false)
const approveAction = ref(false)
const approveTarget = ref<ApplicationVO | null>(null)
const comment = ref('')

onMounted(() => load(1))

function load(page: number) {
  loading.value = true
  pageNum.value = page
  approvalApi.pending({ pageNum: page, pageSize })
    .then(res => { rows.value = res.records ?? []; total.value = res.total })
    .finally(() => loading.value = false)
}

function openApprove(row: ApplicationVO, approved: boolean) {
  approveTarget.value = row
  approveAction.value = approved
  comment.value = ''
  dialogOpen.value = true
}

async function doApprove() {
  if (!approveTarget.value || !comment.value.trim()) return
  saving.value = true
  try {
    await approvalApi.approve(approveTarget.value.id, { approved: approveAction.value, comment: comment.value.trim() })
    dialogOpen.value = false
    load(pageNum.value)
  } catch { /* ignore */ }
  saving.value = false
}
</script>

<style scoped>
.empty { text-align: center; padding: 24px; color: var(--muted); }
.pager { display: flex; align-items: center; gap: 12px; margin-top: 12px; justify-content: center; }
.btn-sm { padding: 4px 12px; font-size: 13px; }
.danger { background: var(--danger); color: #fff; border-color: var(--danger); }
</style>
