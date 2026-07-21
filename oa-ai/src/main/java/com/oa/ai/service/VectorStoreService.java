package com.oa.ai.service;

import com.oa.ai.vo.SourceRefVO;

import java.util.List;

public interface VectorStoreService {

    void store(Long docId, String title, String content, String category, String tags, String accessRoles, float[] embedding);

    void delete(Long docId);

    /** KNN vector similarity search */
    List<SourceRefVO> search(float[] queryEmbedding, List<String> userRoles, int topK);

    /** Fallback: keyword text search */
    List<SourceRefVO> searchByKeyword(String query, List<String> userRoles, int topK);

    /** Fallback: MySQL LIKE search */
    List<SourceRefVO> searchFromMysql(String query, List<String> userRoles, int topK);
}
