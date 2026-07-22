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
      roles: r.roles,
      permissions: r.permissions,
    );
  }
}
