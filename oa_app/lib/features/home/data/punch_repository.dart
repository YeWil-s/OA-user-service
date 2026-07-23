import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';
import '../models/punch_status.dart';

class PunchRepository {
  final Dio _dio;

  PunchRepository(this._dio);

  Future<PunchStatus> getTodayStatus() async {
    final response = await _dio.get(
      ApiConstants.attendanceRecordsMine,
      queryParameters: {'pageNum': 1, 'pageSize': 1},
    );
    final data = response.data['data'] as Map<String, dynamic>?;
    final records = data?['records'] as List<dynamic>? ?? [];
    if (records.isNotEmpty) {
      return PunchStatus.fromJson(records.first as Map<String, dynamic>);
    }
    return const PunchStatus(status: 'not_punched');
  }

  Future<PunchStatus> punchIn({
    int type = 1,
    String? location,
    double? latitude,
    double? longitude,
    String? deviceInfo,
  }) async {
    final response = await _dio.post(
      ApiConstants.punchIn,
      data: {
        'punchType': type,
        if (location != null) 'location': location,
        if (latitude != null) 'latitude': latitude,
        if (longitude != null) 'longitude': longitude,
        if (deviceInfo != null) 'deviceInfo': deviceInfo,
      },
    );
    final data = response.data['data'] as Map<String, dynamic>?;
    if (data != null) {
      return PunchStatus.fromPunchResponse(data);
    }
    return getTodayStatus();
  }

  Future<PunchStatus> punchOut({
    int type = 1,
    String? location,
    double? latitude,
    double? longitude,
    String? deviceInfo,
  }) async {
    final response = await _dio.post(
      ApiConstants.punchOut,
      data: {
        'punchType': type,
        if (location != null) 'location': location,
        if (latitude != null) 'latitude': latitude,
        if (longitude != null) 'longitude': longitude,
        if (deviceInfo != null) 'deviceInfo': deviceInfo,
      },
    );
    final data = response.data['data'] as Map<String, dynamic>?;
    if (data != null) {
      return PunchStatus.fromPunchResponse(data);
    }
    return getTodayStatus();
  }
}
