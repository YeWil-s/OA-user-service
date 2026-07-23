<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">菜单管理</h2>
        <p class="page-subtitle">前端路由、权限标识与菜单显隐</p>
      </div>
      <div class="toolbar">
        <button class="btn" :disabled="loading" @click="load"><RefreshCw class="icon" />{{ loading ? '刷新中' : '刷新' }}</button>
        <button v-if="!allExpanded" class="btn" @click="expandAll">展开全部</button>
        <button v-else class="btn" @click="collapseAll">折叠全部</button>
        <button class="btn primary" @click="openCreate()"><Plus class="icon" />新增</button>
      </div>
    </div>

    <p v-if="error" class="error-banner">{{ error }}</p>
    <p v-if="message" class="success-banner">{{ message }}</p>

    <section class="panel panel-pad">
      <div class="tree-header">
        <span class="tree-header-label">菜单名称</span>
        <span class="tree-header-label">类型</span>
        <span class="tree-header-label">路径</span>
        <span class="tree-header-label">权限标识</span>
        <span class="tree-header-label">状态</span>
        <span class="tree-header-label">操作</span>
      </div>
      <div v-if="loading && rows.length === 0" class="tree-loading">
        <div v-for="i in 6" :key="i" class="skeleton skeleton-line" style="margin-bottom:12px;height:36px" />
      </div>
      <MenuTree
        v-else
        :nodes="rows"
        :expanded-ids="expandedIds"
        @toggle-expand="toggleExpand"
      >
        <template #node="{ node }">
          <span class="tree-node-name">{{ node.menuName }}</span>
          <span class="tree-node-type">{{ typeText(node.menuType) }}</span>
          <span class="tree-node-path">{{ node.path || '-' }}</span>
          <span class="tree-node-perm">{{ node.permissionCode || '-' }}</span>
          <span class="tree-node-status"><StatusPill :value="node.status" /></span>
          <span class="tree-node-actions">
            <button class="btn icon-btn" aria-label="新增下级" @click.stop="openCreate(node.id)"><Plus class="icon" /></button>
            <button class="btn icon-btn" aria-label="编辑" @click.stop="openEdit(node)"><Pencil class="icon" /></button>
            <button class="btn icon-btn danger" aria-label="删除" @click.stop="remove(node)"><Trash2 class="icon" /></button>
          </span>
        </template>
      </MenuTree>
    </section>

    <ModalDialog v-model="dialogOpen" :title="editing?.id ? '编辑菜单' : '新增菜单'">
      <div class="form-grid">
        <label class="form-item"><span class="form-label">上级菜单</span><select v-model.number="form.parentId" class="select"><option :value="0">根菜单</option><option v-for="menu in flatOptions" :key="menu.id" :value="menu.id">{{ '─'.repeat(menu.level) + ' ' + menu.menuName }}</option></select></label>
        <label class="form-item"><span class="form-label">菜单名称</span><input v-model="form.menuName" class="field" /></label>
        <label class="form-item"><span class="form-label">类型</span><select v-model.number="form.menuType" class="select"><option :value="1">目录</option><option :value="2">菜单</option><option :value="3">按钮</option></select></label>
        <label class="form-item"><span class="form-label">路由路径</span><input v-model="form.path" class="field" /></label>
        <label class="form-item"><span class="form-label">组件路径</span><input v-model="form.component" class="field" /></label>
        <label class="form-item"><span class="form-label">权限标识</span><input v-model="form.permissionCode" class="field" /></label>
        <label class="form-item"><span class="form-label">排序</span><input v-model.number="form.sortOrder" class="field" type="number" /></label>
        <label class="form-item"><span class="form-label">状态</span><select v-model.number="form.status" class="select"><option :value="1">启用</option><option :value="0">停用</option></select></label>
      </div>
      <template #footer><button class="btn" @click="dialogOpen = false">取消</button><button class="btn primary" :disabled="saving" @click="save">{{ saving ? '保存中' : '保存' }}</button></template>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { Pencil, Plus, RefreshCw, Trash2 } from 'lucide-vue-next'
import { computed, onMounted, reactive, ref } from 'vue'
import MenuTree from '@/components/MenuTree.vue'
import ModalDialog from '@/components/ModalDialog.vue'
import StatusPill from '@/components/StatusPill.vue'
import { systemApi } from '@/api/services'
import type { MenuNode } from '@/api/types'

const rows = ref<MenuNode[]>([])
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const message = ref('')
const dialogOpen = ref(false)
const editing = ref<MenuNode | null>(null)
const form = reactive<Partial<MenuNode>>({})
const expandedIds = ref<number[]>([])

