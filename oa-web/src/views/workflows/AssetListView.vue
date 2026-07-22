<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">资产台账</h2>
        <p class="page-subtitle">固定资产、办公用品、电子设备管理</p>
      </div>
      <div class="toolbar">
        <button class="btn primary" @click="openCreate"><Plus class="icon" />登记资产</button>
      </div>
    </div>

    <section class="panel panel-pad">
      <div class="toolbar filters">
        <input v-model="keyword" class="field filter-input" placeholder="名称/编码" @keyup.enter="fetchData(1)" />
        <select v-model.number="filterCategory" class="select filter-input" @change="fetchData(1)">
          <option :value="undefined">全部分类</option>
          <option :value="1">固定资产</option>
          <option :value="2">办公用品</option>
          <option :value="3">电子设备</option>
        </select>
        <select v-model.number="filterStatus" class="select filter-input" @change="fetchData(1)">
          <option :value="undefined">全部状态</option>
          <option :value="1">可领用</option>
          <option :value="2">已领用</option>
          <option :value="0">已报废</option>
        </select>
        <button class="btn ghost" @click="fetchData(1)">搜索</button>
      </div>

      <div v-if="loading" class="empty">加载中...</div>
      <div v-else-if="rows.length === 0" class="empty">暂无资产记录</div>
      <div v-else class="table-wrap">
        <table class="data-table">
          <thead><tr><th>名称</th><th>编码</th><th>分类</th><th>型号</th><th>购置日期</th><th>价格</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="row in rows" :key="row.id">
              <td>{{ row.assetName }}</td>
              <td>{{ row.assetCode }}</td>
              <td>{{ categoryLabel(row.category) }}</td>
              <td>{{ row.model ?? '-' }}</td>
              <td>{{ row.purchaseDate ?? '-' }}</td>
              <td>{{ row.purchasePrice != null ? '¥' + row.purchasePrice : '-' }}</td>
              <td><StatusPill :value="row.status" :map="statusMap" :tone-map="{ 1: 'success', 2: 'warn', 0: 'muted' }" /></td>
              <td>
                <div class="row-actions">
                  <button class="btn icon-btn" @click="openEdit(row)"><Pencil class="icon" /></button>
                  <button v-if="row.status === 1" class="btn icon-btn danger" @click="handleScrap(row.id)"><Trash2 class="icon" /></button>
                  <button v-if="row.status === 1" class="btn ghost" @click="openBorrow(row)">领用</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="total > pageSize" class="pager">
        <button class="btn ghost" :disabled="pageNum <= 1" @click="fetchData(pageNum - 1)">上一页</button>
        <span>{{ pageNum }} / {{ Math.ceil(total / pageSize) }}</span>
        <button class="btn ghost" :disabled="pageNum * pageSize >= total" @click="fetchData(pageNum + 1)">下一页</button>
      </div>
    </section>

    <!-- Asset create/edit dialog -->
    <ModalDialog v-model="assetDialog" :title="editing ? '编辑资产' : '登记资产'">
      <div class="form-grid">
        <label class="form-item"><span>名称</span><input v-model="assetForm.assetName" class="field" /></label>
        <label class="form-item"><span>编码</span><input v-model="assetForm.assetCode" class="field" /></label>
        <label class="form-item"><span>分类</span><select v-model.number="assetForm.category" class="select"><option :value="1">固定资产</option><option :value="2">办公用品</option><option :value="3">电子设备</option></select></label>
        <label class="form-item"><span>型号</span><input v-model="assetForm.model" class="field" /></label>
        <label class="form-item"><span>购置日期</span><input v-model="assetForm.purchaseDate" class="field" type="date" /></label>
        <label class="form-item"><span>价格</span><input v-model.number="assetForm.purchasePrice" class="field" type="number" step="0.01" /></label>
      </div>
      <template #footer>
        <button class="btn" @click="assetDialog = false">取消</button>
        <button class="btn primary" :disabled="saving" @click="handleAssetSave">{{ saving ? '保存中' : '保存' }}</button>
      </template>
    </ModalDialog>

    <!-- Borrow dialog -->
    <ModalDialog v-model="borrowDialog" title="资产领用">
      <div class="form-grid">
        <p>资产: {{ borrowTarget?.assetName }} ({{ borrowTarget?.assetCode }})</p>
        <label class="form-item"><span>领用人ID</span><input v-model.number="borrowForm.userId" class="field" type="number" /></label>
        <label class="form-item"><span>领用日期</span><input v-model="borrowForm.borrowDate" class="field" type="date" /></label>
        <label class="form-item"><span>预计归还</span><input v-model="borrowForm.expectReturnDate" class="field" type="date" /></label>
      </div>
      <template #footer>
        <button class="btn" @click="borrowDialog = false">取消</button>
        <button class="btn primary" :disabled="saving" @click="handleBorrow">{{ saving ? '提交中' : '确认领用' }}</button>
      </template>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Pencil, Plus, Trash2 } from 'lucide-vue-next'
