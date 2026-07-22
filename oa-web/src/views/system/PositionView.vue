<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">岗位管理</h2>
        <p class="page-subtitle">岗位编码、所属部门与启用状态</p>
      </div>
      <div class="toolbar">
        <span v-if="mocked" class="mock-banner">演示数据</span>
        <button class="btn" @click="load"><RefreshCw class="icon" />刷新</button>
        <button class="btn primary" @click="openCreate"><Plus class="icon" />新增</button>
      </div>
    </div>

    <section class="panel panel-pad">
      <div class="table-wrap">
        <table class="data-table">
          <thead><tr><th>岗位名称</th><th>编码</th><th>所属部门</th><th>排序</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="row in rows" :key="row.id">
              <td>{{ row.positionName }}</td>
              <td>{{ row.positionCode }}</td>
              <td>{{ deptName(row.deptId) }}</td>
              <td>{{ row.sortOrder ?? 0 }}</td>
              <td><StatusPill :value="row.status" /></td>
              <td><div class="row-actions">
                <button class="btn icon-btn" aria-label="编辑" @click="openEdit(row)"><Pencil class="icon" /></button>
                <button class="btn icon-btn danger" aria-label="删除" @click="remove(row)"><Trash2 class="icon" /></button>
              </div></td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <ModalDialog v-model="dialogOpen" :title="editing?.id ? '编辑岗位' : '新增岗位'">
      <div class="form-grid">
        <label class="form-item"><span class="form-label">岗位名称</span><input v-model="form.positionName" class="field" /></label>
        <label class="form-item"><span class="form-label">岗位编码</span><input v-model="form.positionCode" class="field" /></label>
        <label class="form-item"><span class="form-label">所属部门</span><select v-model.number="form.deptId" class="select"><option v-for="dept in flatDepts" :key="dept.id" :value="dept.id">{{ dept.deptName }}</option></select></label>
        <label class="form-item"><span class="form-label">排序</span><input v-model.number="form.sortOrder" class="field" type="number" /></label>
        <label class="form-item"><span class="form-label">状态</span><select v-model.number="form.status" class="select"><option :value="1">启用</option><option :value="0">停用</option></select></label>
      </div>
      <template #footer><button class="btn" @click="dialogOpen = false">取消</button><button class="btn primary" @click="save">保存</button></template>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { Pencil, Plus, RefreshCw, Trash2 } from 'lucide-vue-next'
import { computed, onMounted, reactive, ref } from 'vue'
import ModalDialog from '@/components/ModalDialog.vue'
import StatusPill from '@/components/StatusPill.vue'
import { withFallback } from '@/api/http'
import { systemApi } from '@/api/services'
import { mockDepts, mockPositions } from '@/api/mock'
import type { DeptNode, Position } from '@/api/types'

const rows = ref<Position[]>([])
const depts = ref<DeptNode[]>(mockDepts)
const mocked = ref(false)
const dialogOpen = ref(false)
const editing = ref<Position | null>(null)
const form = reactive<Partial<Position>>({})
const flatDepts = computed(() => flatten(depts.value))
const flatten = (nodes: DeptNode[]): DeptNode[] => nodes.flatMap((node) => [node, ...flatten(node.children || [])])
const deptName = (id?: number) => flatDepts.value.find((item) => item.id === id)?.deptName || '-'

async function load() {
  const [positionResult, deptResult] = await Promise.all([
    withFallback(systemApi.positions({ pageNum: 1, pageSize: 100 }), mockPositions),
    withFallback(systemApi.depts(), mockDepts)
  ])
  rows.value = positionResult.data.records || positionResult.data.list || []
  depts.value = deptResult.data
  mocked.value = positionResult.mocked || deptResult.mocked
}

function openCreate() { editing.value = null; Object.assign(form, { positionName: '', positionCode: '', deptId: flatDepts.value[0]?.id, sortOrder: 0, status: 1 }); dialogOpen.value = true }
function openEdit(row: Position) { editing.value = row; Object.assign(form, row); dialogOpen.value = true }
async function save() { if (editing.value?.id) { if (!mocked.value) await systemApi.updatePosition(editing.value.id, form) } else { if (!mocked.value) await systemApi.addPosition(form) } dialogOpen.value = false; await load() }
async function remove(row: Position) { if (!mocked.value) await systemApi.deletePosition(row.id); await load() }
onMounted(load)
</script>
