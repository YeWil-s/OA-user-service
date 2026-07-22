<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">合同管理</h2>
        <p class="page-subtitle">合同到期预警与续签状态</p>
      </div>
      <div class="toolbar">
        <select v-model.number="filterDays" class="select" @change="loadExpiring(1)">
          <option :value="30">30天内到期</option>
          <option :value="60">60天内到期</option>
          <option :value="90">90天内到期</option>
        </select>
        <button class="btn ghost" :class="{ active: tab === 'expiring' }" @click="tab = 'expiring'; loadExpiring(1)">到期预警</button>
        <button class="btn ghost" :class="{ active: tab === 'all' }" @click="tab = 'all'; loadAll(1)">全部合同</button>
      </div>
    </div>

    <div v-if="loading" class="empty">加载中...</div>

    <!-- 到期预警 -->
    <template v-else-if="tab === 'expiring'">
      <div v-if="rows.length === 0" class="panel panel-pad empty">暂无即将到期合同</div>
      <div v-else class="grid-3">
        <article v-for="item in rows" :key="item.id" class="panel panel-pad contract-item">
          <header>
            <FileText class="icon" />
            <StatusPill
              :value="daysUntil(item.contractEnd) <= 0 ? 'expired' : 'expiring'"
              :map="{ expiring: '即将到期', expired: '已到期' }"
              :tone-map="{ expiring: 'warn', expired: 'danger' }"
            />
          </header>
          <h3>员工ID: {{ item.userId }}</h3>
          <p>{{ item.contractStart ?? '-' }} 至 {{ item.contractEnd ?? '-' }}</p>
          <p>剩余 {{ daysUntil(item.contractEnd) }} 天</p>
        </article>
      </div>
      <div v-if="total > pageSize" class="pager">
        <button class="btn ghost" :disabled="pageNum <= 1" @click="loadExpiring(pageNum - 1)">上一页</button>
        <span>{{ pageNum }} / {{ Math.ceil(total / pageSize) }}</span>
        <button class="btn ghost" :disabled="pageNum * pageSize >= total" @click="loadExpiring(pageNum + 1)">下一页</button>
      </div>
    </template>

    <!-- 全部合同 -->
    <template v-else>
      <div v-if="rows.length === 0" class="panel panel-pad empty">暂无合同记录</div>
      <div v-else class="table-wrap panel panel-pad">
        <table class="data-table">
          <thead><tr><th>员工ID</th><th>身份证号</th><th>合同开始</th><th>合同结束</th><th>剩余天数</th><th>状态</th></tr></thead>
          <tbody>
            <tr v-for="item in rows" :key="item.id">
              <td>{{ item.userId }}</td>
              <td>{{ item.idCard ? item.idCard.slice(0, 6) + '****' : '-' }}</td>
              <td>{{ item.contractStart ?? '-' }}</td>
              <td>{{ item.contractEnd ?? '-' }}</td>
              <td>{{ item.contractEnd ? daysUntil(item.contractEnd) + '天' : '-' }}</td>
              <td>
                <StatusPill
                  :value="item.contractEnd ? (daysUntil(item.contractEnd) <= 0 ? 'expired' : daysUntil(item.contractEnd) <= 30 ? 'expiring' : 'normal') : 'normal'"
                  :map="{ normal: '正常', expiring: '即将到期', expired: '已到期' }"
                  :tone-map="{ normal: 'success', expiring: 'warn', expired: 'danger' }"
                />
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="total > pageSize" class="pager">
        <button class="btn ghost" :disabled="pageNum <= 1" @click="loadAll(pageNum - 1)">上一页</button>
        <span>{{ pageNum }} / {{ Math.ceil(total / pageSize) }}</span>
        <button class="btn ghost" :disabled="pageNum * pageSize >= total" @click="loadAll(pageNum + 1)">下一页</button>
      </div>
    </template>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { FileText } from 'lucide-vue-next'
import { assetApi } from '@/api/services'
import type { EmployeeArchive } from '@/api/types'
import StatusPill from '@/components/StatusPill.vue'

const tab = ref<'expiring' | 'all'>('expiring')
const rows = ref<EmployeeArchive[]>([])
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)
const loading = ref(false)
const filterDays = ref(30)

onMounted(() => loadExpiring(1))

function loadExpiring(page: number) {
  loading.value = true
  pageNum.value = page
  assetApi.expiringContracts(filterDays.value)
    .then(res => { rows.value = res.records ?? []; total.value = res.total })
    .finally(() => loading.value = false)
}

function loadAll(page: number) {
  loading.value = true
  pageNum.value = page
  assetApi.contracts({ pageNum: page, pageSize })
    .then(res => { rows.value = res.records ?? []; total.value = res.total })
    .finally(() => loading.value = false)
}

function daysUntil(endDate?: string): number {
  if (!endDate) return 999
  return Math.ceil((new Date(endDate).getTime() - Date.now()) / 86400000)
}
</script>

<style scoped>
.empty { text-align: center; padding: 40px; color: var(--muted); }
.pager { display: flex; align-items: center; gap: 12px; margin-top: 12px; justify-content: center; }
.active { background: var(--primary-soft); color: var(--primary); }
.contract-item { display: grid; gap: 10px; }
.contract-item header { display: flex; justify-content: space-between; align-items: center; }
.contract-item h3, .contract-item p { margin: 0; }
.contract-item p { color: var(--muted); font-size: 13px; }
.grid-3 { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
</style>
