package com.oa.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.ai.config.AiConfig;
import com.oa.ai.service.LlmService;
import com.oa.common.exception.BusinessException;
import com.oa.common.result.ResultCode;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class LlmServiceImpl implements LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmServiceImpl.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final OkHttpClient httpClient;
    private final AiConfig.LlmProperties config;

    public LlmServiceImpl(OkHttpClient httpClient, AiConfig.LlmProperties config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    @Override
    public String chat(String systemPrompt, String userMessage) {
        return chat(systemPrompt, List.of(), userMessage);
    }

    @Override
    public String chat(String systemPrompt, List<Map<String, String>> history, String userMessage) {
        try {
            String url = config.getBaseUrl() + "/v1/chat/completions";
            List<Map<String, String>> messages = buildMessages(systemPrompt, history, userMessage);

            Map<String, Object> body = Map.of(
                    "model", config.getModel(),
                    "messages", messages,
                    "temperature", config.getTemperature(),
                    "max_tokens", config.getMaxTokens(),
                    "stream", false
            );
            String json = OBJECT_MAPPER.writeValueAsString(body);

            Request request = new Request.Builder()
                    .url(url)
                    .header("Authorization", "Bearer " + config.getApiKey())
                    .header("Content-Type", "application/json")
                    .post(RequestBody.create(json, JSON))
                    .build();

            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    log.error("LLM API error: {} {}", response.code(), response.body() != null ? response.body().string() : "");
                    throw new BusinessException(ResultCode.AI_SERVICE_ERROR);
                }

                String responseBody = response.body().source().readString(StandardCharsets.UTF_8);
                JsonNode root = OBJECT_MAPPER.readTree(responseBody);
                return root.path("choices").get(0).path("message").path("content").asText();
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("LLM service error: {}", e.getMessage());
            throw new BusinessException(ResultCode.AI_SERVICE_ERROR);
        }
    }

    @Override
    public Flux<String> chatStream(String systemPrompt, String userMessage) {
        return chatStream(systemPrompt, List.of(), userMessage);
    }

    @Override
    public Flux<String> chatStream(String systemPrompt, List<Map<String, String>> history, String userMessage) {
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        new Thread(() -> {
            try {
                String url = config.getBaseUrl() + "/v1/chat/completions";
                List<Map<String, String>> messages = buildMessages(systemPrompt, history, userMessage);

                Map<String, Object> body = Map.of(
                        "model", config.getModel(),
                        "messages", messages,
                        "temperature", config.getTemperature(),
                        "max_tokens", config.getMaxTokens(),
                        "stream", true
                );
                String json = OBJECT_MAPPER.writeValueAsString(body);

                Request request = new Request.Builder()
                        .url(url)
                        .header("Authorization", "Bearer " + config.getApiKey())
                        .header("Content-Type", "application/json")
                        .post(RequestBody.create(json, JSON))
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        log.error("LLM stream API error: {}", response.code());
                        sink.tryEmitNext("{\"type\":\"error\",\"content\":\"AI服务调用失败\"}");
                        sink.tryEmitComplete();
                        return;
                    }

                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(response.body().byteStream(), StandardCharsets.UTF_8));
                    String line;
                    StringBuilder fullAnswer = new StringBuilder();

                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            if ("[DONE]".equals(data)) {
                                break;
                            }
                            try {
                                JsonNode root = OBJECT_MAPPER.readTree(data);
                                JsonNode choices = root.path("choices");
                                if (choices.size() > 0) {
                                    JsonNode delta = choices.get(0).path("delta");
                                    String content = delta.path("content").asText();
                                    if (!content.isEmpty()) {
                                        fullAnswer.append(content);
                                        sink.tryEmitNext("{\"type\":\"token\",\"content\":" + OBJECT_MAPPER.writeValueAsString(content) + "}");
                                    }
                                }
                            } catch (Exception e) {
                                log.debug("Skipping non-JSON SSE line: {}", data);
                            }
                        }
                    }

                    sink.tryEmitNext("{\"type\":\"done\",\"tokensUsed\":0}");
                    sink.tryEmitComplete();
                }
            } catch (BusinessException e) {
                sink.tryEmitNext("{\"type\":\"error\",\"content\":\"" + e.getMessage() + "\"}");
                sink.tryEmitComplete();
            } catch (Exception e) {
                log.error("LLM stream error: {}", e.getMessage());
                sink.tryEmitNext("{\"type\":\"error\",\"content\":\"AI服务暂时不可用\"}");
                sink.tryEmitComplete();
            }
        }).start();

        return sink.asFlux();
    }

    private List<Map<String, String>> buildMessages(String systemPrompt, List<Map<String, String>> history, String userMessage) {
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", systemPrompt));
        if (history != null) {
            messages.addAll(history);
        }
        messages.add(Map.of("role", "user", "content", userMessage));
        return messages;
    }
}
