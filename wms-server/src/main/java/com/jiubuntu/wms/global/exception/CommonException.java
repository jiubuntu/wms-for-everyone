package com.jiubuntu.wms.global.exception;


import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommonException extends RuntimeException{
    private final ErrorCode errorCode;
    private final String message;

    public CommonException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.message = null;
    }
}
