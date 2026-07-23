class Shift {
  final int id;
  final String name;
  final String startTime;
  final String endTime;
  final String? description;

  const Shift({
    required this.id,
    required this.name,
    required this.startTime,
    required this.endTime,
    this.description,
  });

  factory Shift.fromJson(Map<String, dynamic> json) {
    return Shift(
      id: json['id'] as int,
      name: json['name'] as String,
      startTime: json['startTime'] as String,
      endTime: json['endTime'] as String,
      description: json['description'] as String?,
    );
  }
}
