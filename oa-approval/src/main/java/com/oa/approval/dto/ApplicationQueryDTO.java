package com.oa.approval.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class ApplicationQueryDTO {

    @Min(value = 0, message = "状态值最小为0")
    @Max(value = 4, message = "状态值最大为4")
    private Integer status;

    @Min(value = 1, message = "pageNum最小为1")
    private Integer pageNum = 1;

    @Min(value = 1, message = "pageSize最小为1")
    @Max(value = 100, message = "pageSize最大为100")
    private Integer pageSize = 20;

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public Integer getPageNum() { return pageNum; }
    public void setPageNum(Integer pageNum) { this.pageNum = pageNum; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}
