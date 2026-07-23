<script setup lang="ts">
type Source = {
  docId?: string | number
  title?: string
  snippet?: string
  content?: string
  score?: number | string
}

withDefaults(defineProps<{ sources?: Source[]; loading?: boolean }>(), {
  sources: () => [],
  loading: false
})
</script>

<template>
  <div class="source-list">
    <template v-if="loading">
      <article v-for="n in 3" :key="n" class="source-item source-skeleton">
        <span class="skeleton wide" />
        <span class="skeleton medium" />
        <span class="skeleton short" />
      </article>
    </template>
    <div v-else-if="!sources.length" class="empty-source">
      <span class="empty-orbit" />
      <p>本轮回答暂无引用来源</p>
    </div>
    <article v-for="source in sources" :key="source.docId || source.title" class="source-item">
      <div class="source-heading">
        <span class="source-mark" />
        <strong>{{ source.title || '未命名文档' }}</strong>
      </div>
      <p>{{ source.snippet || source.content || '暂无摘要' }}</p>
      <span v-if="source.score" class="score">匹配度 {{ Number(source.score).toFixed(3) }}</span>
    </article>
  </div>
</template>

<style scoped>
.source-list {
  display: grid;
  gap: 10px;
}

.source-item {
  position: relative;
  display: grid;
  gap: 8px;
  padding: 12px;
  overflow: hidden;
  border: 1px solid var(--border);
  border-radius: 7px;
  background: var(--surface-soft);
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.source-item:hover {
  border-color: var(--border-strong);
  box-shadow: var(--shadow);
  transform: translateY(-2px);
}

.source-heading {
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.source-heading strong {
  overflow: hidden;
  color: var(--text);
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.source-mark {
  width: 7px;
  height: 7px;
  flex: 0 0 7px;
  border-radius: 2px;
  background: var(--primary-soft);
  box-shadow: 0 0 8px var(--primary-glow);
}

.source-item p {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: var(--muted);
  font-size: 11px;
  line-height: 1.6;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.score {
  width: max-content;
  padding: 3px 7px;
  border-radius: 4px;
  background: color-mix(in srgb, var(--primary) 8%, transparent);
  color: var(--primary-soft);
  font-size: 9px;
  font-variant-numeric: tabular-nums;
}

.empty-source {
  min-height: 120px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 10px;
  color: var(--faint);
  text-align: center;
}

.empty-source p { margin: 0; font-size: 11px; }

.empty-orbit {
  width: 28px;
  height: 28px;
  border: 1px solid color-mix(in srgb, var(--primary-soft) 22%, transparent);
  border-radius: 50%;
  box-shadow: 0 0 0 6px color-mix(in srgb, var(--primary) 4%, transparent), 0 0 16px var(--primary-glow);
}

.skeleton {
  position: relative;
  height: 9px;
  overflow: hidden;
  border-radius: 999px;
  background: color-mix(in srgb, var(--muted) 12%, transparent);
}

.skeleton::after {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, color-mix(in srgb, var(--primary) 22%, transparent), transparent);
  content: "";
  transform: translateX(-100%);
  animation: source-shimmer 1.35s infinite;
}

.wide { width: 90%; }
.medium { width: 67%; }
.short { width: 42%; }
.source-skeleton { min-height: 88px; }

@keyframes source-shimmer { to { transform: translateX(100%); } }

@media (prefers-reduced-motion: reduce) {
  .source-item, .skeleton::after { animation: none; transition: none; }
}
</style>
