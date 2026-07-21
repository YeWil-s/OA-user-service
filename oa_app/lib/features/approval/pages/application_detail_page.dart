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
      final app = await repo.getDetail(widget.id);
      final records = await repo.getRecords(widget.id);
      if (mounted) {
        setState(() {
          _app = app;
          _records = records;
          _loading = false;
        });
      }
    } catch (e) {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    if (_loading) {
      return Scaffold(
        appBar: AppBar(title: const Text('申请详情')),
        body: const Center(child: CircularProgressIndicator()),
      );
    }

    final app = _app;
    if (app == null) {
      return Scaffold(
        appBar: AppBar(title: const Text('申请详情')),
        body: const Center(child: Text('未找到申请')),
      );
    }

    return Scaffold(
      appBar: AppBar(title: Text(app.leaveTypeLabel)),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceBetween,
                    children: [
                      Expanded(
                        child: Text(app.leaveTypeLabel,
                            style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
                      ),
                      Chip(
                        label: Text(app.statusLabel, style: const TextStyle(fontSize: 12)),
                        visualDensity: VisualDensity.compact,
                      ),
                    ],
                  ),
                  const Divider(),
                  _infoRow('申请编号', app.applicationNo),
                  _infoRow('开始时间', DateFormat('yyyy-MM-dd HH:mm').format(app.startTime)),
                  _infoRow('结束时间', DateFormat('yyyy-MM-dd HH:mm').format(app.endTime)),
                  _infoRow('时长', '${app.duration} ${app.appType == 1 ? '天' : '小时'}'),
                  _infoRow('申请原因', app.reason),
                ],
              ),
            ),
          ),
          const SizedBox(height: 16),
          Text('审批记录', style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          if (_records.isEmpty)
            const Card(
              child: Padding(
                padding: EdgeInsets.all(32),
                child: Center(child: Text('暂无审批记录')),
              ),
            )
          else
            ..._records.map((r) => Card(
                  margin: const EdgeInsets.only(bottom: 8),
                  child: Padding(
                    padding: const EdgeInsets.all(16),
                    child: Row(
                      children: [
                        Icon(
                          r.action == 1 ? Icons.check_circle : Icons.cancel,
                          color: r.action == 1 ? Colors.green : Colors.red,
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Text('${r.approverName} - ${r.actionLabel}',
                                  style: theme.textTheme.titleSmall),
                              if (r.comment != null && r.comment!.isNotEmpty)
                                Text(r.comment!, style: theme.textTheme.bodySmall),
                            ],
                          ),
                        ),
                        Text(
                          DateFormat('MM-dd HH:mm').format(r.actionTime),
                          style: theme.textTheme.bodySmall,
                        ),
                      ],
                    ),
                  ),
                )),
        ],
      ),
    );
  }

  Widget _infoRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 80,
            child: Text(label, style: TextStyle(color: Theme.of(context).colorScheme.outline)),
          ),
          Expanded(child: Text(value)),
        ],
      ),
    );
  }
}
