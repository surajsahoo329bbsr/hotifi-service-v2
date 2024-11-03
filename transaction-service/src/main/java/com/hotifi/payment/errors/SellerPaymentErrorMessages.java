package com.hotifi.payment.errors;

import com.hotifi.common.constants.BusinessConstants;

public class SellerPaymentErrorMessages {
    public static final String UNEXPECTED_SELLER_PAYMENT_ERROR = "Unexpected seller payment error";
    public static final String UNEXPECTED_SELLER_RECEIPT_RESPONSE = "Unexpected seller receipt response";
    public static final String SELLER_PAYMENT_NOT_FOUND = "No seller payment exists";
    public static final String SELLER_NOT_LEGIT = "Seller not legit";
    public static final String WITHDRAW_AMOUNT_PERIOD_ERROR = "Withdraw amount period is not over";
    public static final String MINIMUM_WITHDRAWAL_AMOUNT_ERROR = "Minimum withdrawal is " + BusinessConstants.MINIMUM_WITHDRAWAL_AMOUNT;
    //public static final String MINIMUM_AMOUNT_INR = "Minimum withdrawal amount must be at least INR " + Constants.MINIMUM_AMOUNT_INR;
    public static final String SELLER_RECEIPT_NOT_FOUND = "Seller receipt not found";
    public static final String NO_PENDING_TRANSFERS_CLAIMED = "Seller did not claim pending transfers";
    public static final String SELLER_PAYMENT_UPI_FAILED = "Seller Payment UPI Failed";
}
