package com.oa.datavisual.client;

import com.oa.common.exception.BusinessException;
import com.oa.common.result.Result;
import com.oa.common.result.ResultCode;
import com.oa.datavisual.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "oa-approval-service", contextId = "visualApprovalClient", path = "/api/approval/internal/visual", configuration = FeignClientConfig.class)
public interface ApprovalVisualClient {

    @GetMapping("/monthly")
    Result<List<DepartmentStats>> monthlyStatsResponse(@RequestParam(value = "month", required = false) String month);

    @GetMapping("/realtime")
    Result<RealtimeStats> realtimeStatsResponse(@RequestParam(value = "month", required = false) String month);

    default List<DepartmentStats> monthlyStats(String month) {
        Result<List<DepartmentStats>> result = monthlyStatsResponse(month);
        if (result == null || result.getCode() != ResultCode.SUCCESS.getCode()) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "调用审批统计服务失败");
        }
        return result.getData() == null ? List.of() : result.getData();
    }

    default RealtimeStats realtimeStats(String month) {
        Result<RealtimeStats> result = realtimeStatsResponse(month);
        if (result == null || result.getCode() != ResultCode.SUCCESS.getCode() || result.getData() == null) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "调用审批实时服务失败");
        }
        return result.getData();
    }

    record DepartmentStats(Long deptId, int totalApplications, int approvedCount, int rejectedCount,
                           int pendingCount, BigDecimal avgApprovalHours) {
    }

    record RealtimeStats(long pendingCount, List<TypeCount> typeCounts) {
    }

    record TypeCount(Integer type, long count) {
    }
}
