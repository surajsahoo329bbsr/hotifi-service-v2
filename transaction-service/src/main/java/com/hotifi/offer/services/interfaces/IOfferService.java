package com.hotifi.offer.services.interfaces;

import com.hotifi.offer.entities.Offer;
import com.hotifi.offer.web.requests.OfferRequest;
import com.hotifi.offer.web.responses.OfferResponse;

import java.util.List;

public interface IOfferService {

    void addOffer(OfferRequest offerRequest, String adminEmail);

    void updateOffer(OfferRequest offerRequest, String adminEmail, Long offerId);

    List<Offer> findAllOffers(int page, int size, boolean isDescending);

    List<OfferResponse> findAllActiveOffers();

    void activateOffer(Long offerId, String adminEmail);

    void verifyOffer(Long userId, String offerCode);

    void deactivateOfferBeforeExpiry(Long offerId, String adminEmail, String deactivateBeforeExpiryReason);

}
