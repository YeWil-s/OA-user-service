import 'dart:async';
import 'dart:convert';
import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:http/http.dart' as http;
import 'package:shared_preferences/shared_preferences.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/constants/app_constants.dart';
import '../../../core/network/interceptors/auth_interceptor.dart';
import '../../../core/network/interceptors/error_interceptor.dart';
import '../../../core/network/auth_callback.dart';
import '../data/chat_repository.dart';
import '../models/chat_message.dart';
import '../models/conversation.dart';

final aiDioProvider = Provider<Dio>((ref) {
  final dio = Dio(BaseOptions(
    baseUrl: ApiConstants.aiBaseUrl,
    connectTimeout: ApiConstants.connectTimeout,
    receiveTimeout: ApiConstants.receiveTimeout,
    sendTimeout: ApiConstants.sendTimeout,
    headers: {'Content-Type': 'application/json'},
  ));
  dio.interceptors.add(AuthInterceptor());
  dio.interceptors.add(ErrorInterceptor(onUnauthorized: triggerLogout));
  return dio;
});

final chatRepositoryProvider = Provider<ChatRepository>((ref) {
  return ChatRepository(ref.watch(aiDioProvider));
});

class ChatState {
  final List<ChatMessage> messages;
  final String? currentSessionId;
  final bool isStreaming;
  final String? formConfirmation; // Agent 填单确认数据（JSON）

  const ChatState({
    required this.messages,
    this.currentSessionId,
    this.isStreaming = false,
    this.formConfirmation,
  });

  ChatState copyWith({
    List<ChatMessage>? messages,
    String? currentSessionId,
    bool? isStreaming,
    String? formConfirmation,
    bool clearFormConfirmation = false,
  }) {
    return ChatState(
      messages: messages ?? this.messages,
      currentSessionId: currentSessionId ?? this.currentSessionId,
      isStreaming: isStreaming ?? this.isStreaming,
      formConfirmation: clearFormConfirmation ? null : (formConfirmation ?? this.formConfirmation),
    );
  }
}

class ChatNotifier extends StateNotifier<ChatState> {
  StreamSubscription? _streamSub;
  http.Client? _httpClient;

  ChatNotifier() : super(const ChatState(messages: []));

  void _addMessage(ChatMessage msg) {
    state = state.copyWith(
      messages: [...state.messages, msg],
    );
  }

  void _appendContent(String text) {
    final messages = [...state.messages];
    final lastIdx = messages.length - 1;
    if (lastIdx < 0) return;
    messages[lastIdx] = messages[lastIdx].copyWith(
      content: messages[lastIdx].content + text,
    );
    state = state.copyWith(messages: messages);
  }

  void sendMessage(String text) {
    if (text.trim().isEmpty || state.isStreaming) return;

    _addMessage(ChatMessage(
      role: 'user',
      content: text.trim(),
      timestamp: DateTime.now(),
    ));

    _handleAgentStream(text.trim());
  }

  void confirmForm() {
    if (state.formConfirmation == null || state.isStreaming) return;
    _startAgentStream(
      message: state.messages.lastWhere((m) => m.role == 'user').content,
      action: 'confirm',
    );
  }

  void modifyForm() {
    if (state.formConfirmation == null || state.isStreaming) return;
    _startAgentStream(
      message: '需要修改申请信息',
      action: 'modify',
    );
  }

  // ---------- Agent 流式（RAG 知识问答 + 意图识别 + 填单） ----------

  void _handleAgentStream(String message) {
    _startAgentStream(message: message);
  }

  void _startAgentStream({required String message, String action = ''}) {
    _cancelStream();
    state = state.copyWith(
      isStreaming: true,
      clearFormConfirmation: true,
    );

    _addMessage(ChatMessage(
      role: 'assistant',
      content: '',
      streaming: true,
      timestamp: DateTime.now(),
    ));

    _connectAgentSSE(message: message, action: action);
  }

