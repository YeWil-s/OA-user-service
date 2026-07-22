import { http } from './http'
import type {
  ApplicationDetail,
  ApplicationVO,
  ApprovalStats,
  Asset,
  AssetRecord,
  AttendanceRecord,
  AttendanceTrend,
  DeptMetric,
  DeptNode,
  Employee,
  EmployeeArchive,
  LoginUser,
  MenuNode,
  Message,
  Notice,
  PageResult,
  Position,
  PunchVO,
  Role,
  Shift,
  StaffChange,
  VisualOverview
} from './types'

export const authApi = {
  login: (payload: { username: string; password: string }) => http.post<unknown, LoginUser>('/api/user/login', payload),
  logout: () => http.post('/api/user/logout')
}

export const systemApi = {
  employees: (params: Record<string, unknown>) => http.get<unknown, PageResult<Employee>>('/api/user/employees', { params }),
  addEmployee: (payload: Partial<Employee>) => http.post('/api/user/employees', payload),
  updateEmployee: (id: number, payload: Partial<Employee>) => http.put(`/api/user/employees/${id}`, payload),
  deleteEmployee: (id: number) => http.delete(`/api/user/employees/${id}`),
  resetPassword: (id: number) => http.put(`/api/user/employees/${id}/reset-pwd`),
  depts: () => http.get<unknown, DeptNode[]>('/api/user/depts'),
  addDept: (payload: Partial<DeptNode>) => http.post('/api/user/depts', payload),
  updateDept: (id: number, payload: Partial<DeptNode>) => http.put(`/api/user/depts/${id}`, payload),
  deleteDept: (id: number) => http.delete(`/api/user/depts/${id}`),
  positions: (params: Record<string, unknown>) => http.get<unknown, PageResult<Position>>('/api/user/positions', { params }),
  addPosition: (payload: Partial<Position>) => http.post('/api/user/positions', payload),
  updatePosition: (id: number, payload: Partial<Position>) => http.put(`/api/user/positions/${id}`, payload),
  deletePosition: (id: number) => http.delete(`/api/user/positions/${id}`),
  roles: (params: Record<string, unknown>) => http.get<unknown, PageResult<Role>>('/api/user/roles', { params }),
  addRole: (payload: Partial<Role>) => http.post('/api/user/roles', payload),
  updateRole: (id: number, payload: Partial<Role>) => http.put(`/api/user/roles/${id}`, payload),
  deleteRole: (id: number) => http.delete(`/api/user/roles/${id}`),
  roleMenus: (id: number) => http.get<unknown, number[]>(`/api/user/roles/${id}/menus`),
  assignRoleMenus: (id: number, menuIds: number[]) => http.put(`/api/user/roles/${id}/menus`, menuIds),
  menus: () => http.get<unknown, MenuNode[]>('/api/user/menus'),
  addMenu: (payload: Partial<MenuNode>) => http.post('/api/user/menus', payload),
  updateMenu: (id: number, payload: Partial<MenuNode>) => http.put(`/api/user/menus/${id}`, payload),
  deleteMenu: (id: number) => http.delete(`/api/user/menus/${id}`)
}

export const noticeApi = {
  notices: (params: Record<string, unknown>) => http.get<unknown, PageResult<Notice>>('/api/notice/list', { params }),
  publish: (payload: Partial<Notice>) => http.post<unknown, number>('/api/notice/list', payload),
  update: (id: number, payload: Partial<Notice>) => http.put(`/api/notice/list/${id}`, payload),
  offline: (id: number) => http.delete(`/api/notice/list/${id}`),
  messages: (params: Record<string, unknown>) => http.get<unknown, PageResult<Message>>('/api/notice/messages', { params }),
  createMessage: (payload: Partial<Message>) => http.post<unknown, number>('/api/notice/messages', payload),
  markRead: (id: number) => http.put(`/api/notice/messages/${id}/read`),
  unreadCount: () => http.get<unknown, { noticeUnread: number; messageUnread: number; totalUnread: number }>('/api/notice/unread-count')
}

