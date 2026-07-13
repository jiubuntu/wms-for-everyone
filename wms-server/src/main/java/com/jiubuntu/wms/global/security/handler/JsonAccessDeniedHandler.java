package com.jiubuntu.wms.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiubuntu.wms.global.payload.constants.ResponseCode;
import com.jiubuntu.wms.global.payload.response.ApiCommonResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JsonAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        ApiCommonResponse<Void> body = ApiCommonResponse.error(ResponseCode.FORBIDDEN);

        response.setStatus(ResponseCode.FORBIDDEN.getHttpCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

}
