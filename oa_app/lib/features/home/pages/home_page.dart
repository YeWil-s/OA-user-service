import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:intl/intl.dart';
import '../../../core/network/api_exception.dart';
import '../../../core/utils/location_service.dart';
import '../models/schedule.dart';
import '../providers/home_provider.dart';
import '../../attendance/providers/attendance_provider.dart';
import '../../attendance/models/attendance_record.dart';

class HomePage extends ConsumerStatefulWidget {
  const HomePage({super.key});

  @override
  ConsumerState<HomePage> createState() => _HomePageState();
}

class _HomePageState extends ConsumerState<HomePage> {
  @override
  void initState() {
    super.initState();
    final now = DateTime.now();
    final month = DateFormat('yyyy-MM').format(now);
    Future.microtask(() {
      ref.read(homeProvider.notifier).loadData();
      ref.read(attendanceListProvider.notifier).loadRecords(month: month);
    });
  }

  String _fmt(DateTime? dt) {
    if (dt == null) return '--:--';
    return DateFormat('HH:mm').format(dt);
  }

  Future<void> _punchWithLocation(bool isIn) async {
    try {
      await ref.read(homeProvider.notifier).fetchPunchLocation();
    } catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(extractErrorMessage(e)), backgroundColor: Colors.red));
      return;
    }
    final loc = ref.read(homeProvider).punchLocation;
    if (loc == null || !mounted) return;

    final selected = await _showLocationPicker(loc);
    if (selected == null) return;

    try {
      if (isIn) {
        await ref.read(homeProvider.notifier).punchIn(selected);
      } else {
        await ref.read(homeProvider.notifier).punchOut(selected);
      }
      _refreshRecords();
    } catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(extractErrorMessage(e)), backgroundColor: Colors.red));
    }
  }

  Future<String?> _showLocationPicker(LocationResult loc) async {
    final theme = Theme.of(context);
    String? selected = loc.address;

    return showModalBottomSheet<String>(
      context: context,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (ctx) {
        return StatefulBuilder(
          builder: (ctx, setDialogState) {
            final places = <String>[];
            if (selected != null && selected!.isNotEmpty) places.add(selected!);
            for (final p in loc.nearbyPlaces) {
              if (!places.contains(p.displayName)) places.add(p.displayName);
            }
            if (places.isEmpty) places.add(loc.locationStr);

            return Container(
              constraints: BoxConstraints(maxHeight: MediaQuery.of(context).size.height * 0.65),
              decoration: const BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
              ),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  // 顶部拖拽指示条
                  Container(
                    margin: const EdgeInsets.only(top: 12),
                    width: 40,
                    height: 4,
                    decoration: BoxDecoration(color: Colors.grey.shade300, borderRadius: BorderRadius.circular(2)),
                  ),
                  // 标题行
                  Padding(
                    padding: const EdgeInsets.fromLTRB(20, 16, 20, 0),
                    child: Row(children: [
                      Container(
                        width: 40, height: 40,
                        decoration: BoxDecoration(
                          gradient: LinearGradient(colors: [theme.colorScheme.primary, theme.colorScheme.primary.withValues(alpha: 0.7)]),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: const Icon(Icons.my_location, color: Colors.white, size: 22),
                      ),
                      const SizedBox(width: 12),
                      Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                        const Text('选择打卡地点', style: TextStyle(fontSize: 17, fontWeight: FontWeight.w700)),
                        Text(loc.locationStr, style: TextStyle(fontSize: 12, color: theme.colorScheme.outline)),
                      ]),
                      const Spacer(),
                      IconButton(
                        icon: const Icon(Icons.refresh_rounded, size: 22),
                        onPressed: () async {
                          Navigator.pop(ctx);
                          if (mounted) {
                            ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('正在刷新定位...'), duration: Duration(seconds: 1)));
                          }
                        },
                        tooltip: '重新定位',
                      ),
                    ]),
                  ),
                  const SizedBox(height: 12),
                  const Divider(height: 1),
                  // 地点列表
                  Flexible(
                    child: ListView.builder(
                      shrinkWrap: true,
                      padding: const EdgeInsets.symmetric(vertical: 8),
                      itemCount: places.length,
                      itemBuilder: (ctx, i) {
                        final place = places[i];
                        final isFirst = i == 0;
                        final checked = place == selected;
                        return InkWell(
                          onTap: () => setDialogState(() => selected = place),
                          child: Container(
                            margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 3),
                            padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
                            decoration: BoxDecoration(
                              color: checked ? theme.colorScheme.primaryContainer.withValues(alpha: 0.4) : Colors.transparent,
                              borderRadius: BorderRadius.circular(14),
                              border: checked ? Border.all(color: theme.colorScheme.primary, width: 1.5) : null,
                            ),
                            child: Row(children: [
                              Container(
                                width: 38, height: 38,
                                decoration: BoxDecoration(
                                  color: isFirst ? const Color(0xFF1565C0).withValues(alpha: 0.1) : Colors.grey.shade100,
                                  borderRadius: BorderRadius.circular(10),
                                ),
                                child: Icon(
                                  isFirst ? Icons.near_me : Icons.place_outlined,
                                  color: isFirst ? const Color(0xFF1565C0) : Colors.grey.shade500,
                                  size: 20,
                                ),
                              ),
                              const SizedBox(width: 12),
                              Expanded(
                                child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                                  Text(place, style: TextStyle(fontSize: 14, fontWeight: checked ? FontWeight.w600 : FontWeight.normal, color: checked ? theme.colorScheme.primary : null)),
                                  if (isFirst) const Text('当前定位', style: TextStyle(fontSize: 11, color: Colors.green)),
                                ]),
                              ),
                              if (checked) Icon(Icons.check_circle, color: theme.colorScheme.primary, size: 22),
                            ]),
                          ),
                        );
                      },
                    ),
                  ),
                  // 底部确认按钮
                  Container(
                    padding: const EdgeInsets.fromLTRB(16, 12, 16, 20),
                    decoration: BoxDecoration(border: Border(top: BorderSide(color: Colors.grey.shade100))),
                    child: SafeArea(
                      child: Row(children: [
                        Expanded(
                          child: SizedBox(
                            height: 50,
                            child: OutlinedButton(
                              onPressed: () => Navigator.pop(ctx),
                              style: OutlinedButton.styleFrom(shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14))),
                              child: const Text('取消'),
                            ),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: SizedBox(
                            height: 50,
                            child: Container(
                              decoration: BoxDecoration(
                                gradient: LinearGradient(colors: [theme.colorScheme.primary, theme.colorScheme.primary.withValues(alpha: 0.8)]),
                                borderRadius: BorderRadius.circular(14),
                              ),
                              child: ElevatedButton(
                                onPressed: () => Navigator.pop(ctx, selected),
                                style: ElevatedButton.styleFrom(backgroundColor: Colors.transparent, shadowColor: Colors.transparent, foregroundColor: Colors.white, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14))),
                                child: const Text('确认打卡', style: TextStyle(fontSize: 16, fontWeight: FontWeight.w600)),
                              ),
                            ),
                          ),
                        ),
                      ]),
                    ),
                  ),
                ],
              ),
            );
          },
        );
      },
    );
  }

  void _refreshRecords() {
    final now = DateTime.now();
    ref.read(attendanceListProvider.notifier).loadRecords(month: DateFormat('yyyy-MM').format(now));
  }

  @override
  Widget build(BuildContext context) {
    final homeState = ref.watch(homeProvider);
    final attState = ref.watch(attendanceListProvider);
    final theme = Theme.of(context);
    final punch = homeState.punchStatus;
    final summary = homeState.monthlySummary;
    final schedules = homeState.weeklySchedules;

    return Scaffold(
      appBar: AppBar(
        title: const Text('首页'),
        actions: [
          Container(
            margin: const EdgeInsets.only(right: 4),
            child: IconButton(
              icon: const Icon(Icons.auto_awesome, size: 24),
              tooltip: 'AI 助手',
              onPressed: () => context.push('/ai/chat'),
              style: IconButton.styleFrom(backgroundColor: theme.colorScheme.primaryContainer.withValues(alpha: 0.4)),
            ),
          ),
        ],
      ),
      body: RefreshIndicator(
        onRefresh: () async {
          await ref.read(homeProvider.notifier).loadData();
          final now = DateTime.now();
          ref.read(attendanceListProvider.notifier).loadRecords(month: DateFormat('yyyy-MM').format(now));
        },
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            // 今日打卡卡片
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(24),
              decoration: BoxDecoration(
                gradient: LinearGradient(
                  colors: [theme.colorScheme.primary, theme.colorScheme.primary.withValues(alpha: 0.8)],
                ),
                borderRadius: BorderRadius.circular(20),
                boxShadow: [BoxShadow(color: theme.colorScheme.primary.withValues(alpha: 0.3), blurRadius: 20, offset: const Offset(0, 6))],
              ),
              child: Column(
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceAround,
                    children: [
                      _timeBlock('上班打卡', _fmt(punch.punchInTime), punch.punchInTime != null, Colors.white),
                      Container(width: 1, height: 48, color: Colors.white.withValues(alpha: 0.2)),
                      _timeBlock('下班打卡', _fmt(punch.punchOutTime), punch.punchOutTime != null, Colors.white),
                    ],
                  ),
                  const SizedBox(height: 20),
                  if (homeState.isPunching)
                    Column(children: [
                      const SizedBox(width: 24, height: 24, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white)),
                      const SizedBox(height: 8),
                      Text('正在定位...', style: TextStyle(color: Colors.white.withValues(alpha: 0.8), fontSize: 12)),
                    ])
                  else if (punch.isCompleted)
                    Container(padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8), decoration: BoxDecoration(color: Colors.white.withValues(alpha: 0.2), borderRadius: BorderRadius.circular(20)), child: const Text('今日打卡完成', style: TextStyle(color: Colors.white, fontWeight: FontWeight.w600)))
                  else
                    SizedBox(
                      height: 48,
                      width: 200,
                      child: ElevatedButton.icon(
                        onPressed: punch.canPunchIn ? () => _punchWithLocation(true) : () => _punchWithLocation(false),
                        icon: const Icon(Icons.my_location, size: 20),
                        label: Text(punch.canPunchIn ? '打卡上班' : '打卡下班', style: const TextStyle(fontSize: 15, color: Colors.black87)),
                        style: ElevatedButton.styleFrom(backgroundColor: Colors.white, shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)), elevation: 0),
                      ),
                    ),
                ],
              ),
            ),
            const SizedBox(height: 20),

            // 本周排班
            _sectionHeader('本周排班'),
            const SizedBox(height: 8),
            _buildWeeklySchedule(theme, schedules),
            const SizedBox(height: 20),

            // 月度汇总
            _sectionHeader('本月考勤汇总'),
            const SizedBox(height: 8),
            Card(
              child: Padding(
                padding: const EdgeInsets.all(18),
                child: Row(
                  children: [
                    Expanded(child: _summaryItem('出勤', '${summary.normalDays}天', theme.colorScheme.primary)),
                    Expanded(child: _summaryItem('迟到', '${summary.lateDays}次', theme.colorScheme.error)),
                    Expanded(child: _summaryItem('早退', '${summary.earlyDays}次', Colors.orange)),
                    Expanded(child: _summaryItem('请假', '${summary.leaveDays}天', Colors.blue)),
                    Expanded(child: _summaryItem('加班', '${summary.overtimeHours}h', theme.colorScheme.tertiary)),
                  ],
                ),
              ),
            ),
            const SizedBox(height: 20),

            // 每日记录
            _sectionHeader('每日记录'),
            const SizedBox(height: 8),
            if (attState.isLoading && attState.records.isEmpty)
              const Padding(padding: EdgeInsets.all(40), child: Center(child: CircularProgressIndicator()))
            else if (attState.records.isEmpty)
              Card(child: Padding(padding: const EdgeInsets.all(40), child: Center(child: Text('暂无考勤记录', style: TextStyle(color: theme.colorScheme.outline)))))
            else
              ...attState.records.map((r) => _buildRecordCard(theme, r)),
          ],
        ),
      ),
    );
  }

  Widget _timeBlock(String label, String time, bool checked, Color color) {
    return Column(children: [
      Text(label, style: TextStyle(fontSize: 13, color: color.withValues(alpha: 0.7))),
      const SizedBox(height: 8),
      Icon(checked ? Icons.check_circle : Icons.radio_button_unchecked, color: color, size: 28),
      const SizedBox(height: 6),
      Text(time, style: TextStyle(fontSize: 22, fontWeight: FontWeight.w700, color: color)),
    ]);
  }

  Widget _sectionHeader(String title) {
    return Row(children: [
      Container(width: 3, height: 16, decoration: BoxDecoration(color: Theme.of(context).colorScheme.primary, borderRadius: BorderRadius.circular(2))),
      const SizedBox(width: 8),
      Text(title, style: Theme.of(context).textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w700)),
    ]);
  }

  Widget _summaryItem(String label, String value, Color color) {
    return Column(children: [
      Text(value, style: TextStyle(fontSize: 16, fontWeight: FontWeight.w700, color: color)),
      const SizedBox(height: 4),
      Text(label, style: TextStyle(fontSize: 12, color: Theme.of(context).colorScheme.outline)),
    ]);
  }

  Widget _buildRecordCard(ThemeData theme, AttendanceRecord record) {
    final dateStr = DateFormat('MM月dd日 EEEE', 'zh_CN').format(record.punchDate);
    final sc = _statusColor(record.status);
    return Card(
      margin: const EdgeInsets.only(bottom: 6),
      child: Padding(
        padding: const EdgeInsets.all(14),
        child: Row(
          children: [
            Column(children: [
              Text(dateStr.substring(0, 6), style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w600)),
              Text(dateStr.substring(6), style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.outline)),
            ]),
            const SizedBox(width: 4),
            Container(width: 1, height: 36, color: theme.dividerColor),
            const SizedBox(width: 12),
            Expanded(
              child: Row(children: [
                Expanded(child: Column(children: [
                  Icon(Icons.login, size: 16, color: theme.colorScheme.primary),
                  const SizedBox(height: 2),
                  Text(_fmt(record.punchInTime), style: theme.textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.w600)),
                  if (record.punchInType == 2) Text('外勤', style: TextStyle(fontSize: 10, color: theme.colorScheme.outline)),
                ])),
                Expanded(child: Column(children: [
                  Icon(Icons.logout, size: 16, color: theme.colorScheme.secondary),
                  const SizedBox(height: 2),
                  Text(_fmt(record.punchOutTime), style: theme.textTheme.bodyMedium?.copyWith(fontWeight: FontWeight.w600)),
                  if (record.punchOutType == 2) Text('外勤', style: TextStyle(fontSize: 10, color: theme.colorScheme.outline)),
                ])),
              ]),
            ),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
              decoration: BoxDecoration(color: sc.withValues(alpha: 0.08), borderRadius: BorderRadius.circular(20)),
              child: Text(record.statusLabel, style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: sc)),
            ),
          ],
        ),
      ),
    );
  }

  Color _statusColor(String status) {
    switch (status) {
      case 'normal': return const Color(0xFF2E7D32);
      case 'late': return const Color(0xFFE65100);
      case 'early': return Colors.amber.shade700;
      case 'absent': return const Color(0xFFC62828);
      case 'leave': return const Color(0xFF1565C0);
      default: return Colors.grey;
    }
  }

  Widget _buildWeeklySchedule(ThemeData theme, List<Schedule> schedules) {
    final labels = ['一', '二', '三', '四', '五', '六', '日'];
    if (schedules.isEmpty) {
      return Card(
        child: Padding(
          padding: const EdgeInsets.all(20),
          child: Center(child: Text('暂无排班信息', style: TextStyle(color: theme.colorScheme.outline))),
        ),
      );
    }
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: SingleChildScrollView(
          scrollDirection: Axis.horizontal,
          child: Row(
            children: List.generate(7, (i) {
              final s = schedules.length > i ? schedules[i] : null;
              final isToday = s != null && s.scheduleDate == _todayStr();
              final isLeave = s?.isLeave ?? false;
              return Container(
                width: 72,
                margin: const EdgeInsets.symmetric(horizontal: 3),
                decoration: BoxDecoration(
                  color: isToday
                      ? theme.colorScheme.primaryContainer.withValues(alpha: 0.5)
                      : isLeave
                          ? Colors.red.shade50
                          : theme.colorScheme.surfaceContainerHighest.withValues(alpha: 0.3),
                  borderRadius: BorderRadius.circular(10),
                  border: isToday ? Border.all(color: theme.colorScheme.primary, width: 1.5) : null,
                ),
                padding: const EdgeInsets.symmetric(vertical: 10, horizontal: 4),
                child: Column(
                  mainAxisSize: MainAxisSize.min,
                  children: [
                    Text(labels[i], style: TextStyle(fontSize: 12, fontWeight: FontWeight.w600, color: isToday ? theme.colorScheme.primary : theme.colorScheme.outline)),
                    const SizedBox(height: 4),
                    Text(s?.scheduleDate.substring(8) ?? '-', style: TextStyle(fontSize: 11, color: theme.colorScheme.outline)),
                    const SizedBox(height: 6),
                    if (isLeave)
                      Container(
                        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                        decoration: BoxDecoration(color: Colors.red.shade100, borderRadius: BorderRadius.circular(8)),
                        child: const Text('请假', style: TextStyle(fontSize: 11, fontWeight: FontWeight.w600, color: Colors.red)),
                      )
                    else if (s != null)
                      Column(children: [
                        Text(s.shiftName, style: TextStyle(fontSize: 12, fontWeight: FontWeight.w700, color: theme.colorScheme.primary), textAlign: TextAlign.center, maxLines: 1, overflow: TextOverflow.ellipsis),
                        const SizedBox(height: 2),
                        Text('${s.startTime.substring(0, 5)}', style: TextStyle(fontSize: 10, color: theme.colorScheme.outline)),
                        Text('${s.endTime.substring(0, 5)}', style: TextStyle(fontSize: 10, color: theme.colorScheme.outline)),
                        if (s.isDefault) Text('默认', style: TextStyle(fontSize: 9, color: theme.colorScheme.outline)),
                      ])
                    else
                      const Text('-', style: TextStyle(fontSize: 12, color: Colors.grey)),
                  ],
                ),
              );
            }),
          ),
        ),
      ),
    );
  }

  String _todayStr() {
    final n = DateTime.now();
    return '${n.year}-${n.month.toString().padLeft(2, '0')}-${n.day.toString().padLeft(2, '0')}';
  }
}
