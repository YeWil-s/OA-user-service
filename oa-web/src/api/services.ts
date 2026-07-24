import { http } from './http'
import type {
  ApprovalApplication,
  ApprovalStats,
  Asset,
  AssetRecord,
  AttendanceRecord,
  AttendanceTrend,
  Contract,
  DeptMetric,
  DeptNode,
  Employee,
  LoginUser,
  MenuNode,
  Message,
  Notice,
  PageResult,
  Position,
  RouterVO,
  PunchResult,
  Role,
  Schedule,
  Shift,
  StaffArchive,
  StaffChange,
  UserShift,
  VisualOverview
} from './types'

export type QueryParams = Record<string, unknown>

function cleanParams(params: QueryParams = {}) {
  return Object.fromEntries(
    Object.entries(params).filter(([, value]) => value !== '' && value !== undefined && value !== null)
  )
}

export const authApi = {
  login: (payload: { username: string; password: string }) => http.post<unknown, LoginUser>('/api/user/login', payload),
  logout: () => http.post('/api/user/logout'),
  current: () => http.get<unknown, LoginUser | null>('/api/user/current')
}

export const systemApi = {
  employees: (params: QueryParams) => http.get<unknown, PageResult<Employee>>('/api/user/employees', { params: cleanParams(params) }),
  addEmployee: (payload: Partial<Employee>) => http.post('/api/user/employees', payload),
  updateEmployee: (id: number, payload: Partial<Employee>) => http.put(`/api/user/employees/${id}`, payload),
  deleteEmployee: (id: number) => http.delete(`/api/user/employees/${id}`),
  resetPassword: (id: number) => http.put(`/api/user/employees/${id}/reset-pwd`),
  updatePassword: (id: number, payload: { oldPassword: string; newPassword: string }) => http.put(`/api/user/employees/${id}/update-pwd`, payload),
  depts: () => http.get<unknown, DeptNode[]>('/api/user/depts'),
  addDept: (payload: Partial<DeptNode>) => http.post('/api/user/depts', payload),
  updateDept: (id: number, payload: Partial<DeptNode>) => http.put(`/api/user/depts/${id}`, payload),
  deleteDept: (id: number) => http.delete(`/api/user/depts/${id}`),
  positions: (params: QueryParams) => http.get<unknown, PageResult<Position>>('/api/user/positions', { params: cleanParams(params) }),
  addPosition: (payload: Partial<Position & { roleIds?: number[] }>) => http.post('/api/user/positions', payload),
  updatePosition: (id: number, payload: Partial<Position & { roleIds?: number[] }>) => http.put(`/api/user/positions/${id}`, payload),
  deletePosition: (id: number) => http.delete(`/api/user/positions/${id}`),
  positionRoles: (id: number) => http.get<unknown, number[]>(`/api/user/positions/${id}/roles`),
  roles: (params: QueryParams) => http.get<unknown, PageResult<Role>>('/api/user/roles', { params: cleanParams(params) }),
  addRole: (payload: Partial<Role>) => http.post('/api/user/roles', payload),
  updateRole: (id: number, payload: Partial<Role>) => http.put(`/api/user/roles/${id}`, payload),
  deleteRole: (id: number) => http.delete(`/api/user/roles/${id}`),
  roleMenus: (id: number) => http.get<unknown, number[]>(`/api/user/roles/${id}/menus`),
  assignRoleMenus: (id: number, menuIds: number[]) => http.put(`/api/user/roles/${id}/menus`, menuIds),
  menus: () => http.get<unknown, MenuNode[]>('/api/user/menus'),
  addMenu: (payload: Partial<MenuNode>) => http.post('/api/user/menus', payload),
  updateMenu: (id: number, payload: Partial<MenuNode>) => http.put(`/api/user/menus/${id}`, payload),
  deleteMenu: (id: number) => http.delete(`/api/user/menus/${id}`),
  routers: () => http.get<unknown, RouterVO[]>('/api/user/menus/routers')
}

