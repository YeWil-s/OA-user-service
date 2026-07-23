package com.oa.user.controller;

import com.oa.common.result.Result;
import com.oa.user.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user/internal")
public class InternalController {

    private final ISysUserService sysUserService;

    public InternalController(ISysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Operation(summary = "内部接口：更新员工部门岗位（调岗审批后回调）")
    @PutMapping("/employees/{id}/dept-position")
    public Result<Void> updateDeptPosition(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long deptId = body.get("deptId") instanceof Number n ? n.longValue() : null;
        Long positionId = body.get("positionId") instanceof Number n ? n.longValue() : null;
        sysUserService.updateEmployeeDeptPosition(id, deptId, positionId);
        return Result.success();
    }
}
