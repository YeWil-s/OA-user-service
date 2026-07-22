import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';
import '../models/monthly_summary.dart';

class SummaryRepository {
  final Dio _dio;

  SummaryRepository(this._dio);

  Future<MonthlySummary> getMonthlySummary(int year, int month) async {
    if (ApiConstants.useStubServices) {
      await Future.delayed(const Duration(milliseconds: 500));
      return MonthlySummary(
        year: year,
        month: month,
        normalDays: 18,
        lateDays: 2,
        earlyDays: 0,
        absentDays: 0,
        leaveDays: 1,
        overtimeHours: 8.5,
      );
    }
    final response = await _dio.get(
      ApiConstants.attendanceSummaryMine,
      queryParameters: {'year': year, 'month': month},
    );
    return MonthlySummary.fromJson(response.data['data'] as Map<String, dynamic>);
  }
}
