<template>
  <div
    class="app-shell"
    :class="{ 'sidebar-open': sidebarOpen, 'sidebar-collapsed': sidebarCollapsed }"
  >
    <button
      class="sidebar-backdrop"
      type="button"
      aria-label="关闭导航"
      @click="sidebarOpen = false"
    />

    <aside class="sidebar">
      <div class="brand">
        <div class="brand-mark">OA</div>
        <div class="brand-copy">
          <strong>OA 管理后台</strong>
          <span>Office Automation</span>
        </div>
        <button
          class="collapse-button"
          type="button"
          :aria-label="sidebarCollapsed ? '展开侧边栏' : '折叠侧边栏'"
          :title="sidebarCollapsed ? '展开侧边栏' : '折叠侧边栏'"
          @click="toggleSidebar"
        >
          <PanelLeftOpen v-if="sidebarCollapsed" class="icon" />
          <PanelLeftClose v-else class="icon" />
        </button>
      </div>

      <nav class="nav">
        <template v-for="group in navGroups" :key="group.meta.title">
          <p class="nav-title">{{ group.meta.title }}</p>
          <RouterLink
            v-for="item in group.children"
            :key="item.path"
            class="nav-link"
            :to="'/' + (item.component || item.path)"
            :title="sidebarCollapsed ? item.meta.title : undefined"
            @click="sidebarOpen = false"
          >
            <component :is="resolveIcon(item.icon || item.meta.icon)" class="icon" />
            <span>{{ item.meta.title }}</span>
          </RouterLink>
        </template>
      </nav>
    </aside>

    <div class="shell-main">
      <header class="topbar">
        <button class="btn icon-btn menu-button" type="button" aria-label="展开导航" @click="sidebarOpen = !sidebarOpen">
          <Menu class="icon" />
        </button>
        <div class="title-block">
          <h1>{{ currentTitle }}</h1>
          <p>{{ today }}</p>
        </div>
        <div class="topbar-actions">
          <ThemeToggle />
          <RouterLink class="user-chip" to="/profile" title="个人信息">
            <UserRound class="icon" />
            <span>{{ auth.displayName }}</span>
          </RouterLink>
          <button class="btn icon-btn" type="button" aria-label="退出登录" title="退出登录" @click="logout">
            <LogOut class="icon" />
          </button>
        </div>
      </header>

      <main class="content">
        <RouterView v-slot="{ Component, route: childRoute }">
          <Transition name="route" mode="out-in">
            <component :is="Component" :key="childRoute.fullPath" />
          </Transition>
        </RouterView>
      </main>
    </div>

    <nav class="mobile-nav" aria-label="移动端快捷导航">
      <RouterLink v-for="item in mobileNav" :key="item.path" :to="item.path">
        <component :is="item.icon" class="icon" />
        <span>{{ item.label }}</span>
      </RouterLink>
    </nav>
  </div>
</template>

<script setup lang="ts">
import {
  BarChart3,
  Bell,
  Bot,
  Box,
  Briefcase,
  Building2,
  CalendarCheck,
  CalendarDays,
  CheckCircle,
  ClipboardCheck,
  ClipboardList,
  Clock,
  FilePlus,
  FileText,
  LayoutDashboard,
  LogOut,
  Menu,
  MessageSquarePlus,
  MessageSquareText,
  Monitor,
  Package,
  PanelLeftClose,
  PanelLeftOpen,
  Settings,
  ShoppingCart,
  TrendingUp,
  UserCheck,
  UserPlus,
  UserRound,
  UsersRound
} from 'lucide-vue-next'
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import ThemeToggle from '@/components/ThemeToggle.vue'
import { useAuthStore } from '@/stores/auth'
import type { RouterVO } from '@/api/types'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const sidebarOpen = ref(false)
const sidebarCollapsed = ref(localStorage.getItem('oa_sidebar_collapsed') === '1')

const currentTitle = computed(() => String(route.meta.title || '首页工作台'))
const today = new Intl.DateTimeFormat('zh-CN', {
  dateStyle: 'full'
}).format(new Date())

