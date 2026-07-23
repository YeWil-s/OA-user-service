package com.oa.attendance.client;

import com.oa.common.exception.BusinessException;
import com.oa.common.remote.DeptInfo;
import com.oa.common.remote.RemotePage;
import com.oa.common.remote.RemoteResult;
import com.oa.common.remote.UserInfo;
import com.oa.common.result.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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

    public List<UserInfo> listUsersByDept(Long deptId) {
        if (deptId == null) {
            return Collections.emptyList();
        }
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/user/employees")
                    .queryParam("pageNum", 1)
                    .queryParam("pageSize", 1000)
                    .queryParam("deptId", deptId)
                    .queryParam("status", 1)
                    .toUriString();
            ResponseEntity<RemoteResult<RemotePage<UserInfo>>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<RemoteResult<RemotePage<UserInfo>>>() {
                    });
            RemoteResult<RemotePage<UserInfo>> body = response.getBody();
            if (body == null || body.getCode() != 200 || body.getData() == null) {
                return Collections.emptyList();
            }
            return body.getData().getRecords() == null ? Collections.emptyList() : body.getData().getRecords();
        } catch (RestClientException ex) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "调用用户服务失败: " + ex.getMessage());
        }
    }

    public Map<Long, UserInfo> mapUsers(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return userIds.stream()
                .distinct()
                .map(this::getUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(UserInfo::getId, Function.identity(), (a, b) -> a));
    }

    public List<Long> collectDeptAndChildren(Long deptId) {
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

    public Map<Long, DeptInfo> mapDepts() {
        return flattenDeptTree(getDeptTree()).stream()
                .collect(Collectors.toMap(DeptInfo::getId, Function.identity(), (a, b) -> a));
    }

    public List<UserInfo> listAllActiveUsers() {
        List<DeptInfo> allDepts = flattenDeptTree(getDeptTree());
        return allDepts.stream()
                .map(DeptInfo::getId)
                .flatMap(deptId -> listUsersByDept(deptId).stream())
                .filter(UserInfo::isActive)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(UserInfo::getId, Function.identity(), (a, b) -> a),
                        m -> new ArrayList<>(m.values())));
    }

    public String getUserName(Long userId) {
        UserInfo user = getUser(userId);
        if (user == null) {
            return "未知用户";
        }
        return user.getRealName() == null ? user.getUsername() : user.getRealName();
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

    private void collectDeptIds(Long deptId, Map<Long, List<DeptInfo>> childrenMap, Set<Long> result) {
        if (!result.add(deptId)) {
            return;
        }
        for (DeptInfo child : childrenMap.getOrDefault(deptId, Collections.emptyList())) {
            collectDeptIds(child.getId(), childrenMap, result);
        }
    }

    private String trimSlash(String value) {
        if (value != null && value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

}
