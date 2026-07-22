<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">待审批</h2>
        <p class="page-subtitle">当前用户需要处理的申请</p>
      </div>
      <button class="btn" :disabled="loading" @click="load"><RefreshCw class="icon" />{{ loading ? '刷新中' : '刷新' }}</button>
    </div>

    <p v-if="error" class="error-banner">{{ error }}</p>
    <p v-if="message" class="success-banner">{{ message }}</p>

    <section v-if="rows.length > 0" class="pending-grid">
      <article v-for="item in rows" :key="item.id" class="panel panel-pad pending-item">
        <header>
          <div>
            <h3>{{ item.applicationNo }}</h3>
            <p>{{ item.appTypeText || appTypeText(item.appType) }} · {{ item.duration ?? '-' }} 小时</p>
          </div>
          <span class="pill warn">{{ item.statusText || '审批中' }}</span>
        </header>
        <div class="approval-line">
          <span>发起人</span><strong>{{ item.applicantName }}</strong>
          <span>提交时间</span><strong>{{ item.createTime || '-' }}</strong>
          <span>当前审批人</span><strong>{{ item.currentApproverName || '-' }}</strong>
          <span>原因</span><strong>{{ item.reason || '-' }}</strong>
        </div>
        <textarea v-model="comments[item.id]" class="textarea" placeholder="审批意见" />
        <div class="toolbar">
          <button class="btn danger" :disabled="submittingId === item.id" @click="approve(item, false)"><XCircle class="icon" />驳回</button>
          <button class="btn primary" :disabled="submittingId === item.id" @click="approve(item, true)"><CheckCircle2 class="icon" />同意</button>
        </div>
      </article>
    </section>
    <section v-else class="panel empty">暂无待审批申请</section>
  </section>
</template>

<script setup lang="ts">
import { CheckCircle2, RefreshCw, XCircle } from 'lucide-vue-next'
import { onMounted, reactive, ref } from 'vue'
import { approvalApi } from '@/api/services'
import type { ApprovalApplication } from '@/api/types'

const rows = ref<ApprovalApplication[]>([])
const loading = ref(false)
const submittingId = ref<number | null>(null)
const error = ref('')
const message = ref('')
const comments = reactive<Record<number, string>>({})
const pageRows = <T,>(page: { records?: T[]; list?: T[] }) => page.records || page.list || []
const appTypeText = (value?: number) => ({ 1: '请假', 2: '加班', 3: '外出' }[value ?? 1])

async function load() {
  loading.value = true
  error.value = ''
  try {
    const result = await approvalApi.pending({ pageNum: 1, pageSize: 50 })
    rows.value = pageRows(result)
  } catch (err) {
    error.value = err instanceof Error ? err.message : '待审批数据加载失败'
  } finally {
    loading.value = false
  }
}

async function approve(item: ApprovalApplication, approved: boolean) {
  submittingId.value = item.id
  error.value = ''
  message.value = ''
  try {
    await approvalApi.approve(item.id, { approved, comment: comments[item.id] || (approved ? '同意' : '驳回') })
    message.value = approved ? '申请已同意' : '申请已驳回'
    delete comments[item.id]
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '审批操作失败'
  } finally {
    submittingId.value = null
  }
}

onMounted(load)
</script>

<style scoped>
.pending-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.pending-item {
  display: grid;
  gap: 14px;
}

.pending-item header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.pending-item h3 {
  margin: 0;
}

.pending-item p {
  margin: 5px 0 0;
  color: var(--muted);
}

.approval-line {
  display: grid;
  grid-template-columns: 80px minmax(0, 1fr);
  gap: 8px;
}

.approval-line span {
  color: var(--muted);
}

@media (max-width: 900px) {
  .pending-grid {
    grid-template-columns: 1fr;
  }
}
</style>