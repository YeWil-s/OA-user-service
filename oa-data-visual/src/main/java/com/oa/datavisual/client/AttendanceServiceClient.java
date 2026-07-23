package com.oa.datavisual.client;

import com.oa.common.remote.RemoteResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class AttendanceServiceClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public AttendanceServiceClient(RestTemplate restTemplate,
                                   @Value("${oa.attendance-service.base-url:http://oa-attendance-service}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = trimSlash(baseUrl);
    }

    /**
     * 获取部门月考勤汇总。
     * 替代 VisualStatisticsExtractMapper.selectAttendanceMonthly 跨库SQL。
     */
    public List<Map<String, Object>> getMonthlySummary(String month) {
        try {
            var response = restTemplate.exchange(
                    baseUrl + "/api/attendance/records/monthly-summary?month=" + month,
                    HttpMethod.GET, null,
                    new ParameterizedTypeReference<RemoteResult<List<Map<String, Object>>>>() {});
            var body = response.getBody();
            if (body != null && body.getCode() == 200 && body.getData() != null) {
                return body.getData();
            }
        } catch (Exception e) {
            // fallback: return empty
        }
        return Collections.emptyList();
    }

    private String trimSlash(String value) {
        if (value != null && value.endsWith("/")) return value.substring(0, value.length() - 1);
        return value;
    }
}
