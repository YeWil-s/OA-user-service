package com.oa.attendance.dto;

public class PunchDTO {

    private Integer punchType = 1;
    private String deviceInfo;
    private String location;
    private java.math.BigDecimal latitude;
    private java.math.BigDecimal longitude;

    public Integer getPunchType() { return punchType; }
    public void setPunchType(Integer punchType) { this.punchType = punchType; }
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public java.math.BigDecimal getLatitude() { return latitude; }
    public void setLatitude(java.math.BigDecimal latitude) { this.latitude = latitude; }
    public java.math.BigDecimal getLongitude() { return longitude; }
    public void setLongitude(java.math.BigDecimal longitude) { this.longitude = longitude; }
}
