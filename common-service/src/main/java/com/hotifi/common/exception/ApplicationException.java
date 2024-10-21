package com.hotifi.common.exception;

import com.hotifi.common.exception.errors.ErrorCode;
import com.hotifi.common.exception.errors.ErrorCodes;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ApplicationException extends RuntimeException {

    @Getter
    private final ErrorCode errorCode;
    private String errorMessage;

    public ApplicationException(ErrorCode errorCode, Throwable throwable) {
        super(String.join(",", errorCode.getMessages()), throwable);
        this.errorCode = errorCode;
    }

    public ApplicationException(ErrorCode errorCode) {
        super(String.join(",", errorCode.getMessages()));
        this.errorCode = errorCode;
    }

    public static ApplicationException wrap(Throwable throwable) {
        log.error("Error:", throwable);
        return throwable instanceof ApplicationException ? (ApplicationException) throwable : new ApplicationException(ErrorCodes.INTERNAL_ERROR);
    }

}
