<template>
  <button
    class="btn icon-btn theme-toggle"
    type="button"
    :aria-label="theme === 'dark' ? '切换到浅色主题' : '切换到深色主题'"
    :title="theme === 'dark' ? '切换到浅色主题' : '切换到深色主题'"
    @click="toggle"
  >
    <Sun v-if="theme === 'dark'" class="icon" />
    <Moon v-else class="icon" />
  </button>
</template>

<script setup lang="ts">
import { Moon, Sun } from 'lucide-vue-next'
import { ref } from 'vue'

type Theme = 'dark' | 'light'

const stored = document.documentElement.dataset.theme
const theme = ref<Theme>(stored === 'light' ? 'light' : 'dark')

function toggle() {
  theme.value = theme.value === 'dark' ? 'light' : 'dark'
  document.documentElement.dataset.theme = theme.value
  document.documentElement.style.colorScheme = theme.value
  document.querySelector('meta[name="theme-color"]')?.setAttribute('content', theme.value === 'light' ? '#f0f4fa' : '#060f1f')
  localStorage.setItem('oa_theme', theme.value)
}
</script>