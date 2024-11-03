package com.hotifi.payment.validators;

import com.hotifi.authentication.entities.Authentication;
import com.hotifi.common.exception.ApplicationException;
import com.hotifi.payment.entities.Purchase;
import com.hotifi.payment.errors.PurchaseErrorCodes;
import com.hotifi.payment.errors.SellerBankAccountErrorCodes;
import com.hotifi.user.entitiies.User;
import com.hotifi.user.errors.codes.UserErrorCodes;

public class PaymentValidatorUtils {

    //Check if buyer is logged in / deleted / activated / freezed / banned / has upi id
    //TODO add this in Transaction-Module
    public static boolean isBuyerValid(User buyer, Authentication authentication) {
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



    public static boolean isSellerLegitByAdmin(User seller, Authentication authentication, String linkedAccountId, String errorDescription) {
        if (seller == null)
            return false;
        if (authentication.isDeleted())
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
    }
}
