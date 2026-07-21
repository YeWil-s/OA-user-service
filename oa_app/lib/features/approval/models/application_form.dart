class ApplicationForm {
  int appType; // 1=请假, 2=加班, 3=外出
  int? leaveType;
  DateTime startDate;
  DateTime? startTime;
  DateTime endDate;
  DateTime? endTime;
  String reason;

  ApplicationForm({
    this.appType = 1,
    this.leaveType,
    required this.startDate,
    this.startTime,
    required this.endDate,
    this.endTime,
    this.reason = '',
  });

  DateTime get fullStartTime {
    if (startTime != null) {
      return DateTime(
        startDate.year, startDate.month, startDate.day,
        startTime!.hour, startTime!.minute,
      );
    }
    return startDate;
  }

  DateTime get fullEndTime {
    if (endTime != null) {
      return DateTime(
        endDate.year, endDate.month, endDate.day,
        endTime!.hour, endTime!.minute,
      );
    }
    return endDate;
  }

  double get duration {
    final diff = fullEndTime.difference(fullStartTime);
    if (appType == 1) {
      // 请假按天计算
      return diff.inHours / 24.0;
    }
    // 加班/外出按小时计算
    return diff.inMinutes / 60.0;
  }

  Map<String, dynamic> toJson() => {
    'appType': appType,
    'leaveType': leaveType,
    'startTime': fullStartTime.toIso8601String(),
    'endTime': fullEndTime.toIso8601String(),
    'duration': duration,
    'reason': reason,
  };
}
