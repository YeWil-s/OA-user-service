class ApplicationForm {
  int appType; // 1=请假, 2=加班, 3=外出, 4=调岗, 5=资产领用
  int? leaveType;
  DateTime startDate;
  DateTime startTime;
  DateTime endDate;
  DateTime endTime;
  String reason;
  int? targetDeptId;
  int? targetPositionId;
  int? assetId;
  DateTime? expectReturnDate;

  ApplicationForm({
    this.appType = 1,
    this.leaveType,
    required this.startDate,
    required this.startTime,
    required this.endDate,
    required this.endTime,
    this.reason = '',
    this.targetDeptId,
    this.targetPositionId,
    this.assetId,
    this.expectReturnDate,
  });

  DateTime get fullStartTime => DateTime(
    startDate.year, startDate.month, startDate.day,
    startTime.hour, startTime.minute,
  );

  DateTime get fullEndTime => DateTime(
    endDate.year, endDate.month, endDate.day,
    endTime.hour, endTime.minute,
  );

  Map<String, dynamic> toJson() {
    final map = <String, dynamic>{
      'appType': appType,
      'leaveType': appType == 1 ? leaveType : null,
      'reason': reason,
    };
    if (appType >= 1 && appType <= 3) {
      map['startTime'] = fullStartTime.toIso8601String();
      map['endTime'] = fullEndTime.toIso8601String();
    }
    if (appType == 4) {
      map['targetDeptId'] = targetDeptId;
      map['targetPositionId'] = targetPositionId;
    }
    if (appType == 5) {
      map['assetId'] = assetId;
      if (expectReturnDate != null) {
        map['expectReturnDate'] = expectReturnDate!.toIso8601String().split('T').first;
      }
    }
    return map;
  }
}
