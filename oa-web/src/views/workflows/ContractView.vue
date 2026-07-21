<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">合同管理</h2>
        <p class="page-subtitle">合同到期预警与续签状态</p>
      </div>
      <span class="mock-banner">资产人事后端未完成</span>
    </div>

    <div class="grid-3">
      <article v-for="item in rows" :key="item.id" class="panel panel-pad contract-item">
        <header>
          <FileText class="icon" />
          <StatusPill :value="item.status" :map="{ normal: '正常', expiring: '即将到期', expired: '已到期' }" :tone-map="{ normal: 'success', expiring: 'warn', expired: 'danger' }" />
        </header>
        <h3>{{ item.name }}</h3>
        <p>{{ item.period }}</p>
        <button class="btn ghost">续签</button>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
import { FileText } from 'lucide-vue-next'
import StatusPill from '@/components/StatusPill.vue'

const rows = [
  { id: 1, name: '李明', period: '2024-07-01 至 2027-06-30', status: 'normal' },
  { id: 2, name: '周宁', period: '2023-08-01 至 2026-08-01', status: 'expiring' },
  { id: 3, name: '王瑜', period: '2023-01-01 至 2026-01-01', status: 'expired' }
]
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
