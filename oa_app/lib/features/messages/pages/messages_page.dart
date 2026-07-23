import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import '../../../../core/network/api_exception.dart';
import '../models/message.dart';
import '../providers/message_provider.dart';

class MessagesPage extends ConsumerWidget {
  const MessagesPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final messagesAsync = ref.watch(messageProvider);
    final theme = Theme.of(context);

    return DefaultTabController(
      length: 3,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('消息'),
          bottom: const TabBar(
            tabs: [Tab(text: '审批通知'), Tab(text: '考勤通知'), Tab(text: '系统通知')],
          ),
        ),
        body: messagesAsync.when(
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (e, _) => Center(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(Icons.wifi_off_rounded, size: 56, color: theme.colorScheme.outline),
                const SizedBox(height: 12),
                Text(extractErrorMessage(e), style: TextStyle(color: theme.colorScheme.outline, fontSize: 15)),
                const SizedBox(height: 16),
                OutlinedButton(onPressed: () => ref.read(messageProvider.notifier).fetch(), child: const Text('重试')),
              ],
            ),
          ),
          data: (messages) => TabBarView(
            children: [1, 2, 3].map((type) {
              final filtered = messages.where((m) => m.msgType == type).toList();
              if (filtered.isEmpty) {
                return Center(
                  child: Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Icon(Icons.inbox_outlined, size: 56, color: theme.colorScheme.outline),
                      const SizedBox(height: 12),
                      Text('暂无消息', style: TextStyle(color: theme.colorScheme.outline)),
                    ],
                  ),
                );
              }
              return RefreshIndicator(
                onRefresh: () => ref.read(messageProvider.notifier).fetch(),
                child: ListView.separated(
                  padding: const EdgeInsets.symmetric(vertical: 8),
                  itemCount: filtered.length,
                  separatorBuilder: (_, __) => const SizedBox(height: 2),
                  itemBuilder: (context, index) => _MessageCard(
                    message: filtered[index],
                    onTap: () => ref.read(messageProvider.notifier).markAsRead(filtered[index].id),
                  ),
                ),
              );
            }).toList(),
          ),
        ),
      ),
    );
  }
}

class _MessageCard extends StatelessWidget {
  final AppMessage message;
  final VoidCallback onTap;
  const _MessageCard({required this.message, required this.onTap});

  static const _typeIcons = {1: Icons.assignment_turned_in, 2: Icons.access_time, 3: Icons.campaign};
  static const _typeColors = {1: Color(0xFF1565C0), 2: Color(0xFFE65100), 3: Color(0xFF2E7D32)};

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final icon = _typeIcons[message.msgType] ?? Icons.notifications;
    final iconColor = _typeColors[message.msgType] ?? theme.colorScheme.primary;

    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 12),
      child: Card(
        child: InkWell(
          onTap: onTap,
          borderRadius: BorderRadius.circular(16),
          child: Padding(
            padding: const EdgeInsets.all(14),
            child: Row(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Container(
                  width: 42, height: 42,
                  decoration: BoxDecoration(color: iconColor.withValues(alpha: 0.1), borderRadius: BorderRadius.circular(12)),
                  child: Stack(
                    children: [
                      Center(child: Icon(icon, color: iconColor, size: 22)),
                      if (!message.isRead) Positioned(right: 8, top: 8, child: Container(width: 8, height: 8, decoration: const BoxDecoration(color: Colors.red, shape: BoxShape.circle))),
                    ],
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
                    Row(children: [
                      Expanded(child: Text(message.title, style: TextStyle(fontWeight: message.isRead ? FontWeight.normal : FontWeight.w600, fontSize: 15))),
                      Text(DateFormat('MM-dd HH:mm').format(message.createTime), style: theme.textTheme.bodySmall?.copyWith(color: theme.colorScheme.outline, fontSize: 12)),
                    ]),
                    const SizedBox(height: 6),
                    Text(message.content, maxLines: 2, overflow: TextOverflow.ellipsis, style: TextStyle(color: theme.colorScheme.outline, fontSize: 13, height: 1.4)),
                  ]),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
