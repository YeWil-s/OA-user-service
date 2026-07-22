package com.oa.ai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.ai.entity.AiConversation;
import com.oa.ai.vo.ConversationVO;

import java.util.List;
import java.util.Map;

public interface IAiConversationService extends IService<AiConversation> {

    void saveConversation(Long userId, String sessionId, String question, String answer, Integer category, Integer tokensUsed);

    /** Fetch recent Q&A history as LLM message list [{role, content}, ...] for short-term memory */
    List<Map<String, String>> getRecentHistory(String sessionId, Long userId, int maxTurns);

    List<ConversationVO> listBySession(String sessionId, Long userId);

    IPage<ConversationVO> pageByUser(Long userId, Integer pageNum, Integer pageSize);

    void deleteSession(String sessionId, Long userId, boolean isAdmin);
}
