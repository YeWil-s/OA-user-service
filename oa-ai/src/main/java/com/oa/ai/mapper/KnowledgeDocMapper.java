package com.oa.ai.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.oa.ai.entity.KnowledgeDoc;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface KnowledgeDocMapper extends BaseMapper<KnowledgeDoc> {

    @Select("<script>" +
            "SELECT * FROM ai_knowledge_doc WHERE status = 1 AND is_deleted = 0 AND (" +
            "<foreach item='kw' collection='keywords' separator=' OR '>" +
            "title LIKE CONCAT('%', #{kw}, '%') OR content LIKE CONCAT('%', #{kw}, '%')" +
            "</foreach>" +
            ") ORDER BY create_time DESC LIMIT #{limit}" +
            "</script>")
    List<KnowledgeDoc> searchByKeywords(@Param("keywords") List<String> keywords, @Param("limit") int limit);
}
