<script setup lang="ts">
import { computed, ref, type Component } from 'vue'
import { BookOpen, Bot, History, LayoutDashboard } from 'lucide-vue-next'
import AiChatPanel from '@/components/ai/AiChatPanel.vue'
import AiConversationPanel from '@/components/ai/AiConversationPanel.vue'
import AiKnowledgePanel from '@/components/ai/AiKnowledgePanel.vue'
import AiOverviewPanel from '@/components/ai/AiOverviewPanel.vue'
import { useAuthStore } from '@/stores/auth'

type WorkspaceTab = 'chat' | 'knowledge' | 'conversations' | 'overview'

const auth = useAuthStore()
const activeTab = ref<WorkspaceTab>('chat')

const tabs: Array<{ id: WorkspaceTab; label: string; icon: Component }> = [
  { id: 'chat', label: 'AI 助手', icon: Bot },
  { id: 'knowledge', label: '知识库', icon: BookOpen },
  { id: 'conversations', label: '会话历史', icon: History },
  { id: 'overview', label: '工作台', icon: LayoutDashboard }
]

const componentMap: Record<WorkspaceTab, Component> = {
  chat: AiChatPanel,
  knowledge: AiKnowledgePanel,
  conversations: AiConversationPanel,
  overview: AiOverviewPanel
}

const activeComponent = computed(() => componentMap[activeTab.value])
</script>

<template>
  <div class="ai-root">
    <nav class="ai-tabs">
      <button
        v-for="tab in tabs"
        :key="tab.id"
        type="button"
        :class="{ active: activeTab === tab.id }"
        @click="activeTab = tab.id"
      >
        <component :is="tab.icon" class="tab-icon" />
        <span>{{ tab.label }}</span>
      </button>
    </nav>
    <div class="ai-content">
      <KeepAlive>
        <component
          :is="activeComponent"
          :key="activeTab"
          :display-name="auth.displayName"
          @navigate="activeTab = $event"
        />
      </KeepAlive>
    </div>
  </div>
</template>

<style scoped>
.ai-root {
  height: calc(100vh - 76px - 44px);
  display: flex;
  flex-direction: column;
  margin: -22px;
}

.ai-tabs {
  display: flex;
  gap: 0;
  padding: 0 20px;
  border-bottom: 1px solid var(--border);
  background: var(--bg-subtle);
}

.ai-tabs button {
  display: inline-flex;
  align-items: center;
  gap: 7px;
  height: 46px;
  padding: 0 18px;
  border: 0;
  border-bottom: 2px solid transparent;
  background: transparent;
  color: var(--muted);
  font: inherit;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: color 0.16s, border-color 0.16s, background 0.16s;
}

.ai-tabs button:hover {
  color: var(--text);
  background: color-mix(in srgb, var(--primary) 4%, transparent);
}

.ai-tabs button.active {
  color: var(--primary);
  border-bottom-color: var(--primary);
}

.tab-icon { width: 16px; height: 16px; }

.ai-content {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}
</style>
