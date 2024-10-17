package com.hotifi.user.services.interfaces;


import com.hotifi.user.entitiies.User;
import com.hotifi.common.models.EmailModel;

public interface IEmailService {

    void sendAccountDeletedEmail(User user, EmailModel emailModel);

    void sendAccountFrozenEmail(User user, EmailModel emailModel);

    void sendBuyerBannedEmail(User user, EmailModel emailModel);

    void sendEmailOtpEmail(EmailModel emailModel);

    void sendLinkedAccountFailed(User user, String errorDescription, EmailModel emailModel);

    void sendLinkedAccountSuccessEmail(User user, EmailModel emailModel);

    void sendWelcomeEmail(User user, EmailModel emailModel);

}
