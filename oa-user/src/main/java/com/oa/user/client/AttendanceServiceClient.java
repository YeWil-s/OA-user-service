package com.oa.user.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class AttendanceServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AttendanceServiceClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AttendanceServiceClient(RestTemplate restTemplate,
                                   @Value("${oa.attendance-service.base-url:http://oa-attendance-service}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = trimSlash(baseUrl);
    }

    public void assignDefaultShift(Long userId) {
        try {
            Map<String, Object> body = new java.util.HashMap<>();
            body.put("userId", userId);
            body.put("shiftId", 1);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", "1");       // 系统管理员身份，内部调用
            headers.set("X-Username", "system");
            restTemplate.postForObject(baseUrl + "/api/attendance/user-shifts",
                    new HttpEntity<>(body, headers), String.class);
        } catch (RestClientException ex) {
            log.warn("分配默认班次失败: userId={}, error={}", userId, ex.getMessage());
        }
    }

    private String trimSlash(String value) {
        if (value != null && value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
