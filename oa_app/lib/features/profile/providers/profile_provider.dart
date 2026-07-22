import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../auth/providers/auth_providers.dart';
import '../data/user_repository.dart';

final userRepositoryProvider = Provider<UserRepository>((ref) {
  return UserRepository(ref.watch(dioProvider));
});

class ProfileNotifier extends StateNotifier<AsyncValue<void>> {
  final UserRepository _repo;

  ProfileNotifier(this._repo) : super(const AsyncValue.data(null));

  Future<void> changePassword(int userId, String oldPassword, String newPassword) async {
    state = const AsyncValue.loading();
    try {
      await _repo.changePassword(userId, oldPassword, newPassword);
      state = const AsyncValue.data(null);
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  void reset() {
    state = const AsyncValue.data(null);
  }
}

final profileProvider = StateNotifierProvider<ProfileNotifier, AsyncValue<void>>((ref) {
  return ProfileNotifier(ref.watch(userRepositoryProvider));
});
