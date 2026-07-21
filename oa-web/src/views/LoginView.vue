<template>
  <main class="login-page">
    <section class="login-visual">
      <div class="visual-grid">
        <div class="visual-stat">
          <span>今日出勤</span>
          <strong>96.5%</strong>
        </div>
        <div class="visual-stat">
          <span>审批平均耗时</span>
          <strong>5.6h</strong>
        </div>
        <div class="visual-bars">
          <span v-for="height in [46, 64, 52, 78, 70, 88, 82]" :key="height" :style="{ height: height + '%' }" />
        </div>
      </div>
    </section>

    <section class="login-card">
      <div class="login-brand">
        <div class="brand-mark">OA</div>
        <div>
          <h1>OA 管理后台</h1>
          <p>统一办公、组织权限、流程协同</p>
        </div>
      </div>

      <form class="login-form" @submit.prevent="submit">
        <label class="form-item">
          <span class="form-label">账号</span>
          <input v-model.trim="form.username" class="field" autocomplete="username" placeholder="admin" />
        </label>
        <label class="form-item">
          <span class="form-label">密码</span>
          <input v-model="form.password" class="field" type="password" autocomplete="current-password" placeholder="请输入密码" />
        </label>

        <p v-if="error" class="error-text">{{ error }}</p>

        <button class="btn primary login-submit" type="submit" :disabled="loading">
          <LogIn class="icon" />
          {{ loading ? '登录中' : '登录' }}
        </button>
        <button class="btn ghost login-submit" type="button" @click="demo">
          <MonitorPlay class="icon" />
          演示进入
        </button>
      </form>
    </section>
  </main>
</template>

<script setup lang="ts">
import { LogIn, MonitorPlay } from 'lucide-vue-next'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)
const error = ref('')
const form = reactive({
  username: 'admin',
  password: ''
})

async function submit() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(form.username, form.password)
    router.push('/dashboard')
  } catch (err) {
    error.value = err instanceof Error ? err.message : '登录失败'
  } finally {
    loading.value = false
  }
}

function demo() {
  auth.useDemo()
  router.push('/dashboard')
}
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(420px, 0.95fr);
  background: #f4f7fb;
}

.login-visual {
  position: relative;
  overflow: hidden;
  display: grid;
  place-items: center;
  background:
    linear-gradient(rgba(18, 31, 50, 0.62), rgba(18, 31, 50, 0.62)),
    url("data:image/svg+xml,%3Csvg width='1200' height='900' viewBox='0 0 1200 900' xmlns='http://www.w3.org/2000/svg'%3E%3Crect width='1200' height='900' fill='%23182333'/%3E%3Cg opacity='.28'%3E%3Crect x='110' y='170' width='300' height='190' fill='%232b8798'/%3E%3Crect x='455' y='120' width='520' height='90' fill='%23ffffff'/%3E%3Crect x='455' y='250' width='220' height='260' fill='%23b87716'/%3E%3Crect x='710' y='250' width='320' height='260' fill='%23ffffff'/%3E%3Crect x='170' y='430' width='300' height='260' fill='%23ffffff'/%3E%3Crect x='520' y='570' width='420' height='150' fill='%232b8798'/%3E%3C/g%3E%3C/svg%3E");
  background-size: cover;
}

.visual-grid {
  width: min(78%, 720px);
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.visual-stat,
.visual-bars {
  min-height: 150px;
  padding: 22px;
  border: 1px solid rgba(255, 255, 255, 0.22);
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.13);
  color: #fff;
  backdrop-filter: blur(12px);
}

.visual-stat span,
.visual-stat strong {
  display: block;
}

.visual-stat span {
  color: #d6e3ef;
  font-size: 14px;
}

.visual-stat strong {
  margin-top: 18px;
  font-size: 42px;
}

.visual-bars {
  grid-column: span 2;
  height: 230px;
  display: flex;
  align-items: flex-end;
  gap: 12px;
}

.visual-bars span {
  flex: 1;
  min-width: 18px;
  border-radius: 5px 5px 0 0;
  background: #e8f4f6;
}

.visual-bars span:nth-child(2n) {
  background: #d59b44;
}

.login-card {
  display: grid;
  align-content: center;
  gap: 30px;
  padding: 56px;
  background: #fff;
}

.login-brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-mark {
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: var(--primary);
  color: #fff;
  font-weight: 800;
}

.login-brand h1 {
  margin: 0;
  font-size: 28px;
}

.login-brand p {
  margin: 5px 0 0;
  color: var(--muted);
}

.login-form {
  display: grid;
  gap: 16px;
}

.login-submit {
  width: 100%;
  height: 42px;
}

.error-text {
  margin: 0;
  color: var(--danger);
  font-size: 13px;
}

@media (max-width: 900px) {
  .login-page {
    grid-template-columns: 1fr;
  }

  .login-visual {
    display: none;
  }

  .login-card {
    min-height: 100vh;
    padding: 28px;
  }
}
</style>
