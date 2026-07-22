<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">合同管理</h2>
        <p class="page-subtitle">合同到期预警与续签状态</p>
      </div>
      <button class="btn" :disabled="loading" @click="load"><RefreshCw class="icon" />{{ loading ? '刷新中' : '刷新' }}</button>
    </div>

    <p v-if="error" class="error-banner">{{ error }}</p>

    <div v-if="rows.length > 0" class="grid-3">
      <article v-for="item in rows" :key="item.id" class="panel panel-pad contract-item">
        <header>
          <FileText class="icon" />
          <StatusPill :value="contractTone(item)" :map="{ normal: '正常', expiring: '即将到期', expired: '已到期' }" :tone-map="{ normal: 'success', expiring: 'warn', expired: 'danger' }" />
        </header>
        <h3>{{ item.realName || item.userName || `员工 ${item.userId}` }}</h3>
        <p>{{ item.contractNo || '未填写合同编号' }}</p>
        <p>{{ item.startDate || '-' }} 至 {{ item.endDate || '-' }}</p>
      </article>
    </div>
    <section v-else class="panel empty">暂无合同数据</section>
  </section>
</template>

<script setup lang="ts">
import { FileText, RefreshCw } from 'lucide-vue-next'
import { onMounted, ref } from 'vue'
import StatusPill from '@/components/StatusPill.vue'
import { assetApi } from '@/api/services'
import type { Contract } from '@/api/types'

const rows = ref<Contract[]>([])
const loading = ref(false)
const error = ref('')
const pageRows = <T,>(page: { records?: T[]; list?: T[] }) => page.records || page.list || []

function contractTone(item: Contract) {
  if (item.status === 'expired' || item.status === 0) return 'expired'
  if (item.status === 'expiring') return 'expiring'
  if (!item.endDate) return 'normal'
  const today = new Date()
  const end = new Date(item.endDate)
  const days = Math.ceil((end.getTime() - today.getTime()) / 86400000)
  if (days < 0) return 'expired'
  if (days <= 30) return 'expiring'
  return 'normal'
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const result = await assetApi.contracts({ pageNum: 1, pageSize: 50 })
    rows.value = Array.isArray(result) ? result : pageRows(result)
  } catch (err) {
    error.value = err instanceof Error ? err.message : '合同数据加载失败'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.contract-item {
  display: grid;
  gap: 12px;
}

.contract-item header {
  display: flex;
  justify-content: space-between;
}

.contract-item h3,
.contract-item p {
  margin: 0;
}

.contract-item p {
  color: var(--muted);
}
</style>