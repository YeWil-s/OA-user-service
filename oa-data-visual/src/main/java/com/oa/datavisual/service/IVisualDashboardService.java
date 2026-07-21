package com.oa.datavisual.service;

import com.oa.datavisual.dto.ApprovalScreenResponse;
import com.oa.datavisual.dto.ApprovalSpeedResponse;
import com.oa.datavisual.dto.ApprovalStatsResponse;
import com.oa.datavisual.dto.AttendanceScreenResponse;
import com.oa.datavisual.dto.AttendanceTrendResponse;
import com.oa.datavisual.dto.DeptMetricResponse;
import com.oa.datavisual.dto.HrScreenResponse;
import com.oa.datavisual.dto.VisualOverviewResponse;

import java.util.List;

public interface IVisualDashboardService {

    VisualOverviewResponse overview(String month);

    List<DeptMetricResponse> deptDistribution(String month);

    List<AttendanceTrendResponse> attendanceTrend(String endMonth, Integer months);

    List<DeptMetricResponse> deptOvertime(String month);

    ApprovalStatsResponse approvalStats(String month);

    List<ApprovalSpeedResponse> approvalSpeed(String month);

    AttendanceScreenResponse attendanceScreen(String month);

    HrScreenResponse hrScreen(String month);

    ApprovalScreenResponse approvalScreen(String month);
}
