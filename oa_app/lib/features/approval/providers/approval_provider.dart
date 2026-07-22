import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../auth/providers/auth_providers.dart';
import '../data/application_repository.dart';
import '../models/application.dart';

final applicationRepositoryProvider = Provider<ApplicationRepository>((ref) {
  return ApplicationRepository(ref.watch(dioProvider));
});

// ---- 我的申请 ----

class MyApplicationsNotifier extends StateNotifier<AsyncValue<List<Application>>> {
  final ApplicationRepository _repo;

  MyApplicationsNotifier(this._repo) : super(const AsyncValue.loading());

  Future<void> fetch({String? status}) async {
    state = const AsyncValue.loading();
    try {
      final list = await _repo.getMyApplications(status: status);
      state = AsyncValue.data(list);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<void> cancel(int id) async {
    await _repo.cancel(id);
    await fetch();
  }
}

final myApplicationsProvider =
    StateNotifierProvider<MyApplicationsNotifier, AsyncValue<List<Application>>>((ref) {
  return MyApplicationsNotifier(ref.watch(applicationRepositoryProvider));
});

// ---- 待审批 ----

class PendingNotifier extends StateNotifier<AsyncValue<List<Application>>> {
  final ApplicationRepository _repo;

  PendingNotifier(this._repo) : super(const AsyncValue.loading());

  Future<void> fetch() async {
    state = const AsyncValue.loading();
    try {
      final list = await _repo.getPending();
      state = AsyncValue.data(list);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<void> approve(int id, {required int action, required String comment}) async {
    await _repo.approve(id, action: action, comment: comment);
    await fetch();
  }
}

final pendingProvider =
    StateNotifierProvider<PendingNotifier, AsyncValue<List<Application>>>((ref) {
  return PendingNotifier(ref.watch(applicationRepositoryProvider));
});
