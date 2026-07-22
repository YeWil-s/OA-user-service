<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">菜单管理</h2>
        <p class="page-subtitle">前端路由、权限标识与菜单显隐</p>
      </div>
      <div class="toolbar">
        <button class="btn" :disabled="loading" @click="load"><RefreshCw class="icon" />{{ loading ? '刷新中' : '刷新' }}</button>
        <button class="btn primary" @click="openCreate()"><Plus class="icon" />新增</button>
      </div>
    </div>

    <p v-if="error" class="error-banner">{{ error }}</p>
    <p v-if="message" class="success-banner">{{ message }}</p>

    <section class="panel panel-pad">
      <div class="table-wrap">
        <table class="data-table menu-table">
          <thead><tr><th>菜单名称</th><th>类型</th><th>路径</th><th>权限标识</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <SkeletonTableRows v-if="loading && flatRows.length === 0" :columns="6" />
            <tr v-for="row in flatRows" :key="row.id">
              <td :style="{ paddingLeft: `${row.level * 22 + 12}px` }">{{ row.menuName }}</td>
              <td>{{ typeText(row.menuType) }}</td>
              <td>{{ row.path || '-' }}</td>
              <td>{{ row.permissionCode || '-' }}</td>
              <td><StatusPill :value="row.status" /></td>
              <td><div class="row-actions">
                <button class="btn icon-btn" aria-label="新增下级" @click="openCreate(row.id)"><Plus class="icon" /></button>
                <button class="btn icon-btn" aria-label="编辑" @click="openEdit(row)"><Pencil class="icon" /></button>
                <button class="btn icon-btn danger" aria-label="删除" @click="remove(row)"><Trash2 class="icon" /></button>
              </div></td>
            </tr>
            <tr v-if="!loading && flatRows.length === 0"><td colspan="6"><div class="empty">暂无菜单数据</div></td></tr>
          </tbody>
        </table>
      </div>
    </section>

    <ModalDialog v-model="dialogOpen" :title="editing?.id ? '编辑菜单' : '新增菜单'">
      <div class="form-grid">
        <label class="form-item"><span class="form-label">上级菜单</span><select v-model.number="form.parentId" class="select"><option :value="0">根菜单</option><option v-for="menu in flatRows" :key="menu.id" :value="menu.id">{{ menu.menuName }}</option></select></label>
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
import ModalDialog from '@/components/ModalDialog.vue'
import SkeletonTableRows from '@/components/SkeletonTableRows.vue'
import StatusPill from '@/components/StatusPill.vue'
import { systemApi } from '@/api/services'
import type { MenuNode } from '@/api/types'

type MenuRow = MenuNode & { level: number }

const rows = ref<MenuNode[]>([])
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const message = ref('')
const dialogOpen = ref(false)
const editing = ref<MenuNode | null>(null)
const form = reactive<Partial<MenuNode>>({})
const flatRows = computed(() => flatten(rows.value))

function flatten(nodes: MenuNode[], level = 0): MenuRow[] {
  return nodes.flatMap((node) => [{ ...node, level }, ...flatten(node.children || [], level + 1)])
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