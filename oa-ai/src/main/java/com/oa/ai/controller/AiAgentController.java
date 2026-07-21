package com.oa.ai.controller;

import com.oa.ai.dto.AgentRequestDTO;
import com.oa.ai.service.AgentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Tag(name = "AI智能助手Agent")
@RestController
@RequestMapping("/api/ai")
public class AiAgentController {

    private final AgentService agentService;

    public AiAgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @Operation(summary = "Agent智能对话（SSE流式，含意图识别和智能填单）")
    @GetMapping(value = "/agent/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> agentStream(@RequestParam String message,
                                     @RequestParam(required = false) String sessionId,
                                     @RequestParam(required = false, defaultValue = "") String action,
                                     HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");

        Long deptId = null;
        Object deptIdAttr = request.getAttribute("deptId");
        if (deptIdAttr != null) {
            deptId = Long.valueOf(deptIdAttr.toString());
        }

        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        return agentService.processMessage(message, action, sessionId, roles, userId, deptId);
    }

    @Operation(summary = "Agent智能对话（POST SSE流式）")
    @PostMapping(value = "/agent/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> agentStreamPost(@Valid @RequestBody AgentRequestDTO dto,
                                         HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");

        Long deptId = null;
        Object deptIdAttr = request.getAttribute("deptId");
        if (deptIdAttr != null) {
            deptId = Long.valueOf(deptIdAttr.toString());
        }

        String sessionId = dto.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        String action = dto.getAction() != null ? dto.getAction() : "";
        return agentService.processMessage(dto.getMessage(), action, sessionId, roles, userId, deptId);
    }
}
