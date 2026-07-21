package com.oa.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.ai.config.AiConfig;
import com.oa.ai.service.EmbeddingService;
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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class EmbeddingServiceImpl implements EmbeddingService {

    private static final Logger log = LoggerFactory.getLogger(EmbeddingServiceImpl.class);
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final OkHttpClient httpClient;
    private final AiConfig.EmbeddingProperties config;

    public EmbeddingServiceImpl(OkHttpClient httpClient, AiConfig.EmbeddingProperties config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    @Override
    public float[] embed(String text) {
        try {
            String url = config.getBaseUrl() + "/v1/embeddings";
            Map<String, Object> body = Map.of(
                    "model", config.getModel(),
                    "input", text
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
                    log.error("Embedding API error: {} {}", response.code(), response.body() != null ? response.body().string() : "");
                    throw new BusinessException(ResultCode.EMBEDDING_SERVICE_ERROR);
                }

                String responseBody = response.body().string();
                JsonNode root = OBJECT_MAPPER.readTree(responseBody);
                JsonNode embeddingNode = root.path("data").get(0).path("embedding");

                float[] embedding = new float[embeddingNode.size()];
                for (int i = 0; i < embeddingNode.size(); i++) {
                    embedding[i] = (float) embeddingNode.get(i).asDouble();
                }
                return embedding;
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Embedding service error: {}", e.getMessage());
            throw new BusinessException(ResultCode.EMBEDDING_SERVICE_ERROR);
        }
    }
}
