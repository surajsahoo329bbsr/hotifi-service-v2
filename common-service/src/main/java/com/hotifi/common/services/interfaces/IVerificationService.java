package com.hotifi.common.services.interfaces;


import com.hotifi.common.constants.codes.CloudClientCodes;
import com.hotifi.common.constants.codes.SocialCodes;

public interface IVerificationService {

    boolean isSocialUserVerified(String email, String identifier, String token, SocialCodes socialCode);

    boolean isPhoneUserVerified(String countryCode, String phone, String token, CloudClientCodes cloudClientCodes);

}
