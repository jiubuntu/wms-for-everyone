package com.jiubuntu.wms.global.exception;

import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import com.jiubuntu.wms.global.payload.response.ApiCommonResponse;
import com.jiubuntu.wms.global.payload.constants.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CommonException.class)
    public ResponseEntity<ApiCommonResponse<Void>> handleCommonException(CommonException e) {
        ErrorCode errorCode = e.getErrorCode();
        log.warn("CommonException occurred: code={}, message={}", errorCode.getCode(), errorCode.getMessage(), e);

        return ResponseEntity
                .status(errorCode.getHttpCode())
                .body(ApiCommonResponse.error(errorCode));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiCommonResponse<Void>> handleException(Exception e) {
        log.error("Unhandled exception occurred", e);

        return ResponseEntity
                .status(ResponseCode.INTERNAL_ERROR.getHttpCode())
                .body(ApiCommonResponse.error(ResponseCode.INTERNAL_ERROR));
    }

}
