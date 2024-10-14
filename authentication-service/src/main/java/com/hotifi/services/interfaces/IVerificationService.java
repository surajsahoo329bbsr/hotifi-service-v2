package com.hotifi.services.interfaces;


import com.hotifi.constants.codes.CloudClientCodes;
import com.hotifi.constants.codes.SocialCodes;

public interface IVerificationService {

    boolean isSocialUserVerified(String email, String identifier, String token, SocialCodes socialCode);

    boolean isPhoneUserVerified(String countryCode, String phone, String token, CloudClientCodes cloudClientCodes);

}
