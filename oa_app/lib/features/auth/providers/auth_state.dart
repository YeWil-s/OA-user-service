import '../models/login_response.dart';

class UserInfo {
  final int userId;
  final String username;
  final String realName;
  final String? avatarUrl;
  final List<String> roles;
  final List<String> permissions;

  const UserInfo({
    required this.userId,
    required this.username,
    required this.realName,
    this.avatarUrl,
    required this.roles,
    required this.permissions,
  });

  factory UserInfo.fromLoginResponse(LoginResponse r) {
    return UserInfo(
      userId: r.userId,
      username: r.username,
      realName: r.realName,
      avatarUrl: r.avatarUrl,
      roles: _normalizeRoles(r.roles),
      permissions: r.permissions,
    );
  }

  /// 去掉后端 ROLE_ 前缀并转小写，统一格式
  static List<String> _normalizeRoles(List<String> raw) {
    return raw.map((r) {
      if (r.startsWith('ROLE_')) {
        return r.substring(5).toLowerCase();
      }
      return r.toLowerCase();
    }).toList();
  }

  bool hasRole(String role) => roles.contains(role);
  bool hasAnyRole(List<String> checkRoles) => roles.any((r) => checkRoles.contains(r));
  bool hasPermission(String perm) => permissions.contains(perm);
  bool get isAdmin => hasRole('admin');
  bool get canApprove => hasAnyRole(['admin', 'hr', 'leader', 'dept_manager']);
}
