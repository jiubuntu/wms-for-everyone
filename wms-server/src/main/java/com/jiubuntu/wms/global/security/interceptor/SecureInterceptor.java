package com.jiubuntu.wms.global.security.interceptor;

import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import com.jiubuntu.wms.global.security.authentication.AuthPrincipal;
import com.jiubuntu.wms.global.security.resolver.annotation.Secure;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;

public class SecureInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        Secure secure = handlerMethod.getMethodAnnotation(Secure.class);
        if (secure == null) {
            secure = handlerMethod.getBeanType().getAnnotation(Secure.class);
        }
        if (secure == null || secure.value().length == 0) {
            return true;
        }

        AuthPrincipal principal = (AuthPrincipal) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (Arrays.stream(secure.value()).noneMatch(role -> role == principal.getRole())) {
            throw new CommonException(ErrorCode.FORBIDDEN);
        }

        return true;
    }

}
