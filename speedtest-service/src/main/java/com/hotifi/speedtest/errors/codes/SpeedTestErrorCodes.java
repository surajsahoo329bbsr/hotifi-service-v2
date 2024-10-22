package com.hotifi.speedtest.errors.codes;

import com.hotifi.common.exception.errors.ErrorCode;
import com.hotifi.common.exception.errors.ErrorCodes;
import com.hotifi.speedtest.errors.messages.SpeedTestErrorMessages;

import java.util.Collections;

public class SpeedTestErrorCodes extends ErrorCodes {
    //Internal Error Codes
    public static ErrorCode UNEXPECTED_SPEED_TEST_ERROR = new ErrorCode("00", Collections.singletonList(getMessage("UNEXPECTED_SPEED_TEST_ERROR", SpeedTestErrorMessages.UNEXPECTED_SPEED_TEST_ERROR)), 500);
    public static ErrorCode SPEED_TEST_RECORD_NOT_FOUND = new ErrorCode("01", Collections.singletonList(getMessage("SPEED_TEST_RECORD_NOT_FOUND", SpeedTestErrorMessages.SPEED_TEST_RECORD_NOT_FOUND)), 500);
    public static ErrorCode SPEED_TEST_INVALID_WIFI_RECORD = new ErrorCode("02", Collections.singletonList(getMessage("SPEED_TEST_INVALID_WIFI_RECORD", SpeedTestErrorMessages.SPEED_TEST_INVALID_WIFI_RECORD)), 500);

}
