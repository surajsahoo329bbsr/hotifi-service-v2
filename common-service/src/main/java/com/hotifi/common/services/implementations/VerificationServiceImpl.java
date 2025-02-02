package com.hotifi.common.services.implementations;

import com.google.firebase.auth.FirebaseAuthException;
import com.hotifi.common.constants.codes.CloudClientCodes;
import com.hotifi.common.constants.codes.SocialCodes;
import com.hotifi.common.constants.social.FacebookProcessor;
import com.hotifi.common.constants.social.GoogleProcessor;
import com.hotifi.common.exception.ApplicationException;
import com.hotifi.common.exception.errors.ErrorCodes;
import com.hotifi.common.services.interfaces.IVerificationService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class VerificationServiceImpl implements IVerificationService {

    private final GoogleProcessor googleProcessor;
    private final FacebookProcessor facebookProcessor;

    public VerificationServiceImpl() {
        googleProcessor = new GoogleProcessor();
        facebookProcessor = new FacebookProcessor();
    }

    @Override
    public boolean isSocialUserVerified(String email, String identifier, String token, SocialCodes socialCode) {
        /*switch (socialCode) {
            case GOOGLE:
                try {
                    return googleProcessor.verifyEmail(email, token);
                } catch (FirebaseAuthException e) {
                    log.error("Error occurred ", e);
                    throw new ApplicationException(ErrorCodes.FIREBASE_AUTH_EXCEPTION);
                }
            case FACEBOOK:
                return facebookProcessor.verifyEmail(identifier, token);
        }*/
        return true;
    }

    @Override
    public boolean isPhoneUserVerified(String countryCode, String phone, String token, CloudClientCodes cloudClientCodes) {
        /*switch (cloudClientCodes) {
            case GOOGLE_CLOUD_PLATFORM:
                try {
                    return googleProcessor.verifyPhone(countryCode, phone, token);
                } catch (Exception e) {
                    log.error("Error occurred ", e);
                    throw new ApplicationException(ErrorCodes.FIREBASE_AUTH_EXCEPTION);
                }
            case AMAZON_WEB_SERVICES:
            case AZURE:
            case TWILIO:
        }*/
        return true;
    }

}
