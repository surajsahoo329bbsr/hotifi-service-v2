package com.hotifi.user.validators;

import com.hotifi.authentication.entities.Authentication;
import com.hotifi.authentication.errors.codes.AuthenticationErrorCodes;
import com.hotifi.common.exception.ApplicationException;
import com.hotifi.user.entitiies.User;
import com.hotifi.user.errors.codes.UserErrorCodes;

public class UserStatusValidator {
    
    public static boolean isAuthenticationLegit(Authentication authentication) {
        if (authentication == null)
            return false;
        if (!authentication.isEmailVerified())
            throw new ApplicationException(AuthenticationErrorCodes.EMAIL_NOT_VERIFIED);
        if (!authentication.isPhoneVerified())
            throw new ApplicationException(AuthenticationErrorCodes.PHONE_ALREADY_EXISTS);
        if (!authentication.isActivated())
            throw new ApplicationException(UserErrorCodes.USER_NOT_ACTIVATED);
        if (authentication.isDeleted())
            throw new ApplicationException(UserErrorCodes.USER_DELETED);
        return true;
    }

    public static boolean isUserLegit(User user, Authentication authentication) {
        if (user == null)
            return false;
        if (!authentication.isActivated())
            throw new ApplicationException(UserErrorCodes.USER_NOT_ACTIVATED);
        if (authentication.isDeleted())
            throw new ApplicationException(UserErrorCodes.USER_DELETED);
        //login check not required because if user has been created then phone and email has been already verified
        return true;
    }

    //Check if buyer is logged in / deleted / activated / freezed / banned / has upi id
    //TODO add this in Transaction-Module
    /*public static boolean isBuyerLegit(User buyer, Authentication authentication) {
        if (buyer == null)
            return false;
        if (!authentication.isActivated())
            throw new ApplicationException(UserErrorCodes.USER_NOT_ACTIVATED);
        if (authentication.isFrozen())
            throw new ApplicationException(UserErrorCodes.USER_FREEZED);
        if (authentication.isBanned())
            throw new ApplicationException(UserErrorCodes.USER_BANNED);
        if (authentication.isDeleted())
            throw new ApplicationException(UserErrorCodes.USER_DELETED);
        if (!buyer.isLoggedIn())
            throw new ApplicationException(UserErrorCodes.USER_NOT_LOGGED_IN);
        return true;
    }

    //Check if seller is deleted / activated / has upi id
    public static boolean isSellerUpiLegit(User seller, Authentication authentication, boolean isUpiIdMandatory) {
        if (seller == null)
            return false;
        if (authentication.isDeleted())
            throw new ApplicationException(UserErrorCodes.USER_DELETED);
        if (!seller.isLoggedIn())
            throw new ApplicationException(UserErrorCodes.USER_NOT_LOGGED_IN);
        if (seller.getUpiId() == null && isUpiIdMandatory)
            throw new ApplicationException(UserErrorCodes.USER_UPI_ID_NULL);
        if(isUpiIdMandatory && !seller.getUpiId().matches(BusinessConstants.VALID_UPI_PATTERN))
            throw new ApplicationException(UserErrorCodes.USER_UPI_ID_INVALID);
        return true;
    }

    //Check if seller is deleted / activated / has linked account id
    public static boolean isSellerLegit(User seller, Authentication authentication, boolean isLinkedAccountIdMandatory) {
        if (seller == null)
            return false;
        if (authentication.isDeleted())
            throw new ApplicationException(UserErrorCodes.USER_DELETED);
        if (!seller.isLoggedIn())
            throw new ApplicationException(UserErrorCodes.USER_NOT_LOGGED_IN);
        if (seller.getBankAccount().getLinkedAccountId() == null && isLinkedAccountIdMandatory)
            throw new ApplicationException(UserErrorCodes.USER_LINKED_ACCOUNT_ID_NULL);
        return true;
    }

    public static boolean isSellerLegitByAdmin(User seller, String linkedAccountId, String errorDescription) {
        if (seller == null)
            return false;
        if (seller.getAuthentication().isDeleted())
            throw new ApplicationException(UserErrorCodes.USER_DELETED);
        if (errorDescription != null && linkedAccountId != null)
            throw new ApplicationException(SellerBankAccountErrorCodes.ERROR_DESCRIPTION_ON_LINKED_ACCOUNT_NOT_FOUND);
        if (errorDescription == null && linkedAccountId == null)
            throw new ApplicationException(SellerBankAccountErrorCodes.ERROR_DESCRIPTION_ON_UNLINKED_ACCOUNT);
        return true;
    }

    public static boolean isPurchaseUpdateLegit(Purchase purchase, double dataUsed) {
        if (purchase == null)
            throw new ApplicationException(PurchaseErrorCodes.PURCHASE_NOT_FOUND);
        //if (purchase.getSessionCreatedAt() == null)
        //  throw new ApplicationException(PurchaseErrorCodes.BUYER_WIFI_SERVICE_NOT_STARTED);
        if (purchase.getSessionFinishedAt() != null)
            throw new ApplicationException(PurchaseErrorCodes.BUYER_WIFI_SERVICE_ALREADY_FINISHED);
        if (Double.compare(dataUsed, purchase.getData()) > 0)
            throw new ApplicationException(PurchaseErrorCodes.DATA_USED_EXCEEDS_DATA_BOUGHT);
        if (Double.compare(dataUsed, purchase.getDataUsed()) < 0)
            throw new ApplicationException(PurchaseErrorCodes.DATA_TO_UPDATE_DECEEDS_DATA_USED);
        return true;
    }*/
    
}
