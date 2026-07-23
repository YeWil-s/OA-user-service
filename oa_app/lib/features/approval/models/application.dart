class Application {
  final int id;
  final String applicationNo;
  final int appType; // 1=请假, 2=加班, 3=外出
  final int? leaveType; // 1=年假,2=事假,3=病假,4=婚假,5=产假
  final DateTime startTime;
  final DateTime endTime;
  final double duration;
  final String reason;
  final String status; // 'draft','pending','approved','rejected','cancelled'
  final String? applicantName;
  final String? deptName;
  final String? currentApproverName;
  final DateTime createTime;
  final DateTime? latestActionTime;

  const Application({
    required this.id,
    required this.applicationNo,
    required this.appType,
    this.leaveType,
    required this.startTime,
    required this.endTime,
    required this.duration,
    required this.reason,
    required this.status,
    this.applicantName,
    this.deptName,
    this.currentApproverName,
    required this.createTime,
    this.latestActionTime,
  });

  String get appTypeLabel {
    switch (appType) {
      case 1:
        return '请假';
      case 2:
        return '加班';
      case 3:
        return '外出';
      default:
        return '未知';
    }
  }

  String get leaveTypeLabel {
    if (appType != 1) return appTypeLabel;
    switch (leaveType) {
      case 1:
        return '年假';
      case 2:
        return '事假';
      case 3:
        return '病假';
      case 4:
        return '婚假';
      case 5:
        return '产假';
      default:
        return '请假';
    }
  }

  String get statusLabel {
    switch (status) {
      case 'draft':
        return '草稿';
      case 'pending':
        return '审批中';
      case 'approved':
        return '已通过';
      case 'rejected':
        return '已驳回';
      case 'cancelled':
        return '已撤销';
      default:
        return status;
    }
  }

  static String _mapStatus(dynamic raw) {
    if (raw is String) return raw;
    if (raw is int) {
      switch (raw) {
        case 0:
          return 'draft';
        case 1:
          return 'pending';
        case 2:
          return 'approved';
        case 3:
          return 'rejected';
        case 4:
          return 'cancelled';
      }
    }
    return 'draft';
  }

  factory Application.fromJson(Map<String, dynamic> json) {
    return Application(
      id: json['id'] as int,
      applicationNo: json['applicationNo'] as String,
      appType: json['appType'] as int? ?? 1,
      leaveType: json['leaveType'] as int?,
      startTime: DateTime.parse(json['startTime'] as String),
      endTime: DateTime.parse(json['endTime'] as String),
      duration: (json['duration'] as num?)?.toDouble() ?? 0,
      reason: json['reason'] as String? ?? '',
      status: _mapStatus(json['status']),
      applicantName: json['applicantName'] as String?,
      deptName: json['deptName'] as String?,
      currentApproverName: json['currentApproverName'] as String?,
      createTime: json['createTime'] != null
          ? DateTime.parse(json['createTime'] as String)
          : DateTime.now(),
      latestActionTime: json['latestActionTime'] != null
          ? DateTime.parse(json['latestActionTime'] as String)
          : null,
    );
  }
}
