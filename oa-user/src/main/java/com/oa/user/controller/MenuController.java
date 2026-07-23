package com.oa.user.controller;

import com.oa.common.result.Result;
import com.oa.user.dto.MenuDTO;
import com.oa.user.service.ISysMenuService;
import com.oa.user.vo.MenuTreeVO;
import com.oa.user.vo.RouterVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "菜单管理")
@RestController
@RequestMapping("/api/user/menus")
public class MenuController {

    private final ISysMenuService sysMenuService;

    public MenuController(ISysMenuService sysMenuService) {
        this.sysMenuService = sysMenuService;
    }

    @Operation(summary = "菜单树（全部）")
    @GetMapping
    public Result<List<MenuTreeVO>> tree() {
        return Result.success(sysMenuService.getMenuTree());
    }

    @Operation(summary = "获取当前用户可访问的路由")
    @GetMapping("/routers")
    public Result<List<RouterVO>> routers(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "未登录");
        }
        return Result.success(sysMenuService.getUserRouters(userId));
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    public Result<Void> add(@Valid @RequestBody MenuDTO dto) {
        sysMenuService.addMenu(dto);
        return Result.success();
    }

    @Operation(summary = "编辑菜单")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody MenuDTO dto) {
        sysMenuService.updateMenu(id, dto);
        return Result.success();
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        sysMenuService.deleteMenu(id);
        return Result.success();
    }
}
