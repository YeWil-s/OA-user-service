import 'package:dio/dio.dart';

class ApiException implements Exception {
  final int code;
  final String message;

  const ApiException(this.code, this.message);

  @override
  String toString() => message;
}

/// 从任意异常中提取可读的错误消息
String extractErrorMessage(dynamic error) {
  // DioException 包装了业务异常
  if (error is DioException) {
    if (error.error is ApiException) {
      return (error.error as ApiException).message;
    }
    if (error.response?.data is Map<String, dynamic>) {
      final msg = (error.response!.data as Map<String, dynamic>)['message'] as String?;
      if (msg != null && msg.isNotEmpty) return msg;
    }
    return switch (error.type) {
      DioExceptionType.connectionTimeout || DioExceptionType.receiveTimeout || DioExceptionType.sendTimeout => '请求超时，请检查网络',
      DioExceptionType.connectionError => '网络连接失败，请检查网络',
      _ => '请求失败(${error.response?.statusCode})',
    };
  }
  // ApiException 直接取其消息
  if (error is ApiException) return error.message;
  // 其他异常
  if (error is Exception || error is Error) return error.toString();
  return '$error';
}

class UnauthorizedException extends ApiException {
  const UnauthorizedException([String message = '未登录或登录已过期'])
      : super(401, message);
}

class BusinessException extends ApiException {
  const BusinessException(int code, String message) : super(code, message);
}

class NetworkException extends ApiException {
  const NetworkException([String message = '网络连接失败'])
      : super(-1, message);
}
