import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import '../models/message.dart';
import '../providers/message_provider.dart';

class MessagesPage extends ConsumerWidget {
  const MessagesPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final messagesAsync = ref.watch(messageProvider);

    return DefaultTabController(
      length: 3,
      child: Scaffold(
        appBar: AppBar(
          title: const Text('消息'),
          bottom: const TabBar(
            tabs: [
              Tab(text: '审批通知'),
              Tab(text: '考勤通知'),
              Tab(text: '系统通知'),
            ],
          ),
        ),
        body: messagesAsync.when(
          loading: () => const Center(child: CircularProgressIndicator()),
          error: (e, _) => Center(child: Text('加载失败: $e')),
          data: (messages) => TabBarView(
            children: [1, 2, 3].map((type) {
              final filtered = messages.where((m) => m.msgType == type).toList();
              if (filtered.isEmpty) {
                return const Center(child: Text('暂无消息'));
              }
              return RefreshIndicator(
                onRefresh: () => ref.read(messageProvider.notifier).fetch(),
                child: ListView.builder(
                  itemCount: filtered.length,
                  itemBuilder: (context, index) {
                    final msg = filtered[index];
                    return _MessageTile(
                      message: msg,
                      onTap: () => ref.read(messageProvider.notifier).markAsRead(msg.id),
                    );
                  },
                ),
              );
            }).toList(),
          ),
        ),
      ),
    );
  }
}

class _MessageTile extends StatelessWidget {
  final AppMessage message;
  final VoidCallback onTap;

  const _MessageTile({required this.message, required this.onTap});

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    return ListTile(
      leading: Stack(
        children: [
          Icon(
            message.msgType == 1
                ? Icons.assignment_turned_in
                : message.msgType == 2
                    ? Icons.access_time
                    : Icons.campaign,
            color: theme.colorScheme.primary,
          ),
          if (!message.isRead)
            Positioned(
              right: 0,
              top: 0,
              child: Container(
                width: 8,
                height: 8,
                decoration: const BoxDecoration(
                  color: Colors.red,
                  shape: BoxShape.circle,
                ),
              ),
            ),
        ],
      ),
      title: Text(
        message.title,
        style: TextStyle(
          fontWeight: message.isRead ? FontWeight.normal : FontWeight.bold,
        ),
      ),
      subtitle: Text(
        message.content,
        maxLines: 1,
        overflow: TextOverflow.ellipsis,
      ),
      trailing: Text(
        DateFormat('MM-dd HH:mm').format(message.createTime),
        style: theme.textTheme.bodySmall,
      ),
      onTap: onTap,
    );
  }
}
