package com.hotifi.payment.processor.codes;

import com.hotifi.common.constants.BusinessConstants;

import java.util.Map;
import java.util.TreeMap;
import java.util.stream.IntStream;

/**
 * For Razorpay Method Codes
 *  upi
 *  netbanking
 *  wallet
 *  card
 *  emi
 */

public enum PaymentMethodCodes {

    UPI,
    NETBANKING,
    WALLET,
    CARD,
    EMI;

    private static final Map<Integer, PaymentMethodCodes> paymentMethods = new TreeMap<>();

    private static final int START_VALUE = BusinessConstants.PAYMENT_METHOD_START_VALUE_CODE;

    static {
        IntStream.range(0, values().length).forEach(i -> {
            values()[i].value = START_VALUE + i * BusinessConstants.PAYMENT_METHOD_START_VALUE_CODE;
            paymentMethods.put(values()[i].value, values()[i]);
        });
    }

    private int value;

    public static PaymentMethodCodes fromInt(int i) {
        return paymentMethods.get(i);
    }

    public int value() {
        return value;
    }

}
