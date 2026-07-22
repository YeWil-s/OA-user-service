class ChatMessage {
  final String role; // 'user' | 'assistant'
  final String content;
  final bool streaming; // assistant 消息是否还在流式接收中
  final DateTime timestamp;

  const ChatMessage({
    required this.role,
    required this.content,
    this.streaming = false,
    required this.timestamp,
  });

  ChatMessage copyWith({String? content, bool? streaming}) {
    return ChatMessage(
      role: role,
      content: content ?? this.content,
      streaming: streaming ?? this.streaming,
      timestamp: timestamp,
    );
  }
}
