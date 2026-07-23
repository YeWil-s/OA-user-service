class ApiConstants {
  ApiConstants._();

  // ---- 切换开关：后端服务未上线时使用 stub mock 数据 ----
  static const bool useStubServices = false;

  // ---- 网关地址 ----
  static const String baseUrl = 'http://shuode.nat100.top';
  static const String aiBaseUrl = 'http://shuode.nat100.top';

  // ---- 超时配置 ----
  static const Duration connectTimeout = Duration(seconds: 10);
  static const Duration receiveTimeout = Duration(seconds: 30);
  static const Duration sendTimeout = Duration(seconds: 10);

  // ---- 分页默认值 ----
  static const int defaultPageSize = 20;

  // ==================== user-service ====================
  static const String login = '/api/user/login';
  static const String currentUser = '/api/user/current';
  static const String employees = '/api/user/employees';
  static String employeeDetail(int id) => '/api/user/employees/$id';
  static String updatePassword(int id) => '/api/user/employees/$id/update-pwd';
  static String resetPassword(int id) => '/api/user/employees/$id/reset-pwd';
  static String employeeRoles(int id) => '/api/user/employees/$id/roles';
  static const String depts = '/api/user/depts';
  static String deptDetail(int id) => '/api/user/depts/$id';
  static const String positions = '/api/user/positions';
  static String positionDetail(int id) => '/api/user/positions/$id';
  static const String roles = '/api/user/roles';
  static String roleDetail(int id) => '/api/user/roles/$id';
  static String roleMenus(int id) => '/api/user/roles/$id/menus';
  static const String menus = '/api/user/menus';
  static String menuDetail(int id) => '/api/user/menus/$id';
  static const String menusRouters = '/api/user/menus/routers';

  // ==================== attendance-service ====================
  static const String punchIn = '/api/attendance/punch/in';
  static const String punchOut = '/api/attendance/punch/out';
  static const String attendanceRecordsMine = '/api/attendance/records/mine';
  static const String attendanceRecordsDept = '/api/attendance/records/dept';
  static const String attendanceRecordsAll = '/api/attendance/records/all';
  static const String attendanceSummaryMine = '/api/attendance/records/mine';
  static const String attendanceShifts = '/api/attendance/shifts';
  static String attendanceShiftDetail(int id) => '/api/attendance/shifts/$id';
  static const String attendanceUserShifts = '/api/attendance/user-shifts';

  // ==================== approval-service ====================
  static const String approvalApplications = '/api/approval/applications';
  static const String approvalApplicationsAll = '/api/approval/applications/all';
  static String approvalApplicationDetail(int id) => '/api/approval/applications/$id';
  static String approvalCancel(int id) => '/api/approval/applications/$id/cancel';
  static const String approvalPending = '/api/approval/pending';
  static const String approvalPendingAll = '/api/approval/pending/all';
  static String approvalApprove(int id) => '/api/approval/pending/$id/approve';
  static const String approvalProcessed = '/api/approval/processed';

  // ==================== notice-service ====================
  static const String noticeList = '/api/notice/list';
  static String noticeDetail(int id) => '/api/notice/list/$id';
  static const String noticeUnreadCount = '/api/notice/unread-count';
  static const String noticeMessages = '/api/notice/messages';
  static String noticeMessageRead(int id) => '/api/notice/messages/$id/read';

  // ==================== asset-service ====================
  static const String assets = '/api/asset/assets';
  static String assetDetail(int id) => '/api/asset/assets/$id';
  static const String assetBorrow = '/api/asset/borrow';
  static String assetReturn(int id) => '/api/asset/borrow/$id/return';
  static const String assetRecords = '/api/asset/records';
  static String staffArchive(int userId) => '/api/asset/staff/archive/$userId';
  static const String staffChanges = '/api/asset/staff/changes';
  static String staffChangeDetail(int id) => '/api/asset/staff/changes/$id';
  static const String contracts = '/api/asset/contracts';
  static const String contractsExpiring = '/api/asset/contracts/expiring';

  // ==================== ai-service ====================
  static const String aiChatStream = '/api/ai/chat/stream';
  static const String aiAgentStream = '/api/ai/agent/stream';
  static const String aiConversations = '/api/ai/conversations';
  static String aiConversationDetail(String sessionId) => '/api/ai/conversations/$sessionId';
  static const String aiKnowledge = '/api/ai/knowledge';
  static String aiKnowledgeDetail(int id) => '/api/ai/knowledge/$id';
  static const String aiKnowledgeTags = '/api/ai/knowledge/tags';
  static String aiKnowledgeTagDetail(int id) => '/api/ai/knowledge/tags/$id';
  static const String aiKnowledgeReindex = '/api/ai/knowledge/reindex';

  // ==================== data-visual-service ====================
  static const String visualDashboardOverview = '/api/visual/dashboard/overview';
  static const String visualDashboardDeptDistribution = '/api/visual/dashboard/dept-distribution';
  static const String visualDashboardAttendanceTrend = '/api/visual/dashboard/attendance-trend';
  static const String visualDashboardDeptOvertime = '/api/visual/dashboard/dept-overtime';
  static const String visualDashboardApprovalStats = '/api/visual/dashboard/approval-stats';
  static const String visualDashboardApprovalSpeed = '/api/visual/dashboard/approval-speed';
  static const String visualScreenAttendance = '/api/visual/screen/attendance';
  static const String visualScreenHr = '/api/visual/screen/hr';
  static const String visualScreenApproval = '/api/visual/screen/approval';
  static const String visualStatisticsSync = '/api/visual/statistics/sync';
  static const String visualStatisticsSyncLastMonth = '/api/visual/statistics/sync/last-month';
}
