package com.jiubuntu.wms.global.payload.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum ResponseCode {

    SUCCESS(HttpStatus.OK.value(), "Success"),
    CREATED(HttpStatus.CREATED.value(), "Created"),
    INVALID_INPUT(HttpStatus.BAD_REQUEST.value(), "Invalid Input"),
    NOT_FOUND(HttpStatus.NOT_FOUND.value(), "Not Found"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN.value(), "Forbidden"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(),"Internal Server Error");



    private final int HttpCode;
    private final String message;
}
