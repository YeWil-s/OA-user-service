<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">待审批</h2>
        <p class="page-subtitle">当前用户需要处理的申请</p>
      </div>
      <span class="mock-banner">审批后端未完成，当前为界面流程</span>
    </div>

    <section class="pending-grid">
      <article v-for="item in rows" :key="item.id" class="panel panel-pad pending-item">
        <header>
          <div>
            <h3>{{ item.no }}</h3>
            <p>{{ item.type }} · {{ item.duration }}</p>
          </div>
          <span class="pill warn">{{ item.status }}</span>
        </header>
        <div class="approval-line">
          <span>发起人</span><strong>李明</strong>
          <span>提交时间</span><strong>{{ item.time }}</strong>
        </div>
        <textarea class="textarea" placeholder="审批意见" />
        <div class="toolbar">
          <button class="btn danger"><XCircle class="icon" />驳回</button>
          <button class="btn primary"><CheckCircle2 class="icon" />同意</button>
        </div>
      </article>
    </section>
  </section>
</template>

<script setup lang="ts">
import { CheckCircle2, XCircle } from 'lucide-vue-next'
import { workflowRows } from '@/api/mock'

const rows = workflowRows.applications.filter((item) => item.status === '审批中')
</script>

<style scoped>
.pending-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.pending-item {
  display: grid;
  gap: 14px;
}

.pending-item header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.pending-item h3 {
  margin: 0;
}

.pending-item p {
  margin: 5px 0 0;
  color: var(--muted);
}

.approval-line {
  display: grid;
  grid-template-columns: 80px minmax(0, 1fr);
  gap: 8px;
}

.approval-line span {
  color: var(--muted);
}

@media (max-width: 900px) {
  .pending-grid {
    grid-template-columns: 1fr;
  }
}
</style>
