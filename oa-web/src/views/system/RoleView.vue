<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">角色管理</h2>
        <p class="page-subtitle">角色、数据范围与菜单授权</p>
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
          <thead><tr><th>角色名称</th><th>编码</th><th>数据范围</th><th>说明</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="row in rows" :key="row.id">
              <td>{{ row.roleName }}</td>
              <td>{{ row.roleCode }}</td>
              <td>{{ dataScopeText(row.dataScope) }}</td>
              <td>{{ row.roleDesc || '-' }}</td>
              <td><StatusPill :value="row.status" /></td>
              <td><div class="row-actions">
                <button class="btn icon-btn" aria-label="授权" @click="openAssign(row)"><ShieldCheck class="icon" /></button>
                <button class="btn icon-btn" aria-label="编辑" @click="openEdit(row)"><Pencil class="icon" /></button>
                <button class="btn icon-btn danger" aria-label="删除" @click="remove(row)"><Trash2 class="icon" /></button>
              </div></td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <ModalDialog v-model="dialogOpen" :title="editing?.id ? '编辑角色' : '新增角色'">
      <div class="form-grid">
        <label class="form-item"><span class="form-label">角色名称</span><input v-model="form.roleName" class="field" /></label>
        <label class="form-item"><span class="form-label">角色编码</span><input v-model="form.roleCode" class="field" /></label>
        <label class="form-item"><span class="form-label">数据范围</span><select v-model.number="form.dataScope" class="select"><option :value="0">全部数据</option><option :value="1">本部门及下级</option><option :value="2">本部门</option><option :value="3">本人</option></select></label>
        <label class="form-item"><span class="form-label">状态</span><select v-model.number="form.status" class="select"><option :value="1">启用</option><option :value="0">停用</option></select></label>
        <label class="form-item full"><span class="form-label">说明</span><textarea v-model="form.roleDesc" class="textarea" /></label>
      </div>
      <template #footer><button class="btn" @click="dialogOpen = false">取消</button><button class="btn primary" @click="save">保存</button></template>
    </ModalDialog>

    <ModalDialog v-model="assignOpen" title="分配菜单" width="560px">
      <div class="menu-checks">
        <label v-for="menu in flatMenus" :key="menu.id" class="check-row" :style="{ paddingLeft: `${menu.level * 18 + 10}px` }">
          <input v-model="checkedMenuIds" type="checkbox" :value="menu.id" />
          <span>{{ menu.menuName }}</span>
          <small>{{ menu.permissionCode || menu.path || '-' }}</small>
        </label>
      </div>
      <template #footer><button class="btn" @click="assignOpen = false">取消</button><button class="btn primary" @click="saveAssign">保存授权</button></template>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { Pencil, Plus, RefreshCw, ShieldCheck, Trash2 } from 'lucide-vue-next'
import { computed, onMounted, reactive, ref } from 'vue'
import ModalDialog from '@/components/ModalDialog.vue'
import StatusPill from '@/components/StatusPill.vue'
import { withFallback } from '@/api/http'
import { systemApi } from '@/api/services'
import { mockMenus, mockRoles } from '@/api/mock'
import type { MenuNode, Role } from '@/api/types'

const rows = ref<Role[]>([])
const menus = ref<MenuNode[]>(mockMenus)
const mocked = ref(false)
const dialogOpen = ref(false)
const assignOpen = ref(false)
const editing = ref<Role | null>(null)
const assigning = ref<Role | null>(null)
const checkedMenuIds = ref<number[]>([])
const form = reactive<Partial<Role>>({})

const flatMenus = computed(() => flatten(menus.value))
function flatten(nodes: MenuNode[], level = 0): Array<MenuNode & { level: number }> {
  return nodes.flatMap((node) => [{ ...node, level }, ...flatten(node.children || [], level + 1)])
}

const dataScopeText = (value?: number) => ({ 0: '全部数据', 1: '本部门及下级', 2: '本部门', 3: '本人' }[value ?? 3])

async function load() {
  const [roleResult, menuResult] = await Promise.all([
    withFallback(systemApi.roles({ pageNum: 1, pageSize: 100 }), mockRoles),
    withFallback(systemApi.menus(), mockMenus)
  ])
  rows.value = roleResult.data.records || roleResult.data.list || []
  menus.value = menuResult.data
  mocked.value = roleResult.mocked || menuResult.mocked
}

function openCreate() { editing.value = null; Object.assign(form, { roleName: '', roleCode: '', roleDesc: '', dataScope: 3, sortOrder: 0, status: 1 }); dialogOpen.value = true }
function openEdit(row: Role) { editing.value = row; Object.assign(form, row); dialogOpen.value = true }
async function save() { if (editing.value?.id) { if (!mocked.value) await systemApi.updateRole(editing.value.id, form); Object.assign(editing.value, form) } else { if (!mocked.value) await systemApi.addRole(form); rows.value.unshift({ id: Date.now(), ...form } as Role) } dialogOpen.value = false }
async function remove(row: Role) { if (!mocked.value) await systemApi.deleteRole(row.id); rows.value = rows.value.filter((item) => item.id !== row.id) }
async function openAssign(row: Role) { assigning.value = row; const result = mocked.value ? { data: [11, 12], mocked: true } : await withFallback(systemApi.roleMenus(row.id), []); checkedMenuIds.value = result.data; assignOpen.value = true }
async function saveAssign() { if (assigning.value && !mocked.value) await systemApi.assignRoleMenus(assigning.value.id, checkedMenuIds.value); assignOpen.value = false }
onMounted(load)
</script>

<style scoped>
.full {
  grid-column: 1 / -1;
}

.menu-checks {
  display: grid;
  gap: 8px;
}

.check-row {
  min-height: 40px;
  display: grid;
  grid-template-columns: 20px minmax(110px, 1fr) minmax(0, 1.4fr);
  align-items: center;
  gap: 10px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--surface-soft);
}

.check-row small {
  color: var(--muted);
}
</style>
