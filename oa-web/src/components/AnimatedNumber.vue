<template>
  <span class="animated-number">{{ display }}</span>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'

const props = withDefaults(defineProps<{
  value: number | string
  duration?: number
}>(), {
  duration: 520
})

const current = ref(0)
let frame = 0

const parsed = computed(() => {
  const raw = String(props.value)
  const match = raw.match(/-?\d+(?:\.\d+)?/)
  if (!match) return { number: 0, prefix: raw, suffix: '', decimals: 0, numeric: false }
  const decimals = match[0].split('.')[1]?.length || 0
  return {
    number: Number(match[0]),
    prefix: raw.slice(0, match.index),
    suffix: raw.slice((match.index || 0) + match[0].length),
    decimals,
    numeric: true
  }
})

const display = computed(() => {
  if (!parsed.value.numeric) return String(props.value)
  const formatted = new Intl.NumberFormat('zh-CN', {
    minimumFractionDigits: parsed.value.decimals,
    maximumFractionDigits: parsed.value.decimals
  }).format(current.value)
  return `${parsed.value.prefix}${formatted}${parsed.value.suffix}`
})

watch(() => props.value, () => {
  cancelAnimationFrame(frame)
  const from = current.value
  const to = parsed.value.number
  const started = performance.now()

  function tick(now: number) {
    const progress = Math.min((now - started) / props.duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3)
    current.value = from + (to - from) * eased
    if (progress < 1) frame = requestAnimationFrame(tick)
  }

  frame = requestAnimationFrame(tick)
}, { immediate: true })

onBeforeUnmount(() => cancelAnimationFrame(frame))
</script>