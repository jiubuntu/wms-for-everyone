package com.jiubuntu.wms.global.payload.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum ResponseCode {

    SUCCESS(HttpStatus.OK.value(), "요청이 성공적으로 처리되었습니다."),
    CREATED(HttpStatus.CREATED.value(), "정상적으로 생성되었습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST.value(), "요청 값이 올바르지 않습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND.value(), "요청한 리소스를 찾을 수 없습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN.value(), "접근 권한이 없습니다."),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");



    private final int HttpCode;
    private final String message;
}
