import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import '../providers/home_provider.dart';

class HomePage extends ConsumerStatefulWidget {
  const HomePage({super.key});

  @override
  ConsumerState<HomePage> createState() => _HomePageState();
}

class _HomePageState extends ConsumerState<HomePage> {
  @override
  void initState() {
    super.initState();
    Future.microtask(() => ref.read(homeProvider.notifier).loadData());
  }

  String _formatTime(DateTime? dt) {
    if (dt == null) return '--:--';
    return DateFormat('HH:mm').format(dt);
  }

  @override
  Widget build(BuildContext context) {
    final homeState = ref.watch(homeProvider);
    final theme = Theme.of(context);
    final punch = homeState.punchStatus;
    final summary = homeState.monthlySummary;

    return Scaffold(
      appBar: AppBar(
        title: const Text('首页'),
        actions: [
          IconButton(
            icon: const Icon(Icons.auto_awesome),
            tooltip: 'AI 助手',
            onPressed: () => context.push('/ai/chat'),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () => ref.read(homeProvider.notifier).loadData(),
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // 今日打卡状态卡片
            Text(
              '今日打卡',
              style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(20),
                child: Column(
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceAround,
                      children: [
                        _buildTimeBlock(
                          '上班打卡',
                          _formatTime(punch.punchInTime),
                          punch.punchInTime != null ? Icons.check_circle : Icons.radio_button_unchecked,
                          punch.punchInTime != null
                              ? theme.colorScheme.primary
                              : theme.colorScheme.outline,
                        ),
                        Container(
                          width: 1,
                          height: 40,
                          color: theme.dividerColor,
                        ),
                        _buildTimeBlock(
                          '下班打卡',
                          _formatTime(punch.punchOutTime),
                          punch.punchOutTime != null ? Icons.check_circle : Icons.radio_button_unchecked,
                          punch.punchOutTime != null
                              ? theme.colorScheme.primary
                              : theme.colorScheme.outline,
                        ),
                      ],
                    ),
                    const SizedBox(height: 20),
                    if (punch.isCompleted)
                      Chip(
                        avatar: const Icon(Icons.check, size: 18),
                        label: const Text('今日打卡完成'),
                        backgroundColor: theme.colorScheme.primaryContainer,
                      )
                    else
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                        children: [
                          if (punch.canPunchIn || punch.canPunchOut)
                            FilledButton.icon(
                              onPressed: punch.canPunchIn
                                  ? () => ref.read(homeProvider.notifier).punchIn()
                                  : () => ref.read(homeProvider.notifier).punchOut(),
                              icon: const Icon(Icons.fingerprint),
                              label: Text(punch.canPunchIn ? '上班打卡' : '下班打卡'),
                            ),
                        ],
                      ),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 16),
            Text(
              '本月考勤汇总',
              style: theme.textTheme.titleMedium?.copyWith(fontWeight: FontWeight.bold),
            ),
            const SizedBox(height: 8),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(16),
                child: Column(
                  children: [
                    _buildSummaryRow('出勤天数', '${summary.normalDays} 天', theme.colorScheme.primary),
                    const Divider(),
                    _buildSummaryRow('迟到', '${summary.lateDays} 次', theme.colorScheme.error),
                    const Divider(),
                    _buildSummaryRow('早退', '${summary.earlyDays} 次', Colors.orange),
                    const Divider(),
                    _buildSummaryRow('请假', '${summary.leaveDays} 天', Colors.blue),
                    const Divider(),
                    _buildSummaryRow('加班', '${summary.overtimeHours} 小时', theme.colorScheme.tertiary),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTimeBlock(String label, String time, IconData icon, Color color) {
    return Column(
      children: [
        Text(label, style: TextStyle(color: Theme.of(context).colorScheme.outline)),
        const SizedBox(height: 8),
        Icon(icon, color: color, size: 32),
        const SizedBox(height: 4),
        Text(time, style: const TextStyle(fontSize: 20, fontWeight: FontWeight.w600)),
      ],
    );
  }

  Widget _buildSummaryRow(String label, String value, Color valueColor) {
    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(label),
          Text(value, style: TextStyle(fontWeight: FontWeight.w600, color: valueColor)),
        ],
      ),
    );
  }
}
