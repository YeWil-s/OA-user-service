import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import '../providers/approval_provider.dart';

class PendingPage extends ConsumerStatefulWidget {
  const PendingPage({super.key});

  @override
  ConsumerState<PendingPage> createState() => _PendingPageState();
}

class _PendingPageState extends ConsumerState<PendingPage> {
  @override
  void initState() {
    super.initState();
    Future.microtask(() => ref.read(pendingProvider.notifier).fetch());
  }

  Future<void> _handleApprove(int id, int action) async {
    final commentController = TextEditingController();
    final result = await showDialog<String>(
      context: context,
      builder: (ctx) => AlertDialog(
        title: Text(action == 1 ? '同意审批' : '驳回申请'),
        content: TextField(
          controller: commentController,
          decoration: const InputDecoration(
            hintText: '请输入审批意见',
            border: OutlineInputBorder(),
          ),
          maxLines: 2,
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('取消')),
          FilledButton(
            onPressed: () => Navigator.pop(ctx, commentController.text.trim()),
            child: const Text('确认'),
          ),
        ],
      ),
    );
    if (result == null) return;

    ref.read(pendingProvider.notifier).approve(
      id,
      action: action,
      comment: result,
    );
  }

  @override
  Widget build(BuildContext context) {
    final pendingAsync = ref.watch(pendingProvider);
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(title: const Text('待审批')),
      body: pendingAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('加载失败: $e')),
        data: (list) => RefreshIndicator(
          onRefresh: () => ref.read(pendingProvider.notifier).fetch(),
          child: list.isEmpty
              ? const Center(child: Text('暂无待审批事项'))
              : ListView.builder(
                  itemCount: list.length,
                  itemBuilder: (context, index) {
                    final app = list[index];
                    return Card(
                      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
                      child: Padding(
                        padding: const EdgeInsets.all(16),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                Text(
                                  app.applicantName ?? '',
                                  style: theme.textTheme.titleSmall?.copyWith(
                                    fontWeight: FontWeight.w600,
                                  ),
                                ),
                                const SizedBox(width: 8),
                                Chip(
                                  label: Text(app.leaveTypeLabel, style: const TextStyle(fontSize: 12)),
                                  visualDensity: VisualDensity.compact,
                                  padding: EdgeInsets.zero,
                                ),
                              ],
                            ),
                            const SizedBox(height: 8),
                            Text(
                              '${DateFormat('MM-dd HH:mm').format(app.startTime)} - ${DateFormat('MM-dd HH:mm').format(app.endTime)}',
                              style: theme.textTheme.bodySmall,
                            ),
                            Text(app.reason, style: theme.textTheme.bodySmall),
                            const SizedBox(height: 12),
                            Row(
                              mainAxisAlignment: MainAxisAlignment.end,
                              children: [
                                OutlinedButton(
                                  onPressed: () => _handleApprove(app.id, 2),
                                  style: OutlinedButton.styleFrom(
                                    foregroundColor: theme.colorScheme.error,
                                  ),
                                  child: const Text('驳回'),
                                ),
                                const SizedBox(width: 12),
                                FilledButton(
                                  onPressed: () => _handleApprove(app.id, 1),
                                  child: const Text('同意'),
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                    );
                  },
                ),
        ),
      ),
    );
  }
}
