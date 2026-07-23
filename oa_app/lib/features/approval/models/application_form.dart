class ApplicationForm {
  int appType; // 1=请假, 2=加班, 3=外出
  int? leaveType;
  DateTime startDate;
  DateTime startTime;
  DateTime endDate;
  DateTime endTime;
  String reason;

  ApplicationForm({
    this.appType = 1,
    this.leaveType,
    required this.startDate,
    required this.startTime,
    required this.endDate,
    required this.endTime,
    this.reason = '',
  });

  DateTime get fullStartTime => DateTime(
    startDate.year, startDate.month, startDate.day,
    startTime.hour, startTime.minute,
  );

  DateTime get fullEndTime => DateTime(
    endDate.year, endDate.month, endDate.day,
    endTime.hour, endTime.minute,
  );

  Map<String, dynamic> toJson() => {
    'appType': appType,
    'leaveType': leaveType,
    'startTime': fullStartTime.toIso8601String(),
    'endTime': fullEndTime.toIso8601String(),
    'reason': reason,
  };
}
