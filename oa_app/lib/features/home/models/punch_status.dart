class PunchStatus {
  final DateTime? punchInTime;
  final DateTime? punchOutTime;
  final int punchType; // 1=现场, 2=外勤
  final String status; // 'not_punched', 'punched_in', 'completed'
  final String? statusLabel; // 后端返回的状态标签，如'正常','迟到','早退'
  final int lateMinutes;
  final int earlyMinutes;

  const PunchStatus({
    this.punchInTime,
    this.punchOutTime,
    this.punchType = 1,
    required this.status,
    this.statusLabel,
    this.lateMinutes = 0,
    this.earlyMinutes = 0,
  });

  bool get canPunchIn => status == 'not_punched';
  bool get canPunchOut => status == 'punched_in';
  bool get isCompleted => status == 'completed';

  factory PunchStatus.fromJson(Map<String, dynamic> json) {
    final status = json['status'] as String? ?? '';
    String mappedStatus;
    final hasPunchIn = json['punchInTime'] != null;
    final hasPunchOut = json['punchOutTime'] != null;

    if (hasPunchIn && hasPunchOut) {
      mappedStatus = 'completed';
    } else if (hasPunchIn) {
      mappedStatus = 'punched_in';
    } else {
      mappedStatus = 'not_punched';
    }

    return PunchStatus(
      punchInTime: json['punchInTime'] != null
          ? DateTime.parse(json['punchInTime'] as String)
          : null,
      punchOutTime: json['punchOutTime'] != null
          ? DateTime.parse(json['punchOutTime'] as String)
          : null,
      punchType: json['punchInType'] as int? ?? 1,
      status: mappedStatus,
      statusLabel: json['statusLabel'] as String? ?? status,
      lateMinutes: json['lateMinutes'] as int? ?? 0,
      earlyMinutes: json['earlyMinutes'] as int? ?? 0,
    );
  }

  /// 从打卡接口的 PunchVO 响应创建（仅包含当次打卡信息）
  factory PunchStatus.fromPunchResponse(Map<String, dynamic> data) {
    final msg = data['message'] as String? ?? '';
    final isPunchIn = msg.contains('上班') || msg.contains('签到') ||
        !msg.contains('下班') && !msg.contains('签退');
    return PunchStatus(
      punchInTime: data['punchTime'] != null && isPunchIn
          ? DateTime.parse(data['punchTime'] as String)
          : null,
      punchOutTime: data['punchTime'] != null && !isPunchIn
          ? DateTime.parse(data['punchTime'] as String)
          : null,
      status: isPunchIn ? 'punched_in' : 'completed',
      statusLabel: data['statusLabel'] as String?,
      lateMinutes: data['lateMinutes'] as int? ?? 0,
      earlyMinutes: data['earlyMinutes'] as int? ?? 0,
    );
  }
}
