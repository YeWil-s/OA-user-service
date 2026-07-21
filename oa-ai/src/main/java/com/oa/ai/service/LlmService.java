package com.oa.ai.service;

import reactor.core.publisher.Flux;

public interface LlmService {

    String chat(String systemPrompt, String userMessage);

    Flux<String> chatStream(String systemPrompt, String userMessage);
}
