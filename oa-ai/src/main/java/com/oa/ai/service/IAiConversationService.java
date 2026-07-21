package com.oa.ai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.ai.entity.AiConversation;
import com.oa.ai.vo.ConversationVO;

import java.util.List;

public interface IAiConversationService extends IService<AiConversation> {

    void saveConversation(Long userId, String sessionId, String question, String answer, Integer category, Integer tokensUsed);

    List<ConversationVO> listBySession(String sessionId, Long userId);

    IPage<ConversationVO> pageByUser(Long userId, Integer pageNum, Integer pageSize);

    void deleteSession(String sessionId, Long userId, boolean isAdmin);
}
