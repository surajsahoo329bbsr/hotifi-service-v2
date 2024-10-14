package com.hotifi.exception;

import com.hotifi.exception.errors.ErrorCode;
import com.hotifi.exception.errors.ErrorCodes;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppException extends RuntimeException {

    @Getter
    private final ErrorCode errorCode;
    private String errorMessage;

    public AppException(ErrorCode errorCode, Throwable throwable) {
        super(String.join(",", errorCode.getMessages()), throwable);
        this.errorCode = errorCode;
    }

    public AppException(ErrorCode errorCode) {
        super(String.join(",", errorCode.getMessages()));
        this.errorCode = errorCode;
    }

    public static AppException wrap(Throwable ex) {
        log.error("Error:", ex);
        return ex instanceof AppException ? (AppException) ex : new AppException(ErrorCodes.INTERNAL_ERROR);
    }

}
