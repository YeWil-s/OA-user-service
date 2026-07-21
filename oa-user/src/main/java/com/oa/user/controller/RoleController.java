package com.oa.user.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.oa.common.result.Result;
import com.oa.user.dto.RoleDTO;
import com.oa.user.entity.SysRole;
import com.oa.user.service.ISysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/user/roles")
public class RoleController {

    private final ISysRoleService sysRoleService;

    public RoleController(ISysRoleService sysRoleService) {
        this.sysRoleService = sysRoleService;
    }

    @Operation(summary = "角色列表")
    @GetMapping
    public Result<IPage<SysRole>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(sysRoleService.pageRoles(pageNum, pageSize));
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody RoleDTO dto) {
        sysRoleService.addRole(dto);
        return Result.success();
    }

    @Operation(summary = "编辑角色")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody RoleDTO dto) {
        sysRoleService.updateRole(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysRoleService.deleteRole(id);
        return Result.success();
    }

    @Operation(summary = "分配角色菜单")
    @PutMapping("/{id}/menus")
    public Result<Void> assignMenus(@PathVariable Long id, @RequestBody List<Long> menuIds) {
        sysRoleService.assignMenus(id, menuIds);
        return Result.success();
    }

    @Operation(summary = "获取角色菜单ID列表")
    @GetMapping("/{id}/menus")
    public Result<List<Long>> getRoleMenuIds(@PathVariable Long id) {
        return Result.success(sysRoleService.getRoleMenuIds(id));
    }
}
