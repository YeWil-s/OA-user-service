<template>
  <section class="page ai-page">
    <div class="page-head">
      <div>
        <h2 class="page-title">AI 助手</h2>
        <p class="page-subtitle">智能填单、数据分析、知识问答</p>
      </div>
    </div>

    <p class="error-banner">当前接口文档未提供 AI 助手接口，暂未接入真实服务。</p>

    <section class="panel chat-panel">
      <div class="chat-messages">
        <div class="chat-row assistant">
          <p>请在接口文档补充 AI 助手服务路径后接入真实 API。</p>
        </div>
      </div>
      <form class="chat-input" @submit.prevent="send">
        <input v-model="input" class="field" placeholder="AI 接口未配置，暂不可发送" />
        <button class="btn primary" disabled><Send class="icon" />发送</button>
      </form>
    </section>
  </section>
</template>

<script setup lang="ts">
import { Send } from 'lucide-vue-next'
import { ref } from 'vue'

const input = ref('')
function send() {}
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