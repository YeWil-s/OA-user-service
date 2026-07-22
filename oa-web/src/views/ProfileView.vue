<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">个人信息</h2>
        <p class="page-subtitle">账号资料、角色权限与密码维护</p>
      </div>
    </div>

    <div class="grid-2">
      <section class="panel panel-pad profile-panel">
        <div class="avatar">{{ initials }}</div>
        <h3>{{ auth.displayName }}</h3>
        <p>{{ auth.user?.username }}</p>
        <div class="role-list">
          <span v-for="role in auth.user?.roles || []" :key="role" class="pill success">{{ role }}</span>
        </div>
      </section>

      <section class="panel panel-pad">
        <h3 class="section-title">修改密码</h3>
        <div class="form-grid">
          <label class="form-item">
            <span class="form-label">旧密码</span>
            <input class="field" type="password" />
          </label>
          <label class="form-item">
            <span class="form-label">新密码</span>
            <input class="field" type="password" />
          </label>
          <label class="form-item">
            <span class="form-label">确认新密码</span>
            <input class="field" type="password" />
          </label>
        </div>
        <div class="toolbar form-actions">
          <button class="btn primary" type="button">
            <KeyRound class="icon" />
            保存
          </button>
        </div>
      </section>
    </div>

    <section class="panel panel-pad">
      <h3 class="section-title">权限标识</h3>
      <div class="permission-list">
        <span v-for="permission in auth.user?.permissions || ['dashboard:view']" :key="permission" class="pill">
          {{ permission }}
        </span>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { KeyRound } from 'lucide-vue-next'
import { computed } from 'vue'
import { useAuthStore } from '@/stores/auth'

const auth = useAuthStore()
const initials = computed(() => auth.displayName.slice(0, 2).toUpperCase())
</script>

<style scoped>
.profile-panel {
  min-height: 260px;
  display: grid;
  place-items: center;
  text-align: center;
}

.avatar {
  width: 76px;
  height: 76px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: var(--primary);
  color: #fff;
  font-size: 24px;
  font-weight: 800;
}

.profile-panel h3 {
  margin: 12px 0 0;
  font-size: 20px;
}

.profile-panel p {
  margin: 4px 0 0;
  color: var(--muted);
}

.role-list,
.permission-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.section-title {
  margin: 0 0 14px;
  font-size: 16px;
}

.form-actions {
  margin-top: 16px;
}
</style>
