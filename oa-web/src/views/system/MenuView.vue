<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">菜单管理</h2>
        <p class="page-subtitle">前端路由、权限标识与菜单显隐</p>
      </div>
      <div class="toolbar">
        <span v-if="mocked" class="mock-banner">演示数据</span>
        <button class="btn" @click="load"><RefreshCw class="icon" />刷新</button>
        <button class="btn primary" @click="openCreate()"><Plus class="icon" />新增</button>
      </div>
    </div>

    <section class="panel panel-pad">
      <div class="table-wrap">
        <table class="data-table menu-table">
          <thead><tr><th>菜单名称</th><th>类型</th><th>路径</th><th>权限标识</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
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
import { mockMenus } from '@/api/mock'
import type { MenuNode } from '@/api/types'

type MenuRow = MenuNode & { level: number }

const rows = ref<MenuNode[]>([])
const mocked = ref(false)
const dialogOpen = ref(false)
const editing = ref<MenuNode | null>(null)
const form = reactive<Partial<MenuNode>>({})
const flatRows = computed(() => flatten(rows.value))

function flatten(nodes: MenuNode[], level = 0): MenuRow[] {
  return nodes.flatMap((node) => [{ ...node, level }, ...flatten(node.children || [], level + 1)])
}

const typeText = (type?: number) => ({ 1: '目录', 2: '菜单', 3: '按钮' }[type ?? 2])

async function load() {
  const result = await withFallback(systemApi.menus(), mockMenus)
  rows.value = result.data
  mocked.value = result.mocked
}

function openCreate(parentId = 0) { editing.value = null; Object.assign(form, { parentId, menuName: '', menuType: 2, path: '', component: '', permissionCode: '', sortOrder: 0, status: 1, visible: 1 }); dialogOpen.value = true }
function openEdit(row: MenuNode) { editing.value = row; Object.assign(form, row); dialogOpen.value = true }
async function save() { if (editing.value?.id) { if (!mocked.value) await systemApi.updateMenu(editing.value.id, form) } else { if (!mocked.value) await systemApi.addMenu(form) } dialogOpen.value = false; await load() }
async function remove(row: MenuNode) { if (!mocked.value) await systemApi.deleteMenu(row.id); await load() }
onMounted(load)
</script>
