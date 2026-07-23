import 'package:dio/dio.dart';
import 'package:geocoding/geocoding.dart';
import 'package:geolocator/geolocator.dart';
import 'package:geolocator_android/geolocator_android.dart';

class LocationResult {
  final double latitude;
  final double longitude;
  final String? address;
  final List<NearbyPlace> nearbyPlaces;

  const LocationResult({
    required this.latitude,
    required this.longitude,
    this.address,
    this.nearbyPlaces = const [],
  });

  String get locationStr => '$longitude,$latitude';

  String get displayName {
    if (address != null && address!.isNotEmpty) return address!;
    return '${longitude.toStringAsFixed(6)}, ${latitude.toStringAsFixed(6)}';
  }
}

class NearbyPlace {
  final String name;
  final String address;
  final double latitude;
  final double longitude;
  final int distance; // 米

  const NearbyPlace({
    required this.name,
    required this.address,
    required this.latitude,
    required this.longitude,
    required this.distance,
  });

  String get displayName => '$name ($address  ${distance}m)';

  factory NearbyPlace.fromAmapJson(Map<String, dynamic> json) {
    final locStr = json['location'] as String? ?? '0,0';
    final parts = locStr.split(',');
    return NearbyPlace(
      name: json['name'] as String? ?? '',
      address: json['address'] as String? ?? '',
      longitude: double.tryParse(parts[0]) ?? 0,
      latitude: double.tryParse(parts[1]) ?? 0,
      distance: int.tryParse(json['distance'] as String? ?? '0') ?? 0,
    );
  }
}

class LocationService {
  LocationService._();

  static Future<bool> isLocationServiceEnabled() async {
    return Geolocator.isLocationServiceEnabled();
  }

  static Future<LocationPermission> checkPermission() async {
    return Geolocator.checkPermission();
  }

  static Future<LocationPermission> requestPermission() async {
    return Geolocator.requestPermission();
  }

  static Future<LocationResult> getCurrentLocation() async {
    final position = await _getPositionFast();
    return LocationResult(
      latitude: position.latitude,
      longitude: position.longitude,
    );
  }

  static Future<LocationResult> getLocationWithAddress() async {
    final position = await _getPositionFast();
    // 地理编码和高德周边搜索并行，不阻塞打卡
    final results = await Future.wait([
      _reverseGeocode(position.latitude, position.longitude),
      _fetchNearbyPlaces(position.latitude, position.longitude),
    ], eagerError: false);
    return LocationResult(
      latitude: position.latitude,
      longitude: position.longitude,
      address: results[0] as String?,
      nearbyPlaces: (results[1] as List<NearbyPlace>?) ?? [],
    );
  }

  static Future<List<NearbyPlace>> getNearbyPlaces(double lat, double lng) async {
    return _fetchNearbyPlaces(lat, lng);
  }

  // --- private ---

  /// 快速获取位置：先尝试缓存位置，失败后用 medium 精度 5 秒超时
  static Future<Position> _getPositionFast() async {
    final serviceEnabled = await Geolocator.isLocationServiceEnabled();
    if (!serviceEnabled) {
      throw LocationException('定位服务未开启，请在设置中开启GPS');
    }

    var permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      permission = await Geolocator.requestPermission();
      if (permission == LocationPermission.denied) {
        throw LocationException('定位权限被拒绝');
      }
    }
    if (permission == LocationPermission.deniedForever) {
      throw LocationException('定位权限被永久拒绝，请在设置中开启');
    }

    // 优先用缓存位置（毫秒级），新鲜度要求 5 分钟内
    final lastPos = await Geolocator.getLastKnownPosition();
    if (lastPos != null) {
      final age = DateTime.now().difference(lastPos.timestamp!);
      if (age.inSeconds < 300) return lastPos;
    }

    return Geolocator.getCurrentPosition(
      locationSettings: AndroidSettings(
        accuracy: LocationAccuracy.low,
        timeLimit: const Duration(seconds: 15),
        forceLocationManager: true,
      ),
    );
  }

  static Future<String?> _reverseGeocode(double lat, double lng) async {
    try {
      final placemarks = await placemarkFromCoordinates(lat, lng)
          .timeout(const Duration(seconds: 3));
      if (placemarks.isNotEmpty) {
        final p = placemarks.first;
        final parts = [
          if (p.administrativeArea != null && p.administrativeArea != p.locality)
            p.administrativeArea,
          if (p.locality != null) p.locality,
          if (p.subLocality != null) p.subLocality,
          if (p.street != null) p.street,
        ].where((s) => s != null && s!.isNotEmpty);
        if (parts.isNotEmpty) return parts.join('');
      }
    } catch (_) {}
    return null;
  }

  static Future<List<NearbyPlace>> _fetchNearbyPlaces(double lat, double lng) async {
    const amapKey = '93c05481e59aad02c17b6ee2675a832d';
    try {
      final dio = Dio(BaseOptions(
        connectTimeout: const Duration(seconds: 3),
        receiveTimeout: const Duration(seconds: 3),
      ));
      final response = await dio.get(
        'https://restapi.amap.com/v3/place/around',
        queryParameters: {
          'key': amapKey,
          'location': '$lng,$lat',
          'radius': 1000,
          'extensions': 'base',
          'offset': 10,
        },
      );
      final data = response.data as Map<String, dynamic>;
      if (data['status'] == '1') {
        final pois = data['pois'] as List<dynamic>? ?? [];
        return pois.map((e) => NearbyPlace.fromAmapJson(e as Map<String, dynamic>)).toList();
      }
    } catch (_) {}
    return [];
  }
}

class LocationException implements Exception {
  final String message;
  const LocationException(this.message);

  @override
  String toString() => message;
}
