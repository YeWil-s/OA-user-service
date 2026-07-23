package com.oa.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.ai.config.AiConfig;
import com.oa.ai.service.EmbeddingService;
import com.oa.ai.service.IAiConversationService;
import com.oa.ai.service.LlmService;
import com.oa.ai.service.PromptService;
import com.oa.ai.service.RagService;
import com.oa.ai.service.VectorStoreService;
import com.oa.ai.vo.SourceRefVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.Map;

@Service
public class RagServiceImpl implements RagService {

    private static final Logger log = LoggerFactory.getLogger(RagServiceImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;
    private final PromptService promptService;
    private final LlmService llmService;
    private final IAiConversationService conversationService;
    private final AiConfig.AgentProperties agentProperties;

    public RagServiceImpl(EmbeddingService embeddingService, VectorStoreService vectorStoreService,
                          PromptService promptService, LlmService llmService,
                          IAiConversationService conversationService,
                          AiConfig.AgentProperties agentProperties) {
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
        this.promptService = promptService;
        this.llmService = llmService;
        this.conversationService = conversationService;
        this.agentProperties = agentProperties;
    }

    @Override
    public Flux<String> answerQuestion(String question, List<String> userRoles, Long userId, Long deptId, Long positionId, String sessionId) {
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        new Thread(() -> {
            try {
                sink.tryEmitNext("{\"type\":\"thinking\",\"content\":\"正在检索相关知识...\"}");

                // Step 1: Embed question → float[] vector
                float[] queryEmbedding = embeddingService.embed(question);

                // Step 2: KNN vector similarity search in Redis Stack with role filter
                List<SourceRefVO> sources = vectorStoreService.search(queryEmbedding, userRoles, deptId, positionId, 5);

                // Send sources to client
                try {
                    String sourcesJson = OBJECT_MAPPER.writeValueAsString(sources);
                    sink.tryEmitNext("{\"type\":\"sources\",\"data\":" + sourcesJson + "}");
                } catch (Exception e) {
                    log.warn("Failed to serialize sources: {}", e.getMessage());
                }

                sink.tryEmitNext("{\"type\":\"thinking\",\"content\":\"正在生成回答...\"}");

                // Step 3: Build prompt
                String systemPrompt = promptService.buildRagSystemPrompt(sources, userRoles);
                String userPrompt = promptService.buildRagUserPrompt(question, sources);

                // Step 4: Fetch conversation history for short-term memory
                List<Map<String, String>> history = conversationService.getRecentHistory(
                        sessionId, userId, agentProperties.getMaxHistoryTurns());

                // Step 5: LLM stream with history
                StringBuilder fullAnswer = new StringBuilder();
                llmService.chatStream(systemPrompt, history, userPrompt)
                        .doOnNext(chunk -> {
                            // Extract text from SSE token JSON for readable storage
                            String text = extractTokenContent(chunk);
                            if (text != null) fullAnswer.append(text);
                            sink.tryEmitNext(chunk);
                        })
                        .doOnComplete(() -> {
                            try {
                                conversationService.saveConversation(userId, sessionId, question,
                                        fullAnswer.toString(), 3, 0);
                                sink.tryEmitNext("{\"type\":\"done\",\"sessionId\":\"" + sessionId + "\"}");
                                sink.tryEmitComplete();
                            } catch (Exception e) {
                                log.error("Failed to save conversation: {}", e.getMessage());
                                sink.tryEmitNext("{\"type\":\"done\",\"sessionId\":\"" + sessionId + "\"}");
                                sink.tryEmitComplete();
                            }
                        })
                        .doOnError(e -> {
                            log.error("RAG stream error: {}", e.getMessage());
                            sink.tryEmitNext("{\"type\":\"error\",\"content\":\"AI服务暂时不可用\"}");
                            sink.tryEmitComplete();
                        })
                        .subscribe();
            } catch (Exception e) {
                log.error("RAG pipeline error: {}", e.getMessage());
                sink.tryEmitNext("{\"type\":\"error\",\"content\":\"AI服务暂时不可用\"}");
                sink.tryEmitComplete();
            }
        }).start();

        return sink.asFlux();
    }

    /** Extract "content" value from SSE token JSON like {"type":"token","content":"请假"} */
    private String extractTokenContent(String chunk) {
        if (chunk == null || chunk.isBlank()) return null;
        try {
            JsonNode node = OBJECT_MAPPER.readTree(chunk);
            if ("token".equals(node.path("type").asText())) {
                String content = node.path("content").asText();
                return content.isEmpty() && !node.path("content").isMissingNode() ? content : content;
            }
        } catch (Exception ignored) {
            // fallback to simple extraction for malformed JSON
        }
        return null;
    }
}
