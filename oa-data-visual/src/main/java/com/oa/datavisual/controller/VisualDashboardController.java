package com.oa.datavisual.controller;

import com.oa.common.result.Result;
import com.oa.datavisual.dto.ApprovalScreenResponse;
import com.oa.datavisual.dto.ApprovalSpeedResponse;
import com.oa.datavisual.dto.ApprovalStatsResponse;
import com.oa.datavisual.dto.AttendanceScreenResponse;
import com.oa.datavisual.dto.AttendanceTrendResponse;
import com.oa.datavisual.dto.DeptMetricResponse;
import com.oa.datavisual.dto.HrScreenResponse;
import com.oa.datavisual.dto.VisualOverviewResponse;
import com.oa.datavisual.service.IVisualDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "数据可视化")
@RestController
@RequestMapping("/api/visual")
public class VisualDashboardController {

    private final IVisualDashboardService visualDashboardService;

    public VisualDashboardController(IVisualDashboardService visualDashboardService) {
        this.visualDashboardService = visualDashboardService;
    }

    @Operation(summary = "全公司概览数据")
    @GetMapping("/dashboard/overview")
    public Result<VisualOverviewResponse> overview(@RequestParam(required = false) String month) {
        return Result.success(visualDashboardService.overview(month));
    }

    @Operation(summary = "部门人员分布")
    @GetMapping("/dashboard/dept-distribution")
    public Result<List<DeptMetricResponse>> deptDistribution(@RequestParam(required = false) String month) {
        return Result.success(visualDashboardService.deptDistribution(month));
    }

    @Operation(summary = "月度考勤趋势")
    @GetMapping("/dashboard/attendance-trend")
    public Result<List<AttendanceTrendResponse>> attendanceTrend(
            @RequestParam(required = false) String endMonth,
            @RequestParam(required = false) Integer months
    ) {
        return Result.success(visualDashboardService.attendanceTrend(endMonth, months));
    }

    @Operation(summary = "部门加班对比")
    @GetMapping("/dashboard/dept-overtime")
    public Result<List<DeptMetricResponse>> deptOvertime(@RequestParam(required = false) String month) {
        return Result.success(visualDashboardService.deptOvertime(month));
    }

    @Operation(summary = "审批流转统计")
    @GetMapping("/dashboard/approval-stats")
    public Result<ApprovalStatsResponse> approvalStats(@RequestParam(required = false) String month) {
        return Result.success(visualDashboardService.approvalStats(month));
    }

    @Operation(summary = "审批效率")
    @GetMapping("/dashboard/approval-speed")
    public Result<List<ApprovalSpeedResponse>> approvalSpeed(@RequestParam(required = false) String month) {
        return Result.success(visualDashboardService.approvalSpeed(month));
    }

    @Operation(summary = "考勤数据大屏")
    @GetMapping("/screen/attendance")
    public Result<AttendanceScreenResponse> attendanceScreen(@RequestParam(required = false) String month) {
        return Result.success(visualDashboardService.attendanceScreen(month));
    }

    @Operation(summary = "人事数据大屏")
    @GetMapping("/screen/hr")
    public Result<HrScreenResponse> hrScreen(@RequestParam(required = false) String month) {
        return Result.success(visualDashboardService.hrScreen(month));
    }

    @Operation(summary = "审批数据大屏")
    @GetMapping("/screen/approval")
    public Result<ApprovalScreenResponse> approvalScreen(@RequestParam(required = false) String month) {
        return Result.success(visualDashboardService.approvalScreen(month));
    }
}
