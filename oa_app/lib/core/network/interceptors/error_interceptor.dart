import 'package:dio/dio.dart';
import 'package:flutter/foundation.dart';
import '../api_exception.dart';

class ErrorInterceptor extends Interceptor {
  final VoidCallback onUnauthorized;

  ErrorInterceptor({required this.onUnauthorized});

  @override
  void onError(DioException err, ErrorInterceptorHandler handler) {
    if (err.response != null) {
      final code = err.response!.statusCode ?? -1;
      final responseData = err.response!.data;

      String message = '请求失败';
      if (responseData is Map<String, dynamic>) {
        message = responseData['message'] as String? ?? '请求失败';
      }

      if (code == 401) {
        onUnauthorized();
        handler.reject(DioException(
          requestOptions: err.requestOptions,
          error: const UnauthorizedException(),
        ));
        return;
      }

      handler.reject(DioException(
        requestOptions: err.requestOptions,
        error: BusinessException(code, message),
      ));
      return;
    }

    switch (err.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.receiveTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.connectionError:
        handler.reject(DioException(
          requestOptions: err.requestOptions,
          error: const NetworkException('网络连接失败，请检查网络'),
        ));
        break;
      default:
        handler.reject(DioException(
          requestOptions: err.requestOptions,
          error: const NetworkException(),
        ));
    }
  }
}