const navGroups = computed(() => {
  if (!auth.menus || auth.menus.length === 0) {
    return []
  }
  return auth.menus.filter((m: RouterVO) => m.menuType === 1 && m.children && m.children.length > 0)
})

const mobileNav = computed(() => {
  const quickPaths = ['/dashboard', '/attendance/punch', '/approval/submit', '/notice/message', '/profile']
  const icons: Record<string, any> = {
    '/dashboard': LayoutDashboard,
    '/attendance/punch': CalendarCheck,
    '/approval/submit': MessageSquarePlus,
    '/notice/message': Bell,
    '/profile': UserRound
  }
  return quickPaths.map(p => {
    const nameMap: Record<string, string> = {
      '/dashboard': '首页',
      '/attendance/punch': '打卡',
      '/approval/submit': '申请',
      '/notice/message': '通知',
      '/profile': '我的'
    }
    return { label: nameMap[p] || p, path: p, icon: icons[p] }
  })
})

const iconMap: Record<string, any> = {
  LayoutDashboard, BarChart3, Bell, Bot, Box, Briefcase, Building2,
  CalendarCheck, CalendarDays, CheckCircle, ClipboardCheck, ClipboardList,
  Clock, FilePlus, FileText, MessageSquareText, Monitor, Package,
  Settings, ShoppingCart, TrendingUp, UserCheck, UserPlus, UsersRound,
  Menu, MessageSquarePlus, PanelLeftClose, PanelLeftOpen, UserRound,
  LogOut
}

function resolveIcon(name?: string) {
  if (name && iconMap[name]) {
    return iconMap[name]
  }
  return Box
}

function toggleSidebar() {
  sidebarCollapsed.value = !sidebarCollapsed.value
  localStorage.setItem('oa_sidebar_collapsed', sidebarCollapsed.value ? '1' : '0')
}

async function logout() {
  await auth.logout()
  router.push('/login')
}

onMounted(async () => {
  // 路由守卫已自动加载菜单，此处无需重复加载
})
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: grid;
  grid-template-columns: 264px minmax(0, 1fr);
  transition: grid-template-columns 0.22s ease-out;
}

.app-shell.sidebar-collapsed {
  grid-template-columns: 82px minmax(0, 1fr);
}

.sidebar {
  position: sticky;
  top: 0;
  z-index: 40;
  height: 100vh;
  display: grid;
  grid-template-rows: auto minmax(0, 1fr);
  overflow: hidden;
  border-right: 1px solid var(--border);
  background:
    linear-gradient(180deg, color-mix(in srgb, var(--primary) 5%, transparent), transparent 34%),
    var(--sidebar);
  color: var(--text);
  box-shadow: 8px 0 32px rgba(0, 0, 0, 0.06), inset -1px 0 0 rgba(128, 160, 210, 0.08);
  backdrop-filter: blur(16px) saturate(120%);
}

.brand {
  height: 76px;
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.07);
}

.brand-mark {
  width: 40px;
  height: 40px;
  flex: 0 0 40px;
  display: grid;
  place-items: center;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  background: var(--brand-gradient);
  color: #fff;
  font-weight: 820;
  box-shadow: 0 10px 24px var(--primary-glow), inset 0 1px 0 rgba(255, 255, 255, 0.2);
}

.brand-copy {
  min-width: 0;
  white-space: nowrap;
  transition: opacity 0.16s ease, width 0.2s ease;
}

.brand strong,
.brand span {
  display: block;
}

.brand strong {
  font-size: 14px;
}

.brand span {
  margin-top: 4px;
  color: var(--muted);
  font-size: 10px;
  text-transform: uppercase;
}

.collapse-button {
  width: 30px;
  height: 30px;
  flex: 0 0 30px;
  display: grid;
  place-items: center;
  margin-left: auto;
  border: 1px solid var(--border);
  border-radius: 6px;
  background: var(--surface-soft);
  color: var(--muted);
  cursor: pointer;
  transition: transform 0.14s ease, background-color 0.18s ease, color 0.18s ease;
}

.collapse-button:hover {
  background: var(--surface);
  color: var(--primary-soft);
}

.collapse-button:active {
  transform: scale(0.95);
}

