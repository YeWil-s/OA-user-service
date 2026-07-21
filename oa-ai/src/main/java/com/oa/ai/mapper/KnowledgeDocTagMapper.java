package com.oa.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.ai.entity.KnowledgeDocTag;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface KnowledgeDocTagMapper extends BaseMapper<KnowledgeDocTag> {

    @Select("SELECT tag_id FROM ai_knowledge_doc_tag WHERE doc_id = #{docId}")
    List<Long> selectTagIdsByDocId(@Param("docId") Long docId);

    @Delete("DELETE FROM ai_knowledge_doc_tag WHERE doc_id = #{docId}")
    int deleteByDocId(@Param("docId") Long docId);
}
