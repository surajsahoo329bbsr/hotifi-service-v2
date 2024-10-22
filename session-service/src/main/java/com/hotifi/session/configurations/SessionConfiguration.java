package com.hotifi.session.configurations;

import com.hotifi.session.repositories.SessionRepository;
import com.hotifi.session.repositories.SpeedTestRepository;
import com.hotifi.session.services.implementations.SessionServiceImpl;
import com.hotifi.session.services.interfaces.ISessionService;
import com.hotifi.speedtest.services.interfaces.ISpeedTestService;
import com.hotifi.user.repositories.UserRepository;
import com.hotifi.user.services.interfaces.INotificationService;
import com.hotifi.user.services.interfaces.IUserStatusService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SessionConfiguration {

    @Bean
    public ISessionService sessionService(UserRepository userRepository, SpeedTestRepository speedTestRepository, SessionRepository sessionRepository, ISpeedTestService speedTestService, IUserStatusService userStatusService, INotificationService notificationService){
        return new SessionServiceImpl(userRepository, speedTestRepository, sessionRepository, speedTestService, userStatusService, notificationService);
    }

}
