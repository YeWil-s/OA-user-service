import type {
  ApprovalStats,
  AttendanceTrend,
  DeptMetric,
  DeptNode,
  Employee,
  MenuNode,
  Message,
  Notice,
  PageResult,
  Position,
  Role,
  VisualOverview
} from './types'

export const mockUser = {
  accessToken: 'mock-token',
  userId: 1,
  username: 'admin',
  realName: '系统管理员',
  roles: ['ADMIN'],
  permissions: ['*:*:*']
}

export const mockDepts: DeptNode[] = [
  {
    id: 1,
    parentId: 0,
    deptName: '总公司',
    deptCode: 'ROOT',
    status: 1,
    children: [
      { id: 2, parentId: 1, deptName: '技术部', deptCode: 'TECH', status: 1 },
      { id: 3, parentId: 1, deptName: '人事部', deptCode: 'HR', status: 1 },
      { id: 4, parentId: 1, deptName: '财务部', deptCode: 'FINANCE', status: 1 }
    ]
  }
]

export const mockEmployees: PageResult<Employee> = {
  records: [
    { id: 1, username: 'admin', realName: '系统管理员', phone: '13800000000', deptId: 2, positionId: 1, entryDate: '2026-07-01', status: 1 },
    { id: 2, username: 'E1002', realName: '李明', phone: '13900000002', deptId: 2, positionId: 2, entryDate: '2026-07-08', status: 1 },
    { id: 3, username: 'E1003', realName: '周宁', phone: '13700000003', deptId: 3, positionId: 3, entryDate: '2026-06-18', status: 1 }
  ],
  total: 3,
  current: 1,
  size: 10
}

export const mockPositions: PageResult<Position> = {
  records: [
    { id: 1, positionName: '系统管理员', positionCode: 'ADMIN', deptId: 2, status: 1 },
    { id: 2, positionName: '研发工程师', positionCode: 'RD', deptId: 2, status: 1 },
    { id: 3, positionName: 'HR 专员', positionCode: 'HR', deptId: 3, status: 1 }
  ],
  total: 3
}

export const mockRoles: PageResult<Role> = {
  records: [
    { id: 1, roleName: '超级管理员', roleCode: 'ADMIN', roleDesc: '系统全部权限', dataScope: 0, status: 1 },
    { id: 2, roleName: '部门主管', roleCode: 'LEADER', roleDesc: '部门审批与统计', dataScope: 2, status: 1 },
    { id: 3, roleName: '普通员工', roleCode: 'EMPLOYEE', roleDesc: '个人办公权限', dataScope: 3, status: 1 }
  ],
  total: 3
}

export const mockMenus: MenuNode[] = [
  { id: 1, parentId: 0, menuName: '系统管理', menuType: 1, path: '/system', status: 1, children: [
    { id: 11, parentId: 1, menuName: '员工管理', menuType: 2, path: '/system/employee', permissionCode: 'user:employee:list', status: 1 },
    { id: 12, parentId: 1, menuName: '角色管理', menuType: 2, path: '/system/role', permissionCode: 'user:role:list', status: 1 }
  ] },
  { id: 2, parentId: 0, menuName: '公告通知', menuType: 1, path: '/notice', status: 1 }
]

export const mockNotices: PageResult<Notice> = {
  list: [
    { id: 1, title: '7 月考勤规则调整通知', noticeType: 1, targetType: 1, status: 1, createTime: '2026-07-21 09:00:00' },
    { id: 2, title: '研发中心周会安排', noticeType: 2, targetType: 2, status: 1, createTime: '2026-07-20 18:30:00' }
  ],
  total: 2
}

export const mockMessages: PageResult<Message> = {
  list: [
    { id: 1, userId: 1, title: '请假申请已通过', content: '你的年假申请已审批通过。', msgType: 1, isRead: 0, createTime: '2026-07-21 10:20:00' },
    { id: 2, userId: 1, title: '今日打卡提醒', content: '下班打卡时间即将开始。', msgType: 2, isRead: 1, createTime: '2026-07-21 17:45:00' }
  ],
  total: 2
}

export const mockOverview: VisualOverview = {
  statMonth: '2026-07',
  totalEmployees: 126,
  newHires: 8,
  resignations: 2,
  attendanceRate: 96.5,
  overtimeHours: 328.5,
  totalApplications: 86,
  approvedCount: 70,
  rejectedCount: 6,
  pendingCount: 10,
  approvalPassRate: 81.4,
  avgApprovalHours: 5.6,
  refreshTime: '2026-07-21T16:30:00'
}

export const mockDeptDistribution: DeptMetric[] = [
  { deptId: 2, deptName: '技术部', value: 48, rate: 38.1, unit: '人' },
  { deptId: 3, deptName: '人事部', value: 24, rate: 19.05, unit: '人' },
  { deptId: 4, deptName: '财务部', value: 18, rate: 14.29, unit: '人' },
  { deptId: 5, deptName: '运营部', value: 36, rate: 28.57, unit: '人' }
]

export const mockAttendanceTrend: AttendanceTrend[] = ['2026-02', '2026-03', '2026-04', '2026-05', '2026-06', '2026-07'].map((month, index) => ({
  statMonth: month,
  normalCount: 1800 + index * 38,
  lateCount: 42 - index * 3,
  earlyCount: 18 + index,
  absentCount: 6 - Math.min(index, 4),
  leaveCount: 22 + index * 2,
  overtimeHours: 210 + index * 21,
  attendanceRate: 94.2 + index * 0.46
}))

export const mockApprovalStats: ApprovalStats = {
  statMonth: '2026-07',
  totalApplications: 86,
  approvedCount: 70,
  rejectedCount: 6,
  pendingCount: 10,
  passRate: 81.4,
  rejectRate: 6.98,
  pendingRate: 11.63
}

export const workflowRows = {
  attendanceRecords: [
    { id: 1, name: '李明', dept: '技术部', date: '2026-07-21', punchIn: '08:57', punchOut: '18:34', status: '正常' },
    { id: 2, name: '周宁', dept: '人事部', date: '2026-07-21', punchIn: '09:18', punchOut: '-', status: '迟到' }
  ],
  shifts: [
    { id: 1, name: '标准班', start: '09:00', end: '18:00', flex: '15 分钟', status: '启用' },
    { id: 2, name: '弹性班', start: '10:00', end: '19:00', flex: '60 分钟', status: '启用' }
  ],
  applications: [
    { id: 1, no: 'LV20260721001', type: '请假', duration: '1 天', status: '审批中', time: '2026-07-21 09:30' },
    { id: 2, no: 'OT20260720002', type: '加班', duration: '3 小时', status: '已通过', time: '2026-07-20 18:10' }
  ],
  assets: [
    { id: 1, code: 'NB-2026-001', name: 'ThinkPad T14', category: '电子设备', owner: '李明', status: '已领用' },
    { id: 2, code: 'OF-2026-014', name: '人体工学椅', category: '办公用品', owner: '-', status: '可领用' }
  ]
}
