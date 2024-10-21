package com.hotifi.common.services.interfaces;

import com.hotifi.common.dto.UserEventDTO;

public interface IEmailService {

    void sendAccountDeletedEmail(UserEventDTO userEventDTO);

    void sendAccountFrozenEmail(UserEventDTO userEventDTO);

    void sendBuyerBannedEmail(UserEventDTO userEventDTO);

    void sendEmailOtpEmail(String toEmailAddress, String emailOtp);

    void sendLinkedAccountFailed(UserEventDTO userEventDTO);

    void sendLinkedAccountSuccessEmail(UserEventDTO userEventDTO);

    void sendWelcomeEmail(UserEventDTO userEventDTO);

}
