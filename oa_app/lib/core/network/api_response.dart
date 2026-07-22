class ApiResponse<T> {
  final int code;
  final String message;
  final T? data;
  final int timestamp;

  const ApiResponse({
    required this.code,
    required this.message,
    this.data,
    required this.timestamp,
  });

  bool get isSuccess => code == 200;

  factory ApiResponse.fromJson(
    Map<String, dynamic> json,
    T? Function(dynamic data)? fromJsonData,
  ) {
    return ApiResponse(
      code: json['code'] as int? ?? -1,
      message: json['message'] as String? ?? '',
      data: json['data'] != null && fromJsonData != null
          ? fromJsonData(json['data'])
          : null,
      timestamp: json['timestamp'] as int? ?? 0,
    );
  }
}

/// 分页响应
class PageData<T> {
  final List<T> records;
  final int total;
  final int current;
  final int size;

  const PageData({
    required this.records,
    required this.total,
    required this.current,
    required this.size,
  });

  bool get hasMore => current * size < total;

  factory PageData.fromJson(
    Map<String, dynamic> json,
    T Function(Map<String, dynamic>) fromJsonItem,
  ) {
    final rawList = json['records'] as List<dynamic>? ?? [];
    return PageData(
      records: rawList.map((e) => fromJsonItem(e as Map<String, dynamic>)).toList(),
      total: json['total'] as int? ?? 0,
      current: json['current'] as int? ?? 1,
      size: json['size'] as int? ?? 20,
    );
  }
}
