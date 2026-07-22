package com.oa.ai.service;

import com.oa.ai.vo.SourceRefVO;

import java.util.List;
import java.util.Map;

public interface PromptService {

    String buildRagSystemPrompt(List<SourceRefVO> sources, List<String> roles);

    String buildIntentPrompt();

    String buildExtractionPrompt(String currentDate);

    /** Multi-turn extraction: includes existing fields so LLM fills in the gaps */
    String buildExtractionPrompt(String currentDate, Map<String, Object> existingFields);

    String buildRagUserPrompt(String question, List<SourceRefVO> sources);
}
