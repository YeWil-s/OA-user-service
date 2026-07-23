import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../../../../core/network/api_exception.dart';
import '../models/application_form.dart';
import '../providers/approval_provider.dart';

class SubmitApplicationPage extends ConsumerStatefulWidget {
  const SubmitApplicationPage({super.key});

  @override
  ConsumerState<SubmitApplicationPage> createState() => _SubmitApplicationPageState();
}

class _SubmitApplicationPageState extends ConsumerState<SubmitApplicationPage> {
  int _appType = 1;
  int? _leaveType;
  DateTime _startDate = DateTime.now();
  DateTime _endDate = DateTime.now().add(const Duration(days: 1));
  TimeOfDay _startTime = const TimeOfDay(hour: 9, minute: 0);
  TimeOfDay _endTime = const TimeOfDay(hour: 18, minute: 0);
  final _reasonController = TextEditingController();
  final _formKey = GlobalKey<FormState>();
  bool _submitting = false;
  bool _loadingRefData = false;
  String? _refDataError;

  // type 4
  int? _targetDeptId;
  int? _targetPositionId;

  // type 5
  int? _assetId;
  DateTime? _expectReturnDate;

  // reference data
  List<Map<String, dynamic>> _depts = [];
  List<Map<String, dynamic>> _positions = [];
  List<Map<String, dynamic>> _assets = [];

  static const _leaveTypes = [
    (1, '年假'), (2, '事假'), (3, '病假'), (4, '婚假'), (5, '产假')
  ];

  // dropdown 用 — 确保 value 始终匹配某个 item
  static const _emptyChoice = -1;

  @override
  void initState() {
    super.initState();
    _loadRefData();
  }

  Future<void> _loadRefData() async {
    setState(() { _loadingRefData = true; _refDataError = null; });
    final repo = ref.read(applicationRepositoryProvider);

    String? deptErr, posErr, assetErr;
    try {
      _depts = await repo.getDepts();
    } catch (e) {
      deptErr = extractErrorMessage(e);
    }
    try {
      _positions = await repo.getPositions();
    } catch (e) {
      posErr = extractErrorMessage(e);
    }
    try {
      _assets = await repo.getAvailableAssets();
    } catch (e) {
      assetErr = extractErrorMessage(e);
    }

    final errors = [deptErr, posErr, assetErr].where((e) => e != null).toList();
    if (mounted) {
      setState(() {
        _loadingRefData = false;
        _refDataError = errors.isEmpty ? null : errors.join('；');
      });
    }
  }

