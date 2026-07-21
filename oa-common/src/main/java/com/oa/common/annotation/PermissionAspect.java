package com.oa.common.annotation;

import com.oa.common.exception.BusinessException;
import com.oa.common.result.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
public class PermissionAspect {

    private final HttpServletRequest request;

    public PermissionAspect(HttpServletRequest request) {
        this.request = request;
    }

    @Before("@annotation(requiresRole)")
    public void checkRole(RequiresRole requiresRole) {
        String[] requiredRoles = requiresRole.value();
        if (requiredRoles.length == 0) return;

        @SuppressWarnings("unchecked")
        List<String> userRoles = (List<String>) request.getAttribute("roles");
        if (userRoles == null || userRoles.isEmpty()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if (requiresRole.matchAll()) {
            for (String role : requiredRoles) {
                if (!userRoles.contains(role)) {
                    throw new BusinessException(ResultCode.FORBIDDEN, "需要角色: " + role);
                }
            }
        } else {
            for (String role : requiredRoles) {
                if (userRoles.contains(role)) return;
            }
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }

    @Before("@annotation(requiresPermission)")
    public void checkPermission(RequiresPermission requiresPermission) {
        String[] requiredPerms = requiresPermission.value();
        if (requiredPerms.length == 0) return;

        @SuppressWarnings("unchecked")
        List<String> userPermissions = (List<String>) request.getAttribute("permissions");
        if (userPermissions == null || userPermissions.isEmpty()) {
            throw new BusinessException(ResultCode.FORBIDDEN);
        }

        if (requiresPermission.matchAll()) {
            for (String perm : requiredPerms) {
                if (!userPermissions.contains(perm)) {
                    throw new BusinessException(ResultCode.FORBIDDEN, "需要权限: " + perm);
                }
            }
        } else {
            for (String perm : requiredPerms) {
                if (userPermissions.contains(perm)) return;
            }
            throw new BusinessException(ResultCode.FORBIDDEN);
        }
    }
}
