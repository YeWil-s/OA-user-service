class Schedule {
  final int id;
  final int userId;
  final String userName;
  final String deptName;
  final String scheduleDate;
  final int shiftId;
  final String shiftName;
  final String startTime;
  final String endTime;
  final int status;
  final String? statusText;

  const Schedule({
    required this.id,
    required this.userId,
    required this.userName,
    required this.deptName,
    required this.scheduleDate,
    required this.shiftId,
    required this.shiftName,
    required this.startTime,
    required this.endTime,
    required this.status,
    this.statusText,
  });

  factory Schedule.fromJson(Map<String, dynamic> json) {
    return Schedule(
      id: json['id'] as int? ?? 0,
      userId: json['userId'] as int? ?? 0,
      userName: json['userName'] as String? ?? '',
      deptName: json['deptName'] as String? ?? '',
      scheduleDate: json['scheduleDate'] as String? ?? '',
      shiftId: json['shiftId'] as int? ?? 0,
      shiftName: json['shiftName'] as String? ?? '',
      startTime: json['startTime'] as String? ?? '',
      endTime: json['endTime'] as String? ?? '',
      status: json['status'] as int? ?? 0,
      statusText: json['statusText'] as String?,
    );
  }

  bool get isLeave => status == 2;
  bool get isDefault => statusText == '默认';
}
