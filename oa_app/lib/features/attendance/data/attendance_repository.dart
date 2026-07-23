import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_response.dart';
import '../models/attendance_record.dart';
import '../models/shift.dart';

class AttendanceRepository {
  final Dio _dio;

  AttendanceRepository(this._dio);

  // ---- 打卡 ----
  Future<PunchResult> punchIn({
    int type = 1,
    String? location,
    double? latitude,
    double? longitude,
    String? deviceInfo,
  }) async {
    if (ApiConstants.useStubServices) {
      await Future.delayed(const Duration(milliseconds: 500));
      return PunchResult(punchTime: DateTime.now(), location: location ?? '--', type: type);
    }
    final response = await _dio.post(ApiConstants.punchIn, data: {
      'punchType': type,
      if (location != null) 'location': location,
      if (latitude != null) 'latitude': latitude,
      if (longitude != null) 'longitude': longitude,
      if (deviceInfo != null) 'deviceInfo': deviceInfo,
    });
    final data = response.data['data'] as Map<String, dynamic>?;
    return PunchResult(
      punchTime: data?['punchTime'] != null ? DateTime.parse(data!['punchTime'] as String) : DateTime.now(),
      location: location ?? '--',
      type: type,
    );
  }

  Future<PunchResult> punchOut({
    int type = 1,
    String? location,
    double? latitude,
    double? longitude,
    String? deviceInfo,
  }) async {
    if (ApiConstants.useStubServices) {
      await Future.delayed(const Duration(milliseconds: 500));
      return PunchResult(punchTime: DateTime.now(), location: location ?? '--', type: type);
    }
    final response = await _dio.post(ApiConstants.punchOut, data: {
      'punchType': type,
      if (location != null) 'location': location,
      if (latitude != null) 'latitude': latitude,
      if (longitude != null) 'longitude': longitude,
      if (deviceInfo != null) 'deviceInfo': deviceInfo,
    });
    final data = response.data['data'] as Map<String, dynamic>?;
    return PunchResult(
      punchTime: data?['punchTime'] != null ? DateTime.parse(data!['punchTime'] as String) : DateTime.now(),
      location: location ?? '--',
      type: type,
    );
  }

  // ---- 我的考勤记录 ----
  Future<PageData<AttendanceRecord>> getMyRecords({
    int pageNum = 1,
    int pageSize = 20,
    String? month,
  }) async {
    if (ApiConstants.useStubServices) {
      await Future.delayed(const Duration(milliseconds: 500));
      return PageData(records: [], total: 0, current: 1, size: 20);
    }
    final response = await _dio.get(ApiConstants.attendanceRecordsMine, queryParameters: {
      'pageNum': pageNum,
      'pageSize': pageSize,
      if (month != null) 'month': month,
    });
    return PageData.fromJson(
      response.data['data'] as Map<String, dynamic>,
      (json) => AttendanceRecord.fromJson(json),
    );
  }

  // ---- 部门考勤记录 ----
  Future<PageData<AttendanceRecord>> getDeptRecords({
    int pageNum = 1,
    int pageSize = 20,
    String? month,
  }) async {
    if (ApiConstants.useStubServices) {
      await Future.delayed(const Duration(milliseconds: 500));
      return PageData(records: [], total: 0, current: 1, size: 20);
    }
    final response = await _dio.get(ApiConstants.attendanceRecordsDept, queryParameters: {
      'pageNum': pageNum,
      'pageSize': pageSize,
      if (month != null) 'month': month,
    });
    return PageData.fromJson(
      response.data['data'] as Map<String, dynamic>,
      (json) => AttendanceRecord.fromJson(json),
    );
  }

  // ---- 班次列表 ----
  Future<PageData<Shift>> getShifts({int pageNum = 1, int pageSize = 20}) async {
    if (ApiConstants.useStubServices) {
      await Future.delayed(const Duration(milliseconds: 500));
      return PageData(records: [], total: 0, current: 1, size: 20);
    }
    final response = await _dio.get(ApiConstants.attendanceShifts, queryParameters: {
      'pageNum': pageNum,
      'pageSize': pageSize,
    });
    return PageData.fromJson(
      response.data['data'] as Map<String, dynamic>,
      (json) => Shift.fromJson(json),
    );
  }
}

class PunchResult {
  final DateTime punchTime;
  final String? location;
  final int type;

  const PunchResult({
    required this.punchTime,
    this.location,
    required this.type,
  });
}