  Future<void> _connectAgentSSE({
    required String message,
    String action = '',
  }) async {
    try {
      final prefs = await SharedPreferences.getInstance();
      final token = prefs.getString(AppConstants.tokenKey) ?? '';

      final uri = Uri.parse('${ApiConstants.aiBaseUrl}${ApiConstants.aiAgentStream}');
      final request = http.Request('POST', uri);
      request.headers.addAll({
        'Content-Type': 'application/json',
        'Authorization': 'Bearer $token',
      });
      request.body = jsonEncode({
        'message': message,
        if (state.currentSessionId != null) 'sessionId': state.currentSessionId,
        if (action.isNotEmpty) 'action': action,
      });

      _httpClient = http.Client();
      final response = await _httpClient!.send(request);

      final lineStream = response.stream
          .transform(utf8.decoder)
          .transform(const LineSplitter());

      _streamSub = lineStream.listen(
        (line) => _onSseLine(line),
        onDone: _onStreamDone,
        onError: (_) => state = state.copyWith(isStreaming: false),
        cancelOnError: true,
      );
    } catch (e) {
      _appendContent('\n[连接失败: $e]');
      _finishStream();
    }
  }

  void _onSseLine(String line) {
    if (!line.startsWith('data:')) return;

    final data = line.substring(5).trim();
    if (data.isEmpty || data == '[DONE]') return;

    // 尝试解析 JSON
    Map<String, dynamic> event;
    try {
      event = jsonDecode(data) as Map<String, dynamic>;
    } catch (_) {
      // 非 JSON，当作纯文本 token 追加
      _appendContent(data);
      return;
    }

    final type = event['type'] as String? ?? '';
    final content = event['content'] as String? ?? '';

    switch (type) {
      case 'token':
        _appendContent(content);
        break;
      case 'message':
        _appendContent(content);
        break;
      case 'thinking':
        // 可显示状态，目前跳过
        break;
      case 'sources':
        // 参考来源，目前跳过
        break;
      case 'intent':
        // 意图识别结果，目前跳过
        break;
      case 'clarification':
        _appendContent('\n\n$content');
        break;
      case 'confirmation':
        // Agent 填单：显示确认信息
        final sid = event['sessionId'] as String?;
        state = state.copyWith(
          formConfirmation: data,
          currentSessionId: sid ?? state.currentSessionId,
        );
        _appendContent(content);
        final fields = event['fields'];
        if (fields != null) {
          _appendContent('\n\n---\n');
          _appendContent(_formatFormFields(fields as Map<String, dynamic>));
          _appendContent('\n---\n请在下方确认提交');
        }
        break;
      case 'submitted':
        _appendContent('\n\n$content');
        break;
      case 'error':
        _appendContent('\n[错误: $content]');
        break;
      case 'done':
        final sessionId = event['sessionId'] as String?;
        if (sessionId != null) {
          state = state.copyWith(currentSessionId: sessionId);
        }
        _finishStream();
        break;
      default:
        // 未知类型，如果 content 非空则追加
        if (content.isNotEmpty) {
          _appendContent(content);
        }
    }
  }

  String _formatFormFields(Map<String, dynamic> fields) {
    final buf = StringBuffer();
    for (final entry in fields.entries) {
      buf.writeln('${entry.key}: ${entry.value}');
    }
    return buf.toString();
  }

  void _onStreamDone() {
    if (state.isStreaming) {
      _finishStream();
    }
  }

  void _finishStream() {
    final messages = [...state.messages];
    final lastIdx = messages.length - 1;
    if (lastIdx >= 0 && messages[lastIdx].streaming) {
      messages[lastIdx] = messages[lastIdx].copyWith(streaming: false);
    }
    state = state.copyWith(
      messages: messages,
      isStreaming: false,
    );
  }

  void clearMessages() {
    _cancelStream();
    state = const ChatState(messages: []);
  }

  void _cancelStream() {
    _streamSub?.cancel();
    _streamSub = null;
    _httpClient?.close();
    _httpClient = null;
  }

  @override
  void dispose() {
    _cancelStream();
    super.dispose();
  }
}

final chatProvider = StateNotifierProvider<ChatNotifier, ChatState>((ref) {
  return ChatNotifier();
});

final conversationListProvider =
    FutureProvider<List<Conversation>>((ref) async {
  final repo = ref.watch(chatRepositoryProvider);
  return repo.getConversations();
});
