<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">人事变动</h2>
        <p class="page-subtitle">入职、转正、调岗、离职记录</p>
      </div>
      <div class="toolbar">
        <span class="mock-banner">资产人事后端未完成</span>
        <button class="btn primary" @click="dialogOpen = true"><Plus class="icon" />新增</button>
      </div>
    </div>

    <section class="panel panel-pad">
      <div class="timeline">
        <article v-for="item in rows" :key="item.id" class="timeline-item">
          <span class="dot" />
          <div>
            <strong>{{ item.name }} · {{ item.type }}</strong>
            <p>{{ item.desc }}</p>
          </div>
          <span>{{ item.date }}</span>
        </article>
      </div>
    </section>

    <ModalDialog v-model="dialogOpen" title="新增人事变动">
      <div class="form-grid">
        <label class="form-item"><span class="form-label">员工</span><input class="field" /></label>
        <label class="form-item"><span class="form-label">类型</span><select class="select"><option>入职</option><option>转正</option><option>调岗</option><option>离职</option></select></label>
        <label class="form-item"><span class="form-label">日期</span><input class="field" type="date" /></label>
        <label class="form-item"><span class="form-label">目标部门</span><input class="field" /></label>
      </div>
      <template #footer><button class="btn" @click="dialogOpen = false">取消</button><button class="btn primary" @click="dialogOpen = false">保存</button></template>
    </ModalDialog>
  </section>
</template>

<script setup lang="ts">
import { Plus } from 'lucide-vue-next'
import { ref } from 'vue'
import ModalDialog from '@/components/ModalDialog.vue'

const dialogOpen = ref(false)
const rows = [
  { id: 1, name: '李明', type: '调岗', desc: '从平台组调整至数据可视化组', date: '2026-07-20' },
  { id: 2, name: '周宁', type: '转正', desc: '试用期通过，转为正式员工', date: '2026-07-18' }
]
</script>

<style scoped>
.timeline {
  display: grid;
  gap: 12px;
}

.timeline-item {
  min-height: 64px;
  display: grid;
  grid-template-columns: 18px minmax(0, 1fr) 110px;
  align-items: center;
  gap: 12px;
  padding: 0 12px;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--surface-soft);
}

.dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--primary);
}

.timeline-item p,
.timeline-item > span:last-child {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 13px;
}
</style>
