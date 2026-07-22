package com.oa.ai.service;

import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public interface LlmService {

    String chat(String systemPrompt, String userMessage);

    Flux<String> chatStream(String systemPrompt, String userMessage);

    /** Chat with conversation history for short-term memory */
    String chat(String systemPrompt, List<Map<String, String>> history, String userMessage);

    /** Stream chat with conversation history for short-term memory */
    Flux<String> chatStream(String systemPrompt, List<Map<String, String>> history, String userMessage);
}
