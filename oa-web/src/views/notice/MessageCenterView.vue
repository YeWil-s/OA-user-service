<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">消息中心</h2>
        <p class="page-subtitle">审批通知、考勤通知、系统通知</p>
      </div>
      <div class="toolbar">
        <button class="btn" :disabled="loading" @click="load"><RefreshCw class="icon" />{{ loading ? '刷新中' : '刷新' }}</button>
        <button class="btn primary" @click="openCreate"><Send class="icon" />发送</button>
      </div>
    </div>

    <p v-if="error" class="error-banner">{{ error }}</p>
    <p v-if="message" class="success-banner">{{ message }}</p>

    <section class="message-list">
      <article v-for="row in rows" :key="row.id" class="panel message-item" :class="{ unread: !isRead(row) }" @click="markRead(row)">
        <div class="message-icon"><Bell class="icon" /></div>
        <div>
          <h3>{{ row.title }}</h3>
          <p>{{ row.content }}</p>
          <span>{{ messageType(row.msgType) }} · {{ row.createTime || '-' }}</span>
        </div>
        <StatusPill :value="isRead(row) ? 1 : 0" :map="{ '0': '未读', '1': '已读' }" :tone-map="{ '0': 'warn', '1': 'success' }" />
      </article>
      <div v-if="!loading && rows.length === 0" class="panel empty">暂无消息数据</div>
    </section>

    <ModalDialog v-model="dialogOpen" title="发送站内消息">
      <div class="form-grid">
        <label class="form-item"><span class="form-label">接收人 ID</span><input v-model.number="form.userId" class="field" type="number" /></label>
        <label class="form-item"><span class="form-label">类型</span><select v-model.number="form.msgType" class="select"><option :value="1">审批通知</option><option :value="2">考勤通知</option><option :value="3">系统通知</option></select></label>
        <label class="form-item full"><span class="form-label">标题</span><input v-model="form.title" class="field" /></label>
        <label class="form-item full"><span class="form-label">内容</span><textarea v-model="form.content" class="textarea" /></label>
      </div>
      <template #footer><button class="btn" @click="dialogOpen = false">取消</button><button class="btn primary" :disabled="saving" @click="send">{{ saving ? '发送中' : '发送' }}</button></template>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { Bell, RefreshCw, Send } from 'lucide-vue-next'
import { onMounted, reactive, ref } from 'vue'
import ModalDialog from '@/components/ModalDialog.vue'
import StatusPill from '@/components/StatusPill.vue'
import { noticeApi } from '@/api/services'
import type { Message } from '@/api/types'

const rows = ref<Message[]>([])
const loading = ref(false)
const saving = ref(false)
const error = ref('')
const message = ref('')
const dialogOpen = ref(false)
const form = reactive<Partial<Message>>({})
const messageType = (value?: number) => ({ 1: '审批通知', 2: '考勤通知', 3: '系统通知' }[value ?? 3])
const pageRows = <T,>(page: { records?: T[]; list?: T[] }) => page.records || page.list || []
const isRead = (row: Message) => row.read === true || row.isRead === 1

async function load() {
  loading.value = true
  error.value = ''
  try {
    const result = await noticeApi.messages({ current: 1, size: 20 })
    rows.value = pageRows(result)
  } catch (err) {
    error.value = err instanceof Error ? err.message : '消息数据加载失败'
  } finally {
    loading.value = false
  }
}

function openCreate() {
  Object.assign(form, { userId: undefined, msgType: 3, title: '', content: '' })
  dialogOpen.value = true
}

async function send() {
  saving.value = true
  error.value = ''
  message.value = ''
  try {
    await noticeApi.createMessage(form)
    message.value = '站内消息已发送'
    dialogOpen.value = false
    await load()
  } catch (err) {
    error.value = err instanceof Error ? err.message : '消息发送失败'
  } finally {
    saving.value = false
  }
}

async function markRead(row: Message) {
  if (isRead(row)) return
  error.value = ''
  try {
    await noticeApi.markRead(row.id)
    row.read = true
    row.isRead = 1
  } catch (err) {
    error.value = err instanceof Error ? err.message : '标记已读失败'
  }
}

onMounted(load)
</script>

<style scoped>
.message-list {
  display: grid;
  gap: 12px;
}

.message-item {
  min-height: 86px;
  display: grid;
  grid-template-columns: 42px minmax(0, 1fr) auto;
  align-items: center;
  gap: 14px;
  padding: 14px;
  cursor: pointer;
}

.message-item.unread {
  border-color: #f0d6a4;
}

.message-icon {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: #eef4f7;
  color: var(--primary);
}

.message-item h3 {
  margin: 0;
  font-size: 15px;
}

.message-item p {
  margin: 5px 0;
  color: var(--text);
}

.message-item span {
  color: var(--muted);
  font-size: 12px;
}

.full {
  grid-column: 1 / -1;
}
</style>