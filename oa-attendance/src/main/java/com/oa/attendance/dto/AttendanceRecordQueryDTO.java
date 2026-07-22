package com.oa.attendance.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class AttendanceRecordQueryDTO {

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate date;
    private String month;

    @Min(value = 1, message = "pageNum最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "pageSize最小为1")
    @Max(value = 100, message = "pageSize最大为100")
    private Integer pageSize = 20;

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}
