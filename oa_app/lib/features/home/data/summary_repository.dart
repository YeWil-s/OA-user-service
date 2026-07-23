import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';
import '../models/monthly_summary.dart';
import '../models/punch_status.dart';

class SummaryRepository {
  final Dio _dio;

  SummaryRepository(this._dio);

  Future<MonthlySummary> getMonthlySummary(int year, int month) async {
    final monthStr = '$year-${month.toString().padLeft(2, '0')}';
    final response = await _dio.get(
      ApiConstants.attendanceSummaryMine,
      queryParameters: {'month': monthStr, 'pageNum': 1, 'pageSize': 100},
    );
    final data = response.data['data'] as Map<String, dynamic>;

    // 尝试从 summary 字段解析（如果后端有专门汇总端点）
    if (data.containsKey('normalDays')) {
      return MonthlySummary.fromJson(data);
    }

    // 从 records 列表计算汇总
    final records = data['records'] as List<dynamic>? ?? [];
    int normalDays = 0;
    int lateDays = 0;
    int earlyDays = 0;
    int absentDays = 0;
    int leaveDays = 0;
    double overtimeHours = 0;

    for (final r in records) {
      final record = r as Map<String, dynamic>;
      final status = record['status'] as String? ?? '';
      switch (status) {
        case 'normal':
          normalDays++;
          break;
        case 'late':
          lateDays++;
          break;
        case 'early':
          earlyDays++;
          break;
        case 'absent':
          absentDays++;
          break;
        case 'leave':
          leaveDays++;
          break;
      }
      // 加班时长（如果 records 包含 overtime 字段）
      overtimeHours += (record['overtimeHours'] as num?)?.toDouble() ?? 0;
    }

    return MonthlySummary(
      year: year,
      month: month,
      normalDays: normalDays,
      lateDays: lateDays,
      earlyDays: earlyDays,
      absentDays: absentDays,
      leaveDays: leaveDays,
      overtimeHours: overtimeHours,
    );
  }
}
