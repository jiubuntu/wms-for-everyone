package com.jiubuntu.wms.global.exception.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    BAD_REQUEST(HttpStatus.BAD_REQUEST.value(), "0001", "요청 파라미터가 올바르지 않습니다."),

    ;

    private final int httpCode;
    private final String code;
    private final String message;


}
