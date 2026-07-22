<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">{{ mode === 'mine' ? '我的申请' : '已办审批' }}</h2>
        <p class="page-subtitle">{{ mode === 'mine' ? '查看提交的申请及状态' : '查看已处理的审批记录' }}</p>
      </div>
      <div class="toolbar">
        <select v-model.number="filterStatus" class="select" @change="load(1)">
          <option :value="undefined">全部状态</option><option :value="1">审批中</option><option :value="2">已通过</option><option :value="3">已驳回</option><option :value="4">已撤销</option>
        </select>
      </div>
    </div>
    <section class="panel panel-pad">
      <div v-if="loading" class="empty">加载中...</div>
      <div v-else-if="rows.length === 0" class="empty">暂无记录</div>
      <div v-else class="table-wrap">
        <table class="data-table">
          <thead><tr><th>申请单号</th><th>类型</th><th>时间</th><th>原因</th><th>状态</th><th v-if="mode === 'mine'">操作</th></tr></thead>
          <tbody>
            <tr v-for="row in rows" :key="row.id">
              <td>{{ row.applicationNo }}</td>
              <td>{{ row.appTypeText }}</td>
              <td>{{ row.startTime?.slice(0,10) }} ~ {{ row.endTime?.slice(0,10) }}</td>
              <td>{{ row.reason?.slice(0, 24) }}{{ row.reason?.length > 24 ? '...' : '' }}</td>
              <td><span class="pill" :class="statusClass(row.status)">{{ row.statusText }}</span></td>
              <td v-if="mode === 'mine' && row.status === 1">
                <button class="btn ghost btn-sm" @click="handleCancel(row.id)">撤销</button>
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
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { approvalApi } from '@/api/services'
import type { ApplicationVO } from '@/api/types'

const props = defineProps<{ mode: 'mine' | 'processed' }>()

const rows = ref<ApplicationVO[]>([])
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)
const loading = ref(false)
const filterStatus = ref<number | undefined>(undefined)

onMounted(() => load(1))

function load(page: number) {
  loading.value = true
  pageNum.value = page
  const params: Record<string, unknown> = { pageNum: page, pageSize }
  if (filterStatus.value != null) params.status = filterStatus.value
  const api = props.mode === 'mine' ? approvalApi.myApplications(params) : approvalApi.processed(params)
  api.then(res => { rows.value = res.records ?? []; total.value = res.total })
    .finally(() => loading.value = false)
}

async function handleCancel(id: number) {
  if (!confirm('确定撤销该申请？')) return
  await approvalApi.cancel(id)
  load(pageNum.value)
}

function statusClass(s: number) {
  return { 1: 'warn', 2: 'success', 3: 'danger', 4: 'muted' }[s] || ''
}
</script>

<style scoped>
.empty { text-align: center; padding: 24px; color: var(--muted); }
.pager { display: flex; align-items: center; gap: 12px; margin-top: 12px; justify-content: center; }
.btn-sm { padding: 4px 12px; font-size: 13px; }
</style>
