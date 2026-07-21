package com.oa.ai.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.ai.dto.ApprovalSubmitDTO;
import com.oa.ai.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AgentServiceImpl implements AgentService {

    private static final Logger log = LoggerFactory.getLogger(AgentServiceImpl.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final PromptService promptService;
    private final LlmService llmService;
    private final RagService ragService;
    private final IApprovalService approvalService;
    private final IAiConversationService conversationService;

    // session state: stores pending form data waiting for user confirmation
    private final ConcurrentHashMap<String, Map<String, Object>> sessionForms = new ConcurrentHashMap<>();

    public AgentServiceImpl(PromptService promptService, LlmService llmService, RagService ragService,
                            IApprovalService approvalService, IAiConversationService conversationService) {
        this.promptService = promptService;
        this.llmService = llmService;
        this.ragService = ragService;
        this.approvalService = approvalService;
        this.conversationService = conversationService;
    }

    @Override
    public Flux<String> processMessage(String message, String action, String sessionId,
                                        List<String> userRoles, Long userId, Long deptId) {
        if (sessionId == null || sessionId.isBlank()) {
            sessionId = UUID.randomUUID().toString();
        }

        final String sid = sessionId;

        if ("confirm".equals(action)) {
            Map<String, Object> formData = sessionForms.remove(sid);
            if (formData == null) {
                return ragService.answerQuestion(message, userRoles, userId, sid);
            }
            return confirmAndSubmit(formData, userId, deptId, sid);
        }

        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        new Thread(() -> {
            try {
                // Step 1: Intent classification
                sink.tryEmitNext("{\"type\":\"thinking\",\"content\":\"正在理解您的意图...\"}");

                String intentPrompt = promptService.buildIntentPrompt();
                String intentResult = llmService.chat(intentPrompt, message);

                JsonNode intentJson;
                try {
                    intentResult = intentResult.trim();
                    if (intentResult.startsWith("```")) {
                        intentResult = intentResult.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
                    }
                    intentJson = OBJECT_MAPPER.readTree(intentResult);
                } catch (Exception e) {
                    log.warn("Failed to parse intent JSON, falling back to rag: {}", intentResult);
                    ragService.answerQuestion(message, userRoles, userId, sid)
                            .subscribe(sink::tryEmitNext, sink::tryEmitError, sink::tryEmitComplete);
                    return;
                }

                String intent = intentJson.path("intent").asText("UNKNOWN");
                double confidence = intentJson.path("confidence").asDouble(0.0);

                sink.tryEmitNext("{\"type\":\"intent\",\"intent\":\"" + intent + "\",\"confidence\":" + confidence + "}");

                // Step 2: Route based on intent
                switch (intent) {
                    case "FORM_FILLING" -> handleFormFilling(message, userId, deptId, sid, sink);
                    case "KNOWLEDGE_QA" -> {
                        ragService.answerQuestion(message, userRoles, userId, sid)
                                .subscribe(sink::tryEmitNext, sink::tryEmitError, sink::tryEmitComplete);
                    }
                    default -> {
                        sink.tryEmitNext("{\"type\":\"message\",\"content\":\"抱歉，我只是一个办公助手，无法回答这个问题。您可以尝试询问公司制度、请假流程、操作规范等方面的问题，或者直接告诉我您想申请什么（如请假、加班、外出）。\"}");
                        sink.tryEmitNext("{\"type\":\"done\",\"sessionId\":\"" + sid + "\"}");
                        sink.tryEmitComplete();
                    }
                }
            } catch (Exception e) {
                log.error("Agent pipeline error: {}", e.getMessage());
                sink.tryEmitNext("{\"type\":\"error\",\"content\":\"AI服务暂时不可用\"}");
                sink.tryEmitComplete();
            }
        }).start();

        return sink.asFlux();
    }

    private void handleFormFilling(String message, Long userId, Long deptId, String sessionId,
                                    Sinks.Many<String> sink) {
        try {
            sink.tryEmitNext("{\"type\":\"thinking\",\"content\":\"正在提取申请信息...\"}");

            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String extractionPrompt = promptService.buildExtractionPrompt(currentDate);
            String extractionResult = llmService.chat(extractionPrompt, message);

            JsonNode extractionJson;
            try {
                String cleaned = extractionResult.trim();
                if (cleaned.startsWith("```")) {
                    cleaned = cleaned.replaceAll("```json\\s*", "").replaceAll("```\\s*", "");
                }
                extractionJson = OBJECT_MAPPER.readTree(cleaned);
            } catch (Exception e) {
                log.warn("Failed to parse extraction JSON: {}", extractionResult);
                sink.tryEmitNext("{\"type\":\"error\",\"content\":\"信息提取失败，请重新描述您的申请。\"}");
                sink.tryEmitComplete();
                return;
            }

            boolean needClarification = extractionJson.path("needClarification").asBoolean(false);
            if (needClarification) {
                String clarificationQ = extractionJson.path("clarificationQuestion").asText("请提供更多信息");
                sink.tryEmitNext("{\"type\":\"clarification\",\"content\":\"" + clarificationQ + "\"}");
                sink.tryEmitComplete();
                return;
            }

            // Build confirm form
            Map<String, Object> formData = new HashMap<>();
            formData.put("appType", extractionJson.path("appType").asInt(1));
            formData.put("leaveType", extractionJson.path("leaveType").isNull() ? null : extractionJson.path("leaveType").asInt());
            formData.put("startTime", extractionJson.path("startTime").asText());
            formData.put("endTime", extractionJson.path("endTime").asText());
            formData.put("duration", extractionJson.path("duration").asDouble(1.0));
            formData.put("reason", extractionJson.path("reason").asText(""));

            sessionForms.put(sessionId, formData);

            // Build confirmation message
            int appType = (int) formData.get("appType");
            String appTypeName = switch (appType) {
                case 1 -> "请假";
                case 2 -> "加班";
                case 3 -> "外出";
                default -> "未知";
            };

            String leaveTypeName = "";
            if (appType == 1 && formData.get("leaveType") != null) {
                leaveTypeName = switch ((int) formData.get("leaveType")) {
                    case 1 -> "年假";
                    case 2 -> "事假";
                    case 3 -> "病假";
                    case 4 -> "婚假";
                    case 5 -> "产假";
                    default -> "";
                };
            }

            StringBuilder confirmMsg = new StringBuilder();
            confirmMsg.append("我理解您想要：\n");
            confirmMsg.append("- 类型：").append(appTypeName);
            if (!leaveTypeName.isEmpty()) confirmMsg.append("（").append(leaveTypeName).append("）");
            confirmMsg.append("\n- 开始时间：").append(formData.get("startTime"));
            confirmMsg.append("\n- 结束时间：").append(formData.get("endTime"));
            confirmMsg.append("\n- 时长：").append(formData.get("duration")).append("天");
            if (formData.get("reason") != null && !formData.get("reason").toString().isBlank()) {
                confirmMsg.append("\n- 原因：").append(formData.get("reason"));
            }
            confirmMsg.append("\n\n确认无误请回复「确认」或「提交」，如需修改请重新描述。");

            try {
                String fieldsJson = OBJECT_MAPPER.writeValueAsString(formData);
                sink.tryEmitNext("{\"type\":\"confirmation\",\"content\":\"" + escapeJson(confirmMsg.toString()) + "\",\"fields\":" + fieldsJson + "}");
            } catch (Exception e) {
                sink.tryEmitNext("{\"type\":\"confirmation\",\"content\":\"" + escapeJson(confirmMsg.toString()) + "\"}");
            }
            sink.tryEmitComplete();
        } catch (Exception e) {
            log.error("Form filling error: {}", e.getMessage());
            sink.tryEmitNext("{\"type\":\"error\",\"content\":\"表单处理失败，请重试。\"}");
            sink.tryEmitComplete();
        }
    }

    private Flux<String> confirmAndSubmit(Map<String, Object> formData, Long userId, Long deptId, String sessionId) {
        Sinks.Many<String> sink = Sinks.many().unicast().onBackpressureBuffer();

        new Thread(() -> {
            try {
                sink.tryEmitNext("{\"type\":\"thinking\",\"content\":\"正在提交申请...\"}");

                ApprovalSubmitDTO dto = new ApprovalSubmitDTO();
                dto.setAppType((int) formData.get("appType"));
                if (formData.get("leaveType") != null) {
                    dto.setLeaveType((int) formData.get("leaveType"));
                }
                dto.setStartTime(LocalDateTime.parse(formData.get("startTime").toString().replace(" ", "T")));
                dto.setEndTime(LocalDateTime.parse(formData.get("endTime").toString().replace(" ", "T")));
                dto.setDuration(BigDecimal.valueOf((double) formData.get("duration")));
                dto.setReason(formData.get("reason") != null ? formData.get("reason").toString() : null);

                String applicationNo = approvalService.submitApplication(dto, userId, deptId);

                String result = "申请已提交成功！\n- 申请单号：" + applicationNo + "\n- 状态：待审批\n\n请等待审批人处理，您可以在「我的申请」中查看进度。";

                sink.tryEmitNext("{\"type\":\"submitted\",\"applicationNo\":\"" + applicationNo + "\",\"content\":\"" + escapeJson(result) + "\"}");
                sink.tryEmitNext("{\"type\":\"done\",\"sessionId\":\"" + sessionId + "\"}");

                conversationService.saveConversation(userId, sessionId,
                        "确认提交申请", result, 1, 0);
                sink.tryEmitComplete();
            } catch (Exception e) {
                log.error("Form submit error: {}", e.getMessage());
                sink.tryEmitNext("{\"type\":\"error\",\"content\":\"提交失败：\" + e.getMessage()}");
                sink.tryEmitComplete();
            }
        }).start();

        return sink.asFlux();
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
