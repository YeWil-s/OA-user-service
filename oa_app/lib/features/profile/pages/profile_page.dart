import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_theme.dart';
import '../../auth/providers/auth_providers.dart';
import '../providers/profile_provider.dart';

class ProfilePage extends ConsumerWidget {
  const ProfilePage({super.key});

  static const _roleColors = {
    'admin': Color(0xFFE53935),
    'hr': Color(0xFF7B1FA2),
    'leader': Color(0xFF1565C0),
    'dept_manager': Color(0xFF00695C),
    'employee': Color(0xFF6D4C41),
  };

  void _showChangePasswordDialog(BuildContext context, WidgetRef ref) {
    final oldPwd = TextEditingController();
    final newPwd = TextEditingController();
    final confirmPwd = TextEditingController();
    final formKey = GlobalKey<FormState>();

    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
        title: const Text('修改密码'),
        content: Form(
          key: formKey,
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              TextFormField(
                controller: oldPwd,
                obscureText: true,
                decoration: const InputDecoration(labelText: '旧密码'),
                validator: (v) => v == null || v.isEmpty ? '请输入旧密码' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: newPwd,
                obscureText: true,
                decoration: const InputDecoration(labelText: '新密码'),
                validator: (v) => v == null || v.isEmpty ? '请输入新密码' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: confirmPwd,
                obscureText: true,
                decoration: const InputDecoration(labelText: '确认新密码'),
                validator: (v) {
                  if (v == null || v.isEmpty) return '请确认新密码';
                  if (v != newPwd.text) return '两次密码不一致';
                  return null;
                },
              ),
            ],
          ),
        ),
        actions: [
          TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('取消')),
          FilledButton(
            onPressed: () async {
              if (!formKey.currentState!.validate()) return;
              final user = ref.read(authProvider).valueOrNull;
              if (user == null) return;
              await ref.read(profileProvider.notifier).changePassword(user.userId, oldPwd.text, newPwd.text);
              if (ctx.mounted) {
                final error = ref.read(profileProvider).error;
                if (error != null) {
                  ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text('修改失败: $error')));
                } else {
                  Navigator.pop(ctx);
                  ScaffoldMessenger.of(context).showSnackBar(const SnackBar(content: Text('密码修改成功')));
                }
                ref.read(profileProvider.notifier).reset();
              }
            },
            child: const Text('确认修改'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(authProvider).valueOrNull;
    final theme = Theme.of(context);

    return Scaffold(
      appBar: AppBar(title: const Text('我的')),
      body: ListView(
        padding: const EdgeInsets.all(16),
        children: [
          // 头部信息卡片
          Container(
            width: double.infinity,
            padding: const EdgeInsets.symmetric(vertical: 32, horizontal: 20),
            decoration: BoxDecoration(
              gradient: AppTheme.primaryGradient,
              borderRadius: BorderRadius.circular(20),
              boxShadow: [
                BoxShadow(color: AppTheme.primaryColor.withValues(alpha: 0.25), blurRadius: 16, offset: const Offset(0, 6)),
              ],
            ),
            child: Column(
              children: [
                CircleAvatar(
                  radius: 42,
                  backgroundColor: Colors.white.withValues(alpha: 0.2),
                  child: Text(
                    user?.realName.isNotEmpty == true ? user!.realName[0] : '?',
                    style: const TextStyle(fontSize: 34, fontWeight: FontWeight.bold, color: Colors.white),
                  ),
                ),
                const SizedBox(height: 14),
                Text(user?.realName ?? '--', style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold, color: Colors.white)),
                const SizedBox(height: 4),
                Text(user?.username ?? '--', style: TextStyle(fontSize: 14, color: Colors.white.withValues(alpha: 0.85))),
                if (user?.roles.isNotEmpty == true) ...[
                  const SizedBox(height: 14),
                  Wrap(
                    spacing: 8,
                    runSpacing: 6,
                    children: user!.roles.map((r) {
                      final color = _roleColors[r] ?? Colors.white;
                      return Container(
                        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 5),
                        decoration: BoxDecoration(
                          color: Colors.white.withValues(alpha: 0.2),
                          borderRadius: BorderRadius.circular(20),
                        ),
                        child: Text(r, style: TextStyle(fontSize: 12, fontWeight: FontWeight.w500, color: color)),
                      );
                    }).toList(),
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(height: 16),
          // 功能菜单
          Card(
            child: Column(
              children: [
                _menuItem(Icons.lock_outline, '修改密码', onTap: () => _showChangePasswordDialog(context, ref)),
                const Divider(),
                _menuItem(Icons.info_outline, '关于', onTap: () {
                  showAboutDialog(context: context, applicationName: 'OA 管理系统', applicationVersion: '1.0.0');
                }),
                const Divider(),
                _menuItem(Icons.logout, '退出登录', color: theme.colorScheme.error, onTap: () {
                  showDialog(
                    context: context,
                    builder: (ctx) => AlertDialog(
                      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
                      title: const Text('提示'),
                      content: const Text('确定要退出登录吗？'),
                      actions: [
                        TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('取消')),
                        FilledButton(
                          onPressed: () {
                            ref.read(authProvider.notifier).logout();
                            Navigator.pop(ctx);
                          },
                          child: const Text('确定'),
                        ),
                      ],
                    ),
                  );
                }),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _menuItem(IconData icon, String title, {Color? color, VoidCallback? onTap}) {
    return ListTile(
      leading: Icon(icon, color: color),
      title: Text(title, style: TextStyle(color: color)),
      trailing: const Icon(Icons.chevron_right, size: 20),
      onTap: onTap,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
    );
  }
}
