import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';
import '../models/message.dart';

class MessageRepository {
  final Dio _dio;

  MessageRepository(this._dio);

  Future<List<AppMessage>> getMessages({int? msgType}) async {
    if (ApiConstants.useStubServices) {
      await Future.delayed(const Duration(milliseconds: 500));
      return _mockMessages.where((m) => msgType == null || m.msgType == msgType).toList();
    }
    final response = await _dio.get(
      ApiConstants.noticeMessages,
      queryParameters: msgType != null ? {'msgType': msgType} : null,
    );
    final list = response.data['data'] as List<dynamic>;
    return list.map((e) => AppMessage.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<void> markAsRead(int id) async {
    if (ApiConstants.useStubServices) {
      await Future.delayed(const Duration(milliseconds: 200));
      return;
    }
    await _dio.put(ApiConstants.noticeMessageRead(id));
  }

  Future<int> getUnreadCount() async {
    if (ApiConstants.useStubServices) {
      return _mockMessages.where((m) => !m.isRead).length;
    }
    final response = await _dio.get(ApiConstants.noticeUnreadCount);
    return response.data['data'] as int? ?? 0;
  }

  static final List<AppMessage> _mockMessages = [
    AppMessage(
      id: 1,
      title: '请假审批通过',
      content: '您的年假申请（LV20260720001）已通过审批。',
      msgType: 1,
      isRead: false,
      createTime: DateTime.now().subtract(const Duration(hours: 1)),
    ),
    AppMessage(
      id: 2,
      title: '考勤异常提醒',
      content: '您今天上班打卡时间为 09:15，已迟到。',
      msgType: 2,
      isRead: false,
      createTime: DateTime.now().subtract(const Duration(hours: 2)),
    ),
    AppMessage(
      id: 3,
      title: '系统维护通知',
      content: 'OA 系统将于本周六 02:00-06:00 进行升级维护。',
      msgType: 3,
      isRead: true,
      createTime: DateTime.now().subtract(const Duration(days: 1)),
    ),
    AppMessage(
      id: 4,
      title: '加班审批通过',
      content: '您的加班申请已通过审批，加班时间 3 小时。',
      msgType: 1,
      isRead: true,
      createTime: DateTime.now().subtract(const Duration(days: 1)),
    ),
    AppMessage(
      id: 5,
      title: '月度考勤汇总已生成',
      content: '您的 7 月考勤汇总已生成，本月迟到 2 次。',
      msgType: 2,
      isRead: false,
      createTime: DateTime.now().subtract(const Duration(days: 2)),
    ),
  ];
}
