package com.hotifi.user.configuration;

import com.hotifi.authentication.repositories.AuthenticationRepository;
import com.hotifi.common.services.interfaces.IFirebaseMessagingService;
import com.hotifi.common.services.interfaces.IVerificationService;
import com.hotifi.user.events.UserEvent;
import com.hotifi.user.repositories.DeviceRepository;
import com.hotifi.user.repositories.UserRepository;
import com.hotifi.user.repositories.UserStatusRepository;
import com.hotifi.user.services.implementations.DeviceServiceImpl;
import com.hotifi.user.services.implementations.NotificationServiceImpl;
import com.hotifi.user.services.implementations.UserServiceImpl;
import com.hotifi.user.services.implementations.UserStatusServiceImpl;
import com.hotifi.user.services.interfaces.IDeviceService;
import com.hotifi.user.services.interfaces.INotificationService;
import com.hotifi.user.services.interfaces.IUserService;
import com.hotifi.user.services.interfaces.IUserStatusService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.kafka.core.KafkaTemplate;

@Configuration
@EnableJpaRepositories(basePackages = {"com.hotifi.user.repositories", "com.hotifi.authentication.repositories"})
public class UserConfiguration {

    @Bean
    public IDeviceService deviceService(UserRepository userRepository, DeviceRepository deviceRepository){
        return new DeviceServiceImpl(userRepository, deviceRepository);
    }

    @Bean
    public INotificationService notificationService(DeviceRepository deviceRepository, IDeviceService deviceService, IFirebaseMessagingService firebaseMessagingService){
        return new NotificationServiceImpl(deviceRepository, deviceService, firebaseMessagingService);
    }

    @Bean
    public IUserService userService(UserRepository userRepository, AuthenticationRepository authenticationRepository, IVerificationService verificationService, INotificationService notificationService, KafkaTemplate<String, UserEvent> userRegistrationEventKafkaTemplate){
        return new UserServiceImpl(userRepository, authenticationRepository, verificationService, notificationService, userRegistrationEventKafkaTemplate);
    }

    @Bean
    public IUserStatusService userStatusService(AuthenticationRepository authenticationRepository, UserStatusRepository userStatusRepository, UserRepository userRepository, IDeviceService deviceService, INotificationService notificationService, KafkaTemplate<String, UserEvent> userEventKafkaTemplate){
        return new UserStatusServiceImpl(authenticationRepository, userStatusRepository, userRepository, deviceService, notificationService, userEventKafkaTemplate);
    }

}
