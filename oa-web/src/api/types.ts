export interface PageResult<T> {
  records?: T[]
  list?: T[]
  total: number
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
  avatarUrl?: string
  roles: string[]
  permissions: string[]
}

export interface Employee {
  id: number
  username: string
  realName: string
  phone?: string
  email?: string
  gender?: number
  deptId?: number
  positionId?: number
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
  content?: string
  publisherId?: number
  noticeType?: number
  targetType?: number
  targetIds?: string
  startTime?: string
  endTime?: string
  status?: number
  createTime?: string
  read?: boolean
}

export interface Message {
  id: number
  userId: number
  title: string
  content?: string
  msgType: number
  relatedId?: number
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
}

// ---- asset-service ----
export interface Asset {
  id: number
  assetName: string
  assetCode: string
  category: number
  model?: string
  purchaseDate?: string
  purchasePrice?: number
  status: number
  createTime?: string
}

export interface AssetRecord {
  id: number
  assetId: number
  userId: number
  borrowDate?: string
  expectReturnDate?: string
  actualReturnDate?: string
  status: number
  createTime?: string
}

export interface StaffChange {
  id: number
  userId: number
  changeType: number
  beforeDept?: number
  afterDept?: number
  beforePosition?: number
  afterPosition?: number
  changeDate?: string
  remark?: string
  createTime?: string
}

export interface EmployeeArchive {
  id: number
  userId: number
  idCard?: string
  education?: number
  major?: string
  graduateSchool?: string
  address?: string
  emergencyContact?: string
  emergencyPhone?: string
  contractStart?: string
  contractEnd?: string
  createTime?: string
}
