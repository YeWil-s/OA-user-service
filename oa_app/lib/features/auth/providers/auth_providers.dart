import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/interceptors/auth_interceptor.dart';
import '../../../core/network/interceptors/error_interceptor.dart';
import '../../../core/network/auth_callback.dart';
import '../data/auth_local_source.dart';
import '../data/auth_repository.dart';
import 'auth_provider.dart';
import 'auth_state.dart';

final sharedPreferencesProvider = Provider<SharedPreferences>((ref) {
  throw UnimplementedError('Must be overridden in main');
});

final dioProvider = Provider<Dio>((ref) {
  final dio = Dio(BaseOptions(
    baseUrl: ApiConstants.baseUrl,
    connectTimeout: ApiConstants.connectTimeout,
    receiveTimeout: ApiConstants.receiveTimeout,
    sendTimeout: ApiConstants.sendTimeout,
    headers: {'Content-Type': 'application/json'},
  ));

  dio.interceptors.add(AuthInterceptor());
  dio.interceptors.add(ErrorInterceptor(
    onUnauthorized: triggerLogout,
  ));

  return dio;
});

final authLocalSourceProvider = Provider<AuthLocalSource>((ref) {
  final prefs = ref.watch(sharedPreferencesProvider);
  return AuthLocalSource(prefs);
});

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  final dio = ref.watch(dioProvider);
  return AuthRepository(dio);
});

final authProvider =
    StateNotifierProvider<AuthNotifier, AsyncValue<UserInfo?>>((ref) {
  final notifier = AuthNotifier(
    ref.watch(authRepositoryProvider),
    ref.watch(authLocalSourceProvider),
  );
  setLogoutCallback(() => notifier.logout());
  return notifier;
});
