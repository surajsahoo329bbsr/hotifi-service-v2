package com.hotifi.payment.web.responses;

import com.hotifi.payment.entities.BankAccount;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BankAccountAdminResponse {

    private final BankAccount bankAccount;

    private final Long userId;

    private final String email;

}
