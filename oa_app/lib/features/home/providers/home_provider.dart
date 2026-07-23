import 'dart:io';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../auth/providers/auth_providers.dart';
import '../../../core/utils/location_service.dart';
import '../data/punch_repository.dart';
import '../data/summary_repository.dart';
import '../models/punch_status.dart';
import '../models/monthly_summary.dart';
import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';
import '../models/schedule.dart';

final punchRepositoryProvider = Provider<PunchRepository>((ref) {
  return PunchRepository(ref.watch(dioProvider));
});

final summaryRepositoryProvider = Provider<SummaryRepository>((ref) {
  return SummaryRepository(ref.watch(dioProvider));
});

class HomeState {
  final PunchStatus punchStatus;
  final MonthlySummary monthlySummary;
  final List<Schedule> weeklySchedules;
  final bool isLoading;
  final bool isPunching;
  final LocationResult? punchLocation; // 当前打卡定位结果

  const HomeState({
    required this.punchStatus,
    required this.monthlySummary,
    this.weeklySchedules = const [],
    this.isLoading = false,
    this.isPunching = false,
    this.punchLocation,
  });

  HomeState copyWith({
    PunchStatus? punchStatus,
    MonthlySummary? monthlySummary,
    List<Schedule>? weeklySchedules,
    bool? isLoading,
    bool? isPunching,
    LocationResult? punchLocation,
    bool clearLocation = false,
  }) {
    return HomeState(
      punchStatus: punchStatus ?? this.punchStatus,
      monthlySummary: monthlySummary ?? this.monthlySummary,
      weeklySchedules: weeklySchedules ?? this.weeklySchedules,
      isLoading: isLoading ?? this.isLoading,
      isPunching: isPunching ?? this.isPunching,
      punchLocation: clearLocation ? null : (punchLocation ?? this.punchLocation),
    );
  }
}

class HomeNotifier extends StateNotifier<HomeState> {
  final PunchRepository _punchRepo;
  final SummaryRepository _summaryRepo;
  final Dio _dio;

  HomeNotifier(this._punchRepo, this._summaryRepo, this._dio)
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
        _loadSchedules(),
      ]);
      state = HomeState(
        punchStatus: results[0] as PunchStatus,
        monthlySummary: results[1] as MonthlySummary,
        weeklySchedules: results[2] as List<Schedule>,
      );
    } catch (_) {
      state = state.copyWith(isLoading: false);
    }
  }

  Future<List<Schedule>> _loadSchedules() async {
    try {
      final now = DateTime.now();
      final monday = now.subtract(Duration(days: now.weekday - 1));
      final sunday = monday.add(const Duration(days: 6));
      final fmt = (DateTime d) =>
          '${d.year}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';
      final response = await _dio.get(
        ApiConstants.attendanceSchedulesMine,
        queryParameters: {'startDate': fmt(monday), 'endDate': fmt(sunday)},
      );
      final data = response.data['data'] as List<dynamic>? ?? [];
      return data.map((e) => Schedule.fromJson(e as Map<String, dynamic>)).toList();
    } catch (_) {
      return [];
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
    ref.watch(dioProvider),
  );
});
