<script setup lang="ts">
import { ArrowRight, BookOpen, Bot, FileCheck2, History, Sparkles } from 'lucide-vue-next'

defineProps<{ displayName: string }>()
const emit = defineEmits<{ navigate: [tab: 'chat' | 'knowledge' | 'conversations'] }>()
</script>

<template>
  <div class="overview-root">
    <div class="hero">
      <span class="badge"><Sparkles /> AI 智能工作台</span>
      <h1>{{ displayName }}，下午好</h1>
      <p>检索企业知识、咨询制度流程，或让 AI 帮你一键填单。</p>
      <button class="btn primary" @click="emit('navigate', 'chat')">
        <Bot />开始对话 <ArrowRight />
      </button>
    </div>
    <div class="cards">
      <button class="card" @click="emit('navigate', 'chat')">
        <span class="card-icon c1"><Bot /></span>
        <strong>智能问答</strong>
        <small>基于知识库的流式回答</small>
      </button>
      <button class="card" @click="emit('navigate', 'chat')">
        <span class="card-icon c2"><FileCheck2 /></span>
        <strong>智能填单</strong>
        <small>自然语言提取业务字段</small>
      </button>
      <button class="card" @click="emit('navigate', 'knowledge')">
        <span class="card-icon c3"><BookOpen /></span>
        <strong>知识库</strong>
        <small>管理企业文档与制度</small>
      </button>
      <button class="card" @click="emit('navigate', 'conversations')">
        <span class="card-icon c4"><History /></span>
        <strong>会话历史</strong>
        <small>追溯历史对话上下文</small>
      </button>
    </div>
  </div>
</template>

<style scoped>
.overview-root {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 40px;
  padding: 40px 20px;
  overflow-y: auto;
}

.hero {
  text-align: center;
  max-width: 540px;
}

.badge {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 5px 14px;
  border: 1px solid var(--border);
  border-radius: 20px;
  background: var(--surface);
  color: var(--primary-soft);
  font-size: 11px; font-weight: 650;
}
.badge svg { width: 13px; }

.hero h1 {
  margin: 16px 0 8px;
  font-size: 28px;
  font-weight: 720;
  color: var(--text);
}

.hero p {
  margin: 0 0 20px;
  color: var(--muted);
  font-size: 14px;
  line-height: 1.6;
}

.cards {
  display: grid;
  grid-template-columns: repeat(4, 200px);
  gap: 14px;
}

.card {
  display: grid;
  justify-items: center;
  gap: 8px;
  padding: 24px 16px;
  border: 1px solid var(--border);
  border-radius: 12px;
  background: var(--surface);
  cursor: pointer;
  text-align: center;
  color: inherit;
  transition: border-color .18s, box-shadow .18s, transform .18s;
}
.card:hover {
  border-color: var(--border-strong);
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

.card-icon {
  width: 48px; height: 48px;
  display: grid; place-items: center;
  border-radius: 12px;
  margin-bottom: 4px;
}
.card-icon svg { width: 22px; height: 22px; }
.c1 { background: color-mix(in srgb, var(--primary) 10%, transparent); color: var(--primary-soft); }
.c2 { background: color-mix(in srgb, var(--accent) 10%, transparent); color: var(--accent); }
.c3 { background: color-mix(in srgb, var(--cyan) 10%, transparent); color: var(--cyan); }
.c4 { background: color-mix(in srgb, var(--violet) 10%, transparent); color: var(--violet); }

.card strong { font-size: 14px; color: var(--text); }
.card small { font-size: 11px; color: var(--muted); line-height: 1.4; }

@media (max-width: 900px) {
  .cards { grid-template-columns: repeat(2, 1fr); }
  .hero h1 { font-size: 22px; }
}
@media (max-width: 500px) {
  .cards { grid-template-columns: 1fr; }
}
</style>
