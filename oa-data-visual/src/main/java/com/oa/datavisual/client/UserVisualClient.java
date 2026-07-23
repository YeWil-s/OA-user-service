package com.oa.datavisual.client;

import com.oa.common.exception.BusinessException;
import com.oa.common.result.Result;
import com.oa.common.result.ResultCode;
import com.oa.datavisual.config.FeignClientConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "oa-user-service", contextId = "visualUserClient", path = "/api/user/internal/visual", configuration = FeignClientConfig.class)
public interface UserVisualClient {

    @GetMapping("/departments")
    Result<List<DepartmentStats>> departmentStatsResponse(@RequestParam(value = "month", required = false) String month);

    default List<DepartmentStats> departmentStats(String month) {
        Result<List<DepartmentStats>> result = departmentStatsResponse(month);
        if (result == null || result.getCode() != ResultCode.SUCCESS.getCode()) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "调用用户统计服务失败");
        }
        return result.getData() == null ? List.of() : result.getData();
    }

    record DepartmentStats(Long deptId, String deptName, int activeEmployees, int newHires, int resignations) {
    }
}
