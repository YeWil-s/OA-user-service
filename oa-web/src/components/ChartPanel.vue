<template>
  <section class="panel panel-pad chart-panel">
    <header class="chart-head">
      <div>
        <h3>{{ title }}</h3>
        <p v-if="subtitle">{{ subtitle }}</p>
      </div>
      <slot name="action" />
    </header>
    <div v-if="loading" class="chart-skeleton" aria-label="图表加载中">
      <span v-for="heightValue in [38, 64, 48, 78, 58, 88, 70, 52]" :key="heightValue" :style="{ height: `${heightValue}%` }" />
    </div>
    <div v-show="!loading" ref="el" class="chart-canvas" />
  </section>
</template>

<script setup lang="ts">
import * as echarts from 'echarts'
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'

const props = withDefaults(defineProps<{
  title: string
  subtitle?: string
  option: echarts.EChartsOption | Record<string, unknown>
  height?: number
  loading?: boolean
}>(), {
  loading: false
})

const el = ref<HTMLDivElement>()
let chart: echarts.ECharts | null = null
let observer: ResizeObserver | null = null
let themeObserver: MutationObserver | null = null

function cssVar(name: string) {
  return getComputedStyle(document.documentElement).getPropertyValue(name).trim()
}

function themeDefaults(): echarts.EChartsOption {
  const raw = props.option as Record<string, unknown>
  return {
    backgroundColor: 'transparent',
    animationDuration: 520,
    animationEasing: 'cubicOut',
    textStyle: {
      color: cssVar('--muted'),
      fontFamily: 'Inter, "PingFang SC", "Microsoft YaHei", sans-serif'
    },
    tooltip: {
      backgroundColor: cssVar('--surface-elevated'),
      borderColor: cssVar('--border-strong'),
      borderWidth: 1,
      padding: [10, 12],
      textStyle: { color: cssVar('--text'), fontSize: 12 },
      extraCssText: 'border-radius:7px;box-shadow:0 16px 38px rgba(0,0,0,.24);backdrop-filter:blur(12px)'
    },
    ...(raw.xAxis ? {
      dataZoom: [{
        type: 'inside',
        zoomOnMouseWheel: 'ctrl',
        moveOnMouseMove: true,
        moveOnMouseWheel: false
      }]
    } : {})
  }
}

function render() {
  if (!el.value || props.loading) return
  if (!chart) chart = echarts.init(el.value)
  el.value.style.height = `${props.height ?? 280}px`
  chart.setOption(themeDefaults(), true)
  chart.setOption(props.option as echarts.EChartsOption)
  chart.resize()
}

function resetTheme() {
  chart?.dispose()
  chart = null
  nextTick(render)
}

onMounted(() => {
  nextTick(render)
  if (el.value) {
    observer = new ResizeObserver(() => chart?.resize())
    observer.observe(el.value)
  }
  themeObserver = new MutationObserver(resetTheme)
  themeObserver.observe(document.documentElement, { attributes: true, attributeFilter: ['data-theme'] })
})

watch(() => props.option, () => nextTick(render), { deep: true })
watch(() => props.loading, (loading) => {
  if (!loading) nextTick(render)
})

onBeforeUnmount(() => {
  observer?.disconnect()
  themeObserver?.disconnect()
  chart?.dispose()
  chart = null
})
</script>

<style scoped>
.chart-panel {
  min-height: 350px;
}

.chart-head {
  min-height: 34px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 12px;
}

.chart-head h3 {
  margin: 0;
  color: var(--text);
  font-size: 15px;
}

.chart-head p {
  margin: 5px 0 0;
  color: var(--muted);
  font-size: 12px;
}

.chart-canvas {
  width: 100%;
}

.chart-skeleton {
  height: 280px;
  display: flex;
  align-items: flex-end;
  gap: 12px;
  padding: 28px 22px 18px 34px;
  border-bottom: 1px solid var(--border);
}

.chart-skeleton span {
  position: relative;
  flex: 1;
  overflow: hidden;
  border-radius: 5px 5px 1px 1px;
  background: color-mix(in srgb, var(--primary) 18%, transparent);
}

.chart-skeleton span::after {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, color-mix(in srgb, #fff 10%, transparent), transparent);
  transform: translateX(-100%);
  content: "";
  animation: shimmer 1.35s infinite;
}

@keyframes shimmer {
  to { transform: translateX(100%); }
}
</style>