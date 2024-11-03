package com.hotifi.payment.web.responses;

import com.hotifi.payment.entities.Purchase;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefundReceiptResponse {

    public Purchase purchase;

    public String hotifiBankAccount;

}
