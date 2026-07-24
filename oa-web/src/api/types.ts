export interface PageResult<T> {
  records?: T[]
  list?: T[]
  total?: number
  size?: number
  current?: number
  pageNum?: number
  pageSize?: number
}

export interface LoginUser {
  accessToken: string
  userId: number
  username: string
  realName: string
  avatarUrl?: string | null
  deptId?: number | null
  deptName?: string | null
  positionId?: number | null
  positionName?: string | null
  phone?: string | null
  email?: string | null
  gender?: number | null
  entryDate?: string | null
  roles: string[]
  roleNames?: string[] | null
  permissions: string[]
}

export interface Employee {
  id: number
  username: string
  password?: string
  realName: string
  phone?: string
  email?: string
  gender?: number
  deptId?: number
  deptName?: string
  positionId?: number
  positionName?: string
  entryDate?: string
  status?: number
}

export interface DeptNode {
  id: number
  parentId?: number
  deptName: string
  deptCode?: string
  leaderId?: number
  sortOrder?: number
  status?: number
  children?: DeptNode[]
}

export interface Position {
  id: number
  positionName: string
  positionCode: string
  deptId?: number
  deptName?: string
  sortOrder?: number
  status?: number
}

export interface Role {
  id: number
  roleName: string
  roleCode: string
  roleDesc?: string
  dataScope?: number
  sortOrder?: number
  status?: number
}

export interface RouterVO {
  name: string
  path: string
  component?: string
  icon?: string
  menuType: number
  meta: { title: string; icon?: string; hidden: boolean }
  children?: RouterVO[]
}

export interface MenuNode {
  id: number
  parentId?: number
  menuName: string
  menuType?: number
  path?: string
  component?: string
  permissionCode?: string
  icon?: string
  sortOrder?: number
  visible?: number
  status?: number
  children?: MenuNode[]
}

export interface Notice {
  id: number
  title: string
  content?: string | null
  summary?: string | null
  publisherId?: number
  noticeType?: number
  targetType?: number
  targetIds?: number[]
  startTime?: string | null
  endTime?: string | null
  status?: number
  read?: boolean
  createTime?: string
  updateTime?: string
}

export interface Message {
  id: number
  userId: number
  title: string
  content?: string
  msgType: number
  relatedId?: number
  read?: boolean
  isRead?: number
  createTime?: string
}

export interface VisualOverview {
  statMonth: string
  totalEmployees: number
  newHires: number
  resignations: number
  attendanceRate: number
  overtimeHours: number
  totalApplications: number
  approvedCount: number
  rejectedCount: number
  pendingCount: number
  approvalPassRate: number
  avgApprovalHours: number
  refreshTime: string
}

export interface DeptMetric {
  deptId: number
  deptName: string
  value: number
  rate: number
  unit: string
}

export interface AttendanceTrend {
  statMonth: string
  normalCount: number
  lateCount: number
  earlyCount: number
  absentCount: number
  leaveCount: number
  overtimeHours: number
  attendanceRate: number
}

export interface ApprovalStats {
  statMonth: string
  totalApplications: number
  approvedCount: number
  rejectedCount: number
  pendingCount: number
  passRate: number
  rejectRate: number
  pendingRate: number
  statusDistribution?: Array<{ name: string; value: number; unit?: string }>
}

export interface Shift {
  id: number
  shiftName: string
  startTime: string
  endTime: string
  flexStart?: string
  flexEnd?: string
  status?: number
  createTime?: string
  updateTime?: string
}

export interface Schedule {
  id: number
  userId: number
  userName: string
  deptName: string
  scheduleDate: string
  shiftId: number
  shiftName: string
  startTime: string
  endTime: string
  status: number
  statusText?: string
  overtimeHours?: number
}

export interface UserShift {
  userId: number
  userName: string
  deptName: string
  shiftId: number
  shiftName: string
  startTime: string
  endTime: string
}

export interface AttendanceRecord {
  id: number
  userId: number
  realName?: string
  deptId?: number
  deptName?: string
  shiftId?: number
  shiftName?: string
  recordDate: string
  punchInTime?: string | null
  punchOutTime?: string | null
  lateMinutes?: number
  earlyMinutes?: number
  workHours?: number
  punchType?: number
  deviceInfo?: string
  location?: string
  statusLabel?: string
}

export interface PunchResult {
  message: string
  userId: number
  recordDate: string
  punchTime: string
  shiftId?: number
  shiftName?: string
  statusLabel?: string
  lateMinutes?: number
  earlyMinutes?: number
}

export interface ApprovalApplication {
  id: number
  applicationNo: string
  userId: number
  applicantName: string
  deptId?: number
  deptName?: string
  appType: number
  appTypeText?: string
  leaveType?: number
  leaveTypeText?: string
  startTime?: string
  endTime?: string
  duration?: number
  targetDeptId?: number
  targetDeptName?: string
  targetPositionId?: number
  targetPositionName?: string
  assetId?: number
  assetName?: string
  assetCode?: string
  expectReturnDate?: string
  reason?: string
  status: number
  statusText?: string
  currentApproverId?: number
  currentApproverName?: string
  latestAction?: number | null
  latestActionText?: string | null
  latestActionTime?: string | null
  createTime?: string
  attachments?: string[]
  timeline?: Array<{
    id: number
    approverId: number
    approverName: string
    action: number
    actionText: string
    comment?: string
    actionTime: string
  }>
}

export interface Asset {
  id: number
  assetCode?: string
  code?: string
  assetName?: string
  name?: string
  category?: number | string
  categoryName?: string
  brand?: string
  model?: string
  userId?: number
  ownerId?: number
  ownerName?: string
  userName?: string
  status?: number
  purchaseDate?: string
  purchasePrice?: number
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface AssetRecord {
  id: number
  assetId: number
  assetCode?: string
  assetName?: string
  userId: number
  userName?: string
  borrowTime?: string
  returnTime?: string
  status?: number
}

export interface StaffChange {
  id: number
  userId: number
  realName?: string
  userName?: string
  changeType: number
  changeDate?: string
  fromDeptId?: number
  fromDeptName?: string
  toDeptId?: number
  toDeptName?: string
  fromPositionId?: number
  fromPositionName?: string
  toPositionId?: number
  toPositionName?: string
  remark?: string
  createTime?: string
  updateTime?: string
}

export interface StaffArchive {
  userId: number
  education?: number
  nativePlace?: string
  address?: string
  emergencyContact?: string
  emergencyPhone?: string
  remark?: string
}

export interface Contract {
  id: number
  userId: number
  realName?: string
  userName?: string
  contractNo?: string
  contractType?: number | string
  startDate?: string
  endDate?: string
  status?: number | string
  createTime?: string
  updateTime?: string
}