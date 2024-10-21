package com.hotifi.session.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor
public class TransferUpdate {

    private Long sellerId;

    private Date paidAt;

    private String transferId;

    private String settlementId;

    private boolean onHold;

    private Date onHoldUntil;

}