.nav {
  overflow-y: auto;
  overflow-x: hidden;
  padding: 12px 10px 20px;
  overscroll-behavior: contain;
  scrollbar-width: none;
}

.nav::-webkit-scrollbar {
  display: none;
}

.nav-title {
  height: 20px;
  margin: 16px 10px 7px;
  overflow: hidden;
  color: var(--faint);
  font-size: 10px;
  font-weight: 740;
  text-transform: uppercase;
  white-space: nowrap;
  transition: opacity 0.16s ease;
}

.nav-link {
  position: relative;
  height: 42px;
  display: flex;
  align-items: center;
  gap: 11px;
  margin-bottom: 2px;
  padding: 0 12px;
  border: 1px solid transparent;
  border-radius: 7px;
  color: var(--muted);
  font-size: 13px;
  font-weight: 650;
  white-space: nowrap;
  transition: background-color 0.18s ease, color 0.18s ease, border-color 0.18s ease, transform 0.14s ease;
}

.nav-link::before {
  position: absolute;
  left: -10px;
  width: 3px;
  height: 22px;
  border-radius: 0 3px 3px 0;
  background: transparent;
  box-shadow: 0 0 14px transparent;
  content: "";
  transition: background-color 0.18s ease, box-shadow 0.18s ease;
}

.nav-link:hover {
  border-color: color-mix(in srgb, var(--primary-soft) 18%, transparent);
  background: linear-gradient(90deg, color-mix(in srgb, var(--primary) 14%, transparent), color-mix(in srgb, var(--accent) 8%, transparent));
  color: var(--text);
  transform: translateX(4px) scale(1.02);
}

.nav-link:hover .icon {
  color: var(--cyan);
  filter: drop-shadow(0 0 7px rgba(6, 182, 212, 0.42));
  transform: scale(1.08);
}

.nav-link .icon {
  transition: color var(--duration-fast) ease, filter var(--duration-fast) ease, transform var(--duration-fast) ease;
}

.nav-link.router-link-active {
  border-color: color-mix(in srgb, var(--primary-soft) 26%, transparent);
  background: linear-gradient(90deg, color-mix(in srgb, var(--primary) 24%, transparent), color-mix(in srgb, var(--accent) 14%, transparent));
  color: var(--text);
  box-shadow: inset 0 1px 0 rgba(128, 180, 230, 0.12), 0 8px 24px rgba(37, 99, 235, 0.08);
}

.nav-link.router-link-active::before {
  background: linear-gradient(180deg, var(--cyan), var(--primary));
  box-shadow: 0 0 16px rgba(6, 182, 212, 0.46);
}

.sidebar-collapsed .brand {
  height: 116px;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
  padding: 10px 0;
}

.sidebar-collapsed .brand-copy,
.sidebar-collapsed .nav-title,
.sidebar-collapsed .nav-link span {
  width: 0;
  opacity: 0;
  pointer-events: none;
}

.sidebar-collapsed .brand-copy {
  display: none;
}

.sidebar-collapsed .collapse-button {
  position: static;
  margin: 0;
  transform: none;
}

.sidebar-collapsed .nav {
  padding-right: 12px;
  padding-left: 12px;
}

.sidebar-collapsed .nav-link {
  justify-content: center;
  padding: 0;
}

.sidebar-collapsed .nav-link:hover {
  transform: none;
}

.sidebar-collapsed .nav-link::before {
  left: -12px;
}

.shell-main {
  min-width: 0;
  display: grid;
  grid-template-rows: 76px minmax(0, 1fr);
}

.topbar {
  position: sticky;
  top: 0;
  z-index: 30;
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 0 22px;
  border-bottom: 1px solid var(--border);
  background: color-mix(in srgb, var(--bg-subtle) 76%, transparent);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.06);
  backdrop-filter: blur(16px) saturate(120%);
}

.title-block h1 {
  margin: 0;
  color: var(--text);
  font-size: 18px;
  font-weight: 760;
}

.title-block p {
  margin: 5px 0 0;
  color: var(--muted);
  font-size: 11px;
}

.topbar-actions {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 9px;
}

