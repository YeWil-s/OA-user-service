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
    await auth.loadMenus()
    const roles = auth.user?.roles ?? []
    if (roles.includes('ROLE_EMPLOYEE') && !roles.includes('ROLE_ADMIN') && !roles.includes('ROLE_HR') && !roles.includes('ROLE_LEADER')) {
      router.push('/attendance/punch')
    } else {
      router.push('/dashboard')
    }
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
  background: #f8fafc;
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
  background:
    linear-gradient(160deg, #eff6ff 0%, #f8fafc 40%, #f0f4fa 100%);
}

.login-visual::before {
  position: absolute;
  inset: 0;
  pointer-events: none;
  background-image:
    radial-gradient(circle at 20% 30%, rgba(59, 130, 246, 0.06) 0%, transparent 50%),
    radial-gradient(circle at 80% 70%, rgba(14, 165, 233, 0.05) 0%, transparent 50%);
  content: "";
}

.visual-grid {
  width: min(78%, 720px);
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
  position: relative;
  z-index: 1;
}

.visual-stat,
.visual-bars {
  min-height: 150px;
  padding: 22px;
  border: 1px solid rgba(59, 130, 246, 0.1);
  border-radius: 10px;
  background: #fff;
  color: #0f172a;
  box-shadow: 0 4px 24px rgba(20, 40, 80, 0.06), 0 1px 3px rgba(0, 0, 0, 0.04);
}

.visual-stat span,
.visual-stat strong {
  display: block;
}

.visual-stat span {
  color: #5a7098;
  font-size: 12px;
}

.visual-stat strong {
  margin-top: 20px;
  font-size: 38px;
  font-weight: 780;
  color: #0f172a;
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
  border-radius: 6px 6px 2px 2px;
  background: linear-gradient(180deg, #60a5fa, #2563eb);
}

.visual-bars span:nth-child(2n) {
  background: linear-gradient(180deg, #22d3ee, #0891b2);
}

.login-card {
  display: grid;
  align-content: center;
  gap: 32px;
  padding: clamp(36px, 5vw, 72px);
  background: #fff;
  box-shadow: -2px 0 24px rgba(20, 40, 80, 0.04);
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
  border-radius: 10px;
  background: linear-gradient(135deg, #2563eb, #0ea5e9);
  color: #fff;
  font-weight: 820;
  box-shadow: 0 8px 24px rgba(37, 99, 235, 0.2);
}

.login-brand h1 {
  margin: 0;
  color: #0f172a;
  font-size: 27px;
}

.login-brand p {
  margin: 6px 0 0;
  color: #5a7098;
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
