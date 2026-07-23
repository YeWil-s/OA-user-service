package com.oa.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.common.context.CurrentUser;
import com.oa.common.context.UserContextHolder;
import com.oa.common.exception.BusinessException;
import com.oa.common.page.PageResult;
import com.oa.common.result.ResultCode;
import com.oa.notice.constants.NoticeConstants;
import com.oa.notice.dto.MessageCreateRequest;
import com.oa.notice.dto.MessageQuery;
import com.oa.notice.dto.MessageResponse;
import com.oa.notice.dto.NoticeCreateRequest;
import com.oa.notice.dto.NoticeQuery;
import com.oa.notice.dto.NoticeResponse;
import com.oa.notice.dto.NoticeUpdateRequest;
import com.oa.notice.dto.UnreadCountResponse;
import com.oa.notice.entity.Notice;
import com.oa.notice.entity.NoticeMessage;
import com.oa.notice.entity.NoticeReadStatus;
import com.oa.notice.mapper.NoticeMapper;
import com.oa.notice.mapper.NoticeMessageMapper;
import com.oa.notice.mapper.NoticeReadStatusMapper;
import com.oa.notice.service.INoticeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class NoticeServiceImpl implements INoticeService {

    private final NoticeMapper noticeMapper;
    private final NoticeReadStatusMapper readStatusMapper;
    private final NoticeMessageMapper messageMapper;

    public NoticeServiceImpl(
            NoticeMapper noticeMapper,
            NoticeReadStatusMapper readStatusMapper,
            NoticeMessageMapper messageMapper
    ) {
        this.noticeMapper = noticeMapper;
        this.readStatusMapper = readStatusMapper;
        this.messageMapper = messageMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long publish(NoticeCreateRequest request) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        requireNoticeManager(currentUser);
        validateTimeRange(request.getStartTime(), request.getEndTime());

        Notice notice = new Notice();
        notice.setTitle(request.getTitle().trim());
        notice.setContent(request.getContent());
        notice.setPublisherId(currentUser.getUserId());
        notice.setNoticeType(defaultValue(request.getNoticeType(), NoticeConstants.NOTICE_TYPE_COMPANY));
        notice.setTargetType(defaultValue(request.getTargetType(), NoticeConstants.TARGET_ALL));
        notice.setTargetIds(toTargetIdsCsv(notice.getTargetType(), request.getTargetIds()));
        notice.setStartTime(request.getStartTime());
        notice.setEndTime(request.getEndTime());
        notice.setStatus(defaultValue(request.getStatus(), NoticeConstants.STATUS_PUBLISHED));
        noticeMapper.insert(notice);
        return notice.getId();
    }

    @Override
    public PageResult<NoticeResponse> list(NoticeQuery query) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        LambdaQueryWrapper<Notice> wrapper = visibleNoticeWrapper(currentUser);
        if (query.getNoticeType() != null) {
            wrapper.eq(Notice::getNoticeType, query.getNoticeType());
        }
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            wrapper.and(item -> item.like(Notice::getTitle, keyword).or().like(Notice::getContent, keyword));
        }
        if (Boolean.TRUE.equals(query.getUnreadOnly())) {
            wrapper.notExists("SELECT 1 FROM ntc_read_status rs WHERE rs.notice_id = ntc_notice.id "
                    + "AND rs.user_id = " + currentUser.getUserId() + " AND rs.is_read = 1");
        }
        wrapper.orderByDesc(Notice::getCreateTime);

        Page<Notice> page = noticeMapper.selectPage(new Page<>(query.getCurrent(), query.getSize()), wrapper);
        Set<Long> readNoticeIds = readNoticeIds(
                page.getRecords().stream().map(Notice::getId).toList(),
                currentUser.getUserId()
        );
        List<NoticeResponse> records = page.getRecords()
                .stream()
                .map(notice -> toNoticeResponse(notice, readNoticeIds.contains(notice.getId()), false))
                .toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NoticeResponse detail(Long id) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        Notice notice = findVisibleNotice(id, currentUser);
        markNoticeRead(notice.getId(), currentUser.getUserId());
        return toNoticeResponse(notice, true, true);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, NoticeUpdateRequest request) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        requireNoticeManager(currentUser);
        Notice notice = findNotice(id);

        if (request.getTitle() != null) {
            if (!StringUtils.hasText(request.getTitle())) {
                throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "公告标题不能为空");
            }
            notice.setTitle(request.getTitle().trim());
        }
        if (request.getContent() != null) {
            notice.setContent(request.getContent());
        }
        if (request.getNoticeType() != null) {
            notice.setNoticeType(request.getNoticeType());
        }
        if (request.getTargetType() != null) {
            notice.setTargetType(request.getTargetType());
            notice.setTargetIds(toTargetIdsCsv(request.getTargetType(), request.getTargetIds()));
        } else if (request.getTargetIds() != null) {
            notice.setTargetIds(toTargetIdsCsv(notice.getTargetType(), request.getTargetIds()));
        }
        if (request.getStartTime() != null) {
            notice.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            notice.setEndTime(request.getEndTime());
        }
        validateTimeRange(notice.getStartTime(), notice.getEndTime());
        if (request.getStatus() != null) {
            notice.setStatus(request.getStatus());
        }
        noticeMapper.updateById(notice);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offline(Long id) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        requireNoticeManager(currentUser);
        Notice notice = findNotice(id);
        notice.setStatus(NoticeConstants.STATUS_OFFLINE);
        noticeMapper.updateById(notice);
    }

    @Override
    public UnreadCountResponse unreadCount() {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        List<Notice> visibleNotices = noticeMapper.selectList(visibleNoticeWrapper(currentUser));
        Set<Long> readNoticeIds = readNoticeIds(
                visibleNotices.stream().map(Notice::getId).toList(),
                currentUser.getUserId()
        );
        long noticeUnread = visibleNotices.stream()
                .map(Notice::getId)
                .filter(id -> !readNoticeIds.contains(id))
                .count();
        Long messageUnread = messageMapper.selectCount(new LambdaQueryWrapper<NoticeMessage>()
                .eq(NoticeMessage::getUserId, currentUser.getUserId())
                .eq(NoticeMessage::getIsRead, NoticeConstants.READ_NO));
        return new UnreadCountResponse(noticeUnread, messageUnread == null ? 0 : messageUnread);
    }

    @Override
    public PageResult<MessageResponse> listMessages(MessageQuery query) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        LambdaQueryWrapper<NoticeMessage> wrapper = new LambdaQueryWrapper<NoticeMessage>()
                .eq(NoticeMessage::getUserId, currentUser.getUserId());
        if (query.getMsgType() != null) {
            wrapper.eq(NoticeMessage::getMsgType, query.getMsgType());
        }
        if (query.getRead() != null) {
            wrapper.eq(NoticeMessage::getIsRead, Boolean.TRUE.equals(query.getRead())
                    ? NoticeConstants.READ_YES
                    : NoticeConstants.READ_NO);
        }
        wrapper.orderByDesc(NoticeMessage::getCreateTime);

        Page<NoticeMessage> page = messageMapper.selectPage(new Page<>(query.getCurrent(), query.getSize()), wrapper);
        List<MessageResponse> records = page.getRecords().stream().map(this::toMessageResponse).toList();
        return new PageResult<>(records, page.getTotal(), page.getCurrent(), page.getSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMessage(MessageCreateRequest request) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        requireNoticeManager(currentUser);

        NoticeMessage message = new NoticeMessage();
        message.setUserId(request.getUserId());
        message.setTitle(request.getTitle().trim());
        message.setContent(request.getContent());
        message.setMsgType(request.getMsgType());
        message.setRelatedId(request.getRelatedId());
        message.setIsRead(NoticeConstants.READ_NO);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
        return message.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long sendSystemMessage(MessageCreateRequest request) {
        NoticeMessage message = new NoticeMessage();
        message.setUserId(request.getUserId());
        message.setTitle(request.getTitle().trim());
        message.setContent(request.getContent());
        message.setMsgType(request.getMsgType());
        message.setRelatedId(request.getRelatedId());
        message.setIsRead(NoticeConstants.READ_NO);
        message.setCreateTime(LocalDateTime.now());
        messageMapper.insert(message);
        return message.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markMessageRead(Long id) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        NoticeMessage message = messageMapper.selectOne(new LambdaQueryWrapper<NoticeMessage>()
                .eq(NoticeMessage::getId, id)
                .eq(NoticeMessage::getUserId, currentUser.getUserId()));
        if (message == null) {
            throw new BusinessException(40004, "消息不存在或不可访问");
        }
        if (!Objects.equals(message.getIsRead(), NoticeConstants.READ_YES)) {
            message.setIsRead(NoticeConstants.READ_YES);
            messageMapper.updateById(message);
        }
    }

    private Notice findNotice(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(ResultCode.NOTICE_NOT_FOUND);
        }
        return notice;
    }

    private Notice findVisibleNotice(Long id, CurrentUser currentUser) {
        Notice notice = findNotice(id);
        if (!isVisibleAndEffective(notice, currentUser)) {
            throw new BusinessException(ResultCode.NOT_FOUND.getCode(), "公告不存在或不可访问");
        }
        return notice;
    }

    private void markNoticeRead(Long noticeId, Long userId) {
        NoticeReadStatus readStatus = readStatusMapper.selectOne(new LambdaQueryWrapper<NoticeReadStatus>()
                .eq(NoticeReadStatus::getNoticeId, noticeId)
                .eq(NoticeReadStatus::getUserId, userId));
        LocalDateTime now = LocalDateTime.now();
        if (readStatus == null) {
            readStatus = new NoticeReadStatus();
            readStatus.setNoticeId(noticeId);
            readStatus.setUserId(userId);
            readStatus.setIsRead(NoticeConstants.READ_YES);
            readStatus.setReadTime(now);
            readStatusMapper.insert(readStatus);
            return;
        }
        if (!Objects.equals(readStatus.getIsRead(), NoticeConstants.READ_YES)) {
            readStatus.setIsRead(NoticeConstants.READ_YES);
            readStatus.setReadTime(now);
            readStatusMapper.updateById(readStatus);
        }
    }

    private LambdaQueryWrapper<Notice> visibleNoticeWrapper(CurrentUser currentUser) {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus, NoticeConstants.STATUS_PUBLISHED)
                .and(item -> item.isNull(Notice::getStartTime).or().le(Notice::getStartTime, now))
                .and(item -> item.isNull(Notice::getEndTime).or().ge(Notice::getEndTime, now));

        wrapper.and(item -> {
            item.eq(Notice::getTargetType, NoticeConstants.TARGET_ALL);
            if (currentUser.getDeptId() != null) {
                item.or(dept -> dept.eq(Notice::getTargetType, NoticeConstants.TARGET_DEPT)
                        .apply("FIND_IN_SET({0}, target_ids)", currentUser.getDeptId()));
            }
            item.or(user -> user.eq(Notice::getTargetType, NoticeConstants.TARGET_USER)
                    .apply("FIND_IN_SET({0}, target_ids)", currentUser.getUserId()));
        });
        return wrapper;
    }

    private boolean isVisibleAndEffective(Notice notice, CurrentUser currentUser) {
        if (!Objects.equals(notice.getStatus(), NoticeConstants.STATUS_PUBLISHED)) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (notice.getStartTime() != null && notice.getStartTime().isAfter(now)) {
            return false;
        }
        if (notice.getEndTime() != null && notice.getEndTime().isBefore(now)) {
            return false;
        }
        Integer targetType = notice.getTargetType();
        if (Objects.equals(targetType, NoticeConstants.TARGET_ALL)) {
            return true;
        }
        Set<Long> targetIds = parseTargetIds(notice.getTargetIds());
        if (Objects.equals(targetType, NoticeConstants.TARGET_DEPT)) {
            return currentUser.getDeptId() != null && targetIds.contains(currentUser.getDeptId());
        }
        if (Objects.equals(targetType, NoticeConstants.TARGET_USER)) {
            return targetIds.contains(currentUser.getUserId());
        }
        return false;
    }

    private Set<Long> readNoticeIds(Collection<Long> noticeIds, Long userId) {
        if (noticeIds == null || noticeIds.isEmpty()) {
            return Set.of();
        }
        return readStatusMapper.selectList(new LambdaQueryWrapper<NoticeReadStatus>()
                        .eq(NoticeReadStatus::getUserId, userId)
                        .eq(NoticeReadStatus::getIsRead, NoticeConstants.READ_YES)
                        .in(NoticeReadStatus::getNoticeId, noticeIds))
                .stream()
                .map(NoticeReadStatus::getNoticeId)
                .collect(Collectors.toSet());
    }

    private NoticeResponse toNoticeResponse(Notice notice, boolean read, boolean includeContent) {
        NoticeResponse response = new NoticeResponse();
        response.setId(notice.getId());
        response.setTitle(notice.getTitle());
        response.setContent(includeContent ? notice.getContent() : null);
        response.setSummary(toSummary(notice.getContent()));
        response.setPublisherId(notice.getPublisherId());
        response.setNoticeType(notice.getNoticeType());
        response.setTargetType(notice.getTargetType());
        response.setTargetIds(parseTargetIds(notice.getTargetIds()).stream().toList());
        response.setStartTime(notice.getStartTime());
        response.setEndTime(notice.getEndTime());
        response.setStatus(notice.getStatus());
        response.setRead(read);
        response.setCreateTime(notice.getCreateTime());
        response.setUpdateTime(notice.getUpdateTime());
        return response;
    }

    private MessageResponse toMessageResponse(NoticeMessage message) {
        MessageResponse response = new MessageResponse();
        response.setId(message.getId());
        response.setUserId(message.getUserId());
        response.setTitle(message.getTitle());
        response.setContent(message.getContent());
        response.setMsgType(message.getMsgType());
        response.setRelatedId(message.getRelatedId());
        response.setRead(Objects.equals(message.getIsRead(), NoticeConstants.READ_YES));
        response.setCreateTime(message.getCreateTime());
        return response;
    }

    private void requireNoticeManager(CurrentUser currentUser) {
        if (currentUser.hasRole("ROLE_ADMIN")
                || currentUser.hasRole("ROLE_HR")
                || currentUser.hasPermission("notice:publish")
                || currentUser.hasPermission("notice:manage")) {
            return;
        }
        throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "无公告维护权限");
    }

    private void validateTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "公告失效时间不能早于生效时间");
        }
    }

    private String toTargetIdsCsv(Integer targetType, List<Long> targetIds) {
        if (Objects.equals(targetType, NoticeConstants.TARGET_ALL)) {
            return null;
        }
        if (targetIds == null || targetIds.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "指定发布范围时目标ID不能为空");
        }
        String csv = targetIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new))
                .stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        if (!StringUtils.hasText(csv)) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "指定发布范围时目标ID不能为空");
        }
        return csv;
    }

    private Set<Long> parseTargetIds(String targetIds) {
        if (!StringUtils.hasText(targetIds)) {
            return Set.of();
        }
        return Arrays.stream(targetIds.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .map(Long::parseLong)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String toSummary(String content) {
        if (!StringUtils.hasText(content)) {
            return "";
        }
        String plainText = content.replaceAll("<[^>]*>", "").replaceAll("\\s+", " ").trim();
        if (plainText.length() <= 120) {
            return plainText;
        }
        return plainText.substring(0, 120);
    }

    private Integer defaultValue(Integer value, Integer defaultValue) {
        return value == null ? defaultValue : value;
    }
}