export const noticeApi = {
  notices: (params: QueryParams) => http.get<unknown, PageResult<Notice>>('/api/notice/list', { params: cleanParams(params) }),
  detail: (id: number) => http.get<unknown, Notice>(`/api/notice/list/${id}`),
  publish: (payload: Partial<Notice>) => http.post<unknown, number>('/api/notice/list', payload),
  update: (id: number, payload: Partial<Notice>) => http.put(`/api/notice/list/${id}`, payload),
  offline: (id: number) => http.delete(`/api/notice/list/${id}`),
  messages: (params: QueryParams) => http.get<unknown, PageResult<Message>>('/api/notice/messages', { params: cleanParams(params) }),
  createMessage: (payload: Partial<Message>) => http.post<unknown, number>('/api/notice/messages', payload),
  markRead: (id: number) => http.put(`/api/notice/messages/${id}/read`),
  unreadCount: () => http.get<unknown, { noticeUnread: number; messageUnread: number; totalUnread: number }>('/api/notice/unread-count')
}

export const visualApi = {
  overview: (month?: string) => http.get<unknown, VisualOverview>('/api/visual/dashboard/overview', { params: cleanParams({ month }) }),
  deptDistribution: (month?: string) => http.get<unknown, DeptMetric[]>('/api/visual/dashboard/dept-distribution', { params: cleanParams({ month }) }),
  attendanceTrend: (endMonth?: string, months = 6) => http.get<unknown, AttendanceTrend[]>('/api/visual/dashboard/attendance-trend', { params: cleanParams({ endMonth, months }) }),
  deptOvertime: (month?: string) => http.get<unknown, DeptMetric[]>('/api/visual/dashboard/dept-overtime', { params: cleanParams({ month }) }),
  approvalStats: (month?: string) => http.get<unknown, ApprovalStats>('/api/visual/dashboard/approval-stats', { params: cleanParams({ month }) }),
  approvalSpeed: (month?: string) => http.get<unknown, Array<{ deptId: number; deptName: string; totalApplications: number; avgApprovalHours: number }>>('/api/visual/dashboard/approval-speed', { params: cleanParams({ month }) }),
  sync: (month?: string) => http.post('/api/visual/statistics/sync', null, { params: cleanParams({ month }) })
}

export const attendanceApi = {
  shifts: (params: QueryParams) => http.get<unknown, PageResult<Shift>>('/api/attendance/shifts', { params: cleanParams(params) }),
  shiftDetail: (id: number) => http.get<unknown, Shift>(`/api/attendance/shifts/${id}`),
  addShift: (payload: Partial<Shift>) => http.post<unknown, Shift>('/api/attendance/shifts', payload),
  updateShift: (id: number, payload: Partial<Shift>) => http.put<unknown, Shift>(`/api/attendance/shifts/${id}`, payload),
  deleteShift: (id: number) => http.delete(`/api/attendance/shifts/${id}`),
  assignShift: (payload: { userId: number; shiftId: number }) => http.post('/api/attendance/user-shifts', payload),
  punchIn: (payload?: { punchType?: number; deviceInfo?: string; location?: string; latitude?: number; longitude?: number }) => http.post<unknown, PunchResult>('/api/attendance/punch/in', payload || {}),
  punchOut: (payload?: { punchType?: number; deviceInfo?: string; location?: string; latitude?: number; longitude?: number }) => http.post<unknown, PunchResult>('/api/attendance/punch/out', payload || {}),
  myRecords: (params: QueryParams) => http.get<unknown, PageResult<AttendanceRecord>>('/api/attendance/records/mine', { params: cleanParams(params) }),
  deptRecords: (params: QueryParams) => http.get<unknown, PageResult<AttendanceRecord>>('/api/attendance/records/dept', { params: cleanParams(params) }),
  allRecords: (params: QueryParams) => http.get<unknown, PageResult<AttendanceRecord>>('/api/attendance/records/all', { params: cleanParams(params) }),
  mySchedules: (startDate: string, endDate: string) => http.get<unknown, Schedule[]>('/api/attendance/schedules/mine', { params: { startDate, endDate } }),
  deptSchedules: (deptId: number, startDate: string, endDate: string) => http.get<unknown, Schedule[]>('/api/attendance/schedules/dept', { params: { deptId, startDate, endDate } }),
  userSchedules: (userId: number, startDate: string, endDate: string) => http.get<unknown, Schedule[]>(`/api/attendance/schedules/user/${userId}`, { params: { startDate, endDate } }),
  batchSchedule: (items: Array<{ userId: number; scheduleDate: string; shiftId: number }>) => http.post('/api/attendance/schedules', { items }),
  allUserShifts: () => http.get<unknown, UserShift[]>('/api/attendance/user-shifts/all')
}

