package com.oa.ai.client;

import com.oa.ai.config.FeignConfig;
import com.oa.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

@FeignClient(name = "oa-approval-service", configuration = FeignConfig.class)
public interface ApprovalServiceClient {

    @PostMapping("/api/approval/applications")
    Result<Map<String, Object>> submit(@RequestBody Map<String, Object> request);

    @GetMapping("/api/approval/applications/{id}")
    Result<Map<String, Object>> getDetail(@PathVariable("id") Long id);
}
