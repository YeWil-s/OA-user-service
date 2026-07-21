package com.oa.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.ai.entity.AiConversation;
import com.oa.ai.mapper.AiConversationMapper;
import com.oa.ai.service.IAiConversationService;
import com.oa.ai.vo.ConversationVO;
import com.oa.common.exception.BusinessException;
import com.oa.common.result.ResultCode;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiConversationServiceImpl extends ServiceImpl<AiConversationMapper, AiConversation> implements IAiConversationService {

    @Override
    public void saveConversation(Long userId, String sessionId, String question, String answer, Integer category, Integer tokensUsed) {
        AiConversation conv = new AiConversation();
        conv.setUserId(userId);
        conv.setSessionId(sessionId);
        conv.setQuestion(question);
        conv.setAnswer(answer);
        conv.setCategory(category);
        conv.setTokensUsed(tokensUsed);
        baseMapper.insert(conv);
    }

    @Override
    public List<ConversationVO> listBySession(String sessionId, Long userId) {
        LambdaQueryWrapper<AiConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConversation::getSessionId, sessionId)
                .eq(AiConversation::getUserId, userId)
                .orderByAsc(AiConversation::getCreateTime);
        return baseMapper.selectList(wrapper).stream().map(this::toVO).toList();
    }

    @Override
    public IPage<ConversationVO> pageByUser(Long userId, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<AiConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConversation::getUserId, userId)
                .orderByDesc(AiConversation::getCreateTime);
        Page<AiConversation> page = new Page<>(pageNum, pageSize);
        return baseMapper.selectPage(page, wrapper).convert(this::toVO);
    }

    @Override
    public void deleteSession(String sessionId, Long userId, boolean isAdmin) {
        LambdaQueryWrapper<AiConversation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AiConversation::getSessionId, sessionId);
        if (!isAdmin) {
            wrapper.eq(AiConversation::getUserId, userId);
        }
        List<AiConversation> convs = baseMapper.selectList(wrapper);
        if (convs.isEmpty()) {
            throw new BusinessException(ResultCode.SESSION_NOT_FOUND);
        }
        for (AiConversation conv : convs) {
            baseMapper.deleteById(conv.getId());
        }
    }

    private ConversationVO toVO(AiConversation conv) {
        ConversationVO vo = new ConversationVO();
        vo.setId(conv.getId());
        vo.setSessionId(conv.getSessionId());
        vo.setQuestion(conv.getQuestion());
        vo.setAnswer(conv.getAnswer());
        vo.setCategory(conv.getCategory());
        vo.setTokensUsed(conv.getTokensUsed());
        vo.setCreateTime(conv.getCreateTime());
        return vo;
    }
}