.user-chip {
  min-height: 38px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 0 12px;
  border: 1px solid var(--border);
  border-radius: 7px;
  background: var(--surface);
  color: var(--text);
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
  font-size: 12px;
  backdrop-filter: blur(10px);
  transition: border-color 0.18s ease, background-color 0.18s ease;
}

.user-chip:hover {
  border-color: var(--border-strong);
  background: var(--surface-elevated);
}

.menu-button,
.mobile-nav,
.sidebar-backdrop {
  display: none;
}

.content {
  min-width: 0;
  padding: 22px;
}

@media (max-width: 980px) {
  .app-shell,
  .app-shell.sidebar-collapsed {
    grid-template-columns: minmax(0, 1fr);
  }

  .sidebar {
    position: fixed;
    z-index: 60;
    width: 268px;
    transform: translateX(-100%);
    transition: transform 0.22s ease-out;
  }

  .sidebar-open .sidebar {
    transform: translateX(0);
  }

  .sidebar-collapsed .brand {
    height: 76px;
    flex-direction: row;
    justify-content: flex-start;
    gap: 12px;
    padding: 0 16px;
  }

  .sidebar-collapsed .brand-copy,
  .sidebar-collapsed .nav-title,
  .sidebar-collapsed .nav-link span {
    width: auto;
    opacity: 1;
    pointer-events: auto;
  }

  .sidebar-collapsed .brand-copy {
    display: block;
  }

  .sidebar-collapsed .collapse-button {
    position: static;
    margin-left: auto;
    transform: none;
  }

  .sidebar-collapsed .nav {
    padding: 12px 10px 20px;
  }

  .sidebar-collapsed .nav-link {
    justify-content: flex-start;
    padding: 0 12px;
  }

  .sidebar-backdrop {
    position: fixed;
    inset: 0;
    z-index: 50;
    border: 0;
    background: var(--overlay);
    backdrop-filter: blur(6px);
  }

  .sidebar-open .sidebar-backdrop {
    display: block;
    animation: backdrop-in 0.2s ease-out;
  }

  .menu-button {
    display: inline-flex;
  }

  .collapse-button {
    display: none;
  }
}

@media (max-width: 640px) {
  .shell-main {
    grid-template-rows: 66px minmax(0, 1fr);
  }

  .topbar {
    padding: 0 12px;
  }

  .title-block {
    display: none;
  }

  .user-chip {
    width: 38px;
    padding: 0;
    justify-content: center;
  }

  .user-chip span {
    display: none;
  }

  .content {
    padding: 14px 12px 92px;
  }

  .mobile-nav {
    position: fixed;
    right: 10px;
    bottom: max(10px, env(safe-area-inset-bottom));
    left: 10px;
    z-index: 55;
    height: 64px;
    display: grid;
    grid-template-columns: repeat(5, minmax(0, 1fr));
    padding: 6px;
    border: 1px solid var(--border-strong);
    border-radius: 8px;
    background: color-mix(in srgb, var(--surface-elevated) 90%, transparent);
    box-shadow: 0 18px 46px rgba(0, 0, 0, 0.3), inset 0 1px 0 rgba(255, 255, 255, 0.06);
    backdrop-filter: blur(16px) saturate(125%);
  }

  .mobile-nav a {
    position: relative;
    display: grid;
    place-items: center;
    align-content: center;
    gap: 3px;
    border-radius: 6px;
    color: var(--muted);
    font-size: 9px;
    transition: background-color 0.18s ease, color 0.18s ease, transform 0.14s ease;
  }

  .mobile-nav a:active {
    transform: scale(0.95);
  }

  .mobile-nav a.router-link-active {
    background: linear-gradient(145deg, color-mix(in srgb, var(--primary) 16%, transparent), color-mix(in srgb, var(--accent) 10%, transparent));
    color: var(--primary-soft);
  }

  .mobile-nav a.router-link-active::before {
    position: absolute;
    top: 0;
    width: 20px;
    height: 2px;
    border-radius: 999px;
    background: var(--primary-soft);
    box-shadow: 0 0 10px var(--primary-glow);
    content: "";
  }

  .mobile-nav .icon {
    width: 19px;
    height: 19px;
  }
}

@keyframes backdrop-in {
  from { opacity: 0; }
}
</style>