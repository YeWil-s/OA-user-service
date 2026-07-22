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
        if (dto.getKeyword() != null && !dto.getKeyword().isBlank()) {
            wrapper.and(w -> w.like(KnowledgeDoc::getTitle, dto.getKeyword())
                    .or().like(KnowledgeDoc::getSummary, dto.getKeyword()));
        }
        wrapper.orderByDesc(KnowledgeDoc::getCreateTime);

        Page<KnowledgeDoc> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        IPage<KnowledgeDoc> docPage = baseMapper.selectPage(page, wrapper);

        return docPage.convert(doc -> toVO(doc));
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
        KnowledgeDoc doc = new KnowledgeDoc();
        doc.setTitle(dto.getTitle());
        doc.setContent(dto.getContent());
        doc.setSummary(dto.getSummary());
        doc.setCategory(dto.getCategory());
        doc.setStatus(1);
        doc.setCreateBy(userId);

        if (dto.getAccessRoles() != null && !dto.getAccessRoles().isEmpty()) {
            doc.setAccessRoles(toJsonArray(dto.getAccessRoles()));
        } else {
            doc.setAccessRoles("[\"ROLE_EMPLOYEE\",\"ROLE_LEADER\",\"ROLE_HR\",\"ROLE_ADMIN\"]");
        }

        baseMapper.insert(doc);

        if (dto.getTagIds() != null && !dto.getTagIds().isEmpty()) {
            saveDocTags(doc.getId(), dto.getTagIds());
        }

        try {
            syncEmbedding(doc);
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

        if (dto.getAccessRoles() != null && !dto.getAccessRoles().isEmpty()) {
            doc.setAccessRoles(toJsonArray(dto.getAccessRoles()));
        }

        baseMapper.updateById(doc);

        if (dto.getTagIds() != null) {
            docTagMapper.deleteByDocId(id);
            if (!dto.getTagIds().isEmpty()) {
                saveDocTags(id, dto.getTagIds());
            }
        }

        try {
            vectorStoreService.delete(id);
            syncEmbedding(doc);
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
        syncEmbedding(doc);
    }

    private void syncEmbedding(KnowledgeDoc doc) {
        String text = doc.getTitle() + "\n\n" + doc.getContent();
        float[] embedding = embeddingService.embed(text);

        List<Long> tagIds = docTagMapper.selectTagIdsByDocId(doc.getId());
        String tags = "";
        if (tagIds != null && !tagIds.isEmpty()) {
            List<KnowledgeTag> tagList = tagMapper.selectBatchIds(tagIds);
            tags = tagList.stream().map(KnowledgeTag::getTagCode).collect(Collectors.joining(","));
        }

        String accessRoles = doc.getAccessRoles();
        if (accessRoles != null && accessRoles.startsWith("[")) {
            accessRoles = accessRoles.replace("[", "").replace("]", "").replace("\"", "").replace(" ", "");
        }

        vectorStoreService.store(doc.getId(), doc.getTitle(), doc.getContent(),
                String.valueOf(doc.getCategory()), tags, accessRoles, embedding);
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
                syncEmbedding(doc);
            } catch (Exception e) {
                log.error("Failed to reindex doc id={}: {}", doc.getId(), e.getMessage());
            }
        }
        log.info("Full reindex completed");
    }

    private void saveDocTags(Long docId, List<Long> tagIds) {
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

        String roles = doc.getAccessRoles();
        if (roles != null && roles.startsWith("[")) {
            roles = roles.replace("[", "").replace("]", "").replace("\"", "");
            vo.setAccessRoles(List.of(roles.split(",")));
        }

        return vo;
    }

    private String getCategoryDesc(Integer category) {
        for (KnowledgeCategory kc : KnowledgeCategory.values()) {
            if (kc.getCode() == category) return kc.getDesc();
        }
        return "未知";
    }

    private String toJsonArray(List<String> list) {
        return "[" + list.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(",")) + "]";
    }
}
