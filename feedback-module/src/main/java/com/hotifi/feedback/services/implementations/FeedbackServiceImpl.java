package com.hotifi.feedback.services.implementations;

import com.hotifi.common.exception.ApplicationException;
import com.hotifi.feedback.entities.Feedback;
import com.hotifi.feedback.error.codes.FeedbackErrorCodes;
import com.hotifi.feedback.repositories.FeedbackRepository;
import com.hotifi.feedback.services.interfaces.IFeedbackService;
import com.hotifi.feedback.web.request.FeedbackRequest;
import com.hotifi.feedback.web.responses.FeedbackResponse;
import com.hotifi.session.repositories.SessionRepository;
import com.hotifi.payment.entities.Purchase;
import com.hotifi.payment.repositories.PurchaseRepository;
import com.hotifi.payment.web.responses.SellerReviewsResponse;
import com.hotifi.user.entitiies.User;
import com.hotifi.user.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.text.DecimalFormat;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Predicate;

@Slf4j
public class FeedbackServiceImpl implements IFeedbackService {

    private final UserRepository userRepository;
    private final SessionRepository sessionRepository;
    private final PurchaseRepository purchaseRepository;
    private final FeedbackRepository feedbackRepository;

    public FeedbackServiceImpl(UserRepository userRepository, SessionRepository sessionRepository, PurchaseRepository purchaseRepository, FeedbackRepository feedbackRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.purchaseRepository = purchaseRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @Transactional
    @Override
    public void addFeedback(FeedbackRequest feedbackRequest) {
        Purchase purchase = purchaseRepository.findById(feedbackRequest.getPurchaseId()).orElse(null);
        if (purchase == null)
            throw new ApplicationException(FeedbackErrorCodes.PURCHASE_NOT_FOUND_FOR_FEEDBACK);
        try {
            Feedback feedback = new Feedback();
            feedback.setComment(feedbackRequest.getComment());
            feedback.setRating(feedbackRequest.getRating());
            feedback.setComment(feedbackRequest.getComment());
            feedback.setWifiSlow(feedbackRequest.isWifiSlow());
            feedback.setWifiStopped(feedbackRequest.isWifiStopped());
            feedback.setPurchase(purchase);
            feedbackRepository.save(feedback);
        } catch (DataIntegrityViolationException e) {
            log.error("Feedback already given ", e);
            throw new ApplicationException(FeedbackErrorCodes.FEEDBACK_ALREADY_GIVEN);
        } catch (Exception e) {
            log.error("Error occurred ", e);
            throw new ApplicationException(FeedbackErrorCodes.UNEXPECTED_FEEDBACK_ERROR);
        }
    }

    @Transactional
    @Override
    public Feedback getPurchaseFeedback(Long purchaseId) {
        Purchase purchase = purchaseRepository.findById(purchaseId).orElse(null);
        if (purchase == null)
            throw new ApplicationException(FeedbackErrorCodes.FEEDBACK_NOT_FOUND_FOR_NON_EXISTENT_PURCHASE);
        return feedbackRepository.findFeedbackByPurchaseId(purchaseId);
    }

    @Transactional
    @Override
    public List<FeedbackResponse> getSellerFeedbacks(Long sellerId, int page, int size, boolean isDescending) {
        /*User seller = userRepository.findById(sellerId).orElse(null);
        if (seller == null || seller.getAuthentication().isDeleted())
            throw new ApplicationException(UserErrorCodes.USER_NOT_FOUND);
        try {
            List<Feedback> feedbacks = getFeedbacksFromSeller(seller, page, size, isDescending);
            if (feedbacks == null)
                return null;
            List<FeedbackResponse> feedbackResponses = new ArrayList<>();
            Predicate<Feedback> feedbackPredicate = getFeedbacks -> ! getFeedbacks.getComment().isEmpty();
            feedbacks.stream().filter(feedbackPredicate).forEach(feedback -> {
                User user = feedback.getPurchase().getPurchaseOrder().getUser();
                String buyerName = user.getFirstName() + " " + user.getLastName();
                String buyerPhotoUrl = user.getPhotoUrl();
                FeedbackResponse feedbackResponse = new FeedbackResponse();
                feedbackResponse.setFeedback(feedback);
                feedbackResponse.setBuyerName(buyerName);
                feedbackResponse.setBuyerPhotoUrl(buyerPhotoUrl);
                feedbackResponses.add(feedbackResponse);
            });
            return feedbackResponses;

        } catch (Exception e) {
            log.error("Error occurred", e);
            throw new ApplicationException(FeedbackErrorCodes.UNEXPECTED_FEEDBACK_ERROR);
        }*/
        return null;
    }

    //No need for try catch exceptions
    @Transactional
    @Override
    public String getAverageRating(Long sellerId) {
        User seller = userRepository.findById(sellerId).orElse(null);
        if (seller == null) return null;
        OptionalDouble optionalDoubleRating = getFeedbacksFromSeller(seller, 0, Integer.MAX_VALUE, true)
                .stream().mapToDouble(Feedback::getRating).average();
        if (optionalDoubleRating == null) return null;
        double rating = optionalDoubleRating.orElseThrow(IllegalStateException::new);
        DecimalFormat decimalFormat = new DecimalFormat("#.#");
        return decimalFormat.format(rating);
    }

    @Transactional
    @Override
    public SellerReviewsResponse getSellerRatingDetails(Long sellerId) {
        /*User seller = userRepository.findById(sellerId).orElse(null);
        if (seller == null || seller.getAuthentication().isDeleted())
            throw new ApplicationException(UserErrorCodes.USER_NOT_FOUND);
        try {
            List<Feedback> feedbacks = getFeedbacksFromSeller(seller, 0, Integer.MAX_VALUE, true);
            String getAverageRating = getAverageRating(sellerId);
            String averageRating = getAverageRating != null ? getAverageRating : "0.0";
            long totalReviews = feedbacks != null ? feedbacks.stream()
                    .filter(feedback -> feedback.getComment() != null)
                    .count() : 0;
            long totalRatings = feedbacks != null ? (long) feedbacks.size() : 0;
            Supplier<Stream<Feedback>> feedbackSupplier = feedbacks != null ? feedbacks::stream : null;

            long oneStarCount = feedbackSupplier != null ? feedbackSupplier.get()
                    .filter(getFeedbackPredicate(1.0F, 1.0F)).count() : 0;
            long twoStarCount = feedbackSupplier != null ? feedbackSupplier.get()
                    .filter(getFeedbackPredicate(1.5F, 2.0F)).count() : 0;
            long threeStarCount = feedbackSupplier != null ? feedbackSupplier.get()
                    .filter(getFeedbackPredicate(2.5F, 3.0F)).count() : 0;
            long fourStarCount = feedbackSupplier != null ? feedbackSupplier.get()
                    .filter(getFeedbackPredicate(3.5F, 4.0F)).count() : 0;
            long fiveStarCount = feedbackSupplier != null ? feedbackSupplier.get()
                    .filter(getFeedbackPredicate(4.5F, 5.0F)).count() : 0;

            List<Long> eachRatings = Arrays.asList(oneStarCount, twoStarCount, threeStarCount, fourStarCount, fiveStarCount);

            return new SellerReviewsResponse(totalReviews, totalRatings, averageRating, eachRatings);

        } catch (Exception e) {
            log.error("Error Occurred", e);
            throw new ApplicationException(UserStatusErrorCodes.UNEXPECTED_USER_STATUS_ERROR);
        }*/
        return null;
    }

    public List<Feedback> getFeedbacksFromSeller(User seller, int page, int size, boolean isDescending) {
        /*Pageable pageable = isDescending ? PageRequest.of(page, size, Sort.by("created_at").descending())
                : PageRequest.of(page, size, Sort.by("created_at"));
        Pageable pageableAll = isDescending ? PageRequest.of(0, Integer.MAX_VALUE, Sort.by("created_at").descending())
                : PageRequest.of(page, size, Sort.by("created_at"));
        List<Long> speedTestIds = seller.getSpeedTests()
                .stream().map(SpeedTest::getId)
                .collect(Collectors.toList());
        List<Long> sessionIds = sessionRepository.findSessionsBySpeedTestIds(speedTestIds, pageableAll)
                .stream().map(Session::getId)
                .collect(Collectors.toList());
        List<Long> purchaseIds = purchaseRepository.findPurchasesBySessionIds(sessionIds)
                .stream().map(Purchase::getId)
                .collect(Collectors.toList());
        return feedbackRepository.findFeedbacksByPurchaseIds(purchaseIds, pageable);*/
        return null;
    }

    public Predicate<Feedback> getFeedbackPredicate(Float firstRating, Float secondRating) {
        return feedback -> Float.compare(feedback.getRating(), firstRating) == 0 || Float.compare(feedback.getRating(), secondRating) == 0;
    }
}
