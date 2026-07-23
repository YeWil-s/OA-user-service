import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import '../providers/approval_provider.dart';
import '../models/application.dart';
import '../models/approval_record.dart';

class ApplicationDetailPage extends ConsumerStatefulWidget {
  final int id;
  const ApplicationDetailPage({super.key, required this.id});

  @override
  ConsumerState<ApplicationDetailPage> createState() => _ApplicationDetailPageState();
}

class _ApplicationDetailPageState extends ConsumerState<ApplicationDetailPage> {
  Application? _app;
  List<ApprovalRecord> _records = [];
  bool _loading = true;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    try {
      final repo = ref.read(applicationRepositoryProvider);
      final results = await Future.wait([repo.getDetail(widget.id), repo.getRecords(widget.id)]);
      if (mounted) setState(() { _app = results[0] as Application; _records = results[1] as List<ApprovalRecord>; _loading = false; });
    } catch (e) {
      if (mounted) setState(() => _loading = false);
    }
  }

  static const _statusColors = {'approved': Color(0xFF2E7D32), 'pending': Color(0xFFE65100), 'rejected': Color(0xFFC62828), 'cancelled': Color(0xFF757575)};

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    if (_loading) return Scaffold(appBar: AppBar(title: const Text('申请详情')), body: const Center(child: CircularProgressIndicator()));

    final app = _app;
    if (app == null) return Scaffold(appBar: AppBar(title: const Text('申请详情')), body: const Center(child: Text('未找到申请')));

    final sc = _statusColors[app.status] ?? Colors.grey;

    return Scaffold(
      appBar: AppBar(title: Text(app.leaveTypeLabel)),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // 申请信息卡
          Card(
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    children: [
                      Container(width: 44, height: 44, decoration: BoxDecoration(color: sc.withValues(alpha: 0.1), borderRadius: BorderRadius.circular(12)), child: Icon(Icons.description_outlined, color: sc)),
                      const SizedBox(width: 12),
                      Expanded(child: Text(app.leaveTypeLabel, style: const TextStyle(fontWeight: FontWeight.w700, fontSize: 17))),
                      Container(padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 5), decoration: BoxDecoration(color: sc.withValues(alpha: 0.08), borderRadius: BorderRadius.circular(20)), child: Text(app.statusLabel, style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600, color: sc))),
                    ],
                  ),
                  const Divider(height: 28),
                  _row('申请编号', app.applicationNo, theme),
                  _row('开始时间', DateFormat('yyyy-MM-dd HH:mm').format(app.startTime), theme),
                  _row('结束时间', DateFormat('yyyy-MM-dd HH:mm').format(app.endTime), theme),
                  _row('时长', app.appType == 1 ? '${(app.duration / 24).toStringAsFixed(1)} 天' : '${app.duration} 小时', theme),
                  _row('申请原因', app.reason, theme),
                  if (app.applicantName != null) _row('申请人', app.applicantName!, theme),
                  if (app.deptName != null) _row('部门', app.deptName!, theme),
                ],
              ),
            ),
          ),
          const SizedBox(height: 20),
          // 审批时间线
          Row(children: [
            const Icon(Icons.timeline, size: 20),
            const SizedBox(width: 8),
            Text('审批进度', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.w600)),
          ]),
          const SizedBox(height: 10),
          if (_records.isEmpty)
            Card(child: Padding(padding: const EdgeInsets.all(40), child: Center(child: Text('暂无审批记录', style: TextStyle(color: theme.colorScheme.outline)))))
          else
            ..._records.map((r) => Padding(
              padding: const EdgeInsets.only(bottom: 8),
              child: Card(
                child: Padding(
                  padding: const EdgeInsets.all(14),
                  child: Row(
                    children: [
                      Container(
                        width: 40, height: 40,
                        decoration: BoxDecoration(
                          color: r.action == 1 ? const Color(0xFF2E7D32).withValues(alpha: 0.1) : const Color(0xFFC62828).withValues(alpha: 0.1),
                          borderRadius: BorderRadius.circular(10),
                        ),
                        child: Icon(r.action == 1 ? Icons.check : Icons.close, color: r.action == 1 ? const Color(0xFF2E7D32) : const Color(0xFFC62828)),
                      ),
                      const SizedBox(width: 12),
                      Expanded(
                        child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                          Text('${r.approverName} · ${r.actionLabel}', style: const TextStyle(fontWeight: FontWeight.w600)),
                          if (r.comment != null && r.comment!.isNotEmpty) ...[const SizedBox(height: 4), Text(r.comment!, style: TextStyle(fontSize: 13, color: theme.colorScheme.outline))],
                        ]),
                      ),
                      Text(DateFormat('MM-dd HH:mm').format(r.actionTime), style: theme.textTheme.bodySmall),
                    ],
                  ),
                ),
              ),
            )),
        ],
      ),
    );
  }

  Widget _row(String label, String value, ThemeData theme) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 6),
      child: Row(crossAxisAlignment: CrossAxisAlignment.start, children: [
        SizedBox(width: 72, child: Text(label, style: TextStyle(color: theme.colorScheme.outline, fontSize: 13))),
        Expanded(child: Text(value, style: const TextStyle(fontSize: 14))),
      ]),
    );
  }
}