export const approvalApi = {
  submitApplication: (payload: Partial<ApprovalApplication>) => http.post<unknown, ApprovalApplication>('/api/approval/applications', payload),
  applications: (params: QueryParams) => http.get<unknown, PageResult<ApprovalApplication>>('/api/approval/applications', { params: cleanParams(params) }),
  detail: (id: number) => http.get<unknown, ApprovalApplication>(`/api/approval/applications/${id}`),
  cancel: (id: number) => http.put(`/api/approval/applications/${id}/cancel`),
  pending: (params: QueryParams) => http.get<unknown, PageResult<ApprovalApplication>>('/api/approval/pending', { params: cleanParams(params) }),
  approve: (id: number, payload: { approved: boolean; comment: string }) => http.put(`/api/approval/pending/${id}/approve`, payload),
  processed: (params: QueryParams) => http.get<unknown, PageResult<ApprovalApplication>>('/api/approval/processed', { params: cleanParams(params) })
}

export const assetApi = {
  assets: (params: QueryParams) => http.get<unknown, PageResult<Asset>>('/api/asset/assets', { params: cleanParams(params) }),
  assetDetail: (id: number) => http.get<unknown, Asset>(`/api/asset/assets/${id}`),
  addAsset: (payload: Partial<Asset>) => http.post('/api/asset/assets', payload),
  updateAsset: (id: number, payload: Partial<Asset>) => http.put(`/api/asset/assets/${id}`, payload),
  deleteAsset: (id: number) => http.delete(`/api/asset/assets/${id}`),
  borrow: (payload: { assetId: number; userId: number }) => http.post('/api/asset/borrow', payload),
  returnBorrow: (recordId: number) => http.put(`/api/asset/borrow/${recordId}/return`),
  records: (params: QueryParams) => http.get<unknown, PageResult<AssetRecord>>('/api/asset/records', { params: cleanParams(params) }),
  staffChanges: (params: QueryParams) => http.get<unknown, PageResult<StaffChange>>('/api/asset/staff/changes', { params: cleanParams(params) }),
  addStaffChange: (payload: Partial<StaffChange>) => http.post('/api/asset/staff/changes', payload),
  updateStaffChange: (id: number, payload: Partial<StaffChange>) => http.put(`/api/asset/staff/changes/${id}`, payload),
  deleteStaffChange: (id: number) => http.delete(`/api/asset/staff/changes/${id}`),
  archive: (userId: number) => http.get<unknown, StaffArchive>(`/api/asset/staff/archive/${userId}`),
  saveArchive: (userId: number, payload: StaffArchive) => http.put(`/api/asset/staff/archive/${userId}`, payload),
  contracts: (params: QueryParams) => http.get<unknown, PageResult<Contract>>('/api/asset/contracts', { params: cleanParams(params) }),
  expiringContracts: (params: QueryParams) => http.get<unknown, PageResult<Contract> | Contract[]>('/api/asset/contracts/expiring', { params: cleanParams(params) })
}