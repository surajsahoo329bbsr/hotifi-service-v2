package com.hotifi.payment.services.interfaces;

import com.hotifi.payment.entities.BankAccount;
import com.hotifi.payment.web.request.BankAccountRequest;
import com.hotifi.payment.web.responses.BankAccountAdminResponse;

import java.util.List;

public interface IBankAccountService {

    void addBankAccount(BankAccountRequest bankAccountRequest);

    void addUpiId(Long userId, String upiId);

    void updateBankAccountByCustomer(BankAccountRequest bankAccountRequest);

    void updateUpiIdByCustomer(Long userId, String upiId);

    void updateBankAccountByAdmin(Long sellerId, String linkedAccountId, String errorDescription);

    BankAccount getBankAccountByUserId(Long sellerId);

    List<BankAccountAdminResponse> getUnlinkedBankAccounts();

}
