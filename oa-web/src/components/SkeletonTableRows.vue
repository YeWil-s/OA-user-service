<template>
  <tr v-for="row in rows" :key="`skeleton-${row}`" class="skeleton-row" aria-hidden="true">
    <td v-for="column in columns" :key="column">
      <span class="skeleton skeleton-line" :style="{ width: widths[(row + column) % widths.length] }" />
    </td>
  </tr>
</template>

<script setup lang="ts">
withDefaults(defineProps<{
  columns: number
  rows?: number
}>(), {
  rows: 6
})

const widths = ['54%', '68%', '82%', '61%', '74%']
</script>

<style scoped>
.skeleton-row {
  animation: none !important;
}

.skeleton-line {
  display: block;
}
</style>