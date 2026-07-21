package com.oa.ai.service;

import reactor.core.publisher.Flux;

import java.util.List;

public interface AgentService {

    Flux<String> processMessage(String message, String action, String sessionId, List<String> userRoles, Long userId, Long deptId);
}
