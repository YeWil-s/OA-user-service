import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../../../core/theme/app_theme.dart';
import '../../auth/providers/auth_providers.dart';
import '../providers/profile_provider.dart';

class ProfilePage extends ConsumerWidget {
  const ProfilePage({super.key});

  static const _roleConfig = {
    'ROLE_ADMIN': _RoleStyle(color: Color(0xFFE53935), label: '管理员'),
    'ROLE_HR': _RoleStyle(color: Color(0xFF7B1FA2), label: 'HR'),
    'ROLE_LEADER': _RoleStyle(color: Color(0xFF1565C0), label: '主管'),
    'ROLE_EMPLOYEE': _RoleStyle(color: Color(0xFF6D4C41), label: '员工'),
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
                decoration: const InputDecoration(labelText: '旧密码', prefixIcon: Icon(Icons.lock_outline)),
                validator: (v) => v == null || v.isEmpty ? '请输入旧密码' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: newPwd,
                obscureText: true,
                decoration: const InputDecoration(labelText: '新密码', prefixIcon: Icon(Icons.lock_reset)),
                validator: (v) => v == null || v.isEmpty ? '请输入新密码' : null,
              ),
              const SizedBox(height: 12),
              TextFormField(
                controller: confirmPwd,
                obscureText: true,
                decoration: const InputDecoration(labelText: '确认新密码', prefixIcon: Icon(Icons.check_circle_outline)),
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
    final roles = user?.roles ?? <String>[];

    return Scaffold(
      body: CustomScrollView(
        slivers: [
          // 头部
          SliverAppBar(
            expandedHeight: 220,
            pinned: true,
            flexibleSpace: FlexibleSpaceBar(
              background: Container(
                decoration: BoxDecoration(
                  gradient: LinearGradient(
                    begin: Alignment.topLeft,
                    end: Alignment.bottomRight,
                    colors: [AppTheme.primaryColor, AppTheme.primaryColor.withValues(alpha: 0.7), const Color(0xFF6366f1)],
                  ),
                ),
                child: SafeArea(
                  child: Column(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const SizedBox(height: 20),
                      CircleAvatar(
                        radius: 48,
                        backgroundColor: Colors.white.withValues(alpha: 0.2),
                        child: Text(
                          user?.realName.isNotEmpty == true ? user!.realName.characters.first : '?',
                          style: const TextStyle(fontSize: 38, fontWeight: FontWeight.bold, color: Colors.white),
                        ),
                      ),
                      const SizedBox(height: 12),
                      Text(user?.realName ?? '--', style: const TextStyle(fontSize: 22, fontWeight: FontWeight.bold, color: Colors.white)),
                      const SizedBox(height: 4),
                      Text('@${user?.username ?? '--'}', style: TextStyle(fontSize: 14, color: Colors.white.withValues(alpha: 0.8))),
                    ],
                  ),
                ),
              ),
            ),
          ),
          // 角色标签
          if (roles.isNotEmpty)
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(20, 16, 20, 0),
                child: Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: roles.map((r) {
                    final cfg = _roleConfig[r] ?? _RoleStyle(color: Colors.grey, label: r);
                    return Chip(
                      avatar: CircleAvatar(backgroundColor: cfg.color, radius: 6),
                      label: Text(cfg.label, style: TextStyle(fontSize: 12, color: cfg.color, fontWeight: FontWeight.w600)),
                      backgroundColor: cfg.color.withValues(alpha: 0.08),
                      side: BorderSide(color: cfg.color.withValues(alpha: 0.2)),
                      padding: EdgeInsets.zero,
                      visualDensity: VisualDensity.compact,
                    );
                  }).toList(),
                ),
              ),
            ),
          // 功能卡片
          SliverToBoxAdapter(
            child: Padding(
              padding: const EdgeInsets.all(20),
              child: Column(
                children: [
                  _menuCard(
                    icon: Icons.security,
                    title: '修改密码',
                    subtitle: '更新您的登录密码',
                    iconBg: Colors.blue.shade50,
                    iconColor: Colors.blue,
                    onTap: () => _showChangePasswordDialog(context, ref),
                  ),
                  const SizedBox(height: 10),
                  _menuCard(
                    icon: Icons.info_outline,
                    title: '关于',
                    subtitle: 'OA 管理系统 v1.0.0',
                    iconBg: Colors.teal.shade50,
                    iconColor: Colors.teal,
                    onTap: () => showAboutDialog(context: context, applicationName: 'OA 管理系统', applicationVersion: '1.0.0'),
                  ),
                  const SizedBox(height: 24),
                  SizedBox(
                    width: double.infinity,
                    height: 48,
                    child: OutlinedButton.icon(
                      onPressed: () {
                        showDialog(
                          context: context,
                          builder: (ctx) => AlertDialog(
                            shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
                            title: const Text('退出登录'),
                            content: const Text('确定要退出当前账号吗？'),
                            actions: [
                              TextButton(onPressed: () => Navigator.pop(ctx), child: const Text('取消')),
                              FilledButton(
                                onPressed: () {
                                  ref.read(authProvider.notifier).logout();
                                  Navigator.pop(ctx);
                                },
                                style: FilledButton.styleFrom(backgroundColor: theme.colorScheme.error),
                                child: const Text('退出'),
                              ),
                            ],
                          ),
                        );
                      },
                      icon: Icon(Icons.logout, color: theme.colorScheme.error),
                      label: Text('退出登录', style: TextStyle(color: theme.colorScheme.error)),
                      style: OutlinedButton.styleFrom(
                        side: BorderSide(color: theme.colorScheme.error.withValues(alpha: 0.3)),
                        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
                      ),
                    ),
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _menuCard({
    required IconData icon,
    required String title,
    required String subtitle,
    required Color iconBg,
    required Color iconColor,
    VoidCallback? onTap,
  }) {
    return Card(
      elevation: 0,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
      child: ListTile(
        contentPadding: const EdgeInsets.symmetric(horizontal: 16, vertical: 6),
        leading: Container(
          width: 44,
          height: 44,
          decoration: BoxDecoration(color: iconBg, borderRadius: BorderRadius.circular(12)),
          child: Icon(icon, color: iconColor, size: 22),
        ),
        title: Text(title, style: const TextStyle(fontWeight: FontWeight.w600)),
        subtitle: Text(subtitle, style: TextStyle(fontSize: 12, color: Colors.grey.shade500)),
        trailing: const Icon(Icons.chevron_right, size: 20, color: Colors.grey),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(14)),
        onTap: onTap,
      ),
    );
  }
}

class _RoleStyle {
  final Color color;
  final String label;
  const _RoleStyle({required this.color, required this.label});
}
