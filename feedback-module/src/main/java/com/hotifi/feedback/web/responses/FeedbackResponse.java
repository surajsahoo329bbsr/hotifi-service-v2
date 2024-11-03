package com.hotifi.feedback.web.responses;

import com.hotifi.feedback.entities.Feedback;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FeedbackResponse {

    private Feedback feedback;

    private String buyerPhotoUrl;

    private String buyerName;
}
