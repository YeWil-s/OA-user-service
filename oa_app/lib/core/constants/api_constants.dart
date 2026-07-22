class ApiConstants {
  ApiConstants._();

  // ---- 切换开关：后端服务未上线时使用 stub mock 数据 ----
  static const bool useStubServices = false;

  // ---- 网关地址 ----
  // 模拟器用 10.0.2.2，真机用电脑 Wi-Fi IP
  static const String baseUrl = 'http://10.200.173.212:8081';

  // AI 服务独立端口
  static const String aiBaseUrl = 'http://10.200.173.212:8087';

  // ---- 超时配置 ----
  static const Duration connectTimeout = Duration(seconds: 10);
  static const Duration receiveTimeout = Duration(seconds: 30);
  static const Duration sendTimeout = Duration(seconds: 10);

  // ---- 分页默认值 ----
  static const int defaultPageSize = 20;

  // ---- user-service ----
  static const String login = '/api/user/login';
  static const String currentUser = '/api/user/current';
  static const String employees = '/api/user/employees';
  static String employeeDetail(int id) => '/api/user/employees/$id';
  static String updatePassword(int id) => '/api/user/employees/$id/update-pwd';
  static String resetPassword(int id) => '/api/user/employees/$id/reset-pwd';
  static const String depts = '/api/user/depts';
  static const String positions = '/api/user/positions';
  static const String roles = '/api/user/roles';
  static const String menus = '/api/user/menus';
  static const String menusRouters = '/api/user/menus/routers';

  // ---- ai-service ----
  static const String aiChatStream = '/api/ai/chat/stream';
  static const String aiAgentStream = '/api/ai/agent/stream';
  static const String aiConversations = '/api/ai/conversations';
  static const String aiKnowledge = '/api/ai/knowledge';
  static const String aiKnowledgeTags = '/api/ai/knowledge/tags';
  static const String aiKnowledgeReindex = '/api/ai/knowledge/reindex';

  // ---- attendance-service (stub) ----
  static const String punchIn = '/api/attendance/punch/in';
  static const String punchOut = '/api/attendance/punch/out';
  static const String attendanceRecordsMine = '/api/attendance/records/mine';
  static const String attendanceSummaryMine = '/api/attendance/summary/mine';

  // ---- approval-service (stub) ----
  static const String approvalApplications = '/api/approval/applications';
  static String approvalApplicationDetail(int id) => '/api/approval/applications/$id';
  static String approvalCancel(int id) => '/api/approval/applications/$id/cancel';
  static const String approvalPending = '/api/approval/pending';
  static String approvalApprove(int id) => '/api/approval/pending/$id/approve';

  // ---- notice-service (stub) ----
  static const String noticeMessages = '/api/notice/messages';
  static String noticeMessageRead(int id) => '/api/notice/messages/$id/read';
  static const String noticeUnreadCount = '/api/notice/unread-count';
}
