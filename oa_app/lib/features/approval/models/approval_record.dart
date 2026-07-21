class ApprovalRecord {
  final int id;
  final int applicationId;
  final int approverId;
  final String approverName;
  final int action; // 1=同意, 2=驳回
  final String? comment;
  final DateTime actionTime;

  const ApprovalRecord({
    required this.id,
    required this.applicationId,
    required this.approverId,
    required this.approverName,
    required this.action,
    this.comment,
    required this.actionTime,
  });

  String get actionLabel => action == 1 ? '同意' : '驳回';

  factory ApprovalRecord.fromJson(Map<String, dynamic> json) {
    return ApprovalRecord(
      id: json['id'] as int,
      applicationId: json['applicationId'] as int,
      approverId: json['approverId'] as int,
      approverName: json['approverName'] as String? ?? '',
      action: json['action'] as int,
      comment: json['comment'] as String?,
      actionTime: json['actionTime'] != null
          ? DateTime.parse(json['actionTime'] as String)
          : DateTime.now(),
    );
  }
}
