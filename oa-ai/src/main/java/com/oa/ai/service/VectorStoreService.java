package com.oa.ai.service;

import com.oa.ai.entity.KnowledgeDoc;
import com.oa.ai.vo.SourceRefVO;

import java.util.List;

public interface VectorStoreService {

    void store(KnowledgeDoc doc, String tags, float[] embedding);

    void delete(Long docId);

    List<SourceRefVO> search(float[] queryEmbedding, List<String> userRoles, Long deptId, Long positionId, int topK);

    List<SourceRefVO> searchByKeyword(String query, List<String> userRoles, Long deptId, Long positionId, int topK);

    List<SourceRefVO> searchFromMysql(String query, List<String> userRoles, Long deptId, Long positionId, int topK);
}
