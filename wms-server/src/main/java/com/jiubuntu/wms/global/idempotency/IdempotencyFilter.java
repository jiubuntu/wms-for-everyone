package com.jiubuntu.wms.global.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import com.jiubuntu.wms.global.infrastructure.IdempotencyCheckResult;
import com.jiubuntu.wms.global.infrastructure.IdempotencyCheckStatus;
import com.jiubuntu.wms.global.infrastructure.IdempotencyKeyStore;
import com.jiubuntu.wms.global.payload.response.ApiCommonResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RequiredArgsConstructor
public class IdempotencyFilter extends OncePerRequestFilter {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final RequestMappingHandlerMapping handlerMapping;
    private final IdempotencyKeyStore idempotencyKeyStore;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        Idempotent idempotent = resolveAnnotation(request);
        if (idempotent == null) {
            chain.doFilter(request, response);
            return;
        }

        String idempotencyKey = request.getHeader(IDEMPOTENCY_KEY_HEADER);
        if (!StringUtils.hasText(idempotencyKey)) {
            writeError(response, ErrorCode.IDEMPOTENCY_KEY_REQUIRED);
            return;
        }

        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        String requestHash = sha256Hex(cachedRequest.getCachedBody());

        IdempotencyCheckResult check = idempotencyKeyStore.check(idempotent.value(), idempotencyKey, requestHash);
        if (check.getStatus() == IdempotencyCheckStatus.HASH_MISMATCH) {
            writeError(response, ErrorCode.IDEMPOTENCY_REQUEST_MISMATCH);
            return;
        }
        if (check.getStatus() == IdempotencyCheckStatus.IN_PROGRESS) {
            writeError(response, ErrorCode.IDEMPOTENCY_REQUEST_IN_PROGRESS);
            return;
        }
        if (check.getStatus() == IdempotencyCheckStatus.COMPLETED) {
            writeCached(response, check);
            return;
        }

        ContentCachingResponseWrapper cachedResponse = new ContentCachingResponseWrapper(response);
        try {
            chain.doFilter(cachedRequest, cachedResponse);
            String responseBody = new String(cachedResponse.getContentAsByteArray(), StandardCharsets.UTF_8);
            if (cachedResponse.getStatus() < 400) {
                idempotencyKeyStore.complete(idempotent.value(), idempotencyKey, requestHash, cachedResponse.getStatus(), responseBody);
            } else {
                idempotencyKeyStore.fail(idempotent.value(), idempotencyKey, requestHash);
            }
        } finally {
            cachedResponse.copyBodyToResponse();
        }
    }

    private Idempotent resolveAnnotation(HttpServletRequest request) {
        try {
            HandlerExecutionChain chain = handlerMapping.getHandler(request);
            if (chain != null && chain.getHandler() instanceof HandlerMethod handlerMethod) {
                return handlerMethod.getMethodAnnotation(Idempotent.class);
            }
        } catch (Exception ignored) {
            // 핸들러를 찾지 못하면(예: 잘못된 경로) 멱등성 검사 없이 그대로 흘려보내고,
            // 실제 라우팅 단계에서 404 등으로 정상 처리되도록 한다.
        }
        return null;
    }

    private void writeCached(HttpServletResponse response, IdempotencyCheckResult check) throws IOException {
        response.setStatus(check.getCachedHttpStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(check.getCachedResponseBody());
    }

    private void writeError(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getHttpCode());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(ApiCommonResponse.error(errorCode)));
    }

    private String sha256Hex(byte[] data) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(data);
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

}
