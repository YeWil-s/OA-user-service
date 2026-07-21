package com.oa.ai.service;

import reactor.core.publisher.Flux;

import java.util.List;

public interface RagService {

    Flux<String> answerQuestion(String question, List<String> userRoles, Long userId, String sessionId);
}
