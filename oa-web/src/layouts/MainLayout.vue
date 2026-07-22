<template>
  <div class="app-shell" :class="{ 'sidebar-open': sidebarOpen }">
    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">OA</div>
        <div>
          <strong>OA 管理后台</strong>
          <span>Office Automation</span>
        </div>
      </div>

      <nav class="nav">
        <template v-for="group in navGroups" :key="group.title">
          <p class="nav-title">{{ group.title }}</p>
          <RouterLink
            v-for="item in group.items"
            :key="item.path"
            class="nav-link"
            :to="item.path"
            @click="sidebarOpen = false"
          >
            <component :is="item.icon" class="icon" />
            <span>{{ item.label }}</span>
          </RouterLink>
        </template>
      </nav>
    </aside>

    <div class="shell-main">
      <header class="topbar">
        <button class="btn icon-btn menu-button" type="button" aria-label="展开导航" @click="sidebarOpen = !sidebarOpen">
          <Menu class="icon" />
        </button>
        <div>
          <h1>{{ currentTitle }}</h1>
          <p>{{ today }}</p>
        </div>
        <div class="topbar-actions">
          <span v-if="auth.demoMode" class="pill warn">演示数据</span>
          <span class="user-chip">
            <UserRound class="icon" />
            {{ auth.displayName }}
          </span>
          <button class="btn icon-btn" type="button" aria-label="退出登录" @click="logout">
            <LogOut class="icon" />
          </button>
        </div>
      </header>

      <main class="content">
        <RouterView />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import {
  BarChart3,
  Bell,
  Bot,
  BriefcaseBusiness,
  Building2,
  CalendarCheck,
  ClipboardCheck,
  ContactRound,
  FileText,
  LayoutDashboard,
  LogOut,
  Menu,
  MessageSquareText,
  Network,
  ShieldCheck,
  UserRound,
  UsersRound
} from 'lucide-vue-next'
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const sidebarOpen = ref(false)

const currentTitle = computed(() => String(route.meta.title || '首页工作台'))
const today = new Intl.DateTimeFormat('zh-CN', {
  dateStyle: 'full'
}).format(new Date())

const navGroups = [
  {
    title: '总览',
    items: [
      { label: '首页工作台', path: '/dashboard', icon: LayoutDashboard },
      { label: '数据可视化', path: '/visual/dashboard', icon: BarChart3 }
    ]
  },
  {
    title: '组织权限',
    items: [
      { label: '员工管理', path: '/system/employee', icon: UsersRound },
      { label: '部门管理', path: '/system/dept', icon: Building2 },
      { label: '岗位管理', path: '/system/position', icon: ContactRound },
      { label: '角色管理', path: '/system/role', icon: ShieldCheck },
      { label: '菜单管理', path: '/system/menu', icon: Network }
    ]
  },
  {
    title: '办公流程',
    items: [
      { label: '考勤打卡', path: '/attendance/punch', icon: CalendarCheck },
      { label: '考勤记录', path: '/attendance/record', icon: FileText },
      { label: '班次管理', path: '/attendance/shift', icon: ClipboardCheck },
      { label: '提交申请', path: '/approval/submit', icon: MessageSquareText },
      { label: '我的申请', path: '/approval/my-apply', icon: FileText },
      { label: '待审批', path: '/approval/pending', icon: ClipboardCheck }
    ]
  },
  {
    title: '公告资产',
    items: [
      { label: '公告列表', path: '/notice/list', icon: Bell },
      { label: '消息中心', path: '/notice/message', icon: MessageSquareText },
      { label: '资产台账', path: '/asset/assets', icon: BriefcaseBusiness },
      { label: '人事变动', path: '/asset/staff', icon: ContactRound },
      { label: '合同管理', path: '/asset/contracts', icon: FileText },
      { label: 'AI 助手', path: '/ai/assistant', icon: Bot }
    ]
  }
]

function logout() {
  auth.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 260px minmax(0, 1fr);
}

.sidebar {
  position: sticky;
  top: 0;
  height: 100vh;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  overflow: hidden;
  background: var(--sidebar);
  color: #dce5f2;
}

.brand {
  height: 74px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 18px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.brand-mark {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 8px;
  background: #2b8798;
  color: #fff;
  font-weight: 800;
}

.brand strong,
.brand span {
  display: block;
}

.brand span {
  margin-top: 3px;
  color: #9badc5;
  font-size: 12px;
}

.nav {
  overflow: auto;
  padding: 12px;
}

.nav-title {
  margin: 16px 10px 8px;
  color: #8ea1bc;
  font-size: 12px;
}

.nav-link {
  height: 40px;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 0 10px;
  border-radius: 6px;
  color: #dce5f2;
}

.nav-link:hover {
  background: var(--sidebar-soft);
}

.nav-link.router-link-active {
  background: #2b8798;
  color: #fff;
}

.shell-main {
  min-width: 0;
  display: grid;
  grid-template-rows: 74px minmax(0, 1fr);
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 0 22px;
  border-bottom: 1px solid var(--border);
  background: rgba(245, 247, 251, 0.92);
  backdrop-filter: blur(12px);
}

.topbar h1 {
  margin: 0;
  font-size: 19px;
}

.topbar p {
  margin: 4px 0 0;
  color: var(--muted);
  font-size: 12px;
}

.topbar-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-chip {
  min-height: 36px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0 11px;
  border-radius: 999px;
  background: #fff;
  border: 1px solid var(--border);
  font-size: 13px;
}

.menu-button {
  display: none;
}

.content {
  min-width: 0;
  padding: 20px;
}

@media (max-width: 980px) {
  .app-shell {
    grid-template-columns: minmax(0, 1fr);
  }

  .sidebar {
    position: fixed;
    z-index: 40;
    width: 260px;
    transform: translateX(-100%);
    transition: transform 0.2s ease;
  }

  .sidebar-open .sidebar {
    transform: translateX(0);
  }

  .menu-button {
    display: inline-flex;
  }
}

@media (max-width: 560px) {
  .topbar {
    padding: 0 12px;
  }

  .topbar h1,
  .topbar p {
    display: none;
  }

  .content {
    padding: 14px;
  }
}
</style>
