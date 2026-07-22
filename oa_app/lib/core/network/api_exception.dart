class ApiException implements Exception {
  final int code;
  final String message;

  const ApiException(this.code, this.message);

  @override
  String toString() => 'ApiException($code): $message';
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