import { assetApi } from '@/api/services'
import type { Asset } from '@/api/types'
import ModalDialog from '@/components/ModalDialog.vue'
import StatusPill from '@/components/StatusPill.vue'

const rows = ref<Asset[]>([])
const pageNum = ref(1)
const pageSize = 10
const total = ref(0)
const loading = ref(false)
const saving = ref(false)
const keyword = ref('')
const filterCategory = ref<number | undefined>(undefined)
const filterStatus = ref<number | undefined>(undefined)

const statusMap = { 0: '报废', 1: '可领用', 2: '已领用' }

const assetDialog = ref(false)
const borrowDialog = ref(false)
const editing = ref<Asset | null>(null)
const borrowTarget = ref<Asset | null>(null)
const assetForm = ref<Partial<Asset>>({ category: 1 })
const borrowForm = ref({ userId: undefined as number | undefined, borrowDate: new Date().toISOString().slice(0, 10), expectReturnDate: '' as string })

onMounted(() => fetchData(1))

function fetchData(page: number) {
  loading.value = true
  pageNum.value = page
  const params: Record<string, unknown> = { pageNum: page, pageSize }
  if (keyword.value) params.keyword = keyword.value
  if (filterCategory.value != null) params.category = filterCategory.value
  if (filterStatus.value != null) params.status = filterStatus.value
  assetApi.assets(params)
    .then(res => { rows.value = res.records ?? []; total.value = res.total })
    .finally(() => loading.value = false)
}

function openCreate() {
  editing.value = null
  assetForm.value = { assetName: '', assetCode: '', category: 1, model: '', purchaseDate: '', purchasePrice: undefined }
  assetDialog.value = true
}

function openEdit(row: Asset) {
  editing.value = row
  assetForm.value = { ...row }
  assetDialog.value = true
}

async function handleAssetSave() {
  if (!assetForm.value.assetName || !assetForm.value.assetCode) return
  saving.value = true
  try {
    if (editing.value) {
      await assetApi.updateAsset(editing.value.id, assetForm.value)
    } else {
      await assetApi.createAsset(assetForm.value)
    }
    assetDialog.value = false
    fetchData(pageNum.value)
  } finally { saving.value = false }
}

async function handleScrap(id: number) {
  if (!confirm('确定报废该资产？')) return
  await assetApi.scrapAsset(id)
  fetchData(pageNum.value)
}

function openBorrow(row: Asset) {
  borrowTarget.value = row
  borrowForm.value = { userId: undefined, borrowDate: new Date().toISOString().slice(0, 10), expectReturnDate: '' }
  borrowDialog.value = true
}

async function handleBorrow() {
  if (!borrowForm.value.userId) return
  saving.value = true
  try {
    await assetApi.borrow({ assetId: borrowTarget.value!.id, userId: borrowForm.value.userId, borrowDate: borrowForm.value.borrowDate, expectReturnDate: borrowForm.value.expectReturnDate || undefined })
    borrowDialog.value = false
    fetchData(pageNum.value)
  } finally { saving.value = false }
}

function categoryLabel(c: number) { const m: Record<number, string> = { 1: '固定资产', 2: '办公用品', 3: '电子设备' }; return m[c] ?? '-' }
</script>

<style scoped>
.empty { text-align: center; padding: 40px; color: var(--muted); }
.pager { display: flex; align-items: center; gap: 12px; margin-top: 12px; justify-content: center; }
.danger { color: var(--danger); }
</style>
