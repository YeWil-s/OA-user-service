package com.oa.notice.controller;

import com.oa.common.response.Result;
import com.oa.model.page.PageResult;
import com.oa.notice.dto.MessageCreateRequest;
import com.oa.notice.dto.MessageQuery;
import com.oa.notice.dto.MessageResponse;
import com.oa.notice.dto.NoticeCreateRequest;
import com.oa.notice.dto.NoticeQuery;
import com.oa.notice.dto.NoticeResponse;
import com.oa.notice.dto.NoticeUpdateRequest;
import com.oa.notice.dto.UnreadCountResponse;
import com.oa.notice.service.NoticeService;
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

@Validated
@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @PostMapping("/list")
    public Result<Long> publish(@Valid @RequestBody NoticeCreateRequest request) {
        return Result.success(noticeService.publish(request));
    }

    @GetMapping("/list")
    public Result<PageResult<NoticeResponse>> list(NoticeQuery query) {
        return Result.success(noticeService.list(query));
    }

    @GetMapping("/list/{id}")
    public Result<NoticeResponse> detail(@PathVariable Long id) {
        return Result.success(noticeService.detail(id));
    }

    @PutMapping("/list/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody NoticeUpdateRequest request) {
        noticeService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/list/{id}")
    public Result<Void> offline(@PathVariable Long id) {
        noticeService.offline(id);
        return Result.success();
    }

    @GetMapping("/unread-count")
    public Result<UnreadCountResponse> unreadCount() {
        return Result.success(noticeService.unreadCount());
    }

    @GetMapping("/messages")
    public Result<PageResult<MessageResponse>> messages(MessageQuery query) {
        return Result.success(noticeService.listMessages(query));
    }

    @PostMapping("/messages")
    public Result<Long> createMessage(@Valid @RequestBody MessageCreateRequest request) {
        return Result.success(noticeService.createMessage(request));
    }

    @PutMapping("/messages/{id}/read")
    public Result<Void> markMessageRead(@PathVariable Long id) {
        noticeService.markMessageRead(id);
        return Result.success();
    }
}
