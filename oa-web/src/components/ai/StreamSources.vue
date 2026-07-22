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
      <article v-for="index in 3" :key="index" class="source-item source-skeleton">
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
  border: 1px solid rgba(148, 163, 184, 0.13);
  border-radius: 7px;
  background: linear-gradient(145deg, rgba(30, 41, 59, 0.58), rgba(15, 23, 42, 0.38));
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.035);
  transition: transform 0.18s ease, border-color 0.18s ease, box-shadow 0.18s ease;
}

.source-item:hover {
  border-color: rgba(34, 211, 238, 0.24);
  box-shadow: 0 10px 24px rgba(2, 6, 23, 0.22), inset 0 1px 0 rgba(255, 255, 255, 0.055);
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
  color: #e7eefb;
  font-size: 12px;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.source-mark {
  width: 7px;
  height: 7px;
  flex: 0 0 7px;
  border-radius: 2px;
  background: #22d3ee;
  box-shadow: 0 0 10px rgba(34, 211, 238, 0.7);
}

.source-item p {
  display: -webkit-box;
  margin: 0;
  overflow: hidden;
  color: #8494aa;
  font-size: 11px;
  line-height: 1.6;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 3;
}

.score {
  width: max-content;
  padding: 3px 7px;
  border-radius: 4px;
  background: rgba(34, 211, 238, 0.08);
  color: #67e8f9;
  font-size: 9px;
  font-variant-numeric: tabular-nums;
}

.empty-source {
  min-height: 148px;
  display: grid;
  place-items: center;
  align-content: center;
  gap: 12px;
  color: #64748b;
  text-align: center;
}

.empty-source p {
  margin: 0;
  font-size: 11px;
}

.empty-orbit {
  width: 30px;
  height: 30px;
  border: 1px solid rgba(34, 211, 238, 0.25);
  border-radius: 50%;
  box-shadow: 0 0 0 7px rgba(59, 130, 246, 0.04), 0 0 20px rgba(34, 211, 238, 0.08);
}

.skeleton {
  position: relative;
  height: 9px;
  overflow: hidden;
  border-radius: 999px;
  background: rgba(148, 163, 184, 0.1);
}

.skeleton::after {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, rgba(125, 211, 252, 0.24), transparent);
  content: "";
  transform: translateX(-100%);
  animation: source-shimmer 1.35s infinite;
}

.wide { width: 90%; }
.medium { width: 67%; }
.short { width: 42%; }
.source-skeleton { min-height: 94px; }

@keyframes source-shimmer {
  to { transform: translateX(100%); }
}

@media (prefers-reduced-motion: reduce) {
  .source-item,
  .skeleton::after { animation: none; transition: none; }
}
</style>
