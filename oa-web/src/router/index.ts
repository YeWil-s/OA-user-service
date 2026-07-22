import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const MainLayout = () => import('@/layouts/MainLayout.vue')

export const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: () => import('@/views/LoginView.vue'), meta: { public: true } },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'dashboard', component: () => import('@/views/DashboardView.vue'), meta: { title: '首页工作台' } },
      { path: 'profile', name: 'profile', component: () => import('@/views/ProfileView.vue'), meta: { title: '个人信息' } },
      { path: 'system/employee', name: 'employee', component: () => import('@/views/system/EmployeeView.vue'), meta: { title: '员工管理' } },
      { path: 'system/dept', name: 'dept', component: () => import('@/views/system/DeptView.vue'), meta: { title: '部门管理' } },
      { path: 'system/position', name: 'position', component: () => import('@/views/system/PositionView.vue'), meta: { title: '岗位管理' } },
      { path: 'system/role', name: 'role', component: () => import('@/views/system/RoleView.vue'), meta: { title: '角色管理' } },
      { path: 'system/menu', name: 'menu', component: () => import('@/views/system/MenuView.vue'), meta: { title: '菜单管理' } },
      { path: 'notice/list', name: 'notice-list', component: () => import('@/views/notice/NoticeListView.vue'), meta: { title: '公告列表' } },
      { path: 'notice/message', name: 'notice-message', component: () => import('@/views/notice/MessageCenterView.vue'), meta: { title: '消息中心' } },
      { path: 'visual/dashboard', name: 'visual-dashboard', component: () => import('@/views/visual/VisualDashboardView.vue'), meta: { title: '数据可视化' } },
      { path: 'attendance/punch', name: 'attendance-punch', component: () => import('@/views/workflows/PunchView.vue'), meta: { title: '考勤打卡' } },
      { path: 'attendance/record', name: 'attendance-record', component: () => import('@/views/workflows/ModuleListView.vue'), meta: { title: '考勤记录', module: 'attendanceRecords' } },
      { path: 'attendance/shift', name: 'attendance-shift', component: () => import('@/views/workflows/ModuleListView.vue'), meta: { title: '班次管理', module: 'shifts' } },
      { path: 'approval/my-apply', name: 'approval-my', component: () => import('@/views/workflows/ModuleListView.vue'), meta: { title: '我的申请', module: 'applications' } },
      { path: 'approval/submit', name: 'approval-submit', component: () => import('@/views/workflows/SubmitApplicationView.vue'), meta: { title: '提交申请' } },
      { path: 'approval/pending', name: 'approval-pending', component: () => import('@/views/workflows/ApprovalPendingView.vue'), meta: { title: '待审批' } },
      { path: 'approval/processed', name: 'approval-processed', component: () => import('@/views/workflows/ModuleListView.vue'), meta: { title: '已办审批', module: 'applications' } },
      { path: 'asset/assets', name: 'asset-assets', component: () => import('@/views/workflows/ModuleListView.vue'), meta: { title: '资产台账', module: 'assets' } },
      { path: 'asset/staff', name: 'asset-staff', component: () => import('@/views/workflows/StaffChangeView.vue'), meta: { title: '人事变动' } },
      { path: 'asset/contracts', name: 'asset-contracts', component: () => import('@/views/workflows/ContractView.vue'), meta: { title: '合同管理' } },
      { path: 'ai/assistant', name: 'ai-assistant', component: () => import('@/views/workflows/AiAssistantView.vue'), meta: { title: 'AI 助手' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (!to.meta.public && !auth.isAuthed) {
    return '/login'
  }
  if (to.name === 'login' && auth.isAuthed) {
    return '/dashboard'
  }
  return true
})

export default router
