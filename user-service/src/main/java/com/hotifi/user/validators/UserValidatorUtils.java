package com.hotifi.user.validators;

import com.hotifi.authentication.entities.Authentication;
import com.hotifi.authentication.errors.codes.AuthenticationErrorCodes;
import com.hotifi.common.constants.BusinessConstants;
import com.hotifi.common.exception.ApplicationException;
import com.hotifi.user.entitiies.User;
import com.hotifi.user.errors.codes.UserErrorCodes;

public class UserValidatorUtils {
    
    public static boolean isAuthenticationStatusInvalid(Authentication authentication) {
        if (authentication == null)
            return true;
        if (!authentication.isEmailVerified())
            throw new ApplicationException(AuthenticationErrorCodes.EMAIL_NOT_VERIFIED);
        if (!authentication.isPhoneVerified())
            throw new ApplicationException(AuthenticationErrorCodes.PHONE_ALREADY_EXISTS);
        if (!authentication.isActivated())
            throw new ApplicationException(UserErrorCodes.USER_NOT_ACTIVATED);
        if (authentication.isDeleted())
            throw new ApplicationException(UserErrorCodes.USER_DELETED);
        return false;
    }

    public static boolean isUserInvalid(User user, Authentication authentication) {
        if (user == null)
            return true;
        if (!authentication.isActivated())
            throw new ApplicationException(UserErrorCodes.USER_NOT_ACTIVATED);
        if (authentication.isDeleted())
            throw new ApplicationException(UserErrorCodes.USER_DELETED);
        //login check not required because if user has been created then phone and email has been already verified
        return false;
    }

    //Check if seller is deleted / activated / has linked account id
    public static boolean isSellerValid(User seller, Authentication authentication, boolean isLinkedAccountIdMandatory) {
        if (seller == null)
            return false;
        if (authentication.isDeleted())
            throw new ApplicationException(UserErrorCodes.USER_DELETED);
        if (!seller.isLoggedIn())
            throw new ApplicationException(UserErrorCodes.USER_NOT_LOGGED_IN);
        if (seller.getBankAccountId() == null && isLinkedAccountIdMandatory)
            throw new ApplicationException(UserErrorCodes.USER_LINKED_ACCOUNT_ID_NULL);
        return true;
    }

    //Check if seller is deleted / activated / has upi id
    public static boolean isSellerUPILegit(User seller, Authentication authentication, boolean isUpiIdMandatory) {
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
    
}
