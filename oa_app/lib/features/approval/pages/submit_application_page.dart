import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import '../models/application_form.dart';
import '../providers/approval_provider.dart';

class SubmitApplicationPage extends ConsumerStatefulWidget {
  const SubmitApplicationPage({super.key});

  @override
  ConsumerState<SubmitApplicationPage> createState() => _SubmitApplicationPageState();
}

class _SubmitApplicationPageState extends ConsumerState<SubmitApplicationPage> {
  int _appType = 1; // 默认请假
  int? _leaveType;
  DateTime _startDate = DateTime.now();
  DateTime _endDate = DateTime.now();
  TimeOfDay? _startTime;
  TimeOfDay? _endTime;
  final _reasonController = TextEditingController();
  final _formKey = GlobalKey<FormState>();
  bool _submitting = false;

  final _leaveTypes = [
    (1, '年假'),
    (2, '事假'),
    (3, '病假'),
    (4, '婚假'),
    (5, '产假'),
  ];

  @override
  void dispose() {
    _reasonController.dispose();
    super.dispose();
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    DateTime? toDateTime(TimeOfDay? tod) {
      if (tod == null) return null;
      final now = DateTime.now();
      return DateTime(now.year, now.month, now.day, tod.hour, tod.minute);
    }

    final form = ApplicationForm(
      appType: _appType,
      leaveType: _appType == 1 ? _leaveType : null,
      startDate: _startDate,
      startTime: _appType != 1 ? toDateTime(_startTime) : null,
      endDate: _endDate,
      endTime: _appType != 1 ? toDateTime(_endTime) : null,
      reason: _reasonController.text.trim(),
    );

    setState(() => _submitting = true);
    try {
      await ref.read(applicationRepositoryProvider).submit(form);
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('提交成功')),
        );
        context.pop();
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(content: Text('提交失败: $e')),
        );
      }
    } finally {
      if (mounted) setState(() => _submitting = false);
    }
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
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text('申请类型', style: theme.textTheme.titleSmall),
              const SizedBox(height: 8),
              SegmentedButton<int>(
                segments: const [
                  ButtonSegment(value: 1, label: Text('请假')),
                  ButtonSegment(value: 2, label: Text('加班')),
                  ButtonSegment(value: 3, label: Text('外出')),
                ],
                selected: {_appType},
                onSelectionChanged: (v) => setState(() => _appType = v.first),
              ),
              const SizedBox(height: 16),
              if (_appType == 1) ...[
                Text('请假类型', style: theme.textTheme.titleSmall),
                const SizedBox(height: 8),
                DropdownButtonFormField<int>(
                  decoration: const InputDecoration(
                    hintText: '请选择请假类型',
                    isDense: true,
                  ),
                  initialValue: _leaveType,
                  items: _leaveTypes.map((t) => DropdownMenuItem(
                    value: t.$1,
                    child: Text(t.$2),
                  )).toList(),
                  onChanged: (v) => setState(() => _leaveType = v),
                  validator: (v) => _appType == 1 && v == null ? '请选择请假类型' : null,
                ),
                const SizedBox(height: 16),
              ],
              Text('开始日期', style: theme.textTheme.titleSmall),
              const SizedBox(height: 8),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: () async {
                        final date = await showDatePicker(
                          context: context,
                          initialDate: _startDate,
                          firstDate: DateTime(2020),
                          lastDate: DateTime(2030),
                        );
                        if (date != null) setState(() => _startDate = date);
                      },
                      icon: const Icon(Icons.calendar_today, size: 18),
                      label: Text(
                        '${_startDate.year}-${_startDate.month.toString().padLeft(2, '0')}-${_startDate.day.toString().padLeft(2, '0')}',
                      ),
                    ),
                  ),
                  if (_appType != 1) ...[
                    const SizedBox(width: 12),
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: () async {
                          final time = await showTimePicker(
                            context: context,
                            initialTime: _startTime ?? TimeOfDay.now(),
                          );
                          if (time != null) setState(() => _startTime = time);
                        },
                        icon: const Icon(Icons.access_time, size: 18),
                        label: Text(_startTime?.format(context) ?? '开始时间'),
                      ),
                    ),
                  ],
                ],
              ),
              const SizedBox(height: 16),
              Text('结束日期', style: theme.textTheme.titleSmall),
              const SizedBox(height: 8),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: () async {
                        final date = await showDatePicker(
                          context: context,
                          initialDate: _endDate,
                          firstDate: DateTime(2020),
                          lastDate: DateTime(2030),
                        );
                        if (date != null) setState(() => _endDate = date);
                      },
                      icon: const Icon(Icons.calendar_today, size: 18),
                      label: Text(
                        '${_endDate.year}-${_endDate.month.toString().padLeft(2, '0')}-${_endDate.day.toString().padLeft(2, '0')}',
                      ),
                    ),
                  ),
                  if (_appType != 1) ...[
                    const SizedBox(width: 12),
                    Expanded(
                      child: OutlinedButton.icon(
                        onPressed: () async {
                          final time = await showTimePicker(
                            context: context,
                            initialTime: _endTime ?? TimeOfDay.now(),
                          );
                          if (time != null) setState(() => _endTime = time);
                        },
                        icon: const Icon(Icons.access_time, size: 18),
                        label: Text(_endTime?.format(context) ?? '结束时间'),
                      ),
                    ),
                  ],
                ],
              ),
              const SizedBox(height: 16),
              Text('申请原因', style: theme.textTheme.titleSmall),
              const SizedBox(height: 8),
              TextFormField(
                controller: _reasonController,
                decoration: const InputDecoration(
                  hintText: '请输入申请原因',
                  isDense: true,
                ),
                maxLines: 3,
                validator: (v) => v == null || v.trim().isEmpty ? '请输入原因' : null,
              ),
              const SizedBox(height: 32),
              SizedBox(
                width: double.infinity,
                height: 48,
                child: FilledButton(
                  onPressed: _submitting ? null : _submit,
                  child: _submitting
                      ? const SizedBox(
                          width: 24, height: 24,
                          child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white),
                        )
                      : const Text('提 交', style: TextStyle(fontSize: 16)),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
