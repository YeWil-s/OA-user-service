package com.oa.ai.dto;

import jakarta.validation.constraints.NotBlank;

public class AgentRequestDTO {

    @NotBlank(message = "消息不能为空")
    private String message;
    private String sessionId;
    private String action;

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
