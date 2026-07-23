class AttendanceRecord {
  final int id;
  final int userId;
  final String? userName;
  final String? deptName;
  final DateTime punchDate;
  final DateTime? punchInTime;
  final DateTime? punchOutTime;
  final int punchInType;
  final int punchOutType;
  final String? punchInLocation;
  final String? punchOutLocation;
  final double? latitude;
  final double? longitude;
  final String status;
  final String? remark;

  const AttendanceRecord({
    required this.id,
    required this.userId,
    this.userName,
    this.deptName,
    required this.punchDate,
    this.punchInTime,
    this.punchOutTime,
    this.punchInType = 1,
    this.punchOutType = 1,
    this.punchInLocation,
    this.punchOutLocation,
    this.latitude,
    this.longitude,
    required this.status,
    this.remark,
  });

  String get statusLabel {
    switch (status) {
      case 'normal': return '正常';
      case 'late': return '迟到';
      case 'early': return '早退';
      case 'absent': return '缺勤';
      case 'leave': return '请假';
      default: return status;
    }
  }

  String get punchTypeLabel {
    if (punchInType == 2 || punchOutType == 2) return '外勤';
    return '现场';
  }

  factory AttendanceRecord.fromJson(Map<String, dynamic> json) {
    return AttendanceRecord(
      id: json['id'] as int,
      userId: json['userId'] as int? ?? 0,
      userName: json['userName'] as String?,
      deptName: json['deptName'] as String?,
      punchDate: DateTime.parse(json['punchDate'] as String),
      punchInTime: json['punchInTime'] != null ? DateTime.parse(json['punchInTime'] as String) : null,
      punchOutTime: json['punchOutTime'] != null ? DateTime.parse(json['punchOutTime'] as String) : null,
      punchInType: json['punchInType'] as int? ?? 1,
      punchOutType: json['punchOutType'] as int? ?? 1,
      punchInLocation: json['punchInLocation'] as String?,
      punchOutLocation: json['punchOutLocation'] as String?,
      latitude: (json['latitude'] as num?)?.toDouble(),
      longitude: (json['longitude'] as num?)?.toDouble(),
      status: json['status'] as String? ?? 'normal',
      remark: json['remark'] as String?,
    );
  }
}
