<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">{{ title }}</h2>
        <p class="page-subtitle">{{ config.subtitle }}</p>
      </div>
      <div class="toolbar">
        <span class="mock-banner">后端接口待完成</span>
        <button class="btn primary" @click="dialogOpen = true"><Plus class="icon" />新增</button>
      </div>
    </div>

    <section class="panel panel-pad">
      <div class="toolbar filters">
        <input v-model="keyword" class="field filter-input" placeholder="关键词" />
        <select class="select filter-input"><option>全部状态</option><option>正常</option><option>审批中</option></select>
      </div>
      <div class="table-wrap">
        <table class="data-table">
          <thead><tr><th v-for="column in config.columns" :key="column.key">{{ column.label }}</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="row in filteredRows" :key="row.id">
              <td v-for="column in config.columns" :key="column.key">{{ rowValue(row, column.key) }}</td>
              <td><div class="row-actions"><button class="btn icon-btn" aria-label="查看"><Eye class="icon" /></button><button class="btn icon-btn" aria-label="编辑"><Pencil class="icon" /></button></div></td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <ModalDialog v-model="dialogOpen" :title="`新增${title}`">
      <div class="form-grid">
        <label v-for="column in config.columns.slice(0, 4)" :key="column.key" class="form-item">
          <span class="form-label">{{ column.label }}</span>
          <input class="field" />
        </label>
      </div>
      <template #footer><button class="btn" @click="dialogOpen = false">取消</button><button class="btn primary" @click="dialogOpen = false">保存</button></template>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { Eye, Pencil, Plus } from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { useRoute } from 'vue-router'
import ModalDialog from '@/components/ModalDialog.vue'
import { workflowRows } from '@/api/mock'

const route = useRoute()
const title = computed(() => String(route.meta.title || '业务管理'))
const moduleKey = computed(() => String(route.meta.module || 'applications') as keyof typeof workflowRows)
const keyword = ref('')
const dialogOpen = ref(false)

const configs = {
  attendanceRecords: { subtitle: '个人、部门、全公司考勤记录查询', columns: [{ key: 'name', label: '姓名' }, { key: 'dept', label: '部门' }, { key: 'date', label: '日期' }, { key: 'punchIn', label: '上班' }, { key: 'punchOut', label: '下班' }, { key: 'status', label: '状态' }] },
  shifts: { subtitle: '班次模板、弹性时间与分配规则', columns: [{ key: 'name', label: '班次' }, { key: 'start', label: '上班' }, { key: 'end', label: '下班' }, { key: 'flex', label: '弹性' }, { key: 'status', label: '状态' }] },
  applications: { subtitle: '申请单状态、时长与审批链路', columns: [{ key: 'no', label: '单号' }, { key: 'type', label: '类型' }, { key: 'duration', label: '时长' }, { key: 'status', label: '状态' }, { key: 'time', label: '提交时间' }] },
  assets: { subtitle: '资产登记、领用、归还与报废', columns: [{ key: 'code', label: '资产编码' }, { key: 'name', label: '资产名称' }, { key: 'category', label: '分类' }, { key: 'owner', label: '领用人' }, { key: 'status', label: '状态' }] }
}

const config = computed(() => configs[moduleKey.value])
const filteredRows = computed(() => workflowRows[moduleKey.value].filter((row) => JSON.stringify(row).includes(keyword.value)))
function rowValue(row: unknown, key: string) {
  return (row as Record<string, unknown>)[key] || '-'
}
</script>

<style scoped>
.filters {
  margin-bottom: 14px;
}

.filter-input {
  width: 180px;
}
</style>
