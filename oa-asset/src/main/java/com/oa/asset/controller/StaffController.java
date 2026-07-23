package com.oa.asset.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oa.asset.dto.EmployeeArchiveDTO;
import com.oa.asset.dto.StaffChangeDTO;
import com.oa.asset.entity.EmployeeArchive;
import com.oa.asset.service.StaffService;
import com.oa.asset.vo.StaffChangeVO;
import com.oa.common.result.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "人事档案与变动")
@RestController
@RequestMapping("/api/asset")
@Validated
/**
 * 人事接口：维护员工扩展档案、合同信息和入职/转正/调岗/离职记录。
 */
public class StaffController {
    private final StaffService service;
    public StaffController(StaffService service) { this.service = service; }

    @GetMapping("/staff/archive/{userId}") public Result<EmployeeArchive> archive(@PathVariable @Positive Long userId) { return Result.success(service.getArchive(userId)); }
    @PutMapping("/staff/archive/{userId}") public Result<Void> saveArchive(@PathVariable @Positive Long userId, @Valid @RequestBody EmployeeArchiveDTO dto) { service.saveArchive(userId, dto); return Result.success(); }
    @GetMapping("/staff/changes")
    public Result<IPage<StaffChangeVO>> changes(@RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int pageSize,
            @RequestParam(required = false) Long userId, @RequestParam(required = false) Integer changeType) {
        return Result.success(service.changes(pageNum, pageSize, userId, changeType));
    }
    @PostMapping("/staff/changes")
    public Result<Void> createChange(@Valid @RequestBody StaffChangeDTO dto) {
        if (dto.getChangeType() != null && dto.getChangeType() == 3) {
            return Result.fail(400, "调岗需要通过审批流程提交申请，不可直接创建");
        }
        service.createChange(dto);
        return Result.success();
    }
    @PutMapping("/staff/changes/{id}")
    public Result<Void> updateChange(@PathVariable Long id, @Valid @RequestBody StaffChangeDTO dto) {
        if (dto.getChangeType() != null && dto.getChangeType() == 3) {
            return Result.fail(400, "调岗记录不可直接修改，请通过审批流程处理");
        }
        service.updateChange(id, dto);
        return Result.success();
    }
    @DeleteMapping("/staff/changes/{id}") public Result<Void> deleteChange(@PathVariable Long id) { service.deleteChange(id); return Result.success(); }

    @PostMapping("/internal/staff/changes")
    public Result<Void> createChangeInternal(@Valid @RequestBody StaffChangeDTO dto) {
        service.createChange(dto);
        return Result.success();
    }
    @GetMapping("/contracts")
    public Result<IPage<EmployeeArchive>> contracts(@RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int pageSize,
            @RequestParam(required = false) Long userId) { return Result.success(service.contracts(pageNum, pageSize, userId)); }
    @GetMapping("/contracts/expiring")
    public Result<IPage<EmployeeArchive>> expiring(@RequestParam(defaultValue = "1") @Min(1) int pageNum,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int pageSize,
            @RequestParam(defaultValue = "30") int days) { return Result.success(service.expiringContracts(pageNum, pageSize, days)); }
}
