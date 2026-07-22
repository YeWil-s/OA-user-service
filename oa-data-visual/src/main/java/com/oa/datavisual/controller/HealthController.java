package com.oa.datavisual.controller;

import com.oa.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/visual")
public class HealthController {

    @GetMapping("/health")
    public Result<Map<String, String>> health() {
        return Result.success(Map.of("service", "oa-data-visual-service", "status", "UP"));
    }
}
