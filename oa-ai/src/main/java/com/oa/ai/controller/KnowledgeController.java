package com.oa.ai.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oa.ai.dto.KnowledgeDocDTO;
import com.oa.ai.dto.KnowledgeDocQueryDTO;
import com.oa.ai.entity.KnowledgeTag;
import com.oa.ai.service.IKnowledgeDocService;
import com.oa.ai.service.IKnowledgeTagService;
import com.oa.ai.vo.KnowledgeDocVO;
import com.oa.common.annotation.RequiresRole;
import com.oa.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "知识库管理")
@RestController
@RequestMapping("/api/ai/knowledge")
public class KnowledgeController {

    private final IKnowledgeDocService knowledgeDocService;
    private final IKnowledgeTagService knowledgeTagService;

    public KnowledgeController(IKnowledgeDocService knowledgeDocService, IKnowledgeTagService knowledgeTagService) {
        this.knowledgeDocService = knowledgeDocService;
        this.knowledgeTagService = knowledgeTagService;
    }

    @Operation(summary = "知识文档列表（分页）")
    @GetMapping
    public Result<IPage<KnowledgeDocVO>> list(KnowledgeDocQueryDTO dto) {
        return Result.success(knowledgeDocService.pageQuery(dto));
    }

    @Operation(summary = "知识文档详情")
    @GetMapping("/{id}")
    public Result<KnowledgeDocVO> detail(@PathVariable Long id) {
        return Result.success(knowledgeDocService.getDetail(id));
    }

    @Operation(summary = "创建知识文档")
    @RequiresRole({"ROLE_ADMIN", "ROLE_HR"})
    @PostMapping
    public Result<Void> create(@Valid @RequestBody KnowledgeDocDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        knowledgeDocService.createDoc(dto, userId);
        return Result.success();
    }

    @Operation(summary = "更新知识文档")
    @RequiresRole({"ROLE_ADMIN", "ROLE_HR"})
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody KnowledgeDocDTO dto) {
        knowledgeDocService.updateDoc(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除知识文档")
    @RequiresRole({"ROLE_ADMIN"})
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        knowledgeDocService.deleteDoc(id);
        return Result.success();
    }

    @Operation(summary = "全量重建向量索引")
    @RequiresRole({"ROLE_ADMIN"})
    @PostMapping("/reindex")
    public Result<Void> reindex() {
        knowledgeDocService.reindexAll();
        return Result.success();
    }

    @Operation(summary = "标签列表")
    @GetMapping("/tags")
    public Result<List<KnowledgeTag>> tags() {
        return Result.success(knowledgeTagService.list());
    }

    @Operation(summary = "创建标签")
    @RequiresRole({"ROLE_ADMIN"})
    @PostMapping("/tags")
    public Result<Void> createTag(@RequestBody KnowledgeTag tag) {
        knowledgeTagService.save(tag);
        return Result.success();
    }

    @Operation(summary = "更新标签")
    @RequiresRole({"ROLE_ADMIN"})
    @PutMapping("/tags/{id}")
    public Result<Void> updateTag(@PathVariable Long id, @RequestBody KnowledgeTag tag) {
        tag.setId(id);
        knowledgeTagService.updateById(tag);
        return Result.success();
    }

    @Operation(summary = "删除标签")
    @RequiresRole({"ROLE_ADMIN"})
    @DeleteMapping("/tags/{id}")
    public Result<Void> deleteTag(@PathVariable Long id) {
        knowledgeTagService.removeById(id);
        return Result.success();
    }
}
