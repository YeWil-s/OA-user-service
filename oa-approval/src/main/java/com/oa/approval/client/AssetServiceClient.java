
package com.oa.approval.client;

import com.oa.common.exception.BusinessException;
import com.oa.common.result.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Map;

@Component
public class AssetServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AssetServiceClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AssetServiceClient(RestTemplate restTemplate,
                              @Value("${oa.asset-service.base-url:http://oa-asset-service}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = trimSlash(baseUrl);
    }

    public void borrow(Long assetId, Long userId, LocalDate expectReturnDate) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("assetId", assetId);
        body.put("userId", userId);
        body.put("borrowDate", LocalDate.now().toString());
        if (expectReturnDate != null) {
            body.put("expectReturnDate", expectReturnDate.toString());
        }
        callInternal("/api/asset/internal/borrow", body, "资产领用");
    }

    public void createStaffChange(Long userId, Long afterDept, Long afterPosition,
                                  LocalDate changeDate, String remark) {
        Map<String, Object> body = new java.util.HashMap<>();
        body.put("userId", userId);
        body.put("changeType", 3);
        body.put("afterDept", afterDept);
        body.put("afterPosition", afterPosition);
        body.put("changeDate", changeDate.toString());
        if (remark != null) {
            body.put("remark", remark);
        }
        callInternal("/api/asset/internal/staff/changes", body, "人事变动");
    }

    private void callInternal(String path, Map<String, Object> body, String desc) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        try {
            ResponseEntity<com.oa.common.remote.RemoteResult<Void>> response = restTemplate.exchange(
                    baseUrl + path,
                    HttpMethod.POST,
                    entity,
                    new ParameterizedTypeReference<com.oa.common.remote.RemoteResult<Void>>() {
                    });
            com.oa.common.remote.RemoteResult<Void> result = response.getBody();
            if (result == null || result.getCode() != 200) {
                String msg = result != null ? result.getMessage() : "未知错误";
                throw new BusinessException(ResultCode.INTERNAL_ERROR, desc + "操作失败: " + msg);
            }
        } catch (RestClientException ex) {
            log.error("{}操作失败: {}", desc, ex.getMessage());
            throw new BusinessException(ResultCode.INTERNAL_ERROR, desc + "操作失败: " + ex.getMessage());
        }
    }

    public void requireAvailable(Long assetId) {
        try {
            ResponseEntity<com.oa.common.remote.RemoteResult<Map<String, Object>>> response = restTemplate.exchange(
                    baseUrl + "/api/asset/assets/" + assetId,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<com.oa.common.remote.RemoteResult<Map<String, Object>>>() {
                    });
            com.oa.common.remote.RemoteResult<Map<String, Object>> body = response.getBody();
            if (body == null || body.getCode() != 200 || body.getData() == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "资产不存在");
            }
            Object statusObj = body.getData().get("status");
            if (!(statusObj instanceof Number) || ((Number) statusObj).intValue() != 1) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "该资产当前不可领用");
            }
        } catch (RestClientException ex) {
            log.error("查询资产失败: {}", ex.getMessage());
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "查询资产失败: " + ex.getMessage());
        }
    }

    private String trimSlash(String value) {
        if (value != null && value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
