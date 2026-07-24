package com.oa.ai.config;

import com.oa.common.constant.UserHeaderConstants;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class FeignConfig {

    private static final String[] FORWARD_HEADERS = {
            UserHeaderConstants.USER_ID_HEADER,
            UserHeaderConstants.DEPT_ID_HEADER,
            UserHeaderConstants.USERNAME_HEADER,
            UserHeaderConstants.ROLES_HEADER,
            UserHeaderConstants.PERMISSIONS_HEADER
    };

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) -> {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                String auth = req.getHeader(HttpHeaders.AUTHORIZATION);
                if (auth != null) {
                    template.header(HttpHeaders.AUTHORIZATION, auth);
                }
                for (String header : FORWARD_HEADERS) {
                    String value = req.getHeader(header);
                    if (value != null) {
                        template.header(header, value);
                    }
                }
            }
        };
    }
}
