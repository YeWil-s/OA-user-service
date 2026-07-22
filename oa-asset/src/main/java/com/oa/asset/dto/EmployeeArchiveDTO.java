package com.oa.asset.dto;

import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;

public class EmployeeArchiveDTO {
    @Pattern(regexp = "(^$)|(^\\d{17}[0-9Xx]$)", message = "身份证号格式不正确")
    private String idCard;
    private Integer education;
    private String major;
    private String graduateSchool;
    private String address;
    private String emergencyContact;
    @Pattern(regexp = "(^$)|(^[0-9+\\-]{6,20}$)", message = "紧急联系电话格式不正确")
    private String emergencyPhone;
    private LocalDate contractStart;
    private LocalDate contractEnd;
    public String getIdCard() { return idCard; } public void setIdCard(String idCard) { this.idCard = idCard; }
    public Integer getEducation() { return education; } public void setEducation(Integer education) { this.education = education; }
    public String getMajor() { return major; } public void setMajor(String major) { this.major = major; }
    public String getGraduateSchool() { return graduateSchool; } public void setGraduateSchool(String graduateSchool) { this.graduateSchool = graduateSchool; }
    public String getAddress() { return address; } public void setAddress(String address) { this.address = address; }
    public String getEmergencyContact() { return emergencyContact; } public void setEmergencyContact(String emergencyContact) { this.emergencyContact = emergencyContact; }
    public String getEmergencyPhone() { return emergencyPhone; } public void setEmergencyPhone(String emergencyPhone) { this.emergencyPhone = emergencyPhone; }
    public LocalDate getContractStart() { return contractStart; } public void setContractStart(LocalDate contractStart) { this.contractStart = contractStart; }
    public LocalDate getContractEnd() { return contractEnd; } public void setContractEnd(LocalDate contractEnd) { this.contractEnd = contractEnd; }
}
