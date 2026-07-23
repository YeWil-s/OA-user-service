package com.oa.notice.service;

import com.oa.common.page.PageResult;
import com.oa.notice.dto.MessageCreateRequest;
import com.oa.notice.dto.MessageQuery;
import com.oa.notice.dto.MessageResponse;
import com.oa.notice.dto.NoticeCreateRequest;
import com.oa.notice.dto.NoticeQuery;
import com.oa.notice.dto.NoticeResponse;
import com.oa.notice.dto.NoticeUpdateRequest;
import com.oa.notice.dto.UnreadCountResponse;

public interface INoticeService {

    Long publish(NoticeCreateRequest request);

    PageResult<NoticeResponse> list(NoticeQuery query);

    NoticeResponse detail(Long id);

    void update(Long id, NoticeUpdateRequest request);

    void offline(Long id);

    UnreadCountResponse unreadCount();

    PageResult<MessageResponse> listMessages(MessageQuery query);

    Long createMessage(MessageCreateRequest request);

    void markMessageRead(Long id);

    /**
     * 系统内部消息创建，不做权限校验。
     */
    Long sendSystemMessage(MessageCreateRequest request);
}
