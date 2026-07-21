import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';
import '../models/punch_status.dart';

class PunchRepository {
  final Dio _dio;

  PunchRepository(this._dio);

  Future<PunchStatus> getTodayStatus() async {
    if (ApiConstants.useStubServices) {
      await Future.delayed(const Duration(milliseconds: 500));
      return const PunchStatus(
        punchInTime: null, // will be set dynamically
        status: 'not_punched',
      );
    }
    final response = await _dio.get(ApiConstants.attendanceRecordsMine);
    final data = response.data['data'];
    return PunchStatus.fromJson(data is List && data.isNotEmpty
        ? data[0] as Map<String, dynamic>
        : <String, dynamic>{});
  }

  Future<PunchStatus> punchIn({int type = 1}) async {
    if (ApiConstants.useStubServices) {
      await Future.delayed(const Duration(milliseconds: 500));
      return PunchStatus(
        punchInTime: DateTime.now(),
        punchType: type,
        status: 'punched_in',
      );
    }
    final response = await _dio.post(
      ApiConstants.punchIn,
      data: {'punchType': type},
    );
    return PunchStatus.fromJson(response.data['data'] as Map<String, dynamic>);
  }

  Future<PunchStatus> punchOut({int type = 1}) async {
    if (ApiConstants.useStubServices) {
      await Future.delayed(const Duration(milliseconds: 500));
      return PunchStatus(
        punchInTime: DateTime.now().subtract(const Duration(hours: 8)),
        punchOutTime: DateTime.now(),
        punchType: type,
        status: 'completed',
      );
    }
    final response = await _dio.post(
      ApiConstants.punchOut,
      data: {'punchType': type},
    );
    return PunchStatus.fromJson(response.data['data'] as Map<String, dynamic>);
  }
}
