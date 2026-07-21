package com.oa.ai.service;

import com.oa.ai.vo.SourceRefVO;

import java.util.List;

public interface PromptService {

    String buildRagSystemPrompt(List<SourceRefVO> sources, List<String> roles);

    String buildIntentPrompt();

    String buildExtractionPrompt(String currentDate);

    String buildRagUserPrompt(String question, List<SourceRefVO> sources);
}
