package com.hotifi.offer.services.implementations;

import com.hotifi.authentication.entities.Authentication;
import com.hotifi.authentication.errors.codes.AuthenticationErrorCodes;
import com.hotifi.authentication.repositories.AuthenticationRepository;
import com.hotifi.common.exception.ApplicationException;
import com.hotifi.offer.entities.Offer;
import com.hotifi.offer.errors.codes.OfferErrorCodes;
import com.hotifi.offer.repositories.OfferRepository;
import com.hotifi.offer.services.interfaces.IOfferService;
import com.hotifi.offer.web.requests.OfferRequest;
import com.hotifi.offer.web.responses.OfferResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class OfferServiceImpl implements IOfferService {

    private final AuthenticationRepository authenticationRepository;
    private final OfferRepository offerRepository;

    public OfferServiceImpl(AuthenticationRepository authenticationRepository, OfferRepository offerRepository) {
        this.authenticationRepository = authenticationRepository;
        this.offerRepository = offerRepository;
    }

    @Override
    @Transactional
    public void addOffer(OfferRequest offerRequest, String adminEmail) {
        Authentication authentication = authenticationRepository.findByEmail(adminEmail);
        if (authentication == null) throw new ApplicationException(AuthenticationErrorCodes.EMAIL_NOT_FOUND);

        try {
            Offer offer = new Offer();
            offer.setName(offerRequest.getName());
            offer.setDescription(offerRequest.getDescription());
            offer.setPromoCode(offerRequest.getPromoCode());
            offer.setPriceBudget(offerRequest.getPriceBudget());
            offer.setDiscountPercentage(offer.getDiscountPercentage());
            offer.setCommissionPercentage(offerRequest.getCommissionPercentage());
            offer.setMaximumDiscountAmount(offerRequest.getMaximumDiscountAmount());
            offer.setMinimumReferrals(offerRequest.getMinimumReferrals());
            offer.setExpiresAt(offerRequest.getExpiresAt());
            offer.setTerms(offerRequest.getTerms());
            offer.setCreatedBy(authentication);
            offerRepository.save(offer);
        } catch (Exception e) {
            throw new ApplicationException(OfferErrorCodes.UNEXPECTED_OFFER_ERROR);
        }

    }

    @Override
    @Transactional
    public void updateOffer(OfferRequest offerRequest, String adminEmail, Long offerId) {
        Authentication authentication = authenticationRepository.findByEmail(adminEmail);
        if (authentication == null) throw new ApplicationException(AuthenticationErrorCodes.EMAIL_NOT_FOUND);

        Offer offer = offerRepository.findById(offerId).orElse(null);
        if (offer == null) throw new ApplicationException(OfferErrorCodes.OFFER_NOT_FOUND);
        if (offer.isActive()) throw new ApplicationException(OfferErrorCodes.ACTIVATE_OFFER_UPDATE_ERROR);

        try {
            offer.setName(offerRequest.getName());
            offer.setDescription(offerRequest.getDescription());
            offer.setPromoCode(offerRequest.getPromoCode());
            offer.setPriceBudget(offerRequest.getPriceBudget());
            offer.setDiscountPercentage(offer.getDiscountPercentage());
            offer.setCommissionPercentage(offerRequest.getCommissionPercentage());
            offer.setMaximumDiscountAmount(offerRequest.getMaximumDiscountAmount());
            offer.setMinimumReferrals(offerRequest.getMinimumReferrals());
            offer.setExpiresAt(offerRequest.getExpiresAt());
            offer.setTerms(offerRequest.getTerms());
            offer.setCreatedBy(authentication);
            //After updating above values from offer
            Date modifiedAt = new Date(System.currentTimeMillis());
            offer.setModifiedBy(authentication);
            offer.setModifiedAt(modifiedAt);
            offerRepository.save(offer);
        } catch (Exception e) {
            throw new ApplicationException(OfferErrorCodes.UNEXPECTED_OFFER_ERROR);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Offer> findAllOffers(int page, int size, boolean isDescending) {
        try {
            Pageable pageable = isDescending ?
                    PageRequest.of(page, size, Sort.by("created_at").descending()) :
                    PageRequest.of(page, size, Sort.by("created_at"));
            return offerRepository.findAll(pageable).toList();
        } catch (Exception e) {
            log.error("Error occurred ", e);
            throw new ApplicationException(OfferErrorCodes.UNEXPECTED_OFFER_ERROR);
        }
    }

    @Override
    public List<OfferResponse> findAllActiveOffers() {
        try {
            Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by("created_at").descending());
            Date now = new Date(System.currentTimeMillis());
            return offerRepository
                    .findAll(pageable).toList()
                    .stream().filter(offer -> offer.isActive() && offer.getExpiresAt().compareTo(now) < 0)
                    .map(offer -> new OfferResponse(offer.getName(), offer.getDescription(),
                            offer.getPromoCode(), offer.getDiscountPercentage(), offer.getMaximumDiscountAmount(),
                            offer.getMinimumReferrals(), offer.getOfferType(), offer.getTerms(),
                            offer.getStartsAt(), offer.getExpiresAt()))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error occurred ", e);
            throw new ApplicationException(OfferErrorCodes.UNEXPECTED_OFFER_ERROR);
        }
    }

    @Override
    @Transactional
    public void activateOffer(Long offerId, String adminEmail) {
        Authentication authentication = authenticationRepository.findByEmail(adminEmail);
        if (authentication == null) throw new ApplicationException(AuthenticationErrorCodes.EMAIL_NOT_FOUND);

        Offer offer = offerRepository.findById(offerId).orElse(null);
        Date now = new Date(System.currentTimeMillis());

        if (offer == null) throw new ApplicationException(OfferErrorCodes.OFFER_NOT_FOUND);
        if (offer.isActive() || offer.getStartsAt() != null)
            throw new ApplicationException(OfferErrorCodes.ACTIVATE_OFFER_UPDATE_ERROR);
        if (offer.getExpiresAt() == null || offer.getExpiresAt().compareTo(now) >= 0)
            throw new ApplicationException(OfferErrorCodes.OFFER_ACTIVATION_AFTER_EXPIRY);

        try {
            offer.setActive(true);
            offer.setStartsAt(now);
            offer.setModifiedBy(authentication);
            offer.setModifiedAt(now);
            offerRepository.save(offer);
        } catch (Exception e) {
            log.error("Error occurred ", e);
            throw new ApplicationException(OfferErrorCodes.UNEXPECTED_OFFER_ERROR);
        }
    }

    @Override
    public void verifyOffer(Long userId, String offerCode) {

    }

    @Override
    @Transactional
    public void deactivateOfferBeforeExpiry(Long offerId, String adminEmail, String deactivateBeforeExpiryReason) {
        Authentication authentication = authenticationRepository.findByEmail(adminEmail);
        if (authentication == null) throw new ApplicationException(AuthenticationErrorCodes.EMAIL_NOT_FOUND);

        Offer offer = offerRepository.findById(offerId).orElse(null);
        Date now = new Date(System.currentTimeMillis());

        if (offer == null) throw new ApplicationException(OfferErrorCodes.OFFER_NOT_FOUND);
        if (!offer.isActive())
            throw new ApplicationException(OfferErrorCodes.OFFER_ALREADY_DEACTIVATED);

        try {
            offer.setActive(false);
            offer.setDeactivateBeforeExpiryReason(deactivateBeforeExpiryReason);
            offer.setModifiedBy(authentication);
            offer.setModifiedAt(now);
            offerRepository.save(offer);
        } catch (Exception e) {
            log.error("Error occurred ", e);
            throw new ApplicationException(OfferErrorCodes.UNEXPECTED_OFFER_ERROR);
        }
    }
}
