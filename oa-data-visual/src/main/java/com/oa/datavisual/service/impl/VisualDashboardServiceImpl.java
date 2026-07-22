package com.oa.datavisual.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.oa.common.exception.BusinessException;
import com.oa.common.result.ResultCode;
import com.oa.datavisual.dto.ApprovalScreenResponse;
import com.oa.datavisual.dto.ApprovalSpeedResponse;
import com.oa.datavisual.dto.ApprovalStatsResponse;
import com.oa.datavisual.dto.ApprovalTrendResponse;
import com.oa.datavisual.dto.AttendanceScreenResponse;
import com.oa.datavisual.dto.AttendanceTrendResponse;
import com.oa.datavisual.dto.DeptMetricResponse;
import com.oa.datavisual.dto.HireResignationTrendResponse;
import com.oa.datavisual.dto.HrScreenResponse;
import com.oa.datavisual.dto.NameValueResponse;
import com.oa.datavisual.dto.TodayAttendanceRealtimeResponse;
import com.oa.datavisual.dto.VisualOverviewResponse;
import com.oa.datavisual.entity.StatApprovalSummary;
import com.oa.datavisual.entity.StatAttendanceMonthly;
import com.oa.datavisual.entity.StatDeptOverview;
import com.oa.datavisual.mapper.StatApprovalSummaryMapper;
import com.oa.datavisual.mapper.StatAttendanceMonthlyMapper;
import com.oa.datavisual.mapper.StatDeptOverviewMapper;
import com.oa.datavisual.mapper.VisualRealtimeMapper;
import com.oa.datavisual.mapper.row.DeptNameRow;
import com.oa.datavisual.mapper.row.TypeCountRow;
import com.oa.datavisual.service.IVisualCacheService;
import com.oa.datavisual.service.IVisualDashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VisualDashboardServiceImpl implements IVisualDashboardService {

    private static final Logger log = LoggerFactory.getLogger(VisualDashboardServiceImpl.class);
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final int DEFAULT_TREND_MONTHS = 6;
    private static final int MAX_TREND_MONTHS = 12;

    private final StatAttendanceMonthlyMapper attendanceMapper;
    private final StatApprovalSummaryMapper approvalMapper;
    private final StatDeptOverviewMapper deptOverviewMapper;
    private final VisualRealtimeMapper realtimeMapper;
    private final IVisualCacheService cacheService;

    public VisualDashboardServiceImpl(
            StatAttendanceMonthlyMapper attendanceMapper,
            StatApprovalSummaryMapper approvalMapper,
            StatDeptOverviewMapper deptOverviewMapper,
            VisualRealtimeMapper realtimeMapper,
            IVisualCacheService cacheService
    ) {
        this.attendanceMapper = attendanceMapper;
        this.approvalMapper = approvalMapper;
        this.deptOverviewMapper = deptOverviewMapper;
        this.realtimeMapper = realtimeMapper;
        this.cacheService = cacheService;
    }

    @Override
    public VisualOverviewResponse overview(String month) {
        String statMonth = normalizeMonth(month);
        return cacheService.get("visual:overview:" + statMonth,
                new TypeReference<VisualOverviewResponse>() {
                },
                () -> buildOverview(statMonth));
    }

    @Override
    public List<DeptMetricResponse> deptDistribution(String month) {
        String statMonth = normalizeMonth(month);
        return cacheService.get("visual:dept-distribution:" + statMonth,
                new TypeReference<List<DeptMetricResponse>>() {
                },
                () -> buildDeptDistribution(statMonth));
    }

    @Override
    public List<AttendanceTrendResponse> attendanceTrend(String endMonth, Integer months) {
        String normalizedEndMonth = normalizeMonth(endMonth);
        int trendMonths = normalizeTrendMonths(months);
        return cacheService.get("visual:attendance-trend:" + normalizedEndMonth + ":" + trendMonths,
                new TypeReference<List<AttendanceTrendResponse>>() {
                },
                () -> buildAttendanceTrend(normalizedEndMonth, trendMonths));
    }

    @Override
    public List<DeptMetricResponse> deptOvertime(String month) {
        String statMonth = normalizeMonth(month);
        return cacheService.get("visual:dept-overtime:" + statMonth,
                new TypeReference<List<DeptMetricResponse>>() {
                },
                () -> buildDeptOvertime(statMonth));
    }

    @Override
    public ApprovalStatsResponse approvalStats(String month) {
        String statMonth = normalizeMonth(month);
        return cacheService.get("visual:approval-stats:" + statMonth,
                new TypeReference<ApprovalStatsResponse>() {
                },
                () -> buildApprovalStats(statMonth));
    }

    @Override
    public List<ApprovalSpeedResponse> approvalSpeed(String month) {
        String statMonth = normalizeMonth(month);
        return cacheService.get("visual:approval-speed:" + statMonth,
                new TypeReference<List<ApprovalSpeedResponse>>() {
                },
                () -> buildApprovalSpeed(statMonth));
    }

    @Override
    public AttendanceScreenResponse attendanceScreen(String month) {
        String statMonth = normalizeMonth(month);
        return cacheService.get("visual:screen:attendance:" + statMonth,
                new TypeReference<AttendanceScreenResponse>() {
                },
                () -> new AttendanceScreenResponse(
                        statMonth,
                        buildOverview(statMonth),
                        buildAttendanceTrend(statMonth, DEFAULT_TREND_MONTHS),
                        buildDeptOvertime(statMonth),
                        buildDeptLateRanking(statMonth),
                        todayAttendanceRealtime(),
                        LocalDateTime.now()
                ));
    }

    @Override
    public HrScreenResponse hrScreen(String month) {
        String statMonth = normalizeMonth(month);
        return cacheService.get("visual:screen:hr:" + statMonth,
                new TypeReference<HrScreenResponse>() {
                },
                () -> new HrScreenResponse(
                        statMonth,
                        buildOverview(statMonth),
                        buildDeptDistribution(statMonth),
                        buildHireResignationTrend(statMonth, DEFAULT_TREND_MONTHS),
                        buildDeptAttendanceRate(statMonth),
                        LocalDateTime.now()
                ));
    }

    @Override
    public ApprovalScreenResponse approvalScreen(String month) {
        String statMonth = normalizeMonth(month);
        return cacheService.get("visual:screen:approval:" + statMonth,
                new TypeReference<ApprovalScreenResponse>() {
                },
                () -> new ApprovalScreenResponse(
                        statMonth,
                        buildApprovalStats(statMonth),
                        buildApprovalSpeed(statMonth),
                        buildApprovalTrend(statMonth, DEFAULT_TREND_MONTHS),
                        buildApplicationTypeDistribution(statMonth),
                        safeLong(realtimeMapper::countPendingApprovals),
                        LocalDateTime.now()
                ));
    }

    private VisualOverviewResponse buildOverview(String statMonth) {
        List<StatDeptOverview> deptOverviews = listDeptOverview(statMonth);
        List<StatAttendanceMonthly> attendanceRecords = listAttendance(statMonth);
        List<StatApprovalSummary> approvalRecords = listApproval(statMonth);

        long totalEmployees = deptOverviews.stream().mapToLong(item -> value(item.getActiveEmployees())).sum();
        if (totalEmployees == 0) {
            totalEmployees = attendanceRecords.stream().mapToLong(item -> value(item.getTotalEmployees())).sum();
        }
        if (totalEmployees == 0) {
            totalEmployees = safeLong(realtimeMapper::countActiveEmployees);
        }

        long newHires = deptOverviews.stream().mapToLong(item -> value(item.getNewHires())).sum();
        long resignations = deptOverviews.stream().mapToLong(item -> value(item.getResignations())).sum();
        BigDecimal attendanceRate = weightedAttendanceRate(deptOverviews, attendanceRecords);
        BigDecimal overtimeHours = attendanceRecords.stream()
                .map(item -> decimal(item.getOvertimeTotal()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(1, RoundingMode.HALF_UP);

        long totalApplications = approvalRecords.stream().mapToLong(item -> value(item.getTotalApplications())).sum();
        long approvedCount = approvalRecords.stream().mapToLong(item -> value(item.getApprovedCount())).sum();
        long rejectedCount = approvalRecords.stream().mapToLong(item -> value(item.getRejectedCount())).sum();
        long pendingCount = approvalRecords.stream().mapToLong(item -> value(item.getPendingCount())).sum();
        BigDecimal avgApprovalHours = weightedApprovalHours(approvalRecords);

        return new VisualOverviewResponse(
                statMonth,
                totalEmployees,
                newHires,
                resignations,
                attendanceRate,
                overtimeHours,
                totalApplications,
                approvedCount,
                rejectedCount,
                pendingCount,
                percent(approvedCount, totalApplications),
                avgApprovalHours,
                LocalDateTime.now()
        );
    }

    private List<DeptMetricResponse> buildDeptDistribution(String statMonth) {
        Map<Long, String> deptNames = deptNames();
        long total = listDeptOverview(statMonth).stream()
                .mapToLong(item -> value(item.getActiveEmployees()))
                .sum();

        return listDeptOverview(statMonth).stream()
                .sorted(Comparator.comparing(StatDeptOverview::getActiveEmployees, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(item -> new DeptMetricResponse(
                        item.getDeptId(),
                        deptName(item.getDeptId(), deptNames),
                        BigDecimal.valueOf(value(item.getActiveEmployees())),
                        percent(value(item.getActiveEmployees()), total),
                        "人"
                ))
                .toList();
    }

    private List<AttendanceTrendResponse> buildAttendanceTrend(String endMonth, int months) {
        List<String> monthRange = recentMonths(endMonth, months);
        Map<String, List<StatAttendanceMonthly>> grouped = listAttendance(monthRange).stream()
                .collect(Collectors.groupingBy(StatAttendanceMonthly::getStatMonth));

        return monthRange.stream()
                .map(month -> {
                    List<StatAttendanceMonthly> records = grouped.getOrDefault(month, List.of());
                    long normal = records.stream().mapToLong(item -> value(item.getNormalCount())).sum();
                    long late = records.stream().mapToLong(item -> value(item.getLateCount())).sum();
                    long early = records.stream().mapToLong(item -> value(item.getEarlyCount())).sum();
                    long absent = records.stream().mapToLong(item -> value(item.getAbsentCount())).sum();
                    long leave = records.stream().mapToLong(item -> value(item.getLeaveCount())).sum();
                    BigDecimal overtime = records.stream()
                            .map(item -> decimal(item.getOvertimeTotal()))
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .setScale(1, RoundingMode.HALF_UP);
                    return new AttendanceTrendResponse(month, normal, late, early, absent, leave,
                            overtime, percent(normal, normal + late + early + absent + leave));
                })
                .toList();
    }

    private List<DeptMetricResponse> buildDeptOvertime(String statMonth) {
        Map<Long, String> deptNames = deptNames();
        return listAttendance(statMonth).stream()
                .sorted(Comparator.comparing(StatAttendanceMonthly::getOvertimeTotal,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(item -> new DeptMetricResponse(
                        item.getDeptId(),
                        deptName(item.getDeptId(), deptNames),
                        decimal(item.getOvertimeTotal()).setScale(1, RoundingMode.HALF_UP),
                        BigDecimal.ZERO,
                        "小时"
                ))
                .toList();
    }

    private List<DeptMetricResponse> buildDeptLateRanking(String statMonth) {
        Map<Long, String> deptNames = deptNames();
        return listAttendance(statMonth).stream()
                .map(item -> {
                    long late = value(item.getLateCount());
                    long total = late
                            + value(item.getNormalCount())
                            + value(item.getEarlyCount())
                            + value(item.getAbsentCount())
                            + value(item.getLeaveCount());
                    return new DeptMetricResponse(
                            item.getDeptId(),
                            deptName(item.getDeptId(), deptNames),
                            BigDecimal.valueOf(late),
                            percent(late, total),
                            "人次"
                    );
                })
                .sorted(Comparator.comparing(DeptMetricResponse::rate).reversed())
                .toList();
    }

    private ApprovalStatsResponse buildApprovalStats(String statMonth) {
        List<StatApprovalSummary> approvalRecords = listApproval(statMonth);
        long total = approvalRecords.stream().mapToLong(item -> value(item.getTotalApplications())).sum();
        long approved = approvalRecords.stream().mapToLong(item -> value(item.getApprovedCount())).sum();
        long rejected = approvalRecords.stream().mapToLong(item -> value(item.getRejectedCount())).sum();
        long pending = approvalRecords.stream().mapToLong(item -> value(item.getPendingCount())).sum();

        List<NameValueResponse> distribution = List.of(
                new NameValueResponse("已通过", BigDecimal.valueOf(approved), "件"),
                new NameValueResponse("已驳回", BigDecimal.valueOf(rejected), "件"),
                new NameValueResponse("审批中", BigDecimal.valueOf(pending), "件")
        );

        return new ApprovalStatsResponse(
                statMonth,
                total,
                approved,
                rejected,
                pending,
                percent(approved, total),
                percent(rejected, total),
                percent(pending, total),
                distribution
        );
    }

    private List<ApprovalSpeedResponse> buildApprovalSpeed(String statMonth) {
        Map<Long, String> deptNames = deptNames();
        return listApproval(statMonth).stream()
                .sorted(Comparator.comparing(StatApprovalSummary::getAvgApprovalHours,
                        Comparator.nullsLast(Comparator.naturalOrder())))
                .map(item -> new ApprovalSpeedResponse(
                        item.getDeptId(),
                        deptName(item.getDeptId(), deptNames),
                        value(item.getTotalApplications()),
                        decimal(item.getAvgApprovalHours()).setScale(1, RoundingMode.HALF_UP)
                ))
                .toList();
    }

    private List<ApprovalTrendResponse> buildApprovalTrend(String endMonth, int months) {
        List<String> monthRange = recentMonths(endMonth, months);
        Map<String, List<StatApprovalSummary>> grouped = listApproval(monthRange).stream()
                .collect(Collectors.groupingBy(StatApprovalSummary::getStatMonth));

        return monthRange.stream()
                .map(month -> {
                    List<StatApprovalSummary> records = grouped.getOrDefault(month, List.of());
                    long total = records.stream().mapToLong(item -> value(item.getTotalApplications())).sum();
                    long approved = records.stream().mapToLong(item -> value(item.getApprovedCount())).sum();
                    long rejected = records.stream().mapToLong(item -> value(item.getRejectedCount())).sum();
                    long pending = records.stream().mapToLong(item -> value(item.getPendingCount())).sum();
                    return new ApprovalTrendResponse(month, total, approved, rejected, pending, weightedApprovalHours(records));
                })
                .toList();
    }

    private List<HireResignationTrendResponse> buildHireResignationTrend(String endMonth, int months) {
        List<String> monthRange = recentMonths(endMonth, months);
        Map<String, List<StatDeptOverview>> grouped = listDeptOverview(monthRange).stream()
                .collect(Collectors.groupingBy(StatDeptOverview::getStatMonth));

        return monthRange.stream()
                .map(month -> {
                    List<StatDeptOverview> records = grouped.getOrDefault(month, List.of());
                    long active = records.stream().mapToLong(item -> value(item.getActiveEmployees())).sum();
                    long hires = records.stream().mapToLong(item -> value(item.getNewHires())).sum();
                    long resignations = records.stream().mapToLong(item -> value(item.getResignations())).sum();
                    return new HireResignationTrendResponse(month, active, hires, resignations);
                })
                .toList();
    }

    private List<DeptMetricResponse> buildDeptAttendanceRate(String statMonth) {
        Map<Long, String> deptNames = deptNames();
        return listDeptOverview(statMonth).stream()
                .sorted(Comparator.comparing(StatDeptOverview::getAttendanceRate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .map(item -> new DeptMetricResponse(
                        item.getDeptId(),
                        deptName(item.getDeptId(), deptNames),
                        decimal(item.getAttendanceRate()).setScale(2, RoundingMode.HALF_UP),
                        decimal(item.getAttendanceRate()).setScale(2, RoundingMode.HALF_UP),
                        "%"
                ))
                .toList();
    }

    private List<NameValueResponse> buildApplicationTypeDistribution(String statMonth) {
        List<TypeCountRow> rows = safeList(() -> realtimeMapper.selectApprovalTypeCounts(statMonth));
        return rows.stream()
                .map(row -> new NameValueResponse(applicationTypeName(row.getType()),
                        BigDecimal.valueOf(row.getCount() == null ? 0 : row.getCount()), "件"))
                .toList();
    }

    private TodayAttendanceRealtimeResponse todayAttendanceRealtime() {
        long activeEmployees = safeLong(realtimeMapper::countActiveEmployees);
        long punchInCount = safeLong(realtimeMapper::countTodayPunchIn);
        long punchOutCount = safeLong(realtimeMapper::countTodayPunchOut);
        return new TodayAttendanceRealtimeResponse(
                LocalDate.now(),
                activeEmployees,
                punchInCount,
                punchOutCount,
                percent(punchInCount, activeEmployees)
        );
    }

    private List<StatAttendanceMonthly> listAttendance(String statMonth) {
        return attendanceMapper.selectList(new LambdaQueryWrapper<StatAttendanceMonthly>()
                .eq(StatAttendanceMonthly::getStatMonth, statMonth)
                .orderByAsc(StatAttendanceMonthly::getDeptId));
    }

    private List<StatAttendanceMonthly> listAttendance(List<String> months) {
        if (months.isEmpty()) {
            return List.of();
        }
        return attendanceMapper.selectList(new LambdaQueryWrapper<StatAttendanceMonthly>()
                .in(StatAttendanceMonthly::getStatMonth, months)
                .orderByAsc(StatAttendanceMonthly::getStatMonth)
                .orderByAsc(StatAttendanceMonthly::getDeptId));
    }

    private List<StatApprovalSummary> listApproval(String statMonth) {
        return approvalMapper.selectList(new LambdaQueryWrapper<StatApprovalSummary>()
                .eq(StatApprovalSummary::getStatMonth, statMonth)
                .orderByAsc(StatApprovalSummary::getDeptId));
    }

    private List<StatApprovalSummary> listApproval(List<String> months) {
        if (months.isEmpty()) {
            return List.of();
        }
        return approvalMapper.selectList(new LambdaQueryWrapper<StatApprovalSummary>()
                .in(StatApprovalSummary::getStatMonth, months)
                .orderByAsc(StatApprovalSummary::getStatMonth)
                .orderByAsc(StatApprovalSummary::getDeptId));
    }

    private List<StatDeptOverview> listDeptOverview(String statMonth) {
        return deptOverviewMapper.selectList(new LambdaQueryWrapper<StatDeptOverview>()
                .eq(StatDeptOverview::getStatMonth, statMonth)
                .orderByAsc(StatDeptOverview::getDeptId));
    }

    private List<StatDeptOverview> listDeptOverview(List<String> months) {
        if (months.isEmpty()) {
            return List.of();
        }
        return deptOverviewMapper.selectList(new LambdaQueryWrapper<StatDeptOverview>()
                .in(StatDeptOverview::getStatMonth, months)
                .orderByAsc(StatDeptOverview::getStatMonth)
                .orderByAsc(StatDeptOverview::getDeptId));
    }

    private Map<Long, String> deptNames() {
        List<DeptNameRow> rows = safeList(realtimeMapper::selectDeptNames);
        return rows.stream()
                .filter(row -> row.getDeptId() != null)
                .collect(Collectors.toMap(DeptNameRow::getDeptId,
                        row -> StringUtils.hasText(row.getDeptName()) ? row.getDeptName() : "部门" + row.getDeptId(),
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    private String deptName(Long deptId, Map<Long, String> deptNames) {
        if (deptId == null) {
            return "未知部门";
        }
        return deptNames.getOrDefault(deptId, "部门" + deptId);
    }

    private BigDecimal weightedAttendanceRate(
            List<StatDeptOverview> deptOverviews,
            List<StatAttendanceMonthly> attendanceRecords
    ) {
        long activeTotal = deptOverviews.stream().mapToLong(item -> value(item.getActiveEmployees())).sum();
        if (activeTotal > 0) {
            BigDecimal weightedRate = deptOverviews.stream()
                    .map(item -> decimal(item.getAttendanceRate())
                            .multiply(BigDecimal.valueOf(value(item.getActiveEmployees()))))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            return weightedRate.divide(BigDecimal.valueOf(activeTotal), 2, RoundingMode.HALF_UP);
        }

        long normal = attendanceRecords.stream().mapToLong(item -> value(item.getNormalCount())).sum();
        long late = attendanceRecords.stream().mapToLong(item -> value(item.getLateCount())).sum();
        long early = attendanceRecords.stream().mapToLong(item -> value(item.getEarlyCount())).sum();
        long absent = attendanceRecords.stream().mapToLong(item -> value(item.getAbsentCount())).sum();
        long leave = attendanceRecords.stream().mapToLong(item -> value(item.getLeaveCount())).sum();
        return percent(normal, normal + late + early + absent + leave);
    }

    private BigDecimal weightedApprovalHours(List<StatApprovalSummary> records) {
        long total = records.stream().mapToLong(item -> value(item.getTotalApplications())).sum();
        if (total == 0) {
            return BigDecimal.ZERO.setScale(1, RoundingMode.HALF_UP);
        }
        BigDecimal weighted = records.stream()
                .map(item -> decimal(item.getAvgApprovalHours())
                        .multiply(BigDecimal.valueOf(value(item.getTotalApplications()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return weighted.divide(BigDecimal.valueOf(total), 1, RoundingMode.HALF_UP);
    }

    private String normalizeMonth(String month) {
        if (!StringUtils.hasText(month)) {
            return YearMonth.now().format(MONTH_FORMATTER);
        }
        try {
            return YearMonth.parse(month.trim(), MONTH_FORMATTER).format(MONTH_FORMATTER);
        } catch (DateTimeParseException ex) {
            throw new BusinessException(ResultCode.BAD_REQUEST.getCode(), "统计月份格式应为 yyyy-MM");
        }
    }

    private int normalizeTrendMonths(Integer months) {
        if (months == null) {
            return DEFAULT_TREND_MONTHS;
        }
        return Math.max(1, Math.min(months, MAX_TREND_MONTHS));
    }

    private List<String> recentMonths(String endMonth, int months) {
        YearMonth end = YearMonth.parse(normalizeMonth(endMonth), MONTH_FORMATTER);
        List<String> result = new ArrayList<>();
        for (int i = months - 1; i >= 0; i--) {
            result.add(end.minusMonths(i).format(MONTH_FORMATTER));
        }
        return result;
    }

    private BigDecimal percent(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal decimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private int value(Integer value) {
        return value == null ? 0 : value;
    }

    private String applicationTypeName(Integer type) {
        if (type == null) {
            return "未知";
        }
        return switch (type) {
            case 1 -> "请假";
            case 2 -> "加班";
            case 3 -> "外出";
            default -> "其他";
        };
    }

    private long safeLong(SqlLongSupplier supplier) {
        try {
            Long value = supplier.get();
            return value == null ? 0 : value;
        } catch (Exception ex) {
            log.debug("Realtime visual metric query failed", ex);
            return 0;
        }
    }

    private <T> List<T> safeList(SqlListSupplier<T> supplier) {
        try {
            List<T> value = supplier.get();
            return value == null ? List.of() : value;
        } catch (Exception ex) {
            log.debug("Realtime visual list query failed", ex);
            return List.of();
        }
    }

    @FunctionalInterface
    private interface SqlLongSupplier {
        Long get();
    }

    @FunctionalInterface
    private interface SqlListSupplier<T> {
        List<T> get();
    }
}
