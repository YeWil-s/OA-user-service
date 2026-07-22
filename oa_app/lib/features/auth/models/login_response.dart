class LoginResponse {
  final String accessToken;
  final int userId;
  final String username;
  final String realName;
  final String? avatarUrl;
  final List<String> roles;
  final List<String> permissions;

  const LoginResponse({
    required this.accessToken,
    required this.userId,
    required this.username,
    required this.realName,
    this.avatarUrl,
    required this.roles,
    required this.permissions,
  });

  factory LoginResponse.fromJson(Map<String, dynamic> json) {
    return LoginResponse(
      accessToken: json['accessToken'] as String,
      userId: json['userId'] as int,
      username: json['username'] as String,
      realName: json['realName'] as String,
      avatarUrl: json['avatarUrl'] as String?,
      roles: List<String>.from(json['roles'] as List? ?? []),
      permissions: List<String>.from(json['permissions'] as List? ?? []),
    );
  }
}
