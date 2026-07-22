class MonthlySummary {
  final int year;
  final int month;
  final int normalDays;
  final int lateDays;
  final int earlyDays;
  final int absentDays;
  final int leaveDays;
  final double overtimeHours;

  const MonthlySummary({
    required this.year,
    required this.month,
    required this.normalDays,
    required this.lateDays,
    required this.earlyDays,
    required this.absentDays,
    required this.leaveDays,
    required this.overtimeHours,
  });

  int get workDays => normalDays + lateDays + earlyDays + absentDays;

  factory MonthlySummary.fromJson(Map<String, dynamic> json) {
    return MonthlySummary(
      year: json['year'] as int,
      month: json['month'] as int,
      normalDays: json['normalDays'] as int? ?? 0,
      lateDays: json['lateDays'] as int? ?? 0,
      earlyDays: json['earlyDays'] as int? ?? 0,
      absentDays: json['absentDays'] as int? ?? 0,
      leaveDays: json['leaveDays'] as int? ?? 0,
      overtimeHours: (json['overtimeHours'] as num?)?.toDouble() ?? 0.0,
    );
  }

  static const empty = MonthlySummary(
    year: 0,
    month: 0,
    normalDays: 0,
    lateDays: 0,
    earlyDays: 0,
    absentDays: 0,
    leaveDays: 0,
    overtimeHours: 0,
  );
}
