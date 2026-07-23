package com.oa.notice.controller;

import com.oa.common.result.Result;
import com.oa.notice.dto.MessageCreateRequest;
import com.oa.notice.service.INoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "内部通知接口")
@RestController
@RequestMapping("/api/notice/internal")
public class InternalNoticeController {

    private final INoticeService noticeService;

    public InternalNoticeController(INoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @Operation(summary = "创建站内消息(内部调用，不走权限校验)")
    @PostMapping("/messages")
    public Result<Long> sendMessage(@Valid @RequestBody MessageCreateRequest request) {
        return Result.success(noticeService.sendSystemMessage(request));
    }
}
