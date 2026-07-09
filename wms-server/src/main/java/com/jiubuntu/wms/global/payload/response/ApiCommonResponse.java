package com.jiubuntu.wms.global.payload.response;

import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import com.jiubuntu.wms.global.payload.constants.ResponseCode;
import lombok.Getter;

@Getter
public class ApiCommonResponse<T> {

    private final int httpCode;
    private final String message;
    private final T data;

    private ApiCommonResponse(int httpCode, String message, T data) {
        this.httpCode = httpCode;
        this.message = message;
        this.data = data;
    }

    public static<T> ApiCommonResponse<T> success(T data) {
        return new ApiCommonResponse<>(
                ResponseCode.SUCCESS.getHttpCode(),
                ResponseCode.SUCCESS.getMessage(),
                data
        );
    }

    public static<T> ApiCommonResponse<T> success(T data, ResponseCode responseCode) {
        return new ApiCommonResponse<>(
                responseCode.getHttpCode(),
                responseCode.getMessage(),
                data
        );
    }

    public static<T> ApiCommonResponse<T> error(ResponseCode responseCode) {
        return new ApiCommonResponse<>(
                responseCode.getHttpCode(),
                responseCode.getMessage(),
                null
        );
    }

    public static<T> ApiCommonResponse<T> error(ErrorCode errorCode) {
        return new ApiCommonResponse<>(
                errorCode.getHttpCode(),
                errorCode.getMessage(),
                null
        );
    }

}
