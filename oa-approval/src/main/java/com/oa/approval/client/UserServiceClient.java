package com.oa.approval.client;

import com.oa.common.exception.BusinessException;
import com.oa.common.result.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class UserServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UserServiceClient(RestTemplate restTemplate,
                             @Value("${oa.user-service.base-url:http://localhost:8081}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = trimSlash(baseUrl);
    }

    public UserInfo getUser(Long userId) {
        if (userId == null) {
            return null;
        }
        try {
            ResponseEntity<RemoteResult<UserInfo>> response = restTemplate.exchange(
                    baseUrl + "/api/user/employees/" + userId,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<RemoteResult<UserInfo>>() {
                    });
            RemoteResult<UserInfo> body = response.getBody();
            if (body == null || body.getCode() != 200) {
                return null;
            }
            return body.getData();
        } catch (RestClientException ex) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "调用用户服务失败: " + ex.getMessage());
        }
    }

    public UserInfo requireActiveUser(Long userId) {
        UserInfo user = getUser(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        if (!user.isActive()) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }
        return user;
    }

    public String getUserName(Long userId) {
        UserInfo user = getUser(userId);
        if (user == null) {
            return "未知用户";
        }
        return user.getRealName() == null ? user.getUsername() : user.getRealName();
    }

    public String getDeptName(Long deptId) {
        DeptInfo dept = mapDepts().get(deptId);
        return dept == null ? "未知部门" : dept.getDeptName();
    }

    public Long resolveApproverId(Long deptId, Long applicantUserId) {
        Map<Long, DeptInfo> deptMap = mapDepts();
        Long currentDeptId = deptId;
        while (currentDeptId != null && currentDeptId > 0) {
            DeptInfo dept = deptMap.get(currentDeptId);
            if (dept == null) {
                break;
            }
            if (isUsableApprover(dept.getLeaderId(), applicantUserId)) {
                return dept.getLeaderId();
            }
            currentDeptId = dept.getParentId();
        }
        if (isUsableApprover(1L, applicantUserId)) {
            return 1L;
        }
        throw new BusinessException(ResultCode.BAD_REQUEST, "未找到可用审批人，请先配置部门主管");
    }

    private boolean isUsableApprover(Long leaderId, Long applicantUserId) {
        return leaderId != null && leaderId > 0 && !Objects.equals(leaderId, applicantUserId);
    }

    public Map<Long, UserInfo> mapUsers(List<Long> userIds) {
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

    private Map<Long, DeptInfo> mapDepts() {
        return flattenDeptTree(getDeptTree()).stream()
                .collect(Collectors.toMap(DeptInfo::getId, Function.identity(), (a, b) -> a));
    }

    private List<DeptInfo> getDeptTree() {
        try {
            ResponseEntity<RemoteResult<List<DeptInfo>>> response = restTemplate.exchange(
                    baseUrl + "/api/user/depts",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<RemoteResult<List<DeptInfo>>>() {
                    });
            RemoteResult<List<DeptInfo>> body = response.getBody();
            if (body == null || body.getCode() != 200 || body.getData() == null) {
                return Collections.emptyList();
            }
            return body.getData();
        } catch (RestClientException ex) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "调用用户服务失败: " + ex.getMessage());
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

    private String trimSlash(String value) {
        if (value != null && value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    public static class RemoteResult<T> {
        private int code;
        private String message;
        private T data;

        public int getCode() { return code; }
        public void setCode(int code) { this.code = code; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        public T getData() { return data; }
        public void setData(T data) { this.data = data; }
    }

    public static class UserInfo {
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

    public static class DeptInfo {
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
