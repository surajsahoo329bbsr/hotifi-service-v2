package com.hotifi.feedback.services.interfaces;

import com.hotifi.feedback.entities.Feedback;
import com.hotifi.feedback.web.request.FeedbackRequest;
import com.hotifi.feedback.web.responses.FeedbackResponse;
import com.hotifi.payment.web.responses.SellerReviewsResponse;

import java.util.List;

public interface IFeedbackService {

    void addFeedback(FeedbackRequest feedbackRequest);

    Feedback getPurchaseFeedback(Long purchaseId);

    List<FeedbackResponse> getSellerFeedbacks(Long sellerId, int page, int size, boolean isDescending);

    String getAverageRating(Long sellerId);

    SellerReviewsResponse getSellerRatingDetails(Long sellerId);

}
