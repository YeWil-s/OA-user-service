package com.oa.datavisual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.oa.common.exception.BusinessException;
import com.oa.common.result.ResultCode;
import com.oa.datavisual.dto.StatisticsSyncResponse;
import com.oa.datavisual.entity.StatApprovalSummary;
import com.oa.datavisual.entity.StatAttendanceMonthly;
import com.oa.datavisual.entity.StatDeptOverview;
import com.oa.datavisual.mapper.StatApprovalSummaryMapper;
import com.oa.datavisual.mapper.StatAttendanceMonthlyMapper;
import com.oa.datavisual.mapper.StatDeptOverviewMapper;
import com.oa.datavisual.mapper.VisualStatisticsExtractMapper;
import com.oa.datavisual.service.IStatisticsSyncService;
import com.oa.datavisual.service.IVisualCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class StatisticsSyncServiceImpl implements IStatisticsSyncService {

    private static final Logger log = LoggerFactory.getLogger(StatisticsSyncServiceImpl.class);
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");

    private final VisualStatisticsExtractMapper extractMapper;
    private final StatAttendanceMonthlyMapper attendanceMapper;
    private final StatApprovalSummaryMapper approvalMapper;
    private final StatDeptOverviewMapper deptOverviewMapper;
    private final IVisualCacheService cacheService;

    public StatisticsSyncServiceImpl(
            VisualStatisticsExtractMapper extractMapper,
            StatAttendanceMonthlyMapper attendanceMapper,
            StatApprovalSummaryMapper approvalMapper,
            StatDeptOverviewMapper deptOverviewMapper,
            IVisualCacheService cacheService
    ) {
        this.extractMapper = extractMapper;
        this.attendanceMapper = attendanceMapper;
        this.approvalMapper = approvalMapper;
        this.deptOverviewMapper = deptOverviewMapper;
        this.cacheService = cacheService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public StatisticsSyncResponse syncMonth(String month) {
        YearMonth statMonth = normalizeMonth(month);
        String statMonthText = statMonth.format(MONTH_FORMATTER);
        LocalDate startDate = statMonth.atDay(1);
        LocalDate endDate = statMonth.plusMonths(1).atDay(1);
        LocalDateTime startTime = startDate.atStartOfDay();
        LocalDateTime endTime = endDate.atStartOfDay();

        List<StatDeptOverview> deptOverviews = extractMapper.selectDeptOverview(
                statMonthText, startDate, endDate, startTime, endTime);
        List<StatAttendanceMonthly> attendanceRecords = extractMapper.selectAttendanceMonthly(
                statMonthText, startDate, endDate);
        List<StatApprovalSummary> approvalRecords = extractMapper.selectApprovalSummary(
                statMonthText, startTime, endTime);

        deleteMonth(statMonthText);
        deptOverviews.forEach(deptOverviewMapper::insert);
        attendanceRecords.forEach(attendanceMapper::insert);
        approvalRecords.forEach(approvalMapper::insert);
        cacheService.evictMonth(statMonthText);

        StatisticsSyncResponse response = new StatisticsSyncResponse(
                statMonthText,
                deptOverviews.size(),
                attendanceRecords.size(),
                approvalRecords.size(),
                LocalDateTime.now()
        );
        log.info("Visual statistics synced: {}", response);
        return response;
    }

    @Override
    public StatisticsSyncResponse syncCurrentMonth() {
        return syncMonth(YearMonth.now().format(MONTH_FORMATTER));
    }

    @Override
    public StatisticsSyncResponse syncLastMonth() {
        return syncMonth(YearMonth.now().minusMonths(1).format(MONTH_FORMATTER));
    }

    private void deleteMonth(String statMonth) {
        deptOverviewMapper.delete(new LambdaQueryWrapper<StatDeptOverview>()
                .eq(StatDeptOverview::getStatMonth, statMonth));
        attendanceMapper.delete(new LambdaQueryWrapper<StatAttendanceMonthly>()
                .eq(StatAttendanceMonthly::getStatMonth, statMonth));
        approvalMapper.delete(new LambdaQueryWrapper<StatApprovalSummary>()
                .eq(StatApprovalSummary::getStatMonth, statMonth));
    }

    private YearMonth normalizeMonth(String month) {
        if (!StringUtils.hasText(month)) {
            return YearMonth.now();
        }
        try {
            return YearMonth.parse(month.trim(), MONTH_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "统计月份格式应为 yyyy-MM");
        }
    }
}
