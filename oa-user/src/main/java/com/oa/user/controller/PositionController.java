package com.oa.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oa.common.result.Result;
import com.oa.user.dto.PositionDTO;
import com.oa.user.entity.SysPosition;
import com.oa.user.service.ISysPositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "岗位管理")
@RestController
@RequestMapping("/api/user/positions")
public class PositionController {

    private final ISysPositionService sysPositionService;

    public PositionController(ISysPositionService sysPositionService) {
        this.sysPositionService = sysPositionService;
    }

    @Operation(summary = "岗位列表")
    @GetMapping
    public Result<IPage<SysPosition>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long deptId) {
        return Result.success(sysPositionService.pagePositions(pageNum, pageSize, deptId));
    }

    @Operation(summary = "新增岗位")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody PositionDTO dto) {
        sysPositionService.addPosition(dto);
        return Result.success();
    }

    @Operation(summary = "编辑岗位")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody PositionDTO dto) {
        sysPositionService.updatePosition(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除岗位")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysPositionService.deletePosition(id);
        return Result.success();
    }

    @Operation(summary = "获取岗位绑定的角色ID列表")
    @GetMapping("/{id}/roles")
    public Result<List<Long>> getRoleIds(@PathVariable Long id) {
        return Result.success(sysPositionService.getRoleIdsByPositionId(id));
    }
}
