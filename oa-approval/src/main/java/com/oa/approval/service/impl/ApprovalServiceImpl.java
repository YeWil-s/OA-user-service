package com.oa.approval.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.oa.approval.client.UserServiceClient;
import com.oa.approval.dto.ApplicationQueryDTO;
import com.oa.approval.dto.ApplicationSubmitDTO;
import com.oa.approval.dto.ApprovalActionDTO;
import com.oa.approval.dto.VisualApprovalRealtimeDTO;
import com.oa.approval.dto.VisualApprovalStatsDTO;
import com.oa.approval.dto.VisualTypeCountDTO;
import com.oa.approval.entity.AppApplication;
import com.oa.approval.entity.AppApprovalRecord;
import com.oa.approval.mapper.AppApplicationMapper;
import com.oa.approval.mapper.AppApprovalRecordMapper;
import com.oa.approval.service.IApprovalService;
import com.oa.approval.vo.ApplicationDetailVO;
import com.oa.approval.vo.ApplicationVO;
import com.oa.approval.vo.ApprovalTimelineVO;
import com.oa.common.context.CurrentUser;
import com.oa.common.context.UserContextHolder;
import com.oa.common.exception.BusinessException;
import com.oa.common.result.ResultCode;
import com.oa.common.utils.RedisUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ApprovalServiceImpl implements IApprovalService {

    private static final DateTimeFormatter NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final AppApplicationMapper appApplicationMapper;
    private final AppApprovalRecordMapper appApprovalRecordMapper;
    private final UserServiceClient userServiceClient;
    private final com.oa.approval.client.NoticeServiceClient noticeServiceClient;
    private final com.oa.approval.client.AttendanceServiceClient attendanceServiceClient;
    private final RedisUtils redisUtils;

    private static final int MSG_TYPE_APPROVAL = 1;

    public ApprovalServiceImpl(AppApplicationMapper appApplicationMapper,
                               AppApprovalRecordMapper appApprovalRecordMapper,
                               UserServiceClient userServiceClient,
                               com.oa.approval.client.NoticeServiceClient noticeServiceClient,
                               com.oa.approval.client.AttendanceServiceClient attendanceServiceClient,
                               RedisUtils redisUtils) {
        this.appApplicationMapper = appApplicationMapper;
        this.appApprovalRecordMapper = appApprovalRecordMapper;
        this.userServiceClient = userServiceClient;
        this.noticeServiceClient = noticeServiceClient;
        this.attendanceServiceClient = attendanceServiceClient;
        this.redisUtils = redisUtils;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApplicationDetailVO
    submit(ApplicationSubmitDTO dto) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        com.oa.common.remote.UserInfo applicant = userServiceClient.requireActiveUser(currentUser.getUserId());
        Long applicantDeptId = applicant.getDeptId() == null ? currentUser.getDeptId() : applicant.getDeptId();
        validateSubmitDTO(dto);

        AppApplication application = new AppApplication();
        application.setApplicationNo(generateApplicationNo(dto.getAppType()));
        application.setUserId(currentUser.getUserId());
        application.setDeptId(applicantDeptId);
        application.setAppType(dto.getAppType());
        application.setLeaveType(Objects.equals(dto.getAppType(), 1) ? dto.getLeaveType() : null);
        application.setStartTime(dto.getStartTime());
        application.setEndTime(dto.getEndTime());
        application.setDuration(calcDuration(dto.getStartTime(), dto.getEndTime()));
        application.setReason(dto.getReason());
        application.setAttachments(joinAttachments(dto.getAttachments()));
        application.setStatus(1);
        application.setCurrentApproverId(userServiceClient.resolveApproverId(applicantDeptId, currentUser.getUserId()));
        appApplicationMapper.insert(application);

        notifyApprover(application, applicant);

        return getDetail(application.getId());
    }

    @Override
    public IPage<ApplicationVO> myApplications(ApplicationQueryDTO dto) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        ApplicationQueryDTO safeDto = safeQuery(dto);
        LambdaQueryWrapper<AppApplication> wrapper = new LambdaQueryWrapper<AppApplication>()
                .eq(AppApplication::getUserId, currentUser.getUserId())
                .orderByDesc(AppApplication::getCreateTime);
        if (safeDto.getStatus() != null) {
            wrapper.eq(AppApplication::getStatus, safeDto.getStatus());
        }
        Page<AppApplication> page = appApplicationMapper.selectPage(new Page<>(safeDto.getPageNum(), safeDto.getPageSize()), wrapper);
        return buildListPage(page);
    }

    @Override
    public ApplicationDetailVO getDetail(Long id) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        AppApplication application = getApplication(id);
        ensureCanView(currentUser, application);
        ApplicationDetailVO detail = toDetailVO(application, null);
        List<AppApprovalRecord> timeline = appApprovalRecordMapper.selectList(new LambdaQueryWrapper<AppApprovalRecord>()
                .eq(AppApprovalRecord::getApplicationId, application.getId())
                .orderByAsc(AppApprovalRecord::getActionTime));
        fillTimeline(detail, timeline);
        return detail;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        AppApplication application = getApplication(id);
        if (!Objects.equals(application.getUserId(), currentUser.getUserId())
                && !currentUser.hasRole("ROLE_ADMIN")
                && !currentUser.hasRole("ROLE_HR")) {
            throw new BusinessException(ResultCode.FORBIDDEN, "只能撤销自己的申请");
        }
        if (!Objects.equals(application.getStatus(), 1)) {
            throw new BusinessException(ResultCode.CANNOT_CANCEL);
        }
        application.setStatus(4);
        application.setCurrentApproverId(null);
        appApplicationMapper.updateById(application);
    }

    @Override
    public IPage<ApplicationVO> pendingApplications(ApplicationQueryDTO dto) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        ApplicationQueryDTO safeDto = safeQuery(dto);
        Page<AppApplication> page = appApplicationMapper.selectPage(new Page<>(safeDto.getPageNum(), safeDto.getPageSize()),
                new LambdaQueryWrapper<AppApplication>()
                        .eq(AppApplication::getStatus, 1)
                        .eq(AppApplication::getCurrentApproverId, currentUser.getUserId())
                        .orderByDesc(AppApplication::getCreateTime));
        return buildListPage(page);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approve(Long id, ApprovalActionDTO dto) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        AppApplication application = getApplication(id);
        // admin/HR 可以审批任意待审批申请
        if (!Objects.equals(application.getCurrentApproverId(), currentUser.getUserId())
                && !currentUser.hasRole("ROLE_ADMIN")
                && !currentUser.hasRole("ROLE_HR")) {
            throw new BusinessException(ResultCode.FORBIDDEN, "你不是当前审批人");
        }
        if (!Objects.equals(application.getStatus(), 1)) {
            throw new BusinessException(ResultCode.ALREADY_APPROVED);
        }

        application.setStatus(Boolean.TRUE.equals(dto.getApproved()) ? 2 : 3);
        application.setCurrentApproverId(null);
        appApplicationMapper.updateById(application);

        AppApprovalRecord record = new AppApprovalRecord();
        record.setApplicationId(application.getId());
        record.setApproverId(currentUser.getUserId());
        record.setAction(Boolean.TRUE.equals(dto.getApproved()) ? 1 : 2);
        record.setComment(dto.getComment());
        record.setActionTime(LocalDateTime.now());
        appApprovalRecordMapper.insert(record);

        notifyApplicant(application);

        if (Objects.equals(application.getAppType(), 1) && Integer.valueOf(2).equals(application.getStatus())) {
            attendanceServiceClient.markLeave(application.getUserId(),
                    application.getStartTime().toLocalDate(),
                    application.getEndTime().toLocalDate(),
                    application.getId());
        }
    }

    @Override
    public IPage<ApplicationVO> processedApplications(ApplicationQueryDTO dto) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        ApplicationQueryDTO safeDto = safeQuery(dto);
        Page<AppApprovalRecord> recordPage = appApprovalRecordMapper.selectPage(new Page<>(safeDto.getPageNum(), safeDto.getPageSize()),
                new LambdaQueryWrapper<AppApprovalRecord>()
                        .eq(AppApprovalRecord::getApproverId, currentUser.getUserId())
                        .orderByDesc(AppApprovalRecord::getActionTime));
        Page<ApplicationVO> result = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());
        if (recordPage.getRecords() == null || recordPage.getRecords().isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }
        List<Long> applicationIds = recordPage.getRecords().stream()
                .map(AppApprovalRecord::getApplicationId)
                .distinct()
                .toList();
        Map<Long, AppApplication> applicationMap = appApplicationMapper.selectList(new LambdaQueryWrapper<AppApplication>()
                        .in(AppApplication::getId, applicationIds))
                .stream()
                .collect(Collectors.toMap(AppApplication::getId, application -> application, (a, b) -> a));
        Map<Long, AppApprovalRecord> recordMap = recordPage.getRecords().stream()
                .collect(Collectors.toMap(AppApprovalRecord::getApplicationId, record -> record, (a, b) -> a));
        result.setRecords(applicationIds.stream()
                .map(applicationMap::get)
                .filter(Objects::nonNull)
                .map(application -> toApplicationVO(application, recordMap.get(application.getId())))
                .toList());
        return result;
    }

    @Override
    public IPage<ApplicationVO> allApplications(ApplicationQueryDTO dto) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        requireAdminOrHr(currentUser);
        ApplicationQueryDTO safeDto = safeQuery(dto);
        LambdaQueryWrapper<AppApplication> wrapper = new LambdaQueryWrapper<AppApplication>()
                .orderByDesc(AppApplication::getCreateTime);
        if (safeDto.getStatus() != null) {
            wrapper.eq(AppApplication::getStatus, safeDto.getStatus());
        }
        Page<AppApplication> page = appApplicationMapper.selectPage(new Page<>(safeDto.getPageNum(), safeDto.getPageSize()), wrapper);
        return buildListPage(page);
    }

    @Override
    public IPage<ApplicationVO> allPending(ApplicationQueryDTO dto) {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        requireAdminOrHr(currentUser);
        ApplicationQueryDTO safeDto = safeQuery(dto);
        Page<AppApplication> page = appApplicationMapper.selectPage(new Page<>(safeDto.getPageNum(), safeDto.getPageSize()),
                new LambdaQueryWrapper<AppApplication>()
                        .eq(AppApplication::getStatus, 1)
                        .orderByDesc(AppApplication::getCreateTime));
        return buildListPage(page);
    }

    @Override
    public List<VisualApprovalStatsDTO> monthlyVisualStats(String month) {
        List<AppApplication> applications = listVisualApplications(month);
        if (applications.isEmpty()) {
            return List.of();
        }
        Map<Long, LocalDateTime> finishTimes = finishTimes(applications);
        Map<Long, VisualApprovalAccumulator> stats = new java.util.TreeMap<>();
        for (AppApplication application : applications) {
            if (application.getDeptId() == null) {
                continue;
            }
            VisualApprovalAccumulator item = stats.computeIfAbsent(application.getDeptId(), ignored -> new VisualApprovalAccumulator());
            item.totalApplications++;
            if (Integer.valueOf(2).equals(application.getStatus())) {
                item.approvedCount++;
            } else if (Integer.valueOf(3).equals(application.getStatus())) {
                item.rejectedCount++;
            } else if (Integer.valueOf(1).equals(application.getStatus())) {
                item.pendingCount++;
            }
            LocalDateTime finishTime = finishTimes.get(application.getId());
            if ((Integer.valueOf(2).equals(application.getStatus()) || Integer.valueOf(3).equals(application.getStatus()))
                    && application.getCreateTime() != null && finishTime != null) {
                item.completedCount++;
                item.totalApprovalMinutes += Math.max(0, Duration.between(application.getCreateTime(), finishTime).toMinutes());
            }
        }
        return stats.entrySet().stream()
                .map(entry -> new VisualApprovalStatsDTO(entry.getKey(), entry.getValue().totalApplications,
                        entry.getValue().approvedCount, entry.getValue().rejectedCount, entry.getValue().pendingCount,
                        entry.getValue().averageHours()))
                .toList();
    }

    @Override
    public VisualApprovalRealtimeDTO visualRealtimeStats(String month) {
        long pendingCount = appApplicationMapper.selectCount(new LambdaQueryWrapper<AppApplication>()
                .eq(AppApplication::getStatus, 1));
        Map<Integer, Long> typeCounts = listVisualApplications(month).stream()
                .filter(application -> application.getAppType() != null)
                .collect(Collectors.groupingBy(AppApplication::getAppType, Collectors.counting()));
        List<VisualTypeCountDTO> types = typeCounts.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new VisualTypeCountDTO(entry.getKey(), entry.getValue()))
                .toList();
        return new VisualApprovalRealtimeDTO(pendingCount, types);
    }

    private List<AppApplication> listVisualApplications(String month) {
        YearMonth statMonth = parseVisualMonth(month);
        LocalDateTime startTime = statMonth.atDay(1).atStartOfDay();
        LocalDateTime endTime = statMonth.plusMonths(1).atDay(1).atStartOfDay();
        return appApplicationMapper.selectList(new LambdaQueryWrapper<AppApplication>()
                .in(AppApplication::getStatus, Set.of(1, 2, 3))
                .ge(AppApplication::getCreateTime, startTime)
                .lt(AppApplication::getCreateTime, endTime));
    }

    private Map<Long, LocalDateTime> finishTimes(List<AppApplication> applications) {
        List<Long> ids = applications.stream().map(AppApplication::getId).toList();
        return appApprovalRecordMapper.selectList(new LambdaQueryWrapper<AppApprovalRecord>()
                        .in(AppApprovalRecord::getApplicationId, ids))
                .stream()
                .filter(record -> record.getActionTime() != null)
                .collect(Collectors.toMap(AppApprovalRecord::getApplicationId, AppApprovalRecord::getActionTime,
                        (left, right) -> left.isAfter(right) ? left : right));
    }

    private YearMonth parseVisualMonth(String month) {
        if (!StringUtils.hasText(month)) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month.trim());
        } catch (DateTimeParseException ex) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "统计月份格式应为 yyyy-MM");
        }
    }

    private static class VisualApprovalAccumulator {
        private int totalApplications;
        private int approvedCount;
        private int rejectedCount;
        private int pendingCount;
        private long totalApprovalMinutes;
        private int completedCount;

        private BigDecimal averageHours() {
            if (completedCount == 0) {
                return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
            }
            return BigDecimal.valueOf(totalApprovalMinutes)
                    .divide(BigDecimal.valueOf(completedCount * 60L), 1, RoundingMode.HALF_UP);
        }
    }

    private void requireAdminOrHr(CurrentUser currentUser) {
        if (!currentUser.hasRole("ROLE_ADMIN") && !currentUser.hasRole("ROLE_HR")) {
            throw new BusinessException(ResultCode.FORBIDDEN, "无权限访问");
        }
    }

    private IPage<ApplicationVO> buildListPage(Page<AppApplication> page) {
        Page<ApplicationVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        if (page.getRecords() == null || page.getRecords().isEmpty()) {
            result.setRecords(Collections.emptyList());
            return result;
        }
        Map<Long, AppApprovalRecord> latestRecordMap = latestRecordMap(page.getRecords().stream()
                .map(AppApplication::getId)
                .toList());
        result.setRecords(page.getRecords().stream()
                .map(application -> toApplicationVO(application, latestRecordMap.get(application.getId())))
                .toList());
        return result;
    }

    private Map<Long, AppApprovalRecord> latestRecordMap(List<Long> applicationIds) {
        if (applicationIds == null || applicationIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<AppApprovalRecord> records = appApprovalRecordMapper.selectList(new LambdaQueryWrapper<AppApprovalRecord>()
                .in(AppApprovalRecord::getApplicationId, applicationIds)
                .orderByDesc(AppApprovalRecord::getActionTime));
        return records.stream().collect(Collectors.toMap(AppApprovalRecord::getApplicationId, record -> record, (first, second) -> first));
    }

    private ApplicationVO toApplicationVO(AppApplication application, AppApprovalRecord latestRecord) {
        ApplicationVO vo = new ApplicationVO();
        copyBase(application, latestRecord, vo);
        return vo;
    }

    private ApplicationDetailVO toDetailVO(AppApplication application, AppApprovalRecord latestRecord) {
        ApplicationDetailVO detail = new ApplicationDetailVO();
        copyBase(application, latestRecord, detail);
        detail.setAttachments(splitAttachments(application.getAttachments()));
        return detail;
    }

    private void copyBase(AppApplication application, AppApprovalRecord latestRecord, ApplicationVO vo) {
        vo.setId(application.getId());
        vo.setApplicationNo(application.getApplicationNo());
        vo.setUserId(application.getUserId());
        vo.setApplicantName(userServiceClient.getUserName(application.getUserId()));
        vo.setDeptId(application.getDeptId());
        vo.setDeptName(userServiceClient.getDeptName(application.getDeptId()));
        vo.setAppType(application.getAppType());
        vo.setAppTypeText(toAppTypeText(application.getAppType()));
        vo.setLeaveType(application.getLeaveType());
        vo.setLeaveTypeText(toLeaveTypeText(application.getLeaveType()));
        vo.setStartTime(application.getStartTime());
        vo.setEndTime(application.getEndTime());
        vo.setDuration(application.getDuration());
        vo.setReason(application.getReason());
        vo.setStatus(application.getStatus());
        vo.setStatusText(toStatusText(application.getStatus()));
        vo.setCurrentApproverId(application.getCurrentApproverId());
        vo.setCurrentApproverName(userServiceClient.getUserName(application.getCurrentApproverId()));
        if (latestRecord != null) {
            vo.setLatestAction(latestRecord.getAction());
            vo.setLatestActionText(toActionText(latestRecord.getAction()));
            vo.setLatestActionTime(latestRecord.getActionTime());
        }
        vo.setCreateTime(application.getCreateTime());
    }

    private void fillTimeline(ApplicationDetailVO detail, List<AppApprovalRecord> records) {
        if (records == null || records.isEmpty()) {
            detail.setTimeline(Collections.emptyList());
            return;
        }
        List<ApprovalTimelineVO> timeline = records.stream().map(record -> {
            ApprovalTimelineVO vo = new ApprovalTimelineVO();
            vo.setId(record.getId());
            vo.setApproverId(record.getApproverId());
            vo.setApproverName(userServiceClient.getUserName(record.getApproverId()));
            vo.setAction(record.getAction());
            vo.setActionText(toActionText(record.getAction()));
            vo.setComment(record.getComment());
            vo.setActionTime(record.getActionTime());
            return vo;
        }).toList();
        detail.setTimeline(timeline);
    }

    private String generateApplicationNo(Integer appType) {
        String prefix = switch (appType) {
            case 1 -> "LV";
            case 2 -> "OT";
            case 3 -> "OUT";
            default -> "AP";
        };
        LocalDate today = LocalDate.now();
        String redisKey = "approval:application:no:" + prefix + ":" + today;
        Long seq = redisUtils.increment(redisKey);
        if (seq != null && seq == 1L) {
            redisUtils.expire(redisKey, Duration.ofDays(2));
        }
        long safeSeq = seq == null ? 1L : seq;
        return prefix + today.format(NO_FORMATTER) + String.format("%04d", safeSeq);
    }

    private void validateSubmitDTO(ApplicationSubmitDTO dto) {
        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "结束时间必须晚于开始时间");
        }
        if (Objects.equals(dto.getAppType(), 1) && dto.getLeaveType() == null) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "请假申请必须填写请假类型");
        }
    }

    private String joinAttachments(List<String> attachments) {
        if (attachments == null || attachments.isEmpty()) {
            return null;
        }
        return attachments.stream()
                .filter(StringUtils::hasText)
                .collect(Collectors.joining(","));
    }

    private List<String> splitAttachments(String attachments) {
        if (!StringUtils.hasText(attachments)) {
            return new ArrayList<>();
        }
        return List.of(attachments.split(","));
    }

    private BigDecimal calcDuration(LocalDateTime startTime, LocalDateTime endTime) {
        long minutes = Duration.between(startTime, endTime).toMinutes();
        return BigDecimal.valueOf(minutes).divide(BigDecimal.valueOf(60), 1, RoundingMode.HALF_UP);
    }

    private ApplicationQueryDTO safeQuery(ApplicationQueryDTO dto) {
        return dto == null ? new ApplicationQueryDTO() : dto;
    }

    private AppApplication getApplication(Long id) {
        AppApplication application = appApplicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(ResultCode.APPLICATION_NOT_FOUND);
        }
        return application;
    }

    private void ensureCanView(CurrentUser currentUser, AppApplication application) {
        if (Objects.equals(currentUser.getUserId(), application.getUserId())
                || Objects.equals(currentUser.getUserId(), application.getCurrentApproverId())
                || currentUser.hasRole("ROLE_ADMIN")
                || currentUser.hasRole("ROLE_HR")) {
            return;
        }
        Long processedCount = appApprovalRecordMapper.selectCount(new LambdaQueryWrapper<AppApprovalRecord>()
                .eq(AppApprovalRecord::getApplicationId, application.getId())
                .eq(AppApprovalRecord::getApproverId, currentUser.getUserId()));
        if (processedCount > 0) {
            return;
        }
        throw new BusinessException(ResultCode.FORBIDDEN, "无权查看该申请");
    }

    private String toAppTypeText(Integer appType) {
        if (appType == null) return null;
        return switch (appType) {
            case 1 -> "请假";
            case 2 -> "加班";
            case 3 -> "外出";
            default -> "未知";
        };
    }

    private String toLeaveTypeText(Integer leaveType) {
        if (leaveType == null) return null;
        return switch (leaveType) {
            case 1 -> "年假";
            case 2 -> "事假";
            case 3 -> "病假";
            case 4 -> "婚假";
            case 5 -> "产假";
            default -> "其他";
        };
    }

    private String toStatusText(Integer status) {
        if (status == null) return null;
        return switch (status) {
            case 0 -> "草稿";
            case 1 -> "审批中";
            case 2 -> "已通过";
            case 3 -> "已驳回";
            case 4 -> "已撤销";
            default -> "未知";
        };
    }

    private String toActionText(Integer action) {
        if (action == null) return null;
        return switch (action) {
            case 1 -> "同意";
            case 2 -> "驳回";
            default -> "未知";
        };
    }

    private void notifyApprover(AppApplication application, com.oa.common.remote.UserInfo applicant) {
        Long approverId = application.getCurrentApproverId();
        if (approverId == null) {
            return;
        }
        String applicantName = applicant.getRealName() != null ? applicant.getRealName() : applicant.getUsername();
        String typeText = toAppTypeText(application.getAppType());
        String title = "您有一条新的审批待办";
        String content = applicantName + "提交了一条" + typeText + "申请，请及时审批。";
        noticeServiceClient.sendMessage(approverId, title, content, MSG_TYPE_APPROVAL, application.getId());
    }

    private void notifyApplicant(AppApplication application) {
        Long applicantId = application.getUserId();
        if (applicantId == null) {
            return;
        }
        String typeText = toAppTypeText(application.getAppType());
        String actionText = toStatusText(application.getStatus());
        String title = "您的申请审批结果";
        String content = "您的" + typeText + "申请已被" + actionText + "。";
        noticeServiceClient.sendMessage(applicantId, title, content, MSG_TYPE_APPROVAL, application.getId());
    }
}
