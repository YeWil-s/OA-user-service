package com.oa.datavisual.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@TableName("stat_dept_overview")
public class StatDeptOverview {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String statMonth;
    private Long deptId;
    private Integer activeEmployees;
    private Integer newHires;
    private Integer resignations;
    private BigDecimal attendanceRate;
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatMonth() {
        return statMonth;
    }

    public void setStatMonth(String statMonth) {
        this.statMonth = statMonth;
    }

    public Long getDeptId() {
        return deptId;
    }

    public void setDeptId(Long deptId) {
        this.deptId = deptId;
    }

    public Integer getActiveEmployees() {
        return activeEmployees;
    }

    public void setActiveEmployees(Integer activeEmployees) {
        this.activeEmployees = activeEmployees;
    }

    public Integer getNewHires() {
        return newHires;
    }

    public void setNewHires(Integer newHires) {
        this.newHires = newHires;
    }

    public Integer getResignations() {
        return resignations;
    }

    public void setResignations(Integer resignations) {
        this.resignations = resignations;
    }

    public BigDecimal getAttendanceRate() {
        return attendanceRate;
    }

    public void setAttendanceRate(BigDecimal attendanceRate) {
        this.attendanceRate = attendanceRate;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
