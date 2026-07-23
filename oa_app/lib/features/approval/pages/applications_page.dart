import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import '../../../../core/network/api_exception.dart';
import '../../auth/providers/auth_providers.dart';
import '../providers/approval_provider.dart';
import '../models/application.dart';

class ApplicationsPage extends ConsumerStatefulWidget {
  const ApplicationsPage({super.key});

  @override
  ConsumerState<ApplicationsPage> createState() => _ApplicationsPageState();
}

class _ApplicationsPageState extends ConsumerState<ApplicationsPage>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;

  static const _tabs = [
    ('全部', null),
    ('审批中', 1),
    ('已通过', 2),
    ('已驳回', 3),
  ];

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: _tabs.length, vsync: this);
    _tabController.addListener(() {
      if (!_tabController.indexIsChanging) {
        ref.read(myApplicationsProvider.notifier).fetch(status: _tabs[_tabController.index].$2);
      }
    });
    Future.microtask(() => ref.read(myApplicationsProvider.notifier).fetch());
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final appsAsync = ref.watch(myApplicationsProvider);
    final user = ref.watch(authProvider).valueOrNull;
    final canApprove = user?.canApprove ?? false;
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(
        title: const Text('我的申请'),
        actions: [
          if (canApprove)
            Container(
              margin: const EdgeInsets.only(right: 8),
              child: OutlinedButton.icon(
                onPressed: () => context.push('/pending'),
                icon: const Icon(Icons.fact_check_outlined, size: 16),
                label: const Text('待审批'),
                style: OutlinedButton.styleFrom(
                  visualDensity: VisualDensity.compact,
                  padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(10)),
                ),
              ),
            ),
        ],
        bottom: TabBar(
          controller: _tabController,
          isScrollable: true,
          labelStyle: const TextStyle(fontWeight: FontWeight.w600),
          tabs: _tabs.map((t) => Tab(text: t.$1)).toList(),
        ),
      ),
      body: appsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(Icons.error_outline, size: 48, color: theme.colorScheme.outline),
              const SizedBox(height: 12),
              Text(extractErrorMessage(e), style: TextStyle(color: theme.colorScheme.outline)),
              const SizedBox(height: 16),
              OutlinedButton(onPressed: () => ref.read(myApplicationsProvider.notifier).fetch(status: _tabs[_tabController.index].$2), child: const Text('重试')),
            ],
          ),
        ),
        data: (apps) => RefreshIndicator(
          onRefresh: () => ref.read(myApplicationsProvider.notifier).fetch(status: _tabs[_tabController.index].$2),
          child: apps.isEmpty
              ? ListView(children: [SizedBox(height: MediaQuery.of(context).size.height * 0.3, child: Center(child: Column(mainAxisSize: MainAxisSize.min, children: [Icon(Icons.inbox_outlined, size: 56, color: theme.colorScheme.outline), const SizedBox(height: 12), Text('暂无申请', style: TextStyle(color: theme.colorScheme.outline))])))])
              : ListView.builder(
                  padding: const EdgeInsets.symmetric(vertical: 8),
                  itemCount: apps.length,
                  itemBuilder: (context, index) => _AppCard(app: apps[index], onTap: () => context.push('/applications/${apps[index].id}')),
                ),
        ),
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () => context.push('/applications/submit'),
        icon: const Icon(Icons.add),
        label: const Text('提交申请'),
      ),
    );
  }
}

class _AppCard extends StatelessWidget {
  final Application app;
  final VoidCallback onTap;
  const _AppCard({required this.app, required this.onTap});

  static const _typeIcons = {1: Icons.beach_access, 2: Icons.timer, 3: Icons.airplanemode_active, 4: Icons.swap_horiz, 5: Icons.inventory_2};
  static const _statusColors = {'approved': Color(0xFF2E7D32), 'pending': Color(0xFFE65100), 'rejected': Color(0xFFC62828), 'cancelled': Color(0xFF757575)};

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final sc = _statusColors[app.status] ?? Colors.grey;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
      child: Card(
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(16),
          child: Padding(
            padding: const EdgeInsets.all(14),
            child: Row(
              children: [
                Container(
                  width: 44, height: 44,
                  decoration: BoxDecoration(color: sc.withValues(alpha: 0.1), borderRadius: BorderRadius.circular(12)),
                  child: Icon(_typeIcons[app.appType] ?? Icons.edit_note, color: sc, size: 22),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(app.leaveTypeLabel, style: const TextStyle(fontWeight: FontWeight.w600, fontSize: 15)),
                      const SizedBox(height: 4),
                      Text('${DateFormat('MM-dd HH:mm').format(app.startTime)} ~ ${DateFormat('MM-dd HH:mm').format(app.endTime)}', style: theme.textTheme.bodySmall),
                      if (app.reason.isNotEmpty)
                        Text(app.reason, maxLines: 1, overflow: TextOverflow.ellipsis, style: TextStyle(fontSize: 12, color: theme.colorScheme.outline)),
                    ],
                  ),
                ),
                const SizedBox(width: 8),
                Container(
                  padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(color: sc.withValues(alpha: 0.08), borderRadius: BorderRadius.circular(20)),
                  child: Text(app.statusLabel, style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: sc)),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
