package com.oa.user.controller;

import com.oa.common.result.Result;
import com.oa.user.dto.LoginDTO;
import com.oa.user.service.ISysUserService;
import com.oa.user.vo.CurrentUserVO;
import com.oa.user.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户认证")
@RestController
@RequestMapping("/api/user")
public class AuthController {

    private final ISysUserService sysUserService;

    public AuthController(ISysUserService sysUserService) {
        this.sysUserService = sysUserService;
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = sysUserService.login(dto.getUsername(), dto.getPassword());
        return Result.success(vo);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }

    @Operation(summary = "获取当前登录用户信息（供其他微服务通过Nacos调用）")
    @GetMapping("/current")
    public Result<CurrentUserVO> current(@RequestHeader("Authorization") String authHeader) {
        CurrentUserVO vo = sysUserService.getCurrentUser(authHeader);
        return Result.success(vo);
    }
}
