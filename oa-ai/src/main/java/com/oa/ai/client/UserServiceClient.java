package com.oa.ai.client;

import com.oa.ai.dto.UserInfoDTO;
import com.oa.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

/** 通过 Nacos 服务发现调用 oa-user 服务获取用户信息 */
@FeignClient(name = "oa-user-service")
public interface UserServiceClient {

    @GetMapping("/api/user/current")
    Result<UserInfoDTO> getCurrentUser(@RequestHeader("Authorization") String authHeader);
}
