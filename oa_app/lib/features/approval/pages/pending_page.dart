import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import '../../../../core/network/api_exception.dart';
import '../../auth/providers/auth_providers.dart';
import '../providers/approval_provider.dart';

class PendingPage extends ConsumerStatefulWidget {
  const PendingPage({super.key});

  @override
  ConsumerState<PendingPage> createState() => _PendingPageState();
}

class _PendingPageState extends ConsumerState<PendingPage> {
  bool get _viewAll {
    final user = ref.read(authProvider).valueOrNull;
    return user?.isAdmin ?? false || (user?.hasRole('hr') ?? false);
  }

  @override
  void initState() {
    super.initState();
    Future.microtask(() => ref.read(pendingProvider.notifier).fetch(viewAll: _viewAll));
  }

  Future<void> _handleApprove(int id, bool approved) async {
    final commentController = TextEditingController();
    final result = await showDialog<String>(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        title: Text(approved ? '同意审批' : '驳回申请'),
        content: TextField(
          controller: commentController,
          decoration: const InputDecoration(hintText: '请输入审批意见'),
          maxLines: 3,
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('取消')),
          FilledButton(onPressed: () => Navigator.pop(ctx, commentController.text.trim()), child: const Text('确认')),
        ],
      ),
    );
    if (result == null) return;
    ref.read(pendingProvider.notifier).approve(id, approved: approved, comment: result, viewAll: _viewAll);
  }

  @override
  Widget build(BuildContext context) {
    final pendingAsync = ref.watch(pendingProvider);
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(title: Text(_viewAll ? '全部待审批' : '待审批')),
      body: pendingAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Column(mainAxisSize: MainAxisSize.min, children: [Icon(Icons.error_outline, size: 48, color: theme.colorScheme.outline), const SizedBox(height: 12), Text(extractErrorMessage(e), style: TextStyle(color: theme.colorScheme.outline))])),
        data: (list) => RefreshIndicator(
          onRefresh: () => ref.read(pendingProvider.notifier).fetch(viewAll: _viewAll),
          child: list.isEmpty
              ? ListView(children: [SizedBox(height: MediaQuery.of(context).size.height * 0.3, child: const Center(child: Text('暂无待审批事项')))])
              : ListView.builder(
                  padding: const EdgeInsets.symmetric(vertical: 8),
                  itemCount: list.length,
                  itemBuilder: (context, index) {
                    final app = list[index];
                    return Padding(
                      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                      child: Card(
                        child: Padding(
                          padding: const EdgeInsets.all(16),
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Container(width: 40, height: 40, decoration: BoxDecoration(color: theme.colorScheme.primaryContainer, borderRadius: BorderRadius.circular(12)), child: Center(child: Text(app.applicantName?.isNotEmpty == true ? app.applicantName![0] : '?', style: TextStyle(fontWeight: FontWeight.bold, color: theme.colorScheme.primary)))),
                                  const SizedBox(width: 12),
                                  Expanded(
                                    child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                                      Text(app.applicantName ?? '', style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 15)),
                                      Text(app.leaveTypeLabel, style: TextStyle(fontSize: 13, color: theme.colorScheme.outline)),
                                    ]),
                                  ),
                                  Container(padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4), decoration: BoxDecoration(color: Colors.orange.withValues(alpha: 0.08), borderRadius: BorderRadius.circular(20)), child: const Text('待审批', style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: Colors.orange))),
                                ],
                              ),
                              const SizedBox(height: 12),
                              Container(
                                padding: const EdgeInsets.all(10),
                                decoration: BoxDecoration(color: theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.4), borderRadius: BorderRadius.circular(10)),
                                child: Column(
                                  crossAxisAlignment: CrossAxisAlignment.start,
                                  children: [
                                    Text('${DateFormat('yyyy-MM-dd HH:mm').format(app.startTime)} ~ ${DateFormat('MM-dd HH:mm').format(app.endTime)}', style: theme.textTheme.bodySmall),
                                    if (app.reason.isNotEmpty) Text(app.reason, style: TextStyle(fontSize: 13, color: theme.colorScheme.outline)),
                                  ],
                                ),
                              ),
                              const SizedBox(height: 14),
                              Row(
                                mainAxisAlignment: MainAxisAlignment.end,
                                children: [
                                  OutlinedButton(
                                    onPressed: () => _handleApprove(app.id, false),
                                    style: OutlinedButton.styleFrom(foregroundColor: theme.colorScheme.error, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)), minimumSize: const Size(0, 40)),
                                    child: const Text('驳回'),
                                  ),
                                  const SizedBox(width: 12),
                                  FilledButton(
                                    onPressed: () => _handleApprove(app.id, true),
                                    style: FilledButton.styleFrom(shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)), minimumSize: const Size(0, 40)),
                                    child: const Text('同意'),
                                  ),
                                ],
                              ),
                            ],
                          ),
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
