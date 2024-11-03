package com.hotifi.feedback.configurations;

import com.hotifi.feedback.repositories.FeedbackRepository;
import com.hotifi.feedback.services.implementations.FeedbackServiceImpl;
import com.hotifi.feedback.services.interfaces.IFeedbackService;
import com.hotifi.session.repositories.SessionRepository;
import com.hotifi.payment.repositories.PurchaseRepository;
import com.hotifi.user.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = {"com.hotifi.feedback", "com.hotifi.session", "com.hotifi.payment"})
public class FeedbackConfiguration {

    @Bean
    public IFeedbackService feedbackService(UserRepository userRepository, SessionRepository sessionRepository, PurchaseRepository purchaseRepository, FeedbackRepository feedbackRepository){
        return new FeedbackServiceImpl(userRepository, sessionRepository, purchaseRepository, feedbackRepository);
    }

}
