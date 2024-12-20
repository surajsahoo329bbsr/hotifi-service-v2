package com.hotifi.authentication.services.interfaces;

import com.hotifi.authentication.entities.Authentication;
import com.hotifi.authentication.web.response.CredentialsResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface
IAuthenticationService extends UserDetailsService {

    CredentialsResponse addEmail(String email, String identifier, String token, String socialClient);

    Authentication getAuthentication(String email);

    void resendEmailOtpSignUp(String email);

    void verifyEmailOtpSignUp(String email, String otp);

    void verifyPhone(String email, String countryCode, String phone, String token);

    boolean isPhoneAvailable(String phone);

    boolean isEmailAvailable(String email);

}
