import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';
import '../models/application.dart';
import '../models/application_form.dart';
import '../models/approval_record.dart';

class ApplicationRepository {
  final Dio _dio;

  ApplicationRepository(this._dio);

  // ---- Mock data ----
  static final List<Application> _mock = [
    Application(
      id: 1, applicationNo: 'LV20260720001', appType: 1, leaveType: 1,
      startTime: DateTime(2026, 7, 22), endTime: DateTime(2026, 7, 22),
      duration: 1.0, reason: '家里有事', status: 'approved',
      createTime: DateTime.now().subtract(const Duration(days: 1)),
    ),
    Application(
      id: 2, applicationNo: 'OV20260721001', appType: 2,
      startTime: DateTime(2026, 7, 21, 18, 0), endTime: DateTime(2026, 7, 21, 21, 0),
      duration: 3.0, reason: '项目赶进度', status: 'pending',
      createTime: DateTime.now().subtract(const Duration(hours: 3)),
    ),
    Application(
      id: 3, applicationNo: 'LV20260719001', appType: 1, leaveType: 3,
      startTime: DateTime(2026, 7, 19), endTime: DateTime(2026, 7, 19),
      duration: 1.0, reason: '身体不适去医院', status: 'approved',
      createTime: DateTime.now().subtract(const Duration(days: 2)),
    ),
  ];

  static final List<ApprovalRecord> _mockRecords = [
    ApprovalRecord(id: 1, applicationId: 1, approverId: 2, approverName: '张经理',
        action: 1, comment: '同意请假', actionTime: DateTime(2026, 7, 20, 10, 30)),
  ];

  Future<List<Application>> getMyApplications({String? status}) async {
    if (ApiConstants.useStubServices) {
      await Future.delayed(const Duration(milliseconds: 500));
      final list = List<Application>.from(_mock);
      if (status != null) list.removeWhere((a) => a.status != status);
      return list;
    }
    final response = await _dio.get(
      ApiConstants.approvalApplications,
      queryParameters: status != null ? {'status': status} : null,
    );
    final data = response.data['data'] as List<dynamic>;
    return data.map((e) => Application.fromJson(e)).toList();
  }

  Future<Application> getDetail(int id) async {
    if (ApiConstants.useStubServices) {
      await Future.delayed(const Duration(milliseconds: 300));
      return _mock.firstWhere((a) => a.id == id);
    }
    final response = await _dio.get(ApiConstants.approvalApplicationDetail(id));
    return Application.fromJson(response.data['data']);
  }

  Future<List<ApprovalRecord>> getRecords(int applicationId) async {
    if (ApiConstants.useStubServices) {
      return _mockRecords.where((r) => r.applicationId == applicationId).toList();
    }
    final response = await _dio.get(ApiConstants.approvalApplicationDetail(applicationId));
    final data = response.data['data'] as Map<String, dynamic>;
    final records = data['records'] as List<dynamic>? ?? [];
    return records.map((e) => ApprovalRecord.fromJson(e)).toList();
  }

  Future<void> submit(ApplicationForm form) async {
    if (ApiConstants.useStubServices) {
      await Future.delayed(const Duration(milliseconds: 800));
      _mock.insert(0, Application(
        id: _mock.length + 1,
        applicationNo: 'LV20260721${_mock.length + 1}'.padRight(10, '0'),
        appType: form.appType,
        leaveType: form.leaveType,
        startTime: form.fullStartTime,
        endTime: form.fullEndTime,
        duration: form.duration,
        reason: form.reason,
        status: 'pending',
        createTime: DateTime.now(),
      ));
      return;
    }
    await _dio.post(ApiConstants.approvalApplications, data: form.toJson());
  }

  Future<void> cancel(int id) async {
    if (ApiConstants.useStubServices) {
      final idx = _mock.indexWhere((a) => a.id == id);
      if (idx != -1) {
        _mock[idx] = Application(
          id: _mock[idx].id, applicationNo: _mock[idx].applicationNo,
          appType: _mock[idx].appType, leaveType: _mock[idx].leaveType,
          startTime: _mock[idx].startTime, endTime: _mock[idx].endTime,
          duration: _mock[idx].duration, reason: _mock[idx].reason,
          status: 'cancelled', createTime: _mock[idx].createTime,
        );
      }
      return;
    }
    await _dio.put(ApiConstants.approvalCancel(id));
  }

  // ---- Pending (审批) ----

  static final List<Application> _mockPending = [
    Application(
      id: 100, applicationNo: 'LV20260721001', appType: 1, leaveType: 1,
      startTime: DateTime(2026, 7, 23), endTime: DateTime(2026, 7, 23),
      duration: 1.0, reason: '有私事需要处理', status: 'pending',
      applicantName: '张三',
      createTime: DateTime.now().subtract(const Duration(hours: 2)),
    ),
    Application(
      id: 101, applicationNo: 'OV20260721002', appType: 2,
      startTime: DateTime(2026, 7, 22, 18, 0), endTime: DateTime(2026, 7, 22, 21, 0),
      duration: 3.0, reason: '系统上线支持', status: 'pending',
      applicantName: '李四',
      createTime: DateTime.now().subtract(const Duration(hours: 5)),
    ),
  ];

  Future<List<Application>> getPending() async {
    if (ApiConstants.useStubServices) {
      await Future.delayed(const Duration(milliseconds: 500));
      return List<Application>.from(_mockPending);
    }
    final response = await _dio.get(ApiConstants.approvalPending);
    final data = response.data['data'] as List<dynamic>;
    return data.map((e) => Application.fromJson(e)).toList();
  }

  Future<void> approve(int id, {required int action, required String comment}) async {
    if (ApiConstants.useStubServices) {
      await Future.delayed(const Duration(milliseconds: 500));
      _mockPending.removeWhere((a) => a.id == id);
      return;
    }
    await _dio.put(
      ApiConstants.approvalApprove(id),
      data: {'action': action, 'comment': comment},
    );
  }
}