  @override
  void dispose() {
    _reasonController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    if (_appType == 4 && (_targetDeptId == null || _targetPositionId == null)) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('请选择目标部门和岗位'), backgroundColor: Colors.red),
      );
      return;
    }
    if (_appType == 5 && _assetId == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('请选择资产'), backgroundColor: Colors.red),
      );
      return;
    }

    final form = ApplicationForm(
      appType: _appType,
      leaveType: _appType == 1 ? _leaveType : null,
      startDate: _startDate,
      startTime: DateTime(2026, 1, 1, _startTime.hour, _startTime.minute),
      endDate: _endDate,
      endTime: DateTime(2026, 1, 1, _endTime.hour, _endTime.minute),
      reason: _reasonController.text.trim(),
      targetDeptId: _targetDeptId,
      targetPositionId: _targetPositionId,
      assetId: _assetId,
      expectReturnDate: _expectReturnDate,
    );

    setState(() => _submitting = true);
    try {
      await ref.read(applicationRepositoryProvider).submit(form);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('提交成功')));
        context.pop();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text(extractErrorMessage(e)), backgroundColor: Colors.red),
        );
      }
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  String _fmtDate(DateTime d) =>
      '${d.year}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

  // ---- 通用下拉构建 ----
  Widget _buildDropdown<T>({
    required String hint,
    required T? value,
    required List<DropdownMenuItem<T>> items,
    required ValueChanged<T?> onChanged,
    String? Function(T?)? validator,
  }) {
    return DropdownButtonFormField<T>(
      decoration: InputDecoration(hintText: hint),
      value: value,
      items: items,
      onChanged: onChanged,
      validator: validator,
    );
  }

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(title: const Text('提交申请')),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16),
        child: Form(
          key: _formKey,
          child: Column(crossAxisAlignment: CrossAxisAlignment.start, children: [
            // ---- 类型选择 ----
            _sectionTitle(theme, '申请类型'),
            const SizedBox(height: 8),
            Row(children: [
              _typeChip('请假', Icons.beach_access, 1),
              const SizedBox(width: 8),
              _typeChip('加班', Icons.timer, 2),
              const SizedBox(width: 8),
              _typeChip('外出', Icons.airplanemode_active, 3),
            ]),
            const SizedBox(height: 8),
            Row(children: [
              _typeChip('调岗', Icons.swap_horiz, 4),
              const SizedBox(width: 8),
              _typeChip('资产领用', Icons.inventory_2, 5),
            ]),
            const SizedBox(height: 20),

            // ---- 参考数据加载状态 ----
            if (_loadingRefData)
              const Padding(
                padding: EdgeInsets.only(bottom: 20),
                child: LinearProgressIndicator(),
              ),
            if (_refDataError != null)
              Padding(
                padding: const EdgeInsets.only(bottom: 20),
                child: Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: Colors.orange.shade50,
                    borderRadius: BorderRadius.circular(10),
                  ),
                  child: Text('⚠ $_refDataError', style: TextStyle(fontSize: 12, color: Colors.orange.shade900)),
                ),
              ),

            // ---- 请假类型 ----
            if (_appType == 1) ...[
              _sectionTitle(theme, '请假类型'),
              const SizedBox(height: 8),
              _buildDropdown<int>(
                hint: '请选择请假类型',
                value: _leaveType,
                items: _leaveTypes.map((t) => DropdownMenuItem(value: t.$1, child: Text(t.$2))).toList(),
                onChanged: (v) => setState(() => _leaveType = v),
                validator: (v) => v == null ? '请选择' : null,
              ),
              const SizedBox(height: 20),
            ],

            // ---- 时间选择 (类型 1-3) ----
            if (_appType >= 1 && _appType <= 3) ...[
              _sectionTitle(theme, '开始时间'),
              const SizedBox(height: 8),
              Row(children: [
                Expanded(child: _dateButton(_startDate, () async {
                  final d = await showDatePicker(
                      context: context, initialDate: _startDate,
                      firstDate: DateTime(2020), lastDate: DateTime(2030));
                  if (d != null) setState(() {
                    _startDate = d;
                    if (_endDate.isBefore(_startDate)) _endDate = _startDate.add(const Duration(days: 1));
                  });
                })),
                const SizedBox(width: 10),
                SizedBox(width: 100, child: _timeButton(_startTime, (t) => setState(() => _startTime = t))),
              ]),
              const SizedBox(height: 16),
              _sectionTitle(theme, '结束时间'),
              const SizedBox(height: 8),
              Row(children: [
                Expanded(child: _dateButton(_endDate, () async {
                  final d = await showDatePicker(
                      context: context, initialDate: _endDate,
                      firstDate: DateTime(2020), lastDate: DateTime(2030));
                  if (d != null) setState(() => _endDate = d);
                })),
                const SizedBox(width: 10),
                SizedBox(width: 100, child: _timeButton(_endTime, (t) => setState(() => _endTime = t))),
              ]),
              const SizedBox(height: 20),
            ],

            // ---- 调岗 (类型 4) ----
            if (_appType == 4) ...[
              _sectionTitle(theme, '目标部门'),
              const SizedBox(height: 8),
              if (_depts.isEmpty)
                _emptyHint(_loadingRefData ? '加载中...' : '暂无部门数据')
              else
                _buildDropdown<int>(
                  hint: '请选择部门',
                  value: _targetDeptId,
                  items: _depts.map((d) => DropdownMenuItem(
                    value: d['id'] as int,
                    child: Text((d['deptName'] ?? '').toString()),
                  )).toList(),
                  onChanged: (v) => setState(() { _targetDeptId = v; _targetPositionId = null; }),
                ),
              const SizedBox(height: 20),

              _sectionTitle(theme, '目标岗位'),
              const SizedBox(height: 8),
              if (_positions.isEmpty)
                _emptyHint(_loadingRefData ? '加载中...' : '暂无岗位数据')
              else
                _buildDropdown<int>(
                  hint: '请选择岗位',
                  value: _targetPositionId,
                  items: _positions.map((p) => DropdownMenuItem(
                    value: p['id'] as int,
                    child: Text((p['positionName'] ?? '').toString()),
                  )).toList(),
                  onChanged: (v) => setState(() => _targetPositionId = v),
                ),
              const SizedBox(height: 20),
            ],

            // ---- 资产领用 (类型 5) ----
            if (_appType == 5) ...[
              _sectionTitle(theme, '选择资产'),
              const SizedBox(height: 8),
              if (_assets.isEmpty)
                _emptyHint(_loadingRefData ? '加载中...' : '暂无可领用资产')
              else
                _buildDropdown<int>(
                  hint: '请选择资产',
                  value: _assetId,
                  items: _assets.map((a) {
                    final name = (a['assetName'] ?? a['name'] ?? '').toString();
                    final code = (a['assetCode'] ?? a['code'] ?? '').toString();
                    return DropdownMenuItem(value: a['id'] as int, child: Text('$name ($code)'));
                  }).toList(),
                  onChanged: (v) => setState(() => _assetId = v),
                ),
              const SizedBox(height: 20),

              _sectionTitle(theme, '预计归还日期'),
              const SizedBox(height: 8),
              OutlinedButton.icon(
                onPressed: () async {
                  final d = await showDatePicker(
                      context: context,
                      initialDate: _expectReturnDate ?? DateTime.now().add(const Duration(days: 7)),
                      firstDate: DateTime.now(), lastDate: DateTime(2030));
                  if (d != null) setState(() => _expectReturnDate = d);
                },
                icon: const Icon(Icons.calendar_today, size: 16),
                label: Text(_expectReturnDate != null ? _fmtDate(_expectReturnDate!) : '请选择归还日期'),
                style: OutlinedButton.styleFrom(
                  minimumSize: const Size(double.infinity, 48),
                  shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
                ),
              ),
              const SizedBox(height: 20),
            ],

            // ---- 原因 ----
            _sectionTitle(theme, '申请原因'),
            const SizedBox(height: 8),
            TextFormField(
              controller: _reasonController,
              decoration: const InputDecoration(hintText: '请输入申请原因'),
              maxLines: 3,
              validator: (v) => v == null || v.trim().isEmpty ? '请输入原因' : null,
            ),
            const SizedBox(height: 32),

            SizedBox(
              width: double.infinity,
              height: 52,
              child: FilledButton(
                onPressed: _submitting ? null : _submit,
                child: _submitting
                    ? const SizedBox(width: 24, height: 24,
                        child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                    : const Text('提 交', style: TextStyle(fontSize: 16)),
              ),
            ),
          ]),
        ),
      ),
    );
  }

  Widget _sectionTitle(ThemeData theme, String text) =>
      Text(text, style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w600));

  Widget _emptyHint(String text) => Padding(
    padding: const EdgeInsets.symmetric(vertical: 12),
    child: Text(text, style: TextStyle(color: Colors.grey.shade500, fontSize: 13)),
  );

  Widget _typeChip(String label, IconData icon, int type) {
    final selected = _appType == type;
    final cs = Theme.of(context).colorScheme;
    final color = selected ? cs.primary : cs.outline;
    return Expanded(
      child: GestureDetector(
        onTap: () {
          setState(() {
            _appType = type;
            _leaveType = null;
            _targetDeptId = null;
            _targetPositionId = null;
            _assetId = null;
            _expectReturnDate = null;
            if (type == 2) {
              _startTime = const TimeOfDay(hour: 18, minute: 0);
              _endTime = const TimeOfDay(hour: 21, minute: 0);
              _endDate = _startDate;
            } else if (type == 1) {
              _startTime = const TimeOfDay(hour: 9, minute: 0);
              _endTime = const TimeOfDay(hour: 18, minute: 0);
              _endDate = _startDate.add(const Duration(days: 1));
            }
          });
        },
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 14),
          decoration: BoxDecoration(
            color: selected ? cs.primaryContainer : cs.surfaceContainerHighest,
            borderRadius: BorderRadius.circular(12),
            border: selected ? Border.all(color: cs.primary, width: 1.5) : null,
          ),
          child: Column(children: [
            Icon(icon, color: color, size: 24),
            const SizedBox(height: 6),
            Text(label, style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600, color: color)),
          ]),
        ),
      ),
    );
  }

  Widget _dateButton(DateTime date, VoidCallback onTap) {
    return OutlinedButton.icon(
      onPressed: onTap,
      icon: const Icon(Icons.calendar_today, size: 16),
      label: Text(_fmtDate(date)),
      style: OutlinedButton.styleFrom(
        minimumSize: const Size(0, 48),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      ),
    );
  }

  Widget _timeButton(TimeOfDay time, ValueChanged<TimeOfDay> onChanged) {
    return OutlinedButton.icon(
      onPressed: () async {
        final t = await showTimePicker(context: context, initialTime: time);
        if (t != null) onChanged(t);
      },
      icon: const Icon(Icons.access_time, size: 16),
      label: Text(time.format(context)),
      style: OutlinedButton.styleFrom(
        minimumSize: const Size(0, 48),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      ),
    );
  }
}
