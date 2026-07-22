<template>
  <main class="login-page">
    <ThemeToggle class="login-theme" />
    <section class="login-visual">
      <div class="visual-grid">
        <div class="visual-stat">
          <span>组织权限</span>
          <strong>统一管理</strong>
        </div>
        <div class="visual-stat">
          <span>流程协同</span>
          <strong>实时流转</strong>
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
      </form>
    </section>
  </main>
</template>

<script setup lang="ts">
import { LogIn } from 'lucide-vue-next'
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import ThemeToggle from '@/components/ThemeToggle.vue'
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
</script>

<style scoped>
.login-page {
  position: relative;
  min-height: 100vh;
  display: grid;
  grid-template-columns: minmax(0, 1.08fr) minmax(420px, 0.92fr);
  overflow: hidden;
  background: var(--bg);
}

.login-theme {
  position: fixed;
  top: 20px;
  right: 20px;
  z-index: 10;
}

.login-visual {
  position: relative;
  overflow: hidden;
  display: grid;
  place-items: center;
  border-right: 1px solid var(--border);
  background:
    linear-gradient(rgba(8, 14, 24, 0.54), rgba(8, 14, 24, 0.72)),
    url("data:image/svg+xml,%3Csvg width='1200' height='900' viewBox='0 0 1200 900' xmlns='http://www.w3.org/2000/svg'%3E%3Crect width='1200' height='900' fill='%23131d2b'/%3E%3Cg opacity='.34'%3E%3Crect x='110' y='170' width='300' height='190' fill='%234c96a7'/%3E%3Crect x='455' y='120' width='520' height='90' fill='%23d8e2ee'/%3E%3Crect x='455' y='250' width='220' height='260' fill='%23d09a4e'/%3E%3Crect x='710' y='250' width='320' height='260' fill='%23d8e2ee'/%3E%3Crect x='170' y='430' width='300' height='260' fill='%23d8e2ee'/%3E%3Crect x='520' y='570' width='420' height='150' fill='%234c96a7'/%3E%3C/g%3E%3C/svg%3E");
  background-size: cover;
}

.login-visual::after {
  position: absolute;
  inset: 0;
  border: 1px solid rgba(255, 255, 255, 0.04);
  pointer-events: none;
  content: "";
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
  border: 1px solid rgba(255, 255, 255, 0.14);
  border-radius: 8px;
  background:
    linear-gradient(145deg, rgba(255, 255, 255, 0.1), transparent 72%),
    rgba(16, 25, 39, 0.36);
  color: #f6f8fb;
  box-shadow: 0 18px 42px rgba(0, 0, 0, 0.16), inset 0 1px 0 rgba(255, 255, 255, 0.08);
  backdrop-filter: blur(14px);
}

.visual-stat span,
.visual-stat strong {
  display: block;
}

.visual-stat span {
  color: #b9c7d8;
  font-size: 12px;
}

.visual-stat strong {
  margin-top: 20px;
  font-size: 38px;
  font-variant-numeric: tabular-nums;
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
  border-radius: 5px 5px 1px 1px;
  background: linear-gradient(180deg, #60a5fa, #2563eb);
  box-shadow: 0 0 18px rgba(59, 130, 246, 0.28);
}

.visual-bars span:nth-child(2n) {
  background: linear-gradient(180deg, #fb923c, #ea580c);
}

.login-card {
  display: grid;
  align-content: center;
  gap: 32px;
  padding: clamp(36px, 5vw, 72px);
  border-left: 1px solid var(--border);
  background:
    linear-gradient(145deg, color-mix(in srgb, var(--surface-elevated) 38%, transparent), transparent 64%),
    color-mix(in srgb, var(--bg-subtle) 88%, transparent);
  backdrop-filter: blur(16px);
}

.login-brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.brand-mark {
  width: 48px;
  height: 48px;
  display: grid;
  place-items: center;
  border: 1px solid color-mix(in srgb, var(--primary-soft) 36%, transparent);
  border-radius: 8px;
  background: var(--brand-gradient);
  color: #fff;
  box-shadow: 0 12px 30px var(--primary-glow), inset 0 1px 0 rgba(255, 255, 255, 0.2);
  font-weight: 820;
}

.login-brand h1 {
  margin: 0;
  color: var(--text);
  font-size: 27px;
}

.login-brand p {
  margin: 6px 0 0;
  color: var(--muted);
  font-size: 13px;
}

.login-form {
  display: grid;
  gap: 17px;
}

.login-submit {
  width: 100%;
  height: 43px;
  margin-top: 2px;
}

.error-text {
  margin: 0;
  color: var(--danger);
  font-size: 12px;
  animation: login-shake 0.3s ease;
}

@keyframes login-shake {
  25% { transform: translateX(-3px); }
  50% { transform: translateX(3px); }
  75% { transform: translateX(-2px); }
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
