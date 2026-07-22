package com.oa.ai.config;

import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
public class AiConfig {

    @Bean
    @ConfigurationProperties(prefix = "oa.ai.llm")
    public LlmProperties llmProperties() {
        return new LlmProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "oa.ai.agent")
    public AgentProperties agentProperties() {
        return new AgentProperties();
    }

    @Bean
    @ConfigurationProperties(prefix = "oa.ai.embedding")
    public EmbeddingProperties embeddingProperties() {
        return new EmbeddingProperties();
    }

    @Bean
    public OkHttpClient okHttpClient() {
        return new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public static class LlmProperties {
        private String baseUrl = "https://api.deepseek.com";
        private String apiKey;
        private String model = "deepseek-chat";
        private double temperature = 0.3;
        private int maxTokens = 2048;

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public double getTemperature() { return temperature; }
        public void setTemperature(double temperature) { this.temperature = temperature; }
        public int getMaxTokens() { return maxTokens; }
        public void setMaxTokens(int maxTokens) { this.maxTokens = maxTokens; }
    }

    public static class EmbeddingProperties {
        private String baseUrl = "https://api.deepseek.com";
        private String apiKey;
        private String model = "text-embedding-3-small";

        public String getBaseUrl() { return baseUrl; }
        public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
        public String getApiKey() { return apiKey; }
        public void setApiKey(String apiKey) { this.apiKey = apiKey; }
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
    }

    public static class AgentProperties {
        private int maxHistoryTurns = 10;

        public int getMaxHistoryTurns() { return maxHistoryTurns; }
        public void setMaxHistoryTurns(int maxHistoryTurns) { this.maxHistoryTurns = maxHistoryTurns; }
    }
}
