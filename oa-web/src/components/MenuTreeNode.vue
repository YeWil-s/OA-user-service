<template>
  <div class="tree-branch">
    <div class="tree-row" :class="{ 'has-children': hasChildren, expanded: isExpanded }">
      <!-- tree lines -->
      <span
        v-for="lvl in level"
        :key="'indent-' + lvl"
        class="tree-indent"
        :class="{ 'line-through': lvl < level || (lvl === level && ancestorIsNotLast(lvl - 1)) }"
      />

      <!-- connector: └── or ├── -->
      <span class="tree-connector">
        <span class="connector-h" />
        <span v-if="index < siblingCount - 1" class="connector-v-tail" />
      </span>

      <!-- expand toggle -->
      <button
        v-if="hasChildren"
        type="button"
        class="tree-toggle"
        :aria-label="isExpanded ? '折叠' : '展开'"
        @click="emit('toggleExpand', node.id)"
      >
        <ChevronRight class="toggle-icon" :class="{ rotated: isExpanded }" />
      </button>
      <span v-else class="tree-toggle-placeholder" />

      <!-- checkbox -->
      <label v-if="checkable" class="tree-check" @click.stop>
        <input
          type="checkbox"
          :checked="checkedIds?.includes(node.id)"
          @change="emit('toggleCheck', node.id)"
        />
      </label>

      <!-- content slot -->
      <slot :node="node" />
    </div>

    <!-- children -->
    <Transition name="tree-children">
      <div v-if="hasChildren && isExpanded" class="tree-children">
        <MenuTreeNode
          v-for="(child, childIndex) in node.children"
          :key="child.id"
          :node="child"
          :level="level + 1"
          :index="childIndex"
          :sibling-count="node.children!.length"
          :ancestors-last="ancestorLastFlags"
          :is-last-sibling="index === siblingCount - 1"
          :checkable="checkable"
          :checked-ids="checkedIds"
          :expanded-ids="expandedIds"
          @toggle-expand="emit('toggleExpand', $event)"
          @toggle-check="emit('toggleCheck', $event)"
        >
          <template #default="{ node: slotNode }">
            <slot :node="slotNode" />
          </template>
        </MenuTreeNode>
      </div>
    </Transition>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ChevronRight } from 'lucide-vue-next'
import type { MenuNode } from '@/api/types'

const props = withDefaults(defineProps<{
  node: MenuNode
  level: number
  index: number
  siblingCount: number
  ancestorsLast?: boolean[]
  isLastSibling?: boolean
  checkable?: boolean
  checkedIds?: number[]
  expandedIds?: number[]
}>(), {
  ancestorsLast: () => [],
  isLastSibling: false
})

const emit = defineEmits<{
  toggleExpand: [id: number]
  toggleCheck: [id: number]
}>()

const hasChildren = computed(() => !!(props.node.children && props.node.children.length > 0))
const isExpanded = computed(() => props.expandedIds?.includes(props.node.id) ?? true)

const ancestorLastFlags = computed(() => [...props.ancestorsLast, props.index === props.siblingCount - 1])

function ancestorIsNotLast(ancestorIndex: number): boolean {
  if (ancestorIndex < 0) return false
  return !props.ancestorsLast[ancestorIndex]
}
</script>

<style scoped>
.tree-branch {
  min-width: max-content;
}

.tree-row {
  position: relative;
  display: flex;
  align-items: center;
  min-height: 44px;
  border-radius: 6px;
  transition: background-color 0.15s ease;
}
.tree-row:hover {
  background: color-mix(in srgb, var(--primary) 6%, transparent);
}

/* indent spacers */
.tree-indent {
  width: 28px;
  flex: 0 0 28px;
  position: relative;
}
.tree-indent.line-through::after {
  position: absolute;
  top: 0;
  bottom: 50%;
  left: 50%;
  width: 1px;
  background: var(--border-strong);
  content: "";
}
.tree-row:last-child .tree-indent.line-through::after {
  bottom: 50%;
}

/* connector */
.tree-connector {
  width: 22px;
  flex: 0 0 22px;
  position: relative;
  height: 44px;
}
.connector-h {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 50%;
  height: 1px;
  background: var(--border-strong);
}
.connector-v-tail {
  position: absolute;
  top: 50%;
  left: 50%;
  width: 1px;
  height: 50%;
  background: var(--border-strong);
}

.tree-toggle {
  width: 24px;
  height: 24px;
  flex: 0 0 24px;
  display: grid;
  place-items: center;
  border: 1px solid var(--border);
  border-radius: 4px;
  background: var(--surface-soft);
  color: var(--muted);
  cursor: pointer;
  transition: background-color 0.15s ease, color 0.15s ease;
}
.tree-toggle:hover {
  background: var(--surface);
  color: var(--primary-soft);
}
.toggle-icon {
  width: 14px;
  height: 14px;
  transition: transform 0.2s ease;
}
.toggle-icon.rotated {
  transform: rotate(90deg);
}

.tree-toggle-placeholder {
  width: 24px;
  flex: 0 0 24px;
}

.tree-check {
  width: 22px;
  flex: 0 0 22px;
  display: grid;
  place-items: center;
  cursor: pointer;
}
.tree-check input {
  width: 15px;
  height: 15px;
  accent-color: var(--primary);
  cursor: pointer;
}

.tree-children {
  overflow: hidden;
}

.tree-children-enter-active,
.tree-children-leave-active {
  transition: opacity 0.2s ease, max-height 0.25s ease;
}
.tree-children-enter-from,
.tree-children-leave-to {
  opacity: 0;
  max-height: 0;
}
.tree-children-enter-to,
.tree-children-leave-from {
  max-height: 2000px;
}
</style>
