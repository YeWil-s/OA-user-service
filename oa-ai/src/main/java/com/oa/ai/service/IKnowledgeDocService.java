package com.oa.ai.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.oa.ai.dto.KnowledgeDocDTO;
import com.oa.ai.dto.KnowledgeDocQueryDTO;
import com.oa.ai.entity.KnowledgeDoc;
import com.oa.ai.vo.KnowledgeDocVO;

public interface IKnowledgeDocService extends IService<KnowledgeDoc> {

    IPage<KnowledgeDocVO> pageQuery(KnowledgeDocQueryDTO dto);

    KnowledgeDocVO getDetail(Long id);

    void createDoc(KnowledgeDocDTO dto, Long userId);

    void updateDoc(Long id, KnowledgeDocDTO dto);

    void syncEmbedding(Long docId);

    void deleteDoc(Long id);

    void reindexAll();
}
