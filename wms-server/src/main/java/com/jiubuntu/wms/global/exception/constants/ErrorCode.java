package com.jiubuntu.wms.global.exception.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), "0001", "요청 파라미터가 올바르지 않습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "0002", "파일 업로드에 실패했습니다."),

    EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT.value(), "0101", "이미 가입된 이메일입니다."),
    BUSINESS_NUMBER_ALREADY_EXISTS(HttpStatus.CONFLICT.value(), "0102", "이미 등록된 사업자등록번호입니다."),
    PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST.value(), "0103", "비밀번호가 일치하지 않습니다."),
    UNSUPPORTED_FILE_TYPE(HttpStatus.BAD_REQUEST.value(), "0104", "지원하지 않는 파일 형식입니다."),
    FILE_TOO_LARGE(HttpStatus.BAD_REQUEST.value(), "0105", "파일 용량이 너무 큽니다."),

    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED.value(), "0201", "이메일 또는 비밀번호가 올바르지 않습니다."),
    ACCOUNT_LOCKED(HttpStatus.UNAUTHORIZED.value(), "0202", "계정이 잠겨 있습니다. 잠시 후 다시 시도해주세요."),
    ACCOUNT_NOT_ACTIVE(HttpStatus.FORBIDDEN.value(), "0203", "아직 사용할 수 없는 계정입니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED.value(), "0204", "유효하지 않은 리프레시 토큰입니다."),
    EXPIRED_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED.value(), "0205", "만료된 리프레시 토큰입니다."),

    ;

    private final int httpCode;
    private final String code;
    private final String message;


}
