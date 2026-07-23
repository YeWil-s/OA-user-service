package com.oa.notice.client;

import com.oa.common.remote.DeptInfo;
import com.oa.common.remote.RemotePage;
import com.oa.common.remote.RemoteResult;
import com.oa.common.remote.UserInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UserServiceClient {

    private static final Logger log = LoggerFactory.getLogger(UserServiceClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public UserServiceClient(@Value("${oa.user-service.base-url:http://localhost:8081}") String baseUrl) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = trimSlash(baseUrl);
    }

    public List<UserInfo> listUsersByDept(Long deptId) {
        if (deptId == null) {
            return Collections.emptyList();
        }
        try {
            String url = UriComponentsBuilder.fromHttpUrl(baseUrl + "/api/user/employees")
                    .queryParam("pageNum", 1)
                    .queryParam("pageSize", 2000)
                    .queryParam("deptId", deptId)
                    .queryParam("status", 1)
                    .toUriString();
            ResponseEntity<RemoteResult<RemotePage<UserInfo>>> response = restTemplate.exchange(
                    url, HttpMethod.GET, null,
                    new ParameterizedTypeReference<RemoteResult<RemotePage<UserInfo>>>() {});
            RemoteResult<RemotePage<UserInfo>> body = response.getBody();
            if (body == null || body.getCode() != 200 || body.getData() == null) {
                return Collections.emptyList();
            }
            return body.getData().getRecords() == null
                    ? Collections.emptyList()
                    : body.getData().getRecords();
        } catch (RestClientException ex) {
            log.warn("获取部门用户列表失败: deptId={}, error={}", deptId, ex.getMessage());
            return Collections.emptyList();
        }
    }

    public List<UserInfo> listAllActiveUsers() {
        List<DeptInfo> allDepts = flattenDeptTree(getDeptTree());
        return allDepts.stream()
                .map(DeptInfo::getId)
                .flatMap(deptId -> listUsersByDept(deptId).stream())
                .filter(UserInfo::isActive)
                .collect(Collectors.collectingAndThen(
                        Collectors.toMap(UserInfo::getId, u -> u, (a, b) -> a),
                        m -> new ArrayList<>(m.values())));
    }

    public Set<Long> collectDeptAndChildren(Long deptId) {
        if (deptId == null) {
            return Collections.emptySet();
        }
        List<DeptInfo> flat = flattenDeptTree(getDeptTree());
        Map<Long, List<DeptInfo>> childrenMap = flat.stream()
                .filter(dept -> dept.getParentId() != null)
                .collect(Collectors.groupingBy(DeptInfo::getParentId));
        Set<Long> result = new LinkedHashSet<>();
        collectDeptIds(deptId, childrenMap, result);
        return result;
    }

    private List<DeptInfo> getDeptTree() {
        try {
            ResponseEntity<RemoteResult<List<DeptInfo>>> response = restTemplate.exchange(
                    baseUrl + "/api/user/depts", HttpMethod.GET, null,
                    new ParameterizedTypeReference<RemoteResult<List<DeptInfo>>>() {});
            RemoteResult<List<DeptInfo>> body = response.getBody();
            if (body == null || body.getCode() != 200 || body.getData() == null) {
                return Collections.emptyList();
            }
            return body.getData();
        } catch (RestClientException ex) {
            log.warn("获取部门树失败: {}", ex.getMessage());
            return Collections.emptyList();
        }
    }

    private List<DeptInfo> flattenDeptTree(List<DeptInfo> tree) {
        List<DeptInfo> result = new ArrayList<>();
        if (tree == null) return result;
        for (DeptInfo dept : tree) {
            result.add(dept);
            result.addAll(flattenDeptTree(dept.getChildren()));
        }
        return result;
    }

    private void collectDeptIds(Long deptId, Map<Long, List<DeptInfo>> childrenMap, Set<Long> result) {
        if (!result.add(deptId)) return;
        for (DeptInfo child : childrenMap.getOrDefault(deptId, Collections.emptyList())) {
            collectDeptIds(child.getId(), childrenMap, result);
        }
    }

    private String trimSlash(String value) {
        if (value != null && value.endsWith("/")) return value.substring(0, value.length() - 1);
        return value;
    }
}
