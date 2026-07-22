<template>
  <span class="sparkline" aria-hidden="true">
    <i v-for="(height, index) in heights" :key="index" :style="{ height: `${height}%` }" />
  </span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{ values: number[] }>()

const heights = computed(() => {
  const values = props.values.length ? props.values : [0]
  const min = Math.min(...values)
  const max = Math.max(...values)
  const range = max - min || 1
  return values.map((value) => 28 + ((value - min) / range) * 72)
})
</script>

<style scoped>
.sparkline {
  width: 78px;
  height: 36px;
  display: flex;
  align-items: flex-end;
  justify-content: flex-end;
  gap: 3px;
}

.sparkline i {
  width: 5px;
  min-height: 5px;
  border-radius: 3px 3px 1px 1px;
  background: linear-gradient(180deg, var(--primary-soft), var(--primary));
  box-shadow: 0 0 10px var(--primary-glow);
  transition: height 0.45s ease-out;
}
.sparkline i:nth-child(3n + 2) {
  background: linear-gradient(180deg, color-mix(in srgb, var(--violet) 72%, #fff), var(--violet));
  box-shadow: 0 0 10px color-mix(in srgb, var(--violet) 32%, transparent);
}

.sparkline i:nth-child(3n) {
  background: linear-gradient(180deg, color-mix(in srgb, var(--cyan) 72%, #fff), var(--cyan));
  box-shadow: 0 0 10px color-mix(in srgb, var(--cyan) 28%, transparent);
}
</style>