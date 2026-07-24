<template>
  <section class="page">
    <div class="page-head">
      <div>
        <h2 class="page-title">个人信息</h2>
        <p class="page-subtitle">员工名片与账号安全</p>
      </div>
    </div>

    <div class="grid-2">
      <!-- Business Card -->
      <section class="panel panel-pad card">
        <div class="card-header">
          <div class="avatar">{{ initials }}</div>
          <div class="card-title">
            <h3>{{ profile.realName || auth.displayName }}</h3>
            <span class="username">@{{ profile.username }}</span>
          </div>
        </div>

        <div class="card-body">
          <div class="info-grid">
            <div class="info-item">
              <Building2 class="icon" />
              <div>
                <span class="info-label">部门</span>
                <span class="info-value">{{ profile.deptName || '未设置' }}</span>
              </div>
            </div>
            <div class="info-item">
              <Briefcase class="icon" />
              <div>
                <span class="info-label">岗位</span>
                <span class="info-value">{{ profile.positionName || '未设置' }}</span>
              </div>
            </div>
            <div class="info-item">
              <Phone class="icon" />
              <div>
                <span class="info-label">手机</span>
                <span class="info-value">{{ profile.phone || '未设置' }}</span>
              </div>
            </div>
            <div class="info-item">
              <Mail class="icon" />
              <div>
                <span class="info-label">邮箱</span>
                <span class="info-value">{{ profile.email || '未设置' }}</span>
              </div>
            </div>
            <div class="info-item">
              <UserRound class="icon" />
              <div>
                <span class="info-label">性别</span>
                <span class="info-value">{{ genderText }}</span>
              </div>
            </div>
            <div class="info-item">
              <CalendarDays class="icon" />
              <div>
                <span class="info-label">入职日期</span>
                <span class="info-value">{{ profile.entryDate || '未设置' }}</span>
              </div>
            </div>
          </div>

          <div class="role-list" v-if="profile.roleNames && profile.roleNames.length">
            <span v-for="name in profile.roleNames" :key="name" class="pill success">{{ name }}</span>
          </div>
        </div>
      </section>

      <!-- Change Password -->
      <section class="panel panel-pad">
        <h3 class="section-title">修改密码</h3>
        <div class="form-grid">
          <label class="form-item">
            <span class="form-label">旧密码</span>
            <input class="field" type="password" v-model="pwd.oldPassword" />
          </label>
          <label class="form-item">
            <span class="form-label">新密码</span>
            <input class="field" type="password" v-model="pwd.newPassword" />
          </label>
          <label class="form-item">
            <span class="form-label">确认新密码</span>
            <input class="field" type="password" v-model="pwd.confirmPassword" />
          </label>
        </div>
        <p class="form-error" v-if="pwdError">{{ pwdError }}</p>
        <p class="form-success" v-if="pwdSuccess">{{ pwdSuccess }}</p>
        <div class="toolbar form-actions">
          <button class="btn primary" type="button" @click="handleChangePassword" :disabled="pwdLoading">
            <KeyRound class="icon" />
            {{ pwdLoading ? '保存中...' : '保存' }}
          </button>
        </div>
      </section>
    </div>
  </section>
</template>

<script setup lang="ts">
import { Building2, Briefcase, Phone, Mail, UserRound, CalendarDays, KeyRound } from 'lucide-vue-next'
import { computed, onMounted, reactive, ref } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { authApi, systemApi } from '@/api/services'
import type { LoginUser } from '@/api/types'

const auth = useAuthStore()

const profile = reactive<LoginUser>({
  accessToken: '',
  userId: 0,
  username: '',
  realName: '',
  roles: [],
  permissions: []
})

const initials = computed(() => {
  const name = profile.realName || profile.username || ''
  return name.slice(0, 2).toUpperCase() || '--'
})

const genderText = computed(() => {
  if (profile.gender === 1) return '男'
  if (profile.gender === 2) return '女'
  return '未设置'
})

// ---- password form ----
const pwd = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdError = ref('')
const pwdSuccess = ref('')
const pwdLoading = ref(false)

async function handleChangePassword() {
  pwdError.value = ''
  pwdSuccess.value = ''
  if (!pwd.oldPassword) { pwdError.value = '请输入旧密码'; return }
  if (!pwd.newPassword) { pwdError.value = '请输入新密码'; return }
  if (pwd.newPassword.length < 6) { pwdError.value = '新密码不能少于6位'; return }
  if (pwd.newPassword !== pwd.confirmPassword) { pwdError.value = '两次输入的新密码不一致'; return }

  pwdLoading.value = true
  try {
    await systemApi.updatePassword(profile.userId, {
      oldPassword: pwd.oldPassword,
      newPassword: pwd.newPassword
    })
    pwdSuccess.value = '密码修改成功'
    pwd.oldPassword = ''
    pwd.newPassword = ''
    pwd.confirmPassword = ''
  } catch (e: any) {
    pwdError.value = e?.message || '密码修改失败'
  } finally {
    pwdLoading.value = false
  }
}

onMounted(async () => {
  if (auth.user) {
    Object.assign(profile, auth.user)
  }
  try {
    const data = await authApi.current()
    if (data) {
      Object.assign(profile, data)
      if (auth.user) Object.assign(auth.user, data)
      // 角色可能已变化，重新加载菜单
      await auth.loadMenus()
    }
  } catch {
    // keep the store data on failure
  }
})
</script>

<style scoped>
/* ---- business card ---- */
.card {
  min-height: 260px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border, #e5e7eb);
  margin-bottom: 20px;
}

.avatar {
  width: 72px;
  height: 72px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: var(--primary);
  color: #fff;
  font-size: 24px;
  font-weight: 800;
  flex-shrink: 0;
}

.card-title h3 {
  margin: 0;
  font-size: 22px;
  font-weight: 700;
}

.username {
  color: var(--muted);
  font-size: 14px;
}

/* ---- info grid ---- */
.info-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.info-item .icon {
  width: 18px;
  height: 18px;
  margin-top: 2px;
  color: var(--muted);
  flex-shrink: 0;
}

.info-item > div {
  display: flex;
  flex-direction: column;
  min-width: 0;
}

.info-label {
  font-size: 12px;
  color: var(--muted);
  margin-bottom: 2px;
}

.info-value {
  font-size: 14px;
  font-weight: 500;
}

/* ---- roles ---- */
.role-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 20px;
  padding-top: 16px;
  border-top: 1px solid var(--border, #e5e7eb);
}

/* ---- form ---- */
.section-title {
  margin: 0 0 14px;
  font-size: 16px;
}

.form-actions {
  margin-top: 16px;
}

.form-error {
  color: #ef4444;
  font-size: 13px;
  margin: 8px 0 0;
}

.form-success {
  color: #22c55e;
  font-size: 13px;
  margin: 8px 0 0;
}
</style>
