package com.jiubuntu.wms.global.exception.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), "0001", "요청 파라미터가 올바르지 않습니다."),
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "0002", "파일 업로드에 실패했습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN.value(), "0003", "접근 권한이 없습니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "0004", "이메일 발송에 실패했습니다."),

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
    INVALID_PASSWORD_RESET_TOKEN(HttpStatus.UNAUTHORIZED.value(), "0206", "유효하지 않은 비밀번호 재설정 링크입니다."),
    EXPIRED_PASSWORD_RESET_TOKEN(HttpStatus.UNAUTHORIZED.value(), "0207", "만료된 비밀번호 재설정 링크입니다."),

    COMPANY_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "0301", "존재하지 않는 기업입니다."),
    COMPANY_FILE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "0302", "존재하지 않는 첨부파일입니다."),
    COMPANY_NOT_PENDING(HttpStatus.BAD_REQUEST.value(), "0303", "승인 대기 상태의 기업이 아닙니다."),

    USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "0401", "존재하지 않는 사용자입니다."),
    WAREHOUSE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "0402", "존재하지 않는 창고입니다."),
    COMPANY_SCOPE_VIOLATION(HttpStatus.FORBIDDEN.value(), "0404", "소속 기업 범위를 벗어난 요청입니다."),
    WAREHOUSE_SCOPE_VIOLATION(HttpStatus.FORBIDDEN.value(), "0405", "담당 창고 범위를 벗어난 요청입니다."),
    LAST_COMPANY_ADMIN_CANNOT_WITHDRAW(HttpStatus.BAD_REQUEST.value(), "0406", "기업 내 유일한 기업관리자는 탈퇴할 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST.value(), "0407", "현재 비밀번호가 일치하지 않습니다."),

    COMMON_CODE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "0501", "존재하지 않는 공통 코드입니다."),
    COMMON_CODE_ALREADY_EXISTS(HttpStatus.CONFLICT.value(), "0502", "이미 등록된 코드입니다."),
    COMMON_CODE_GROUP_NOT_CUSTOMIZABLE(HttpStatus.BAD_REQUEST.value(), "0503", "회사에서 직접 추가할 수 없는 코드 그룹입니다."),

    PRODUCT_UNIT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "0601", "존재하지 않는 상품 단위입니다."),
    PRODUCT_UNIT_ALREADY_EXISTS(HttpStatus.CONFLICT.value(), "0602", "이미 등록된 단위입니다."),

    ;

    private final int httpCode;
    private final String code;
    private final String message;


}
