package com.oa.asset.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.oa.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** 调用用户服务，读取员工、部门和岗位主数据。 */
@FeignClient(name = "oa-user-service", path = "/api/user")
public interface UserDirectoryClient {
    @GetMapping("/employees/{id}")
    Result<EmployeeRef> employee(@PathVariable("id") Long id);

    @GetMapping("/depts")
    Result<List<DeptRef>> departments();

    @GetMapping("/positions")
    Result<PageRef<PositionRef>> positions(@RequestParam("pageNum") int pageNum,
                                           @RequestParam("pageSize") int pageSize,
                                           @RequestParam(value = "deptId", required = false) Long deptId);

    @PutMapping("/employees/{id}")
    Result<Void> updateEmployee(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    @PutMapping("/internal/employees/{id}/dept-position")
    Result<Void> updateDeptPosition(@PathVariable("id") Long id, @RequestBody Map<String, Object> body);

    @JsonIgnoreProperties(ignoreUnknown = true)
    class EmployeeRef {
        private Long id;
        private Long deptId;
        private Long positionId;
        private Integer status;
        private String realName;
        private String username;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getDeptId() { return deptId; }
        public void setDeptId(Long deptId) { this.deptId = deptId; }
        public Long getPositionId() { return positionId; }
        public void setPositionId(Long positionId) { this.positionId = positionId; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class DeptRef {
        private Long id;
        private String deptName;
        private List<DeptRef> children;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getDeptName() { return deptName; }
        public void setDeptName(String deptName) { this.deptName = deptName; }
        public List<DeptRef> getChildren() { return children; }
        public void setChildren(List<DeptRef> children) { this.children = children; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class PositionRef {
        private Long id;
        private Long deptId;
        private String positionName;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getDeptId() { return deptId; }
        public void setDeptId(Long deptId) { this.deptId = deptId; }
        public String getPositionName() { return positionName; }
        public void setPositionName(String positionName) { this.positionName = positionName; }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    class PageRef<T> {
        private List<T> records;
        public List<T> getRecords() { return records; }
        public void setRecords(List<T> records) { this.records = records; }
    }
}
