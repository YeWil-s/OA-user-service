package com.oa.attendance.client;

import com.oa.attendance.config.FeignClientConfig;
import com.oa.common.exception.BusinessException;
import com.oa.common.result.Result;
import com.oa.common.result.ResultCode;
import feign.FeignException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@FeignClient(
        name = "oa-user-service",
        contextId = "attendanceUserServiceClient",
        path = "/api/user",
        configuration = FeignClientConfig.class
)
public interface UserServiceClient {

    @GetMapping("/employees/{userId}")
    Result<UserInfo> getUserResponse(@PathVariable("userId") Long userId);

    @GetMapping("/employees")
    Result<RemotePage<UserInfo>> listUsersResponse(
            @RequestParam("pageNum") Integer pageNum,
            @RequestParam("pageSize") Integer pageSize,
            @RequestParam("deptId") Long deptId,
            @RequestParam("status") Integer status
    );

    @GetMapping("/depts")
    Result<List<DeptInfo>> getDeptTreeResponse();

    default UserInfo getUser(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            Result<UserInfo> result = getUserResponse(userId);
            return isSuccess(result) ? result.getData() : null;
        } catch (FeignException ex) {
            throw userServiceUnavailable(ex);
        }
    }

    default UserInfo requireActiveUser(Long userId) {
        UserInfo user = getUser(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!user.isActive()) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        return user;
    }

    default List<UserInfo> listUsersByDept(Long deptId) {
        if (deptId == null) {
            return Collections.emptyList();
        }
        try {
            Result<RemotePage<UserInfo>> result = listUsersResponse(1, 1000, deptId, 1);
            if (!isSuccess(result) || result.getData() == null || result.getData().getRecords() == null) {
                return Collections.emptyList();
            }
            return result.getData().getRecords();
        } catch (FeignException ex) {
            throw userServiceUnavailable(ex);
        }
    }

    default Map<Long, UserInfo> mapUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .map(this::getUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UserInfo::getId, Function.identity(), (a, b) -> a));
    }

    default List<Long> collectDeptAndChildren(Long deptId) {
        if (deptId == null) {
            return Collections.emptyList();
        }
        List<DeptInfo> flat = flattenDeptTree(getDeptTree());
        Map<Long, List<DeptInfo>> childrenMap = flat.stream()
                .filter(dept -> dept.getParentId() != null)
                .collect(Collectors.groupingBy(DeptInfo::getParentId));
        Set<Long> result = new LinkedHashSet<>();
        collectDeptIds(deptId, childrenMap, result);
        return new ArrayList<>(result);
    }

    default Map<Long, DeptInfo> mapDepts() {
        return flattenDeptTree(getDeptTree()).stream()
                .collect(Collectors.toMap(DeptInfo::getId, Function.identity(), (a, b) -> a));
    }

    private List<DeptInfo> getDeptTree() {
        try {
            Result<List<DeptInfo>> result = getDeptTreeResponse();
            if (!isSuccess(result) || result.getData() == null) {
                return Collections.emptyList();
            }
            return result.getData();
        } catch (FeignException ex) {
            throw userServiceUnavailable(ex);
        }
    }

    private List<DeptInfo> flattenDeptTree(List<DeptInfo> tree) {
        List<DeptInfo> result = new ArrayList<>();
        if (tree == null) {
            return result;
        }
        for (DeptInfo dept : tree) {
            result.add(dept);
            result.addAll(flattenDeptTree(dept.getChildren()));
        }
        return result;
    }

    private void collectDeptIds(Long deptId, Map<Long, List<DeptInfo>> childrenMap, Set<Long> result) {
        if (!result.add(deptId)) {
            return;
        }
        for (DeptInfo child : childrenMap.getOrDefault(deptId, Collections.emptyList())) {
            collectDeptIds(child.getId(), childrenMap, result);
        }
    }

    private boolean isSuccess(Result<?> result) {
        return result != null && result.getCode() == ResultCode.SUCCESS.getCode();
    }

    private BusinessException userServiceUnavailable(FeignException ex) {
        if (ex.status() == 401) {
            return new BusinessException(ResultCode.UNAUTHORIZED, "User service authentication failed");
        }
        return new BusinessException(ResultCode.INTERNAL_ERROR, "调用用户服务失败: " + ex.getMessage());
    }

    class RemotePage<T> {
        private List<T> records;

        public List<T> getRecords() { return records; }
        public void setRecords(List<T> records) { this.records = records; }
    }

    class UserInfo {
        private Long id;
        private String username;
        private String realName;
        private Long deptId;
        private Integer status;

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getRealName() { return realName; }
        public void setRealName(String realName) { this.realName = realName; }
        public Long getDeptId() { return deptId; }
        public void setDeptId(Long deptId) { this.deptId = deptId; }
        public Integer getStatus() { return status; }
        public void setStatus(Integer status) { this.status = status; }
        public boolean isActive() { return !Integer.valueOf(0).equals(status); }
    }

    class DeptInfo {
        private Long id;
        private Long parentId;
        private String deptName;
        private Long leaderId;
        private List<DeptInfo> children = new ArrayList<>();

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Long getParentId() { return parentId; }
        public void setParentId(Long parentId) { this.parentId = parentId; }
        public String getDeptName() { return deptName; }
        public void setDeptName(String deptName) { this.deptName = deptName; }
        public Long getLeaderId() { return leaderId; }
        public void setLeaderId(Long leaderId) { this.leaderId = leaderId; }
        public List<DeptInfo> getChildren() { return children; }
        public void setChildren(List<DeptInfo> children) { this.children = children; }
    }
}
