import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_response.dart';
import '../models/application.dart';
import '../models/application_form.dart';
import '../models/approval_record.dart';

class ApplicationRepository {
  final Dio _dio;

  ApplicationRepository(this._dio);

  Future<PageData<Application>> getMyApplications({
    int? status,
    int pageNum = 1,
    int pageSize = 20,
  }) async {
    final response = await _dio.get(
      ApiConstants.approvalApplications,
      queryParameters: {
        if (status != null) 'status': status,
        'pageNum': pageNum,
        'pageSize': pageSize,
      },
    );
    return PageData.fromJson(
      response.data['data'] as Map<String, dynamic>,
      (json) => Application.fromJson(json),
    );
  }

  Future<Application> getDetail(int id) async {
    final response = await _dio.get(ApiConstants.approvalApplicationDetail(id));
    return Application.fromJson(response.data['data'] as Map<String, dynamic>);
  }

  Future<List<ApprovalRecord>> getRecords(int applicationId) async {
    final response = await _dio.get(ApiConstants.approvalApplicationDetail(applicationId));
    final data = response.data['data'] as Map<String, dynamic>;
    final timeline = data['timeline'] as List<dynamic>? ?? [];
    return timeline.map((e) => ApprovalRecord.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<void> submit(ApplicationForm form) async {
    await _dio.post(ApiConstants.approvalApplications, data: form.toJson());
  }

  Future<void> cancel(int id) async {
    await _dio.put(ApiConstants.approvalCancel(id));
  }

  // ---- Admin/HR 全局视图 ----

  Future<PageData<Application>> getAllApplications({
    int? status,
    int pageNum = 1,
    int pageSize = 20,
  }) async {
    final response = await _dio.get(
      ApiConstants.approvalApplicationsAll,
      queryParameters: {
        if (status != null) 'status': status,
        'pageNum': pageNum,
        'pageSize': pageSize,
      },
    );
    return PageData.fromJson(
      response.data['data'] as Map<String, dynamic>,
      (json) => Application.fromJson(json),
    );
  }

  // ---- Pending (审批) ----

  Future<PageData<Application>> getPending({
    int pageNum = 1,
    int pageSize = 20,
  }) async {
    final response = await _dio.get(
      ApiConstants.approvalPending,
      queryParameters: {'pageNum': pageNum, 'pageSize': pageSize},
    );
    return PageData.fromJson(
      response.data['data'] as Map<String, dynamic>,
      (json) => Application.fromJson(json),
    );
  }

  Future<PageData<Application>> getAllPending({
    int pageNum = 1,
    int pageSize = 20,
  }) async {
    final response = await _dio.get(
      ApiConstants.approvalPendingAll,
      queryParameters: {'pageNum': pageNum, 'pageSize': pageSize},
    );
    return PageData.fromJson(
      response.data['data'] as Map<String, dynamic>,
      (json) => Application.fromJson(json),
    );
  }

  Future<void> approve(int id, {required bool approved, required String comment}) async {
    await _dio.put(
      ApiConstants.approvalApprove(id),
      data: {'approved': approved, 'comment': comment},
    );
  }

  // ---- 参考数据（调岗/资产领用） ----

  Future<List<Map<String, dynamic>>> getDepts() async {
    final res = await _dio.get(ApiConstants.depts);
    // depts 返回的是普通列表，不是分页对象
    final list = res.data['data'] as List<dynamic>? ?? [];
    return list.cast<Map<String, dynamic>>();
  }

  Future<List<Map<String, dynamic>>> getPositions() async {
    final res = await _dio.get(ApiConstants.positions, queryParameters: {'pageSize': 100});
    final data = res.data['data'] as Map<String, dynamic>?;
    final records = data?['records'] as List<dynamic>? ?? [];
    return records.cast<Map<String, dynamic>>();
  }

  Future<List<Map<String, dynamic>>> getAvailableAssets() async {
    final res = await _dio.get(ApiConstants.assets, queryParameters: {'pageSize': 100, 'status': 1});
    final data = res.data['data'] as Map<String, dynamic>?;
    final records = data?['records'] as List<dynamic>? ?? [];
    return records.cast<Map<String, dynamic>>();
  }
}
