package com.oa.ai.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.ai.entity.KnowledgeTag;
import com.oa.ai.mapper.KnowledgeTagMapper;
import com.oa.ai.service.IKnowledgeTagService;
import org.springframework.stereotype.Service;

@Service
public class KnowledgeTagServiceImpl extends ServiceImpl<KnowledgeTagMapper, KnowledgeTag> implements IKnowledgeTagService {
}
