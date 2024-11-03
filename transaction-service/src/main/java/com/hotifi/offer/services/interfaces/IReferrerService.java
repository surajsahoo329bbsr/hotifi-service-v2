package com.hotifi.offer.services.interfaces;

import com.hotifi.offer.entities.Referrer;

public interface IReferrerService {

    String addOrUpdateReferral(Long userId);

    void verifyReferral(String referrerCode);

    boolean isValidReferralUserId(Long userId);

    Referrer getReferral(Long referrerUserId);

}
