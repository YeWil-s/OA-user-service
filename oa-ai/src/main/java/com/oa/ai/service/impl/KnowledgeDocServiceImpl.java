package com.oa.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.oa.ai.dto.KnowledgeDocDTO;
import com.oa.ai.dto.KnowledgeDocQueryDTO;
import com.oa.ai.entity.KnowledgeDoc;
import com.oa.ai.entity.KnowledgeDocTag;
import com.oa.ai.entity.KnowledgeTag;
import com.oa.ai.enums.KnowledgeCategory;
import com.oa.ai.mapper.KnowledgeDocMapper;
import com.oa.ai.mapper.KnowledgeDocTagMapper;
import com.oa.ai.mapper.KnowledgeTagMapper;
import com.oa.ai.service.EmbeddingService;
import com.oa.ai.service.IKnowledgeDocService;
import com.oa.ai.service.VectorStoreService;
import com.oa.ai.vo.KnowledgeDocVO;
import com.oa.common.exception.BusinessException;
import com.oa.common.result.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class KnowledgeDocServiceImpl extends ServiceImpl<KnowledgeDocMapper, KnowledgeDoc> implements IKnowledgeDocService {

    private static final Logger log = LoggerFactory.getLogger(KnowledgeDocServiceImpl.class);
    private static final int VECTOR_OK = 1;
    private static final int VECTOR_FAIL = 2;

    private final KnowledgeDocTagMapper docTagMapper;
    private final KnowledgeTagMapper tagMapper;
    private final EmbeddingService embeddingService;
    private final VectorStoreService vectorStoreService;

    public KnowledgeDocServiceImpl(KnowledgeDocTagMapper docTagMapper, KnowledgeTagMapper tagMapper,
                                   EmbeddingService embeddingService, VectorStoreService vectorStoreService) {
        this.docTagMapper = docTagMapper;
        this.tagMapper = tagMapper;
        this.embeddingService = embeddingService;
        this.vectorStoreService = vectorStoreService;
    }

    @Override
    public IPage<KnowledgeDocVO> pageQuery(KnowledgeDocQueryDTO dto) {
        LambdaQueryWrapper<KnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDoc::getStatus, 1);
        if (dto.getCategory() != null) {
            wrapper.eq(KnowledgeDoc::getCategory, dto.getCategory());
        }
        if (dto.getDeptId() != null) {
            wrapper.eq(KnowledgeDoc::getDeptId, dto.getDeptId());
        }
        if (dto.getVectorStatus() != null) {
            wrapper.eq(KnowledgeDoc::getVectorStatus, dto.getVectorStatus());
        }
        if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
            wrapper.and(w -> w.like(KnowledgeDoc::getTitle, dto.getKeyword())
                    .or().like(KnowledgeDoc::getSummary, dto.getKeyword()));
        }
        wrapper.orderByDesc(KnowledgeDoc::getCreateTime);

        Page<KnowledgeDoc> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        IPage<KnowledgeDoc> docPage = baseMapper.selectPage(page, wrapper);
        return docPage.convert(this::toVO);
    }

    @Override
    public KnowledgeDocVO getDetail(Long id) {
        KnowledgeDoc doc = baseMapper.selectById(id);
        if (doc == null || doc.getStatus() == 0) {
            throw new BusinessException(ResultCode.KNOWLEDGE_NOT_FOUND);
        }
        return toVO(doc);
    }

    @Override
    @Transactional
    public void createDoc(KnowledgeDocDTO dto, Long userId) {
        KnowledgeDoc doc = buildDoc(dto, userId, 1, 0, null);
        baseMapper.insert(doc);
        saveTagsIfNeeded(doc.getId(), dto.getTagIds());
        try {
            syncEmbedding(doc.getId());
        } catch (Exception e) {
            log.error("Failed to sync embedding for new doc id={}: {}", doc.getId(), e.getMessage());
        }
    }

    @Override
    @Transactional
    public void updateDoc(Long id, KnowledgeDocDTO dto) {
        KnowledgeDoc doc = baseMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException(ResultCode.KNOWLEDGE_NOT_FOUND);
        }

        doc.setTitle(dto.getTitle());
        doc.setContent(dto.getContent());
        doc.setSummary(dto.getSummary());
        doc.setCategory(dto.getCategory());
        doc.setDeptId(dto.getDeptId());
        doc.setAccessRoles(defaultAccessRoles(dto.getAccessRoles()));
        doc.setAccessPositions(toJsonArray(dto.getAccessPositions()));
        doc.setAccessDepts(toJsonArray(dto.getAccessDepts()));
        doc.setAccessMode(dto.getAccessMode() != null ? dto.getAccessMode() : 0);
        doc.setVersion(doc.getVersion() == null ? 2 : doc.getVersion() + 1);
        doc.setVectorStatus(0);
        doc.setVectorError(null);
        baseMapper.updateById(doc);

        if (dto.getTagIds() != null) {
            docTagMapper.deleteByDocId(id);
            saveTagsIfNeeded(id, dto.getTagIds());
        }

        try {
            syncEmbedding(id);
        } catch (Exception e) {
            log.error("Failed to sync embedding for updated doc id={}: {}", id, e.getMessage());
        }
    }

    @Override
    public void syncEmbedding(Long docId) {
        KnowledgeDoc doc = baseMapper.selectById(docId);
        if (doc == null || doc.getStatus() == 0) {
            return;
        }
        try {
            syncEmbedding(doc);
            doc.setVectorStatus(VECTOR_OK);
            doc.setVectorError(null);
            baseMapper.updateById(doc);
        } catch (Exception e) {
            doc.setVectorStatus(VECTOR_FAIL);
            doc.setVectorError(truncate(e.getMessage(), 500));
            baseMapper.updateById(doc);
            throw e;
        }
    }

    private void syncEmbedding(KnowledgeDoc doc) {
        String text = safe(doc.getTitle()) + "\n\n" + safe(doc.getContent());
        float[] embedding = embeddingService.embed(text);

        List<Long> tagIds = docTagMapper.selectTagIdsByDocId(doc.getId());
        String tags = "";
        if (tagIds != null && !tagIds.isEmpty()) {
            List<KnowledgeTag> tagList = tagMapper.selectBatchIds(tagIds);
            tags = tagList.stream().map(KnowledgeTag::getTagCode).collect(Collectors.joining(","));
        }

        if (doc.getVersion() == null) {
            doc.setVersion(1);
        }
        if (doc.getAccessMode() == null) {
            doc.setAccessMode(0);
        }
        doc.setVectorStatus(VECTOR_OK);
        vectorStoreService.store(doc, tags, embedding);
    }

    @Override
    public void deleteDoc(Long id) {
        KnowledgeDoc doc = baseMapper.selectById(id);
        if (doc == null) {
            throw new BusinessException(ResultCode.KNOWLEDGE_NOT_FOUND);
        }
        doc.setStatus(0);
        doc.setIsDeleted(1);
        baseMapper.updateById(doc);
        try {
            vectorStoreService.delete(id);
        } catch (Exception e) {
            log.error("Failed to delete vector for doc id={}: {}", id, e.getMessage());
        }
    }

    @Override
    public void reindexAll() {
        LambdaQueryWrapper<KnowledgeDoc> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeDoc::getStatus, 1);
        List<KnowledgeDoc> docs = baseMapper.selectList(wrapper);
        log.info("Starting full reindex of {} documents", docs.size());
        for (KnowledgeDoc doc : docs) {
            try {
                vectorStoreService.delete(doc.getId());
                doc.setVectorStatus(0);
                doc.setVectorError(null);
                baseMapper.updateById(doc);
                syncEmbedding(doc.getId());
            } catch (Exception e) {
                log.error("Failed to reindex doc id={}: {}", doc.getId(), e.getMessage());
            }
        }
        log.info("Full reindex completed");
    }

    private KnowledgeDoc buildDoc(KnowledgeDocDTO dto, Long userId, int version, int vectorStatus, String vectorError) {
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setTitle(dto.getTitle());
        doc.setContent(dto.getContent());
        doc.setSummary(dto.getSummary());
        doc.setCategory(dto.getCategory());
        doc.setDeptId(dto.getDeptId());
        doc.setAccessRoles(toJsonArray(dto.getAccessRoles()));
        doc.setAccessPositions(toJsonArray(dto.getAccessPositions()));
        doc.setAccessDepts(toJsonArray(dto.getAccessDepts()));
        doc.setAccessMode(dto.getAccessMode() != null ? dto.getAccessMode() : 0);
        doc.setVersion(version);
        doc.setVectorStatus(vectorStatus);
        doc.setVectorError(vectorError);
        doc.setStatus(1);
        doc.setCreateBy(userId);
        return doc;
    }

    private void saveTagsIfNeeded(Long docId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }
        for (Long tagId : tagIds) {
            KnowledgeDocTag dt = new KnowledgeDocTag();
            dt.setDocId(docId);
            dt.setTagId(tagId);
            docTagMapper.insert(dt);
        }
    }

    private KnowledgeDocVO toVO(KnowledgeDoc doc) {
        KnowledgeDocVO vo = new KnowledgeDocVO();
        vo.setId(doc.getId());
        vo.setTitle(doc.getTitle());
        vo.setSummary(doc.getSummary());
        vo.setCategory(doc.getCategory());
        vo.setCategoryDesc(getCategoryDesc(doc.getCategory()));
        vo.setDeptId(doc.getDeptId());
        vo.setAccessRoles(parseStringList(doc.getAccessRoles()));
        vo.setAccessPositions(parseLongList(doc.getAccessPositions()));
        vo.setAccessDepts(parseLongList(doc.getAccessDepts()));
        vo.setAccessMode(doc.getAccessMode());
        vo.setVersion(doc.getVersion());
        vo.setVectorStatus(doc.getVectorStatus());
        vo.setVectorError(doc.getVectorError());
        vo.setStatus(doc.getStatus());
        vo.setCreateTime(doc.getCreateTime());
        vo.setUpdateTime(doc.getUpdateTime());

        List<Long> tagIds = docTagMapper.selectTagIdsByDocId(doc.getId());
        if (tagIds != null && !tagIds.isEmpty()) {
            List<KnowledgeTag> tags = tagMapper.selectBatchIds(tagIds);
            vo.setTagNames(tags.stream().map(KnowledgeTag::getTagName).collect(Collectors.toList()));
        } else {
            vo.setTagNames(new ArrayList<>());
        }
        return vo;
    }

    private String getCategoryDesc(Integer category) {
        for (KnowledgeCategory kc : KnowledgeCategory.values()) {
            if (kc.getCode() == category) return kc.getDesc();
        }
        return "未知";
    }

    private String toJsonArray(List<?> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        return "[" + list.stream().map(String::valueOf).map(s -> "\"" + s + "\"").collect(Collectors.joining(",")) + "]";
    }

    private String defaultAccessRoles(List<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return "[\"ROLE_EMPLOYEE\",\"ROLE_LEADER\",\"ROLE_HR\",\"ROLE_ADMIN\"]";
        }
        return toJsonArray(roles);
    }

    private List<String> parseStringList(String json) {
        if (json == null || json.isBlank()) {
            return new ArrayList<>();
        }
        String cleaned = json.replace("[", "").replace("]", "").replace("\"", "").trim();
        if (cleaned.isBlank()) {
            return new ArrayList<>();
        }
        String[] parts = cleaned.split(",");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            if (!part.isBlank()) {
                result.add(part.trim());
            }
        }
        return result;
    }

    private List<Long> parseLongList(String json) {
        List<String> strings = parseStringList(json);
        List<Long> result = new ArrayList<>();
        for (String s : strings) {
            try {
                result.add(Long.valueOf(s));
            } catch (Exception ignored) {
            }
        }
        return result;
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String truncate(String value, int maxLen) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLen ? value : value.substring(0, maxLen);
    }
}
