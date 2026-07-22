package com.oa.user.security;

import com.oa.common.exception.BusinessException;
import com.oa.common.response.ResultCode;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DemoUserAccountService {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final Map<String, LoginUserDetails> users;

    public DemoUserAccountService() {
        this.users = Map.of(
                "admin", new LoginUserDetails(
                        1L,
                        1L,
                        "admin",
                        passwordEncoder.encode("123456"),
                        List.of("ROLE_ADMIN"),
                        List.of(
                                "user:employee:list",
                                "user:employee:add",
                                "attendance:record",
                                "approval:pending",
                                "notice:publish",
                                "notice:manage"
                        )
                ),
                "zhangsan", new LoginUserDetails(
                        2L,
                        2L,
                        "zhangsan",
                        passwordEncoder.encode("123456"),
                        List.of("ROLE_EMPLOYEE"),
                        List.of("attendance:punch", "attendance:record", "approval:submit", "notice:list")
                )
        );
    }

    public LoginUserDetails authenticate(String username, String rawPassword) {
        LoginUserDetails userDetails = users.get(username);
        if (userDetails == null || !passwordEncoder.matches(rawPassword, userDetails.getPassword())) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户名或密码错误");
        }
        return userDetails;
    }
}
