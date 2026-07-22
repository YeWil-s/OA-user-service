package com.oa.common.context;

import com.oa.common.constant.UserHeaderConstants;
import com.oa.common.exception.BusinessException;
import com.oa.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.List;

public final class UserContextHolder {

    private UserContextHolder() {
    }

    public static CurrentUser getCurrentUser() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "缺少当前请求上下文");
        }
        return fromRequest(attributes.getRequest());
    }

    public static CurrentUser fromRequest(HttpServletRequest request) {
        CurrentUser currentUser = new CurrentUser();
        currentUser.setUserId(parseRequiredLong(request.getHeader(UserHeaderConstants.USER_ID_HEADER), UserHeaderConstants.USER_ID_HEADER));
        currentUser.setDeptId(parseOptionalLong(request.getHeader(UserHeaderConstants.DEPT_ID_HEADER), UserHeaderConstants.DEPT_ID_HEADER));
        currentUser.setUsername(request.getHeader(UserHeaderConstants.USERNAME_HEADER));
        currentUser.setRoles(splitHeader(request.getHeader(UserHeaderConstants.ROLES_HEADER)));
        currentUser.setPermissions(splitHeader(request.getHeader(UserHeaderConstants.PERMISSIONS_HEADER)));
        return currentUser;
    }

    public static Long getRequiredUserId() {
        return getCurrentUser().getUserId();
    }

    private static Long parseRequiredLong(String value, String headerName) {
        if (value == null || value.isBlank()) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "缺少用户身份请求头: " + headerName);
        }
        return parseOptionalLong(value, headerName);
    }

    private static Long parseOptionalLong(String value, String headerName) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new BusinessException(ResultCode.UNAUTHORIZED.getCode(), "用户身份请求头格式错误: " + headerName);
        }
    }

    private static List<String> splitHeader(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(item -> !item.isEmpty())
                .toList();
    }
}
