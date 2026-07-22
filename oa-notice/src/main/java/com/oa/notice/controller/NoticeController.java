package com.oa.notice.controller;

import com.oa.common.page.PageResult;
import com.oa.common.result.Result;
import com.oa.notice.dto.MessageCreateRequest;
import com.oa.notice.dto.MessageQuery;
import com.oa.notice.dto.MessageResponse;
import com.oa.notice.dto.NoticeCreateRequest;
import com.oa.notice.dto.NoticeQuery;
import com.oa.notice.dto.NoticeResponse;
import com.oa.notice.dto.NoticeUpdateRequest;
import com.oa.notice.dto.UnreadCountResponse;
import com.oa.notice.service.INoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "公告通知")
@Validated
@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    private final INoticeService noticeService;

    public NoticeController(INoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @Operation(summary = "发布公告")
    @PostMapping("/list")
    public Result<Long> publish(@Valid @RequestBody NoticeCreateRequest request) {
        return Result.success(noticeService.publish(request));
    }

    @Operation(summary = "公告列表")
    @GetMapping("/list")
    public Result<PageResult<NoticeResponse>> list(NoticeQuery query) {
        return Result.success(noticeService.list(query));
    }

    @Operation(summary = "公告详情")
    @GetMapping("/list/{id}")
    public Result<NoticeResponse> detail(@PathVariable Long id) {
        return Result.success(noticeService.detail(id));
    }

    @Operation(summary = "编辑公告")
    @PutMapping("/list/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody NoticeUpdateRequest request) {
        noticeService.update(id, request);
        return Result.success();
    }

    @Operation(summary = "下架公告")
    @DeleteMapping("/list/{id}")
    public Result<Void> offline(@PathVariable Long id) {
        noticeService.offline(id);
        return Result.success();
    }

    @Operation(summary = "未读数量")
    @GetMapping("/unread-count")
    public Result<UnreadCountResponse> unreadCount() {
        return Result.success(noticeService.unreadCount());
    }

    @Operation(summary = "站内消息列表")
    @GetMapping("/messages")
    public Result<PageResult<MessageResponse>> messages(MessageQuery query) {
        return Result.success(noticeService.listMessages(query));
    }

    @Operation(summary = "创建站内消息")
    @PostMapping("/messages")
    public Result<Long> createMessage(@Valid @RequestBody MessageCreateRequest request) {
        return Result.success(noticeService.createMessage(request));
    }

    @Operation(summary = "标记消息已读")
    @PutMapping("/messages/{id}/read")
    public Result<Void> markMessageRead(@PathVariable Long id) {
        noticeService.markMessageRead(id);
        return Result.success();
    }
}
