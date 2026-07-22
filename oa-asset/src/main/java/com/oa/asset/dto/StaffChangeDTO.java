package com.oa.asset.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class StaffChangeDTO {
    @NotNull(message = "员工ID不能为空") @Positive(message = "员工ID必须为正数") private Long userId;
    @NotNull(message = "变动类型不能为空") @Min(1) @Max(4) private Integer changeType;
    private Long beforeDept;
    private Long afterDept;
    private Long beforePosition;
    private Long afterPosition;
    @NotNull(message = "变动日期不能为空") private LocalDate changeDate;
    private String remark;
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public Integer getChangeType() { return changeType; } public void setChangeType(Integer changeType) { this.changeType = changeType; }
    public Long getBeforeDept() { return beforeDept; } public void setBeforeDept(Long beforeDept) { this.beforeDept = beforeDept; }
    public Long getAfterDept() { return afterDept; } public void setAfterDept(Long afterDept) { this.afterDept = afterDept; }
    public Long getBeforePosition() { return beforePosition; } public void setBeforePosition(Long beforePosition) { this.beforePosition = beforePosition; }
    public Long getAfterPosition() { return afterPosition; } public void setAfterPosition(Long afterPosition) { this.afterPosition = afterPosition; }
    public LocalDate getChangeDate() { return changeDate; } public void setChangeDate(LocalDate changeDate) { this.changeDate = changeDate; }
    public String getRemark() { return remark; } public void setRemark(String remark) { this.remark = remark; }
}
