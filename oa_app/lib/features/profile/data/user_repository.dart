import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';

class UserRepository {
  final Dio _dio;

  UserRepository(this._dio);

  Future<void> changePassword(int userId, String oldPassword, String newPassword) async {
    await _dio.put(
      ApiConstants.updatePassword(userId),
      data: {
        'oldPassword': oldPassword,
        'newPassword': newPassword,
      },
    );
  }
}
