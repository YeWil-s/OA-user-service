package com.oa.ai.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oa.ai.service.IAiConversationService;
import com.oa.ai.vo.ConversationVO;
import com.oa.common.annotation.RequiresRole;
import com.oa.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "对话历史")
@RestController
@RequestMapping("/api/ai/conversations")
public class ConversationController {

    private final IAiConversationService conversationService;

    public ConversationController(IAiConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @Operation(summary = "对话历史（分页）")
    @GetMapping
    public Result<IPage<ConversationVO>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                               @RequestParam(defaultValue = "20") Integer pageSize,
                                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(conversationService.pageByUser(userId, pageNum, pageSize));
    }

    @Operation(summary = "获取某次会话详情")
    @GetMapping("/{sessionId}")
    public Result<List<ConversationVO>> detail(@PathVariable String sessionId,
                                                HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(conversationService.listBySession(sessionId, userId));
    }

    @Operation(summary = "删除会话")
    @DeleteMapping("/{sessionId}")
    public Result<Void> delete(@PathVariable String sessionId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) request.getAttribute("roles");
        boolean isAdmin = roles != null && roles.contains("ROLE_ADMIN");
        conversationService.deleteSession(sessionId, userId, isAdmin);
        return Result.success();
    }
}
