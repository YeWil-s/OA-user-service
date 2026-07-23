import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_response.dart';
import '../models/message.dart';

class MessageRepository {
  final Dio _dio;

  MessageRepository(this._dio);

  Future<PageData<AppMessage>> getMessages({
    int? msgType,
    int? read, // 0=未读, 1=已读, null=全部
    int current = 1,
    int size = 20,
  }) async {
    final response = await _dio.get(
      ApiConstants.noticeMessages,
      queryParameters: {
        if (msgType != null) 'msgType': msgType,
        if (read != null) 'read': read,
        'current': current,
        'size': size,
      },
    );
    final data = response.data['data'] as Map<String, dynamic>;
    // Backend returns PageResult format
    return PageData(
      records: (data['records'] as List<dynamic>? ?? [])
          .map((e) => AppMessage.fromJson(e as Map<String, dynamic>))
          .toList(),
      total: data['total'] as int? ?? 0,
      current: data['current'] as int? ?? 1,
      size: data['size'] as int? ?? 20,
    );
  }

  Future<void> markAsRead(int id) async {
    await _dio.put(ApiConstants.noticeMessageRead(id));
  }

  Future<int> getUnreadCount() async {
    final response = await _dio.get(ApiConstants.noticeUnreadCount);
    final data = response.data['data'] as Map<String, dynamic>?;
    return data?['totalUnread'] as int? ?? 0;
  }
}
