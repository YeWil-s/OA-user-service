import 'package:dio/dio.dart';
import '../../../core/constants/api_constants.dart';
import '../../../core/network/api_response.dart';
import '../models/login_request.dart';
import '../models/login_response.dart';

class AuthRepository {
  final Dio _dio;

  AuthRepository(this._dio);

  Future<LoginResponse> login(LoginRequest request) async {
    final response = await _dio.post(
      ApiConstants.login,
      data: request.toJson(),
    );
    final apiResponse = ApiResponse.fromJson(
      response.data as Map<String, dynamic>,
      (data) => LoginResponse.fromJson(data as Map<String, dynamic>),
    );
    if (apiResponse.isSuccess && apiResponse.data != null) {
      return apiResponse.data!;
    }
    throw Exception(apiResponse.message);
  }
}
