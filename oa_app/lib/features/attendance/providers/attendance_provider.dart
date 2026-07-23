import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../auth/providers/auth_providers.dart';
import '../data/attendance_repository.dart';
import '../models/attendance_record.dart';

final attendanceRepoProvider = Provider<AttendanceRepository>((ref) {
  return AttendanceRepository(ref.watch(dioProvider));
});

class AttendanceListState {
  final List<AttendanceRecord> records;
  final bool isLoading;
  final bool hasMore;
  final String? error;
  final String? month;

  const AttendanceListState({
    required this.records,
    this.isLoading = false,
    this.hasMore = true,
    this.error,
    this.month,
  });

  AttendanceListState copyWith({
    List<AttendanceRecord>? records,
    bool? isLoading,
    bool? hasMore,
    String? error,
    String? month,
  }) {
    return AttendanceListState(
      records: records ?? this.records,
      isLoading: isLoading ?? this.isLoading,
      hasMore: hasMore ?? this.hasMore,
      error: error,
      month: month ?? this.month,
    );
  }
}

class AttendanceListNotifier extends StateNotifier<AttendanceListState> {
  final AttendanceRepository _repo;

  AttendanceListNotifier(this._repo)
      : super(const AttendanceListState(records: []));

  int _page = 1;

  Future<void> loadRecords({String? month}) async {
    state = state.copyWith(isLoading: true, month: month);
    _page = 1;
    try {
      final pageData = await _repo.getMyRecords(pageNum: _page, month: month);
      state = AttendanceListState(
        records: pageData.records,
        hasMore: pageData.hasMore,
        month: month,
      );
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> loadMore() async {
    if (!state.hasMore || state.isLoading) return;
    _page++;
    state = state.copyWith(isLoading: true);
    try {
      final pageData = await _repo.getMyRecords(pageNum: _page, month: state.month);
      state = state.copyWith(
        records: [...state.records, ...pageData.records],
        isLoading: false,
        hasMore: pageData.hasMore,
      );
    } catch (e) {
      _page--;
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }
}

final attendanceListProvider =
    StateNotifierProvider<AttendanceListNotifier, AttendanceListState>((ref) {
  return AttendanceListNotifier(ref.watch(attendanceRepoProvider));
});
