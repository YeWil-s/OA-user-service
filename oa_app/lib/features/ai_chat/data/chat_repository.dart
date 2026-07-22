import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';
import '../models/conversation.dart';

class ChatRepository {
  final Dio _dio;

  ChatRepository(this._dio);

  Future<List<Conversation>> getConversations({int pageNum = 1, int pageSize = 20}) async {
    final response = await _dio.get(
      ApiConstants.aiConversations,
      queryParameters: {'pageNum': pageNum, 'pageSize': pageSize},
    );
    final data = response.data['data'];
    final records = data['records'] as List<dynamic>? ?? [];
    return records.map((e) => Conversation.fromJson(e as Map<String, dynamic>)).toList();
  }

  Future<void> deleteConversation(String sessionId) async {
    await _dio.delete('${ApiConstants.aiConversations}/$sessionId');
  }
}
