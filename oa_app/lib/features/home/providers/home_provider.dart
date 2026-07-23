import 'dart:io';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../auth/providers/auth_providers.dart';
import '../../../core/utils/location_service.dart';
import '../data/punch_repository.dart';
import '../data/summary_repository.dart';
import '../models/punch_status.dart';
import '../models/monthly_summary.dart';

final punchRepositoryProvider = Provider<PunchRepository>((ref) {
  return PunchRepository(ref.watch(dioProvider));
});

final summaryRepositoryProvider = Provider<SummaryRepository>((ref) {
  return SummaryRepository(ref.watch(dioProvider));
});

class HomeState {
  final PunchStatus punchStatus;
  final MonthlySummary monthlySummary;
  final bool isLoading;
  final bool isPunching;
  final LocationResult? punchLocation; // 当前打卡定位结果

  const HomeState({
    required this.punchStatus,
    required this.monthlySummary,
    this.isLoading = false,
    this.isPunching = false,
    this.punchLocation,
  });

  HomeState copyWith({
    PunchStatus? punchStatus,
    MonthlySummary? monthlySummary,
    bool? isLoading,
    bool? isPunching,
    LocationResult? punchLocation,
    bool clearLocation = false,
  }) {
    return HomeState(
      punchStatus: punchStatus ?? this.punchStatus,
      monthlySummary: monthlySummary ?? this.monthlySummary,
      isLoading: isLoading ?? this.isLoading,
      isPunching: isPunching ?? this.isPunching,
      punchLocation: clearLocation ? null : (punchLocation ?? this.punchLocation),
    );
  }
}

class HomeNotifier extends StateNotifier<HomeState> {
  final PunchRepository _punchRepo;
  final SummaryRepository _summaryRepo;

  HomeNotifier(this._punchRepo, this._summaryRepo)
      : super(const HomeState(
          punchStatus: PunchStatus(status: 'not_punched'),
          monthlySummary: MonthlySummary.empty,
        ));

  Future<void> loadData() async {
    state = state.copyWith(isLoading: true);
    try {
      final now = DateTime.now();
      final results = await Future.wait([
        _punchRepo.getTodayStatus(),
        _summaryRepo.getMonthlySummary(now.year, now.month),
      ]);
      state = HomeState(
        punchStatus: results[0] as PunchStatus,
        monthlySummary: results[1] as MonthlySummary,
      );
    } catch (_) {
      state = state.copyWith(isLoading: false);
    }
  }

  /// 获取定位+地址+周边地点（打卡前调用）
  Future<void> fetchPunchLocation() async {
    state = state.copyWith(isPunching: true);
    try {
      final loc = await LocationService.getLocationWithAddress();
      state = state.copyWith(isPunching: false, punchLocation: loc);
    } catch (_) {
      state = state.copyWith(isPunching: false);
      rethrow;
    }
  }

  /// 执行打卡（传入用户选择的地点名称和 GPS 坐标）
  Future<void> punchIn([String? selectedPlace]) async {
    if (state.isPunching) return;
    state = state.copyWith(isPunching: true);

    final loc = state.punchLocation;
    final location = selectedPlace ?? loc?.address ?? loc?.locationStr;
    final deviceInfo = '${Platform.operatingSystem} ${Platform.operatingSystemVersion}';

    try {
      final result = await _punchRepo.punchIn(
        location: location,
        latitude: loc?.latitude,
        longitude: loc?.longitude,
        deviceInfo: deviceInfo,
      );
      state = state.copyWith(punchStatus: result, isPunching: false, clearLocation: true);
    } catch (_) {
      state = state.copyWith(isPunching: false);
      rethrow;
    }
  }

  Future<void> punchOut([String? selectedPlace]) async {
    if (state.isPunching) return;
    state = state.copyWith(isPunching: true);

    final loc = state.punchLocation;
    final location = selectedPlace ?? loc?.address ?? loc?.locationStr;
    final deviceInfo = '${Platform.operatingSystem} ${Platform.operatingSystemVersion}';

    try {
      final result = await _punchRepo.punchOut(
        location: location,
        latitude: loc?.latitude,
        longitude: loc?.longitude,
        deviceInfo: deviceInfo,
      );
      state = state.copyWith(punchStatus: result, isPunching: false, clearLocation: true);
    } catch (_) {
      state = state.copyWith(isPunching: false);
      rethrow;
    }
  }
}

final homeProvider = StateNotifierProvider<HomeNotifier, HomeState>((ref) {
  return HomeNotifier(
    ref.watch(punchRepositoryProvider),
    ref.watch(summaryRepositoryProvider),
  );
});
