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

  Future<void> fetch({int? status, bool viewAll = false}) async {
    state = const AsyncValue.loading();
    try {
      final pageData = viewAll
          ? await _repo.getAllApplications(status: status)
          : await _repo.getMyApplications(status: status);
      state = AsyncValue.data(pageData.records);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<void> cancel(int id, {bool viewAll = false}) async {
    await _repo.cancel(id);
    await fetch(viewAll: viewAll);
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

  Future<void> fetch({bool viewAll = false}) async {
    state = const AsyncValue.loading();
    try {
      final pageData = viewAll
          ? await _repo.getAllPending()
          : await _repo.getPending();
      state = AsyncValue.data(pageData.records);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  Future<void> approve(int id, {required bool approved, required String comment, bool viewAll = false}) async {
    await _repo.approve(id, approved: approved, comment: comment);
    await fetch(viewAll: viewAll);
  }
}

final pendingProvider =
    StateNotifierProvider<PendingNotifier, AsyncValue<List<Application>>>((ref) {
  return PendingNotifier(ref.watch(applicationRepositoryProvider));
});
