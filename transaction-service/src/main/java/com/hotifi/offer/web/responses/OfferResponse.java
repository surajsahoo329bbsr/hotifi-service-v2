package com.hotifi.offer.web.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
public class OfferResponse {

    private String name;

    private String description;

    private String promoCode;

    private int discountPercentage;

    private int maximumDiscountAmount;

    private int minimumReferrals;

    private String offerType;

    private String terms;

    private Date startsAt;

    private Date expiresAt;

}
