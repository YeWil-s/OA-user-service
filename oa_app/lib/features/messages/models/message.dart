class AppMessage {
  final int id;
  final String title;
  final String content;
  final int msgType; // 1=审批通知, 2=考勤通知, 3=系统通知
  final int? relatedId;
  final bool isRead;
  final DateTime createTime;

  const AppMessage({
    required this.id,
    required this.title,
    required this.content,
    required this.msgType,
    this.relatedId,
    required this.isRead,
    required this.createTime,
  });

  String get typeLabel {
    switch (msgType) {
      case 1:
        return '审批通知';
      case 2:
        return '考勤通知';
      case 3:
        return '系统通知';
      default:
        return '未知';
    }
  }

  factory AppMessage.fromJson(Map<String, dynamic> json) {
    return AppMessage(
      id: json['id'] as int,
      title: json['title'] as String,
      content: json['content'] as String? ?? '',
      msgType: json['msgType'] as int? ?? 3,
      relatedId: json['relatedId'] as int?,
      isRead: (json['read'] ?? json['isRead']) as bool? ?? false,
      createTime: json['createTime'] != null
          ? DateTime.parse(json['createTime'] as String)
          : DateTime.now(),
    );
  }
}
