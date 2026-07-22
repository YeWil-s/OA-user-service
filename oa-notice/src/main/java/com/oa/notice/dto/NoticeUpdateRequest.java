package com.oa.notice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NoticeUpdateRequest {

    @Size(max = 200, message = "公告标题不能超过200个字符")
    private String title;

    private String content;

    @Min(value = 1, message = "公告类型不正确")
    @Max(value = 3, message = "公告类型不正确")
    private Integer noticeType;

    @Min(value = 1, message = "发布范围不正确")
    @Max(value = 3, message = "发布范围不正确")
    private Integer targetType;

    private List<Long> targetIds;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @Min(value = 0, message = "公告状态不正确")
    @Max(value = 2, message = "公告状态不正确")
    private Integer status;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(Integer noticeType) {
        this.noticeType = noticeType;
    }

    public Integer getTargetType() {
        return targetType;
    }

    public void setTargetType(Integer targetType) {
        this.targetType = targetType;
    }

    public List<Long> getTargetIds() {
        return targetIds;
    }

    public void setTargetIds(List<Long> targetIds) {
        this.targetIds = targetIds == null ? new ArrayList<>() : targetIds;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
