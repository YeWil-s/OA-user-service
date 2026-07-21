<template>
  <span class="pill" :class="tone">{{ label }}</span>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  value?: number | string | boolean
  map?: Record<string, string>
  toneMap?: Record<string, string>
}>()

const label = computed(() => {
  if (props.map && props.value !== undefined) {
    return props.map[String(props.value)] ?? String(props.value)
  }
  if (props.value === true || props.value === 1 || props.value === '1') return '启用'
  if (props.value === false || props.value === 0 || props.value === '0') return '停用'
  return String(props.value ?? '-')
})

const tone = computed(() => {
  const text = String(props.value ?? '')
  if (props.toneMap?.[text]) return props.toneMap[text]
  return ['1', 'true', '启用', '已通过', '正常', '可领用'].includes(text)
    ? 'success'
    : ['3', '4', 'false', '停用', '已驳回', '异常'].includes(text)
      ? 'danger'
      : ['0', '2', '审批中', '草稿', '领用中'].includes(text)
        ? 'warn'
        : ''
})
</script>
