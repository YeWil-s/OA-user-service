package com.oa.approval.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
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

    public void markLeave(Long userId, LocalDate startDate, LocalDate endDate, Long applicationId) {
        try {
            Map<String, Object> body = new java.util.HashMap<>();
            body.put("userId", userId);
            body.put("startDate", startDate.toString());
            body.put("endDate", endDate.toString());
            body.put("applicationId", applicationId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForObject(baseUrl + "/api/attendance/leave",
                    new HttpEntity<>(body, headers), String.class);
        } catch (RestClientException ex) {
            log.warn("标记请假日期失败: userId={}, error={}", userId, ex.getMessage());
        }
    }

    public void cancelLeave(Long applicationId) {
        try {
            restTemplate.exchange(baseUrl + "/api/attendance/leave/" + applicationId,
                    HttpMethod.DELETE, null, String.class);
        } catch (RestClientException ex) {
            log.warn("取消请假标记失败: applicationId={}, error={}", applicationId, ex.getMessage());
        }
    }

    public void markOvertime(Long userId, LocalDate startDate, LocalDate endDate, java.math.BigDecimal overtimeHours, Long applicationId) {
        try {
            Map<String, Object> body = new java.util.HashMap<>();
            body.put("userId", userId);
            body.put("startDate", startDate.toString());
            body.put("endDate", endDate.toString());
            body.put("overtimeHours", overtimeHours);
            body.put("applicationId", applicationId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForObject(baseUrl + "/api/attendance/overtime",
                    new HttpEntity<>(body, headers), String.class);
        } catch (RestClientException ex) {
            log.warn("标记加班失败: userId={}, error={}", userId, ex.getMessage());
        }
    }

    public void cancelOvertime(Long applicationId) {
        try {
            restTemplate.exchange(baseUrl + "/api/attendance/overtime/" + applicationId,
                    HttpMethod.DELETE, null, String.class);
        } catch (RestClientException ex) {
            log.warn("取消加班标记失败: applicationId={}, error={}", applicationId, ex.getMessage());
        }
    }

    public void markFieldWork(Long userId, LocalDate startDate, LocalDate endDate, Long applicationId) {
        try {
            Map<String, Object> body = new java.util.HashMap<>();
            body.put("userId", userId);
            body.put("startDate", startDate.toString());
            body.put("endDate", endDate.toString());
            body.put("applicationId", applicationId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            restTemplate.postForObject(baseUrl + "/api/attendance/fieldwork",
                    new HttpEntity<>(body, headers), String.class);
        } catch (RestClientException ex) {
            log.warn("标记外勤失败: userId={}, error={}", userId, ex.getMessage());
        }
    }

    public void cancelFieldWork(Long applicationId) {
        try {
            restTemplate.exchange(baseUrl + "/api/attendance/fieldwork/" + applicationId,
                    HttpMethod.DELETE, null, String.class);
        } catch (RestClientException ex) {
            log.warn("取消外勤标记失败: applicationId={}, error={}", applicationId, ex.getMessage());
        }
    }

    private String trimSlash(String value) {
        if (value != null && value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