const flatOptions = computed(() => flatten(rows.value))
const allExpanded = computed(() => {
  const all = collectIds(rows.value)
  return all.length > 0 && all.every((id) => expandedIds.value.includes(id))
})

function collectIds(nodes: MenuNode[]): number[] {
  return nodes.flatMap((n) => [n.id, ...collectIds(n.children || [])])
}

function flatten(nodes: MenuNode[], level = 0): Array<MenuNode & { level: number }> {
  return nodes.flatMap((node) => [{ ...node, level }, ...flatten(node.children || [], level + 1)])
}

function toggleExpand(id: number) {
  const idx = expandedIds.value.indexOf(id)
  if (idx >= 0) {
    expandedIds.value.splice(idx, 1)
  } else {
    expandedIds.value.push(id)
  }
}

function expandAll() {
  expandedIds.value = collectIds(rows.value)
}

function collapseAll() {
  expandedIds.value = []
}

const typeText = (type?: number) => ({ 1: '目录', 2: '菜单', 3: '按钮' }[type ?? 2])

function menuPayload() {
  return {
    parentId: form.parentId ?? 0,
    menuName: form.menuName,
    menuType: form.menuType ?? 2,
    path: form.path,
    component: form.component,
    permissionCode: form.permissionCode,
    icon: form.icon,
    sortOrder: form.sortOrder ?? 0,
    visible: form.visible ?? 1,
    status: form.status ?? 1
  }
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    rows.value = await systemApi.menus()
    expandedIds.value = collectIds(rows.value)
  } catch (err) {
    error.value = err instanceof Error ? err.message : '菜单数据加载失败'
  } finally {
    loading.value = false
  }
}

function openCreate(parentId = 0) { editing.value = null; Object.assign(form, { parentId, menuName: '', menuType: 2, path: '', component: '', permissionCode: '', icon: '', sortOrder: 0, status: 1, visible: 1 }); dialogOpen.value = true }
function openEdit(row: MenuNode) { editing.value = row; Object.assign(form, { parentId: row.parentId ?? 0, menuName: row.menuName, menuType: row.menuType ?? 2, path: row.path || '', component: row.component || '', permissionCode: row.permissionCode || '', icon: row.icon || '', sortOrder: row.sortOrder ?? 0, status: row.status ?? 1, visible: row.visible ?? 1 }); dialogOpen.value = true }
async function save() {
  saving.value = true
  error.value = ''
  message.value = ''
  try {
    if (editing.value?.id) {
      await systemApi.updateMenu(editing.value.id, menuPayload())
      message.value = '菜单已更新'
    } else {
      await systemApi.addMenu(menuPayload())
      message.value = '菜单已新增'
    }
    dialogOpen.value = false
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '菜单保存失败'
  } finally {
    saving.value = false
  }
}
async function remove(row: MenuNode) {
  error.value = ''
  message.value = ''
  try {
    await systemApi.deleteMenu(row.id)
    message.value = '菜单已删除'
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '菜单删除失败'
  }
}
onMounted(load)
</script>

<style scoped>
.tree-header {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 12px 10px;
  border-bottom: 1px solid var(--border);
  margin-bottom: 6px;
}
.tree-header-label {
  color: var(--muted);
  font-size: 11px;
  font-weight: 720;
}
.tree-header-label:nth-child(1) { flex: 1; min-width: 140px; }
.tree-header-label:nth-child(2) { width: 52px; flex: 0 0 52px; }
.tree-header-label:nth-child(3) { width: 120px; flex: 0 0 120px; }
.tree-header-label:nth-child(4) { width: 120px; flex: 0 0 120px; }
.tree-header-label:nth-child(5) { width: 56px; flex: 0 0 56px; }
.tree-header-label:nth-child(6) { width: 126px; flex: 0 0 126px; }

.tree-node-name { flex: 1; min-width: 140px; color: var(--text); font-size: 13px; font-weight: 650; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.tree-node-type { width: 52px; flex: 0 0 52px; color: var(--muted); font-size: 11px; }
.tree-node-path { width: 120px; flex: 0 0 120px; color: var(--muted); font-size: 11px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.tree-node-perm { width: 120px; flex: 0 0 120px; color: var(--info); font-size: 10px; font-family: ui-monospace, SFMono-Regular, Consolas, monospace; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.tree-node-status { width: 56px; flex: 0 0 56px; }
.tree-node-actions { width: 126px; flex: 0 0 126px; display: flex; gap: 4px; }
.tree-node-actions .btn { min-height: 30px; }

.tree-loading { padding: 12px 0; }

@media (max-width: 860px) {
  .tree-header-label:nth-child(3),
  .tree-node-path { display: none; }
  .tree-header-label:nth-child(4),
  .tree-node-perm { display: none; }
}
</style>