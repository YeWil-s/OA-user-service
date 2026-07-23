import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/auth_local_source.dart';
import '../data/auth_repository.dart';
import '../models/login_request.dart';
import 'auth_state.dart';

class AuthNotifier extends StateNotifier<AsyncValue<UserInfo?>> {
  final AuthRepository _authRepository;
  final AuthLocalSource _localSource;

  AuthNotifier(
    this._authRepository,
    this._localSource,
  ) : super(const AsyncValue.data(null));

  bool get isLoggedIn => state.valueOrNull != null;

  UserInfo? get currentUser => state.valueOrNull;

  Future<void> checkAutoLogin() async {
    final token = await _localSource.readToken();
    if (token == null || token.isEmpty) {
      state = const AsyncValue.data(null);
      return;
    }
    final userId = _localSource.readUserId();
    final username = _localSource.readUsername();
    final realName = _localSource.readRealName();
    final avatarUrl = _localSource.readAvatarUrl();

    if (userId != null && username != null && realName != null) {
      state = AsyncValue.data(UserInfo(
        userId: userId,
        username: username,
        realName: realName,
        avatarUrl: avatarUrl,
        roles: _localSource.readRoles(),
        permissions: _localSource.readPermissions(),
      ));
    } else {
      state = const AsyncValue.data(null);
    }
  }

  Future<void> login(String username, String password) async {
    state = const AsyncValue.loading();
    try {
      final response = await _authRepository.login(
        LoginRequest(username: username, password: password),
      );
      await _localSource.saveToken(response.accessToken);
      await _localSource.saveUserInfo(
        userId: response.userId,
        username: response.username,
        realName: response.realName,
        avatarUrl: response.avatarUrl,
        roles: response.roles,
        permissions: response.permissions,
      );
      state = AsyncValue.data(UserInfo.fromLoginResponse(response));
    } catch (e, st) {
      state = AsyncValue.error(e, st);
    }
  }

  void logout() {
    _localSource.clearAll();
    state = const AsyncValue.data(null);
  }

  void clearError() {
    state = AsyncValue.data(state.valueOrNull);
  }
}
