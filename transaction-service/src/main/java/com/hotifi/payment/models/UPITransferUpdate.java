package com.hotifi.payment.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class UPITransferUpdate {

    private Long sellerId;

    private Date paidAt;

    private String utr;

    private String upiTransactionId;

    private int status;

    private String errorDescription;

}
