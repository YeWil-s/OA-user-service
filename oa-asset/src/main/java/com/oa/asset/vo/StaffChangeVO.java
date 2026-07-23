package com.oa.asset.vo;

public class StaffChangeVO {
    private Long id;
    private Long userId;
    private String realName;
    private String userName;
    private Integer changeType;
    private String changeDate;
    private Long fromDeptId;
    private String fromDeptName;
    private Long toDeptId;
    private String toDeptName;
    private Long fromPositionId;
    private String fromPositionName;
    private Long toPositionId;
    private String toPositionName;
    private String remark;
    private String createTime;

    public Long getId() { return id; } public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
    public String getRealName() { return realName; } public void setRealName(String realName) { this.realName = realName; }
    public String getUserName() { return userName; } public void setUserName(String userName) { this.userName = userName; }
    public Integer getChangeType() { return changeType; } public void setChangeType(Integer changeType) { this.changeType = changeType; }
    public String getChangeDate() { return changeDate; } public void setChangeDate(String changeDate) { this.changeDate = changeDate; }
    public Long getFromDeptId() { return fromDeptId; } public void setFromDeptId(Long fromDeptId) { this.fromDeptId = fromDeptId; }
    public String getFromDeptName() { return fromDeptName; } public void setFromDeptName(String fromDeptName) { this.fromDeptName = fromDeptName; }
    public Long getToDeptId() { return toDeptId; } public void setToDeptId(Long toDeptId) { this.toDeptId = toDeptId; }
    public String getToDeptName() { return toDeptName; } public void setToDeptName(String toDeptName) { this.toDeptName = toDeptName; }
    public Long getFromPositionId() { return fromPositionId; } public void setFromPositionId(Long fromPositionId) { this.fromPositionId = fromPositionId; }
    public String getFromPositionName() { return fromPositionName; } public void setFromPositionName(String fromPositionName) { this.fromPositionName = fromPositionName; }
    public Long getToPositionId() { return toPositionId; } public void setToPositionId(Long toPositionId) { this.toPositionId = toPositionId; }
    public String getToPositionName() { return toPositionName; } public void setToPositionName(String toPositionName) { this.toPositionName = toPositionName; }
    public String getRemark() { return remark; } public void setRemark(String remark) { this.remark = remark; }
    public String getCreateTime() { return createTime; } public void setCreateTime(String createTime) { this.createTime = createTime; }
}