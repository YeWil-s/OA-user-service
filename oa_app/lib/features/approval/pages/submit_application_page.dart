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

  static const _leaveTypes = [(1, '年假'), (2, '事假'), (3, '病假'), (4, '婚假'), (5, '产假')];

  @override
  void dispose() { _reasonController.dispose(); super.dispose(); }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    final form = ApplicationForm(
      appType: _appType,
      leaveType: _appType == 1 ? _leaveType : null,
      startDate: _startDate,
      startTime: DateTime(2026, 1, 1, _startTime.hour, _startTime.minute),
      endDate: _endDate,
      endTime: DateTime(2026, 1, 1, _endTime.hour, _endTime.minute),
      reason: _reasonController.text.trim(),
    );

    setState(() => _submitting = true);
    try {
      await ref.read(applicationRepositoryProvider).submit(form);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('提交成功')));
        context.pop();
      }
    } catch (e) {
      if (mounted) ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(extractErrorMessage(e)), backgroundColor: Colors.red));
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
  }

  String _fmtDate(DateTime d) => '${d.year}-${d.month.toString().padLeft(2, '0')}-${d.day.toString().padLeft(2, '0')}';

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
            // 类型选择
            _sectionTitle(theme, '申请类型'),
            const SizedBox(height: 8),
            Row(
              children: [
                _typeChip('请假', Icons.beach_access, 1),
                const SizedBox(width: 8),
                _typeChip('加班', Icons.timer, 2),
                const SizedBox(width: 8),
                _typeChip('外出', Icons.airplanemode_active, 3),
              ],
            ),
            const SizedBox(height: 20),

            // 请假类型
            if (_appType == 1) ...[
              _sectionTitle(theme, '请假类型'),
              const SizedBox(height: 8),
              DropdownButtonFormField<int>(
                decoration: const InputDecoration(hintText: '请选择请假类型'),
                value: _leaveType,
                items: _leaveTypes.map((t) => DropdownMenuItem(value: t.$1, child: Text(t.$2))).toList(),
                onChanged: (v) => setState(() => _leaveType = v),
                validator: (v) => v == null ? '请选择' : null,
              ),
              const SizedBox(height: 20),
            ],

            // 开始日期
            _sectionTitle(theme, '开始日期'),
            const SizedBox(height: 8),
            Row(children: [
              Expanded(child: _dateButton(_startDate, () async {
                final d = await showDatePicker(context: context, initialDate: _startDate, firstDate: DateTime(2020), lastDate: DateTime(2030));
                if (d != null) setState(() { _startDate = d; if (_endDate.isBefore(_startDate)) _endDate = _startDate.add(const Duration(days: 1)); });
              })),
              const SizedBox(width: 10),
              SizedBox(width: 100, child: _timeButton(_startTime, (t) => setState(() => _startTime = t))),
            ]),
            const SizedBox(height: 16),

            // 结束日期
            _sectionTitle(theme, '结束日期'),
            const SizedBox(height: 8),
            Row(children: [
              Expanded(child: _dateButton(_endDate, () async {
                final d = await showDatePicker(context: context, initialDate: _endDate, firstDate: DateTime(2020), lastDate: DateTime(2030));
                if (d != null) setState(() => _endDate = d);
              })),
              const SizedBox(width: 10),
              SizedBox(width: 100, child: _timeButton(_endTime, (t) => setState(() => _endTime = t))),
            ]),
            const SizedBox(height: 20),

            // 原因
            _sectionTitle(theme, '申请原因'),
            const SizedBox(height: 8),
            TextFormField(
              controller: _reasonController,
              decoration: const InputDecoration(hintText: '请输入申请原因'),
              maxLines: 3,
              validator: (v) => v == null || v.trim().isEmpty ? '请输入原因' : null,
            ),
            const SizedBox(height: 32),

            SizedBox(width: double.infinity, height: 52, child: FilledButton(
              onPressed: _submitting ? null : _submit,
              child: _submitting ? const SizedBox(width: 24, height: 24, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white)) : const Text('提 交', style: TextStyle(fontSize: 16)),
            )),
          ]),
        ),
      ),
    );
  }

  Widget _sectionTitle(ThemeData theme, String text) => Text(text, style: theme.textTheme.titleSmall?.copyWith(fontWeight: FontWeight.w600));

  Widget _typeChip(String label, IconData icon, int type) {
    final selected = _appType == type;
    final color = selected ? Theme.of(context).colorScheme.primary : Theme.of(context).colorScheme.outline;
    return Expanded(
      child: GestureDetector(
        onTap: () {
          setState(() {
            _appType = type;
            if (type == 2) { _startTime = const TimeOfDay(hour: 18, minute: 0); _endTime = const TimeOfDay(hour: 21, minute: 0); _endDate = _startDate; }
            else if (type == 1) { _startTime = const TimeOfDay(hour: 9, minute: 0); _endTime = const TimeOfDay(hour: 18, minute: 0); _endDate = _startDate.add(const Duration(days: 1)); }
          });
        },
        child: Container(
          padding: const EdgeInsets.symmetric(vertical: 14),
          decoration: BoxDecoration(
            color: selected ? Theme.of(context).colorScheme.primaryContainer : Theme.of(context).colorScheme.surfaceContainerHighest,
            borderRadius: BorderRadius.circular(12),
            border: selected ? Border.all(color: Theme.of(context).colorScheme.primary, width: 1.5) : null,
          ),
          child: Column(children: [Icon(icon, color: color, size: 24), const SizedBox(height: 6), Text(label, style: TextStyle(fontSize: 13, fontWeight: FontWeight.w600, color: color))]),
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
