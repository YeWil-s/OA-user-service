import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../auth/providers/auth_providers.dart';
import '../data/message_repository.dart';
import '../models/message.dart';

final messageRepositoryProvider = Provider<MessageRepository>((ref) {
  return MessageRepository(ref.watch(dioProvider));
});

class MessageNotifier extends StateNotifier<AsyncValue<List<AppMessage>>> {
  final MessageRepository _repo;

  MessageNotifier(this._repo) : super(const AsyncValue.loading()) {
    fetch();
  }

  List<AppMessage> getByType(int msgType) {
    return state.valueOrNull
            ?.where((m) => m.msgType == msgType)
            .toList() ??
        [];
  }

  Future<void> fetch() async {
    state = const AsyncValue.loading();
    try {
      final pageData = await _repo.getMessages();
      state = AsyncValue.data(pageData.records);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<void> markAsRead(int id) async {
    await _repo.markAsRead(id);
    final current = state.valueOrNull;
    if (current == null) return;
    final updated = current.map((m) {
      if (m.id == id) {
        return AppMessage(
          id: m.id,
          title: m.title,
          content: m.content,
          msgType: m.msgType,
          relatedId: m.relatedId,
          isRead: true,
          createTime: m.createTime,
        );
      }
      return m;
    }).toList();
    state = AsyncValue.data(updated);
  }
}

final messageProvider =
    StateNotifierProvider<MessageNotifier, AsyncValue<List<AppMessage>>>((ref) {
  return MessageNotifier(ref.watch(messageRepositoryProvider));
});
