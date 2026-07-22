import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
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
    ('审批中', 'pending'),
    ('已通过', 'approved'),
    ('已驳回', 'rejected'),
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

    return Scaffold(
      appBar: AppBar(
        title: const Text('我的申请'),
        bottom: TabBar(
          controller: _tabController,
          isScrollable: true,
          tabs: _tabs.map((t) => Tab(text: t.$1)).toList(),
        ),
      ),
      body: appsAsync.when(
        loading: () => const Center(child: CircularProgressIndicator()),
        error: (e, _) => Center(child: Text('加载失败: $e')),
        data: (apps) => RefreshIndicator(
          onRefresh: () => ref.read(myApplicationsProvider.notifier).fetch(
                status: _tabs[_tabController.index].$2,
              ),
          child: apps.isEmpty
              ? const Center(child: Text('暂无申请'))
              : ListView.builder(
                  itemCount: apps.length,
                  itemBuilder: (context, index) {
                    final app = apps[index];
                    return _ApplicationCard(
                      application: app,
                      onTap: () => context.push('/applications/${app.id}'),
                    );
                  },
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

class _ApplicationCard extends StatelessWidget {
  final Application application;
  final VoidCallback onTap;

  const _ApplicationCard({required this.application, required this.onTap});

  Color _statusColor(String status) {
    switch (status) {
      case 'approved': return Colors.green;
      case 'pending': return Colors.orange;
      case 'rejected': return Colors.red;
      case 'cancelled': return Colors.grey;
      default: return Colors.grey;
    }
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final app = application;
    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Container(
                width: 40,
                height: 40,
                decoration: BoxDecoration(
                  color: _statusColor(app.status).withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Icon(
                  app.appType == 1 ? Icons.beach_access : Icons.timer,
                  color: _statusColor(app.status),
                  size: 20,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      app.leaveTypeLabel,
                      style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w600),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '${DateFormat('MM-dd HH:mm').format(app.startTime)} - ${DateFormat('MM-dd HH:mm').format(app.endTime)}',
                      style: theme.textTheme.bodySmall,
                    ),
                    Text(
                      app.reason,
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                      style: theme.textTheme.bodySmall?.copyWith(
                        color: theme.colorScheme.outline,
                      ),
                    ),
                  ],
                ),
              ),
              Chip(
                label: Text(
                  app.statusLabel,
                  style: TextStyle(
                    fontSize: 12,
                    color: _statusColor(app.status),
                  ),
                ),
                backgroundColor: _statusColor(app.status).withValues(alpha: 0.1),
                side: BorderSide.none,
                padding: EdgeInsets.zero,
                visualDensity: VisualDensity.compact,
              ),
            ],
          ),
        ),
      ),
    );
  }
}