export const visualApi = {
  overview: (month?: string) => http.get<unknown, VisualOverview>('/api/visual/dashboard/overview', { params: { month } }),
  deptDistribution: (month?: string) => http.get<unknown, DeptMetric[]>('/api/visual/dashboard/dept-distribution', { params: { month } }),
  attendanceTrend: (endMonth?: string, months = 6) => http.get<unknown, AttendanceTrend[]>('/api/visual/dashboard/attendance-trend', { params: { endMonth, months } }),
  deptOvertime: (month?: string) => http.get<unknown, DeptMetric[]>('/api/visual/dashboard/dept-overtime', { params: { month } }),
  approvalStats: (month?: string) => http.get<unknown, ApprovalStats>('/api/visual/dashboard/approval-stats', { params: { month } }),
  approvalSpeed: (month?: string) => http.get<unknown, Array<{ deptId: number; deptName: string; totalApplications: number; avgApprovalHours: number }>>('/api/visual/dashboard/approval-speed', { params: { month } }),
  sync: (month?: string) => http.post('/api/visual/statistics/sync', null, { params: { month } })
}

export const assetApi = {
  // 资产管理
  assets: (params: Record<string, unknown>) => http.get<unknown, PageResult<Asset>>('/api/asset/assets', { params }),
  assetDetail: (id: number) => http.get<unknown, Asset>(`/api/asset/assets/${id}`),
  createAsset: (payload: Partial<Asset>) => http.post('/api/asset/assets', payload),
  updateAsset: (id: number, payload: Partial<Asset>) => http.put(`/api/asset/assets/${id}`, payload),
  scrapAsset: (id: number) => http.delete(`/api/asset/assets/${id}`),
  // 领用归还
  borrow: (payload: { assetId: number; userId: number; borrowDate?: string; expectReturnDate?: string }) => http.post<unknown, AssetRecord>('/api/asset/borrow', payload),
  returnAsset: (recordId: number) => http.put(`/api/asset/borrow/${recordId}/return`),
  records: (params: Record<string, unknown>) => http.get<unknown, PageResult<AssetRecord>>('/api/asset/records', { params }),
  // 人事变动
  staffChanges: (params: Record<string, unknown>) => http.get<unknown, PageResult<StaffChange>>('/api/asset/staff/changes', { params }),
  createStaffChange: (payload: Partial<StaffChange>) => http.post('/api/asset/staff/changes', payload),
  updateStaffChange: (id: number, payload: Partial<StaffChange>) => http.put(`/api/asset/staff/changes/${id}`, payload),
  deleteStaffChange: (id: number) => http.delete(`/api/asset/staff/changes/${id}`),
  // 合同管理
  contracts: (params: Record<string, unknown>) => http.get<unknown, PageResult<EmployeeArchive>>('/api/asset/contracts', { params }),
  expiringContracts: (days?: number) => http.get<unknown, PageResult<EmployeeArchive>>('/api/asset/contracts/expiring', { params: { days: days ?? 30 } })
}

export const attendanceApi = {
  shifts: (params: Record<string, unknown>) => http.get<unknown, PageResult<Shift>>('/api/attendance/shifts', { params }),
  punchIn: () => http.post<unknown, PunchVO>('/api/attendance/punch/in', {}),
  punchOut: () => http.post<unknown, PunchVO>('/api/attendance/punch/out', {}),
  myRecords: (params: Record<string, unknown>) => http.get<unknown, PageResult<AttendanceRecord>>('/api/attendance/records/mine', { params }),
  deptRecords: (params: Record<string, unknown>) => http.get<unknown, PageResult<AttendanceRecord>>('/api/attendance/records/dept', { params }),
  allRecords: (params: Record<string, unknown>) => http.get<unknown, PageResult<AttendanceRecord>>('/api/attendance/records/all', { params })
}

export const approvalApi = {
  submit: (payload: { appType: number; leaveType?: number; startTime: string; endTime: string; reason: string }) => http.post<unknown, ApplicationDetail>('/api/approval/applications', payload),
  myApplications: (params: Record<string, unknown>) => http.get<unknown, PageResult<ApplicationVO>>('/api/approval/applications', { params }),
  detail: (id: number) => http.get<unknown, ApplicationDetail>(`/api/approval/applications/${id}`),
  cancel: (id: number) => http.put(`/api/approval/applications/${id}/cancel`),
  pending: (params: Record<string, unknown>) => http.get<unknown, PageResult<ApplicationVO>>('/api/approval/pending', { params }),
  approve: (id: number, payload: { approved: boolean; comment: string }) => http.put(`/api/approval/pending/${id}/approve`, payload),
  processed: (params: Record<string, unknown>) => http.get<unknown, PageResult<ApplicationVO>>('/api/approval/processed', { params })
}
