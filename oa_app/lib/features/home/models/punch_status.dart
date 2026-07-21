class PunchStatus {
  final DateTime? punchInTime;
  final DateTime? punchOutTime;
  final int punchType; // 1=现场, 2=外勤
  final String status; // 'not_punched', 'punched_in', 'completed'

  const PunchStatus({
    this.punchInTime,
    this.punchOutTime,
    this.punchType = 1,
    required this.status,
  });

  bool get canPunchIn => status == 'not_punched';
  bool get canPunchOut => status == 'punched_in';
  bool get isCompleted => status == 'completed';

  factory PunchStatus.fromJson(Map<String, dynamic> json) {
    return PunchStatus(
      punchInTime: json['punchInTime'] != null
          ? DateTime.parse(json['punchInTime'] as String)
          : null,
      punchOutTime: json['punchOutTime'] != null
          ? DateTime.parse(json['punchOutTime'] as String)
          : null,
      punchType: json['punchType'] as int? ?? 1,
      status: json['status'] as String? ?? 'not_punched',
    );
  }
}
