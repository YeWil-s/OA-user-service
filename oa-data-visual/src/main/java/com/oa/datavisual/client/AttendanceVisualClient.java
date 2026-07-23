package com.oa.datavisual.client;

import com.oa.common.exception.BusinessException;
import com.oa.common.result.Result;
import com.oa.common.result.ResultCode;
import com.oa.datavisual.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@FeignClient(name = "oa-attendance-service", contextId = "visualAttendanceClient", path = "/api/attendance/internal/visual", configuration = FeignClientConfig.class)
public interface AttendanceVisualClient {

    @GetMapping("/monthly")
    Result<List<DepartmentStats>> monthlyStatsResponse(@RequestParam(value = "month", required = false) String month);

    @GetMapping("/today")
    Result<TodayStats> todayStatsResponse();

    default List<DepartmentStats> monthlyStats(String month) {
        Result<List<DepartmentStats>> result = monthlyStatsResponse(month);
        if (result == null || result.getCode() != ResultCode.SUCCESS.getCode()) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "调用考勤统计服务失败");
        }
        return result.getData() == null ? List.of() : result.getData();
    }

    default TodayStats todayStats() {
        Result<TodayStats> result = todayStatsResponse();
        if (result == null || result.getCode() != ResultCode.SUCCESS.getCode() || result.getData() == null) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "调用考勤实时服务失败");
        }
        return result.getData();
    }

    record DepartmentStats(Long deptId, int normalCount, int lateCount, int earlyCount, int absentCount,
                           int leaveCount, BigDecimal overtimeHours) {
    }

    record TodayStats(LocalDate date, long punchInCount, long punchOutCount) {
    }
}
