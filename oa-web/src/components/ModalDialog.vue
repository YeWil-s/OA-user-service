<template>
  <Teleport to="body">
    <div v-if="modelValue" class="modal-mask" @click.self="$emit('update:modelValue', false)">
      <section class="modal-panel" :style="{ maxWidth: width }">
        <header class="modal-head">
          <div>
            <h2>{{ title }}</h2>
            <p v-if="subtitle">{{ subtitle }}</p>
          </div>
          <button class="btn icon-btn" type="button" aria-label="关闭" @click="$emit('update:modelValue', false)">
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
  z-index: 50;
  display: grid;
  place-items: center;
  padding: 24px;
  background: rgba(14, 22, 35, 0.42);
}

.modal-panel {
  width: min(100%, var(--modal-width, 720px));
  max-height: min(86vh, 820px);
  display: grid;
  grid-template-rows: auto minmax(0, 1fr) auto;
  overflow: hidden;
  background: #fff;
  border-radius: 8px;
  border: 1px solid var(--border);
  box-shadow: 0 28px 80px rgba(9, 16, 28, 0.22);
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
  font-size: 18px;
}

.modal-head p {
  margin: 5px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.modal-body {
  overflow: auto;
  padding: 18px;
}
</style>
