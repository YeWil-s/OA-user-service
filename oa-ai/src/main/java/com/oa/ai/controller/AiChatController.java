package com.oa.ai.controller;

import com.oa.ai.dto.ChatRequestDTO;
import com.oa.ai.service.RagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Tag(name = "AI智能问答")
@RestController
@RequestMapping("/api/ai")
public class AiChatController {

    private final RagService ragService;

    public AiChatController(RagService ragService) {
        this.ragService = ragService;
    }

    @Operation(summary = "RAG智能问答（SSE流式）")
    @GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(@RequestParam String question,
                                    @RequestParam(required = false) String sessionId,
                                    HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");

        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        String sid = sessionId;
        return ragService.answerQuestion(question, roles, userId, sid);
    }

    @Operation(summary = "RAG智能问答（POST SSE流式）")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStreamPost(@Valid @RequestBody ChatRequestDTO dto,
                                        HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");

        String sessionId = dto.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        String sid = sessionId;
        return ragService.answerQuestion(dto.getQuestion(), roles, userId, sid);
    }
}
