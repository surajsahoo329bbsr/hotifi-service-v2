package com.hotifi.user.services.interfaces;

import com.hotifi.common.constants.codes.SocialCodes;
import com.hotifi.common.dto.UserRegistrationEventDTO;
import com.hotifi.user.entitiies.User;
import com.hotifi.user.web.request.UserRequest;
import com.hotifi.user.web.response.CredentialsResponse;
import com.hotifi.user.web.response.FacebookDeletionResponse;
import com.hotifi.user.web.response.FacebookDeletionStatusResponse;

public interface IUserService {

    UserRegistrationEventDTO addUser(UserRequest userRequest);

    CredentialsResponse resetPassword(String email, String emailOtp, String identifier, String token, SocialCodes socialCode);

    User getUserByUsername(String username);

    void sendEmailOtpLogin(String email);

    boolean isUsernameAvailable(String username);

    void resendEmailOtpLogin(String email);

    void verifyEmailOtpLogin(String email, String emailOtp);

    void updateUser(UserRequest userRequest);

    void updateUserLogin(String email, boolean isLogin);

    User getUserByEmail(String email);

    FacebookDeletionResponse deleteFacebookUserData(String signedRequest);

    FacebookDeletionStatusResponse getFacebookDeletionStatus(String facebookId, String confirmationCode);

}
