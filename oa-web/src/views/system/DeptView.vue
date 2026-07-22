<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">部门管理</h2>
        <p class="page-subtitle">维护组织树、部门编码与启停状态</p>
      </div>
      <div class="toolbar">
        <span v-if="mocked" class="mock-banner">演示数据</span>
        <button class="btn" type="button" @click="load"><RefreshCw class="icon" />刷新</button>
        <button class="btn primary" type="button" @click="openCreate()"><Plus class="icon" />新增</button>
      </div>
    </div>

    <section class="panel panel-pad">
      <div class="dept-tree">
        <div v-for="node in flatRows" :key="node.id" class="dept-row" :style="{ paddingLeft: `${node.level * 22 + 12}px` }">
          <Building2 class="icon" />
          <strong>{{ node.deptName }}</strong>
          <span>{{ node.deptCode }}</span>
          <StatusPill :value="node.status" />
          <div class="row-actions">
            <button class="btn icon-btn" aria-label="新增下级" @click="openCreate(node.id)"><Plus class="icon" /></button>
            <button class="btn icon-btn" aria-label="编辑" @click="openEdit(node)"><Pencil class="icon" /></button>
            <button class="btn icon-btn danger" aria-label="删除" @click="remove(node)"><Trash2 class="icon" /></button>
          </div>
        </div>
      </div>
    </section>

    <ModalDialog v-model="dialogOpen" :title="editing?.id ? '编辑部门' : '新增部门'">
      <div class="form-grid">
        <label class="form-item">
          <span class="form-label">上级部门</span>
          <select v-model.number="form.parentId" class="select">
            <option :value="0">根部门</option>
            <option v-for="dept in flatRows" :key="dept.id" :value="dept.id">{{ dept.deptName }}</option>
          </select>
        </label>
        <label class="form-item">
          <span class="form-label">部门名称</span>
          <input v-model="form.deptName" class="field" />
        </label>
        <label class="form-item">
          <span class="form-label">部门编码</span>
          <input v-model="form.deptCode" class="field" />
        </label>
        <label class="form-item">
          <span class="form-label">排序</span>
          <input v-model.number="form.sortOrder" class="field" type="number" />
        </label>
        <label class="form-item">
          <span class="form-label">状态</span>
          <select v-model.number="form.status" class="select">
            <option :value="1">启用</option>
            <option :value="0">停用</option>
          </select>
        </label>
      </div>
      <template #footer>
        <button class="btn" @click="dialogOpen = false">取消</button>
        <button class="btn primary" @click="save">保存</button>
      </template>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { Building2, Pencil, Plus, RefreshCw, Trash2 } from 'lucide-vue-next'
import { computed, onMounted, reactive, ref } from 'vue'
import ModalDialog from '@/components/ModalDialog.vue'
import StatusPill from '@/components/StatusPill.vue'
import { withFallback } from '@/api/http'
import { systemApi } from '@/api/services'
import { mockDepts } from '@/api/mock'
import type { DeptNode } from '@/api/types'

type DeptRow = DeptNode & { level: number }

const rows = ref<DeptNode[]>([])
const mocked = ref(false)
const dialogOpen = ref(false)
const editing = ref<DeptNode | null>(null)
const form = reactive<Partial<DeptNode>>({})

const flatRows = computed(() => flatten(rows.value))

function flatten(nodes: DeptNode[], level = 0): DeptRow[] {
  return nodes.flatMap((node) => [{ ...node, level }, ...flatten(node.children || [], level + 1)])
}

async function load() {
  const result = await withFallback(systemApi.depts(), mockDepts)
  rows.value = result.data
  mocked.value = result.mocked
}

function openCreate(parentId = 0) {
  editing.value = null
  Object.assign(form, { parentId, deptName: '', deptCode: '', sortOrder: 0, status: 1 })
  dialogOpen.value = true
}

function openEdit(row: DeptNode) {
  editing.value = row
  Object.assign(form, row)
  dialogOpen.value = true
}

async function save() {
  if (editing.value?.id) {
    if (!mocked.value) await systemApi.updateDept(editing.value.id, form)
  } else {
    if (!mocked.value) await systemApi.addDept(form)
  }
  dialogOpen.value = false
  await load()
}

async function remove(row: DeptNode) {
  if (!mocked.value) await systemApi.deleteDept(row.id)
  await load()
}

onMounted(load)
</script>

<style scoped>
.dept-tree {
  display: grid;
  gap: 8px;
}

.dept-row {
  min-height: 50px;
  display: grid;
  grid-template-columns: 24px minmax(160px, 1fr) 160px 80px auto;
  align-items: center;
  gap: 10px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--surface-soft);
}

.dept-row > span:not(.pill) {
  color: var(--muted);
  font-size: 13px;
}

@media (max-width: 760px) {
  .dept-row {
    grid-template-columns: 24px 1fr;
    padding: 10px 12px;
  }

  .dept-row .row-actions {
    grid-column: 2;
  }
}
</style>
