package com.oa.asset.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDate;
import java.time.LocalDateTime;

@TableName("ast_staff_change")
/** 人事变动记录：1=入职，2=转正，3=调岗，4=离职。 */
public class StaffChange {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer changeType;
    private Long beforeDept;
    private Long afterDept;
    private Long beforePosition;
    private Long afterPosition;
    private LocalDate changeDate;
    private String remark;
    private LocalDateTime createTime;
    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public Integer getChangeType() { return changeType; } public void setChangeType(Integer changeType) { this.changeType = changeType; }
    public Long getBeforeDept() { return beforeDept; } public void setBeforeDept(Long beforeDept) { this.beforeDept = beforeDept; }
    public Long getAfterDept() { return afterDept; } public void setAfterDept(Long afterDept) { this.afterDept = afterDept; }
    public Long getBeforePosition() { return beforePosition; } public void setBeforePosition(Long beforePosition) { this.beforePosition = beforePosition; }
    public Long getAfterPosition() { return afterPosition; } public void setAfterPosition(Long afterPosition) { this.afterPosition = afterPosition; }
    public LocalDate getChangeDate() { return changeDate; } public void setChangeDate(LocalDate changeDate) { this.changeDate = changeDate; }
    public String getRemark() { return remark; } public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreateTime() { return createTime; } public void setCreateTime(LocalDateTime createTime) { this.createTime = createTime; }
}
