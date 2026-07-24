<script setup lang="ts">
import { ref, markRaw, type Component } from 'vue'
import { Bot, BookOpen, History, LayoutDashboard, Menu, X } from 'lucide-vue-next'
import AiChatPanel from '@/components/ai/AiChatPanel.vue'
import AiKnowledgePanel from '@/components/ai/AiKnowledgePanel.vue'
import AiConversationPanel from '@/components/ai/AiConversationPanel.vue'
import AiOverviewPanel from '@/components/ai/AiOverviewPanel.vue'

type Panel = {
  key: string
  label: string
  icon: Component
  component: Component
}

const panels: Panel[] = [
  { key: 'chat', label: 'AI 对话', icon: Bot, component: markRaw(AiChatPanel) },
  { key: 'knowledge', label: '知识库', icon: BookOpen, component: markRaw(AiKnowledgePanel) },
  { key: 'conversations', label: '会话历史', icon: History, component: markRaw(AiConversationPanel) },
  { key: 'overview', label: '工作台', icon: LayoutDashboard, component: markRaw(AiOverviewPanel) },
]

const active = ref('chat')
const sidebarOpen = ref(true)
</script>

<template>
  <div class="ai-layout">
    <aside :class="['sidebar', { collapsed: !sidebarOpen }]">
      <div class="sidebar-head">
        <span v-if="sidebarOpen" class="logo-text">OA AI</span>
        <button class="toggle-btn" @click="sidebarOpen = !sidebarOpen">
          <X v-if="sidebarOpen" />
          <Menu v-else />
        </button>
      </div>
      <nav class="sidebar-nav">
        <button
          v-for="p in panels"
          :key="p.key"
          :class="['nav-item', { active: active === p.key }]"
          @click="active = p.key"
        >
          <component :is="p.icon" class="nav-icon" />
          <span v-if="sidebarOpen" class="nav-label">{{ p.label }}</span>
        </button>
      </nav>
    </aside>

    <main class="main-content">
      <component :is="panels.find(p => p.key === active)!.component" :key="active" />
    </main>
  </div>
</template>

<style scoped>
.ai-layout {
  height: calc(100vh - 76px - 44px);
  display: flex;
  background: var(--bg);
  margin: -22px;
}

.sidebar {
  width: 200px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--border);
  background: var(--bg-subtle);
  transition: width .2s ease;
}
.sidebar.collapsed { width: 56px; }

.sidebar-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 12px;
}
.logo-text {
  font-size: 15px;
  font-weight: 700;
  background: var(--brand-gradient);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  white-space: nowrap;
  overflow: hidden;
}
.toggle-btn {
  width: 32px; height: 32px;
  display: grid; place-items: center;
  border: 0; background: transparent;
  color: var(--muted); cursor: pointer;
  border-radius: 6px;
  flex-shrink: 0;
}
.toggle-btn:hover { background: var(--surface); color: var(--text); }
.toggle-btn svg { width: 16px; height: 16px; }

.sidebar-nav {
  flex: 1;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 2px;
}
.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 12px;
  border: 0;
  border-radius: 8px;
  background: transparent;
  color: var(--muted);
  font-size: 13px;
  cursor: pointer;
  transition: background .14s, color .14s;
  text-align: left;
  white-space: nowrap;
  overflow: hidden;
}
.nav-item:hover { background: var(--surface); color: var(--text); }
.nav-item.active {
  background: color-mix(in srgb, var(--primary) 10%, transparent);
  color: var(--primary);
}
.nav-icon { width: 18px; height: 18px; flex-shrink: 0; }
.nav-label { overflow: hidden; text-overflow: ellipsis; }

.collapsed .nav-item { justify-content: center; padding: 10px; }
.collapsed .logo-text { display: none; }
.collapsed .sidebar-head { justify-content: center; }

.main-content {
  flex: 1;
  min-width: 0;
  overflow: hidden;
}
</style>
