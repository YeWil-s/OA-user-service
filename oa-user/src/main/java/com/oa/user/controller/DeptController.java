package com.oa.user.controller;

import com.oa.common.result.Result;
import com.oa.user.dto.DeptDTO;
import com.oa.user.service.ISysDeptService;
import com.oa.user.vo.DeptTreeVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "部门管理")
@RestController
@RequestMapping("/api/user/depts")
public class DeptController {

    private final ISysDeptService sysDeptService;

    public DeptController(ISysDeptService sysDeptService) {
        this.sysDeptService = sysDeptService;
    }

    @Operation(summary = "部门树")
    @GetMapping
    public Result<List<DeptTreeVO>> tree() {
        return Result.success(sysDeptService.getDeptTree());
    }

    @Operation(summary = "新增部门")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody DeptDTO dto) {
        sysDeptService.addDept(dto);
        return Result.success();
    }

    @Operation(summary = "编辑部门")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody DeptDTO dto) {
        sysDeptService.updateDept(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysDeptService.deleteDept(id);
        return Result.success();
    }
}
