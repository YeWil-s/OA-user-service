package com.oa.approval.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class NoticeServiceClient {

    private static final Logger log = LoggerFactory.getLogger(NoticeServiceClient.class);

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public NoticeServiceClient(RestTemplate restTemplate,
                               @Value("${oa.notice-service.base-url:http://oa-notice-service}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = trimSlash(baseUrl);
    }

    public void sendMessage(Long userId, String title, String content, Integer msgType, Long relatedId) {
        try {
            Map<String, Object> body = new java.util.HashMap<>();
            body.put("userId", userId);
            body.put("title", title);
            body.put("content", content);
            body.put("msgType", msgType);
            body.put("relatedId", relatedId);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            restTemplate.postForObject(baseUrl + "/api/notice/internal/messages", request, Map.class);
        } catch (RestClientException ex) {
            log.warn("发送通知消息失败: userId={}, title={}, error={}", userId, title, ex.getMessage());
        }
    }

    private String trimSlash(String value) {
        if (value != null && value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
