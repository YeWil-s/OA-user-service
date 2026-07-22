<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">公告列表</h2>
        <p class="page-subtitle">公司公告、部门通知与系统通知</p>
      </div>
      <div class="toolbar">
        <span v-if="mocked" class="mock-banner">演示数据</span>
        <button class="btn" @click="load"><RefreshCw class="icon" />刷新</button>
        <button class="btn primary" @click="openCreate"><Megaphone class="icon" />发布</button>
      </div>
    </div>

    <section class="panel panel-pad">
      <div class="table-wrap">
        <table class="data-table">
          <thead><tr><th>标题</th><th>类型</th><th>范围</th><th>发布时间</th><th>状态</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="row in rows" :key="row.id">
              <td>{{ row.title }}</td>
              <td>{{ noticeType(row.noticeType) }}</td>
              <td>{{ targetType(row.targetType) }}</td>
              <td>{{ row.createTime || '-' }}</td>
              <td><StatusPill :value="row.status" :map="{ '1': '已发布', '0': '草稿', '2': '已下架' }" :tone-map="{ '1': 'success', '0': 'warn', '2': 'danger' }" /></td>
              <td><div class="row-actions">
                <button class="btn icon-btn" aria-label="编辑" @click="openEdit(row)"><Pencil class="icon" /></button>
                <button class="btn icon-btn danger" aria-label="下架" @click="offline(row)"><ArchiveX class="icon" /></button>
              </div></td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <ModalDialog v-model="dialogOpen" :title="editing?.id ? '编辑公告' : '发布公告'">
      <div class="form-grid">
        <label class="form-item full"><span class="form-label">标题</span><input v-model="form.title" class="field" /></label>
        <label class="form-item"><span class="form-label">类型</span><select v-model.number="form.noticeType" class="select"><option :value="1">公司公告</option><option :value="2">部门通知</option><option :value="3">系统通知</option></select></label>
        <label class="form-item"><span class="form-label">范围</span><select v-model.number="form.targetType" class="select"><option :value="1">全公司</option><option :value="2">指定部门</option><option :value="3">指定人员</option></select></label>
        <label class="form-item"><span class="form-label">目标 ID</span><input v-model="form.targetIds" class="field" /></label>
        <label class="form-item"><span class="form-label">状态</span><select v-model.number="form.status" class="select"><option :value="1">发布</option><option :value="0">草稿</option></select></label>
        <label class="form-item full"><span class="form-label">内容</span><textarea v-model="form.content" class="textarea" /></label>
      </div>
      <template #footer><button class="btn" @click="dialogOpen = false">取消</button><button class="btn primary" @click="save">保存</button></template>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { ArchiveX, Megaphone, Pencil, RefreshCw } from 'lucide-vue-next'
import { onMounted, reactive, ref } from 'vue'
import ModalDialog from '@/components/ModalDialog.vue'
import StatusPill from '@/components/StatusPill.vue'
import { withFallback } from '@/api/http'
import { noticeApi } from '@/api/services'
import { mockNotices } from '@/api/mock'
import type { Notice } from '@/api/types'

const rows = ref<Notice[]>([])
const mocked = ref(false)
const dialogOpen = ref(false)
const editing = ref<Notice | null>(null)
const form = reactive<Partial<Notice>>({})
const noticeType = (value?: number) => ({ 1: '公司公告', 2: '部门通知', 3: '系统通知' }[value ?? 1])
const targetType = (value?: number) => ({ 1: '全公司', 2: '指定部门', 3: '指定人员' }[value ?? 1])

async function load() {
  const result = await withFallback(noticeApi.notices({ pageNum: 1, pageSize: 20 }), mockNotices)
  rows.value = result.data.records || result.data.list || []
  mocked.value = result.mocked
}

function openCreate() { editing.value = null; Object.assign(form, { title: '', noticeType: 1, targetType: 1, targetIds: '', status: 1, content: '' }); dialogOpen.value = true }
function openEdit(row: Notice) { editing.value = row; Object.assign(form, row); dialogOpen.value = true }
async function save() { if (editing.value?.id) { if (!mocked.value) await noticeApi.update(editing.value.id, form); Object.assign(editing.value, form) } else { if (!mocked.value) await noticeApi.publish(form); rows.value.unshift({ id: Date.now(), createTime: new Date().toLocaleString(), ...form } as Notice) } dialogOpen.value = false }
async function offline(row: Notice) { if (!mocked.value) await noticeApi.offline(row.id); row.status = 2 }
onMounted(load)
</script>

<style scoped>
.full {
  grid-column: 1 / -1;
}
</style>
