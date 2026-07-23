<template>
  <div class="tree-root">
    <MenuTreeNode
      v-for="(node, index) in nodes"
      :key="node.id"
      :node="node"
      :level="0"
      :index="index"
      :sibling-count="nodes.length"
      :checkable="checkable"
      :checked-ids="checkedIds"
      :expanded-ids="expandedIds"
      @toggle-expand="emit('toggleExpand', $event)"
      @toggle-check="emit('toggleCheck', $event)"
    >
      <template #default="{ node: slotNode }">
        <slot name="node" :node="slotNode" />
      </template>
    </MenuTreeNode>
    <div v-if="!nodes.length" class="tree-empty">暂无数据</div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import type { MenuNode } from '@/api/types'
import MenuTreeNode from './MenuTreeNode.vue'

defineProps<{
  nodes: MenuNode[]
  checkable?: boolean
  checkedIds?: number[]
  expandedIds?: number[]
}>()

const emit = defineEmits<{
  toggleExpand: [id: number]
  toggleCheck: [id: number]
}>()
</script>

<style scoped>
.tree-root {
  min-height: 60px;
}
.tree-empty {
  padding: 40px 0;
  text-align: center;
  color: var(--muted);
  font-size: 13px;
}
</style>
