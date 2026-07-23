<template>
  <Teleport to="body">
    <Transition name="drawer">
      <div v-if="modelValue" class="modal-mask" @click.self="$emit('update:modelValue', false)">
        <section class="modal-panel" :style="{ maxWidth: width }" role="dialog" aria-modal="true" :aria-label="title">
          <header class="modal-head">
            <div>
              <h2>{{ title }}</h2>
              <p v-if="subtitle">{{ subtitle }}</p>
            </div>
            <button class="btn icon-btn" type="button" aria-label="关闭" title="关闭" @click="$emit('update:modelValue', false)">
              <X class="icon" />
            </button>
          </header>
          <div class="modal-body">
            <slot />
          </div>
          <footer v-if="$slots.footer" class="modal-foot">
            <slot name="footer" />
          </footer>
        </section>
      </div>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { X } from 'lucide-vue-next'

withDefaults(defineProps<{
  modelValue: boolean
  title: string
  subtitle?: string
  width?: string
}>(), {
  width: '720px'
})

defineEmits<{
  'update:modelValue': [value: boolean]
}>()
</script>

<style scoped>
.modal-mask {
  position: fixed;
  inset: 0;
  z-index: 70;
  display: flex;
  justify-content: flex-end;
  padding: 14px;
  background: var(--overlay);
  backdrop-filter: blur(10px);
}

.modal-panel {
  position: relative;
  width: min(100%, 720px);
  height: 100%;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  overflow: hidden;
  border: 1px solid var(--border-strong);
  border-radius: 8px;
  background:
    linear-gradient(145deg, color-mix(in srgb, var(--surface-elevated) 50%, transparent), transparent 70%),
    var(--surface-elevated);
  box-shadow: -24px 0 70px rgba(0, 0, 0, 0.32), inset 0 1px 0 rgba(255, 255, 255, 0.05);
  backdrop-filter: blur(16px) saturate(120%);
}

.modal-panel::before {
  position: absolute;
  top: 0;
  right: 0;
  left: 0;
  z-index: 2;
  height: 3px;
  background: linear-gradient(90deg, var(--primary), var(--violet), var(--pink));
  box-shadow: 0 0 18px color-mix(in srgb, var(--primary) 34%, transparent);
  content: "";
}
.modal-head,
.modal-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 18px;
  border-bottom: 1px solid var(--border);
}

.modal-foot {
  justify-content: flex-end;
  border-top: 1px solid var(--border);
  border-bottom: 0;
}

.modal-head h2 {
  margin: 0;
  color: var(--text);
  font-size: 17px;
}

.modal-head p {
  margin: 5px 0 0;
  color: var(--muted);
  font-size: 12px;
}

.modal-body {
  overflow: auto;
  padding: 18px;
}

.drawer-enter-active,
.drawer-leave-active {
  transition: opacity 0.22s ease;
}

.drawer-enter-active .modal-panel,
.drawer-leave-active .modal-panel {
  transition: transform 0.24s ease-out, opacity 0.2s ease;
}

.drawer-enter-from,
.drawer-leave-to {
  opacity: 0;
}

.drawer-enter-from .modal-panel {
  opacity: 0;
  transform: translateX(32px) scale(0.985);
}

.drawer-leave-to .modal-panel {
  opacity: 0;
  transform: translateY(18px) scale(0.985);
}

@media (max-width: 640px) {
  .modal-mask {
    align-items: flex-end;
    padding: 0;
  }

  .modal-panel {
    width: 100%;
    height: auto;
    max-width: none !important;
    max-height: min(88vh, 760px);
    border-right: 0;
    border-bottom: 0;
    border-left: 0;
    border-radius: 8px 8px 0 0;
  }

  .drawer-enter-from .modal-panel,
  .drawer-leave-to .modal-panel {
    transform: translateY(36px);
  }
}
</style>