package com.oa.user.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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

    public void createArchive(Long userId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", "1");
            headers.set("X-Username", "system");
            restTemplate.exchange(
                    baseUrl + "/api/asset/staff/archive/" + userId,
                    HttpMethod.PUT,
                    new HttpEntity<>(new java.util.HashMap<>(), headers),
                    String.class);
        } catch (RestClientException ex) {
            log.warn("创建员工档案失败: userId={}, error={}", userId, ex.getMessage());
        }
    }

    private String trimSlash(String value) {
        if (value != null && value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }
}
