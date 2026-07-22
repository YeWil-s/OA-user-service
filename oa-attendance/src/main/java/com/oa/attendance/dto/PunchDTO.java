package com.oa.attendance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public class PunchDTO {

    @Min(value = 1, message = "打卡类型只能是1=现场、2=外勤")
    @Max(value = 2, message = "打卡类型只能是1=现场、2=外勤")
    private Integer punchType = 1;

    @Size(max = 512, message = "设备信息不能超过512个字符")
    private String deviceInfo;

    @Size(max = 200, message = "打卡地点不能超过200个字符")
    private String location;

    public Integer getPunchType() { return punchType; }
    public void setPunchType(Integer punchType) { this.punchType = punchType; }
    public String getDeviceInfo() { return deviceInfo; }
    public void setDeviceInfo(String deviceInfo) { this.deviceInfo = deviceInfo; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
}
