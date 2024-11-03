package com.hotifi.payment.processor.razorpay.codes;

import com.hotifi.common.constants.BusinessConstants;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

public enum RefundStatusCodes {

    PENDING,
    FAILED,
    PROCESSED;

    private static final Map<Integer, RefundStatusCodes> refundStatusCodes = new TreeMap<>();

    private static final int START_VALUE = BusinessConstants.RAZORPAY_REFUND_STATUS_START_VALUE_CODE;

    static {
        IntStream.range(0, values().length).forEach(i -> {
            values()[i].value = START_VALUE + i;
            refundStatusCodes.put(values()[i].value, values()[i]);
        });
    }

    private int value;

    public static RefundStatusCodes fromInt(int i) {
        return refundStatusCodes.get(i);
    }

    public int value() {
        return value;
    }
}
