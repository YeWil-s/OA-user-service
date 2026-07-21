import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../auth/providers/auth_providers.dart';
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

  const HomeState({
    required this.punchStatus,
    required this.monthlySummary,
    this.isLoading = false,
  });

  HomeState copyWith({
    PunchStatus? punchStatus,
    MonthlySummary? monthlySummary,
    bool? isLoading,
  }) {
    return HomeState(
      punchStatus: punchStatus ?? this.punchStatus,
      monthlySummary: monthlySummary ?? this.monthlySummary,
      isLoading: isLoading ?? this.isLoading,
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

  Future<void> punchIn() async {
    final result = await _punchRepo.punchIn();
    state = state.copyWith(punchStatus: result);
  }

  Future<void> punchOut() async {
    final result = await _punchRepo.punchOut();
    state = state.copyWith(punchStatus: result);
  }
}

final homeProvider = StateNotifierProvider<HomeNotifier, HomeState>((ref) {
  return HomeNotifier(
    ref.watch(punchRepositoryProvider),
    ref.watch(summaryRepositoryProvider),
  );
});
