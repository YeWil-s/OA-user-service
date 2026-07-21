package com.oa.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oa.common.result.Result;
import com.oa.user.dto.EmployeeDTO;
import com.oa.user.dto.ResetPasswordDTO;
import com.oa.user.entity.SysUser;
import com.oa.user.service.ISysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "员工管理")
@RestController
@RequestMapping("/api/user/employees")
public class EmployeeController {

    private final ISysUserService sysUserService;

    public EmployeeController(ISysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Operation(summary = "员工列表（分页）")
    @GetMapping
    public Result<IPage<SysUser>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) Long positionId,
            @RequestParam(required = false) String realName,
            @RequestParam(required = false) Integer status) {
        return Result.success(sysUserService.pageEmployees(pageNum, pageSize, deptId, positionId, realName, status));
    }

    @Operation(summary = "新增员工")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody EmployeeDTO dto) {
        sysUserService.addEmployee(dto);
        return Result.success();
    }

    @Operation(summary = "员工详情")
    @GetMapping("/{id}")
    public Result<SysUser> detail(@PathVariable Long id) {
        return Result.success(sysUserService.getEmployeeDetail(id));
    }

    @Operation(summary = "编辑员工")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody EmployeeDTO dto) {
        sysUserService.updateEmployee(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除员工（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysUserService.deleteEmployee(id);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/reset-pwd")
    public Result<Void> resetPassword(@PathVariable Long id) {
        sysUserService.resetPassword(id);
        return Result.success();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/{id}/update-pwd")
    public Result<Void> updatePassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordDTO dto) {
        sysUserService.updatePassword(id, dto);
        return Result.success();
    }
}
