<template>
  <section class="panel panel-pad chart-panel">
    <header class="chart-head">
      <div>
        <h3>{{ title }}</h3>
        <p v-if="subtitle">{{ subtitle }}</p>
      </div>
      <slot name="action" />
    </header>
    <div ref="el" class="chart-canvas" />
  </section>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = defineProps<{
  title: string
  subtitle?: string
  option: echarts.EChartsOption | Record<string, unknown>
  height?: number
}>()

const el = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null

function render() {
  if (!el.value) return
  if (!chart) chart = echarts.init(el.value)
  el.value.style.height = `${props.height ?? 280}px`
  chart.setOption(props.option as echarts.EChartsOption, true)
  chart.resize()
}

function resize() {
  chart?.resize()
}

onMounted(() => {
  nextTick(render)
  window.addEventListener('resize', resize)
})

watch(() => props.option, () => nextTick(render), { deep: true })

onBeforeUnmount(() => {
  window.removeEventListener('resize', resize)
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.chart-panel {
  min-height: 350px;
}

.chart-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}

.chart-head h3 {
  margin: 0;
  font-size: 16px;
}

.chart-head p {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.chart-canvas {
  width: 100%;
}
</style>
