package com.oa.user.controller;

import com.oa.common.context.CurrentUser;
import com.oa.common.context.UserContextHolder;
import com.oa.common.response.Result;
import com.oa.security.jwt.JwtClaims;
import com.oa.security.jwt.JwtProperties;
import com.oa.security.jwt.JwtTokenProvider;
import com.oa.user.dto.CurrentUserResponse;
import com.oa.user.dto.LoginRequest;
import com.oa.user.dto.LoginResponse;
import com.oa.user.security.DemoUserAccountService;
import com.oa.user.security.LoginUserDetails;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class AuthController {

    private final DemoUserAccountService accountService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public AuthController(
            DemoUserAccountService accountService,
            JwtTokenProvider jwtTokenProvider,
            JwtProperties jwtProperties
    ) {
        this.accountService = accountService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtProperties = jwtProperties;
    }

    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginUserDetails userDetails = accountService.authenticate(request.getUsername(), request.getPassword());

        JwtClaims claims = new JwtClaims();
        claims.setUserId(userDetails.getUserId());
        claims.setDeptId(userDetails.getDeptId());
        claims.setUsername(userDetails.getUsername());
        claims.setRoles(userDetails.getRoles());
        claims.setPermissions(userDetails.getPermissions());

        String token = jwtTokenProvider.createToken(claims);
        return Result.success(LoginResponse.from(userDetails, token, jwtProperties.getExpirationMinutes() * 60));
    }

    @GetMapping("/current")
    public Result<CurrentUserResponse> current() {
        CurrentUser currentUser = UserContextHolder.getCurrentUser();
        CurrentUserResponse response = new CurrentUserResponse();
        response.setUserId(currentUser.getUserId());
        response.setDeptId(currentUser.getDeptId());
        response.setUsername(currentUser.getUsername());
        response.setRoles(currentUser.getRoles());
        response.setPermissions(currentUser.getPermissions());
        return Result.success(response);
    }
}
