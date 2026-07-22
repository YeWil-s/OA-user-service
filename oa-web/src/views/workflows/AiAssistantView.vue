<template>
  <section class="page ai-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">AI 助手</h2>
        <p class="page-subtitle">智能填单、数据分析、知识问答</p>
      </div>
      <span class="mock-banner">AI 后端未完成</span>
    </div>

    <section class="panel chat-panel">
      <div class="chat-messages">
        <div v-for="item in messages" :key="item.id" class="chat-row" :class="item.role">
          <p>{{ item.text }}</p>
        </div>
      </div>
      <form class="chat-input" @submit.prevent="send">
        <input v-model="input" class="field" placeholder="输入问题或办公需求" />
        <button class="btn primary"><Send class="icon" />发送</button>
      </form>
    </section>
  </section>
</template>

<script setup lang="ts">
import { Send } from 'lucide-vue-next'
import { ref } from 'vue'

const input = ref('')
const messages = ref([
  { id: 1, role: 'assistant', text: '可以帮你生成请假申请、分析考勤异常、查询 OA 使用规则。' }
])

function send() {
  if (!input.value.trim()) return
  messages.value.push({ id: Date.now(), role: 'user', text: input.value })
  messages.value.push({ id: Date.now() + 1, role: 'assistant', text: '已收到，后端接入后这里会返回流式 AI 响应。' })
  input.value = ''
}
</script>

<style scoped>
.chat-panel {
  min-height: calc(100vh - 180px);
  display: grid;
  grid-template-rows: minmax(0, 1fr) auto;
  overflow: hidden;
}

.chat-messages {
  display: grid;
  align-content: start;
  gap: 12px;
  padding: 18px;
  overflow: auto;
}

.chat-row {
  max-width: min(680px, 86%);
  padding: 12px 14px;
  border-radius: 8px;
  background: var(--surface-soft);
  border: 1px solid var(--border);
}

.chat-row.user {
  justify-self: end;
  background: #eaf5f7;
  border-color: #cce5eb;
}

.chat-row p {
  margin: 0;
  line-height: 1.65;
}

.chat-input {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto;
  gap: 10px;
  padding: 14px;
  border-top: 1px solid var(--border);
}
</style>
