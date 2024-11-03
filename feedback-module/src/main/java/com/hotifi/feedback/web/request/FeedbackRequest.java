package com.hotifi.feedback.web.request;

import com.hotifi.payment.validators.Rating;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
public class FeedbackRequest {

    @Range(min = 1, message = "{feedback.purchase.id.invalid}")
    private Long purchaseId;

    @Rating
    private Float rating;

    @Length(max = 255, message = "{feedback.comment.invalid}")
    private String comment;

    private boolean isWifiSlow;

    private boolean isWifiStopped;
}
