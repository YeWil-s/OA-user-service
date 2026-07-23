import 'package:shared_preferences/shared_preferences.dart';
import '../../../core/constants/app_constants.dart';

class AuthLocalSource {
  final SharedPreferences _prefs;

  AuthLocalSource(this._prefs);

  Future<void> saveToken(String token) async {
    await _prefs.setString(AppConstants.tokenKey, token);
  }

  Future<String?> readToken() async {
    return _prefs.getString(AppConstants.tokenKey);
  }

  Future<void> saveUserInfo({
    required int userId,
    required String username,
    required String realName,
    String? avatarUrl,
    List<String> roles = const [],
    List<String> permissions = const [],
  }) async {
    await _prefs.setInt(AppConstants.userIdKey, userId);
    await _prefs.setString(AppConstants.usernameKey, username);
    await _prefs.setString(AppConstants.realNameKey, realName);
    if (avatarUrl != null) {
      await _prefs.setString(AppConstants.avatarUrlKey, avatarUrl);
    }
    if (roles.isNotEmpty) {
      await _prefs.setStringList(AppConstants.rolesKey, roles);
    }
    if (permissions.isNotEmpty) {
      await _prefs.setStringList(AppConstants.permissionsKey, permissions);
    }
  }

  List<String> readRoles() => _prefs.getStringList(AppConstants.rolesKey) ?? [];
  List<String> readPermissions() => _prefs.getStringList(AppConstants.permissionsKey) ?? [];

  Future<void> clearAll() async {
    await _prefs.remove(AppConstants.tokenKey);
    await _prefs.remove(AppConstants.userIdKey);
    await _prefs.remove(AppConstants.usernameKey);
    await _prefs.remove(AppConstants.realNameKey);
    await _prefs.remove(AppConstants.avatarUrlKey);
    await _prefs.remove(AppConstants.rolesKey);
    await _prefs.remove(AppConstants.permissionsKey);
  }

  int? readUserId() => _prefs.getInt(AppConstants.userIdKey);
  String? readUsername() => _prefs.getString(AppConstants.usernameKey);
  String? readRealName() => _prefs.getString(AppConstants.realNameKey);
  String? readAvatarUrl() => _prefs.getString(AppConstants.avatarUrlKey);
}
