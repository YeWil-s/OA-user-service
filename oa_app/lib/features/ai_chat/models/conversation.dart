class Conversation {
  final String sessionId;
  final String title;
  final String lastMessage;
  final int category;
  final DateTime createTime;

  const Conversation({
    required this.sessionId,
    required this.title,
    required this.lastMessage,
    required this.category,
    required this.createTime,
  });

  factory Conversation.fromJson(Map<String, dynamic> json) {
    return Conversation(
      sessionId: json['sessionId'] as String? ?? '',
      title: json['question'] as String? ?? json['sessionId'] as String? ?? '',
      lastMessage: json['answer'] as String? ?? '',
      category: json['category'] as int? ?? 3,
      createTime: json['createTime'] != null
          ? DateTime.parse(json['createTime'] as String)
          : DateTime.now(),
    );
  }
}
