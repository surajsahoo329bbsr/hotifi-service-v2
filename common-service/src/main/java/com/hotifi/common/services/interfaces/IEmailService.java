package com.hotifi.common.services.interfaces;

import com.hotifi.common.models.EmailModel;

public interface IEmailService {

    void sendAccountDeletedEmail(Long userId, String firstName);

    void sendAccountFrozenEmail(Long userId, String firstName);

    void sendBuyerBannedEmail(Long userId, String firstName);

    void sendEmailOtpEmail(EmailModel emailModel);

    void sendLinkedAccountFailed(Long userId, String firstName, String errorDescription);

    void sendLinkedAccountSuccessEmail(Long userId, String firstName);

    void sendWelcomeEmail(Long userId, String firstName);

}