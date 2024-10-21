package com.hotifi.session.configurations;

import com.hotifi.session.repositories.SpeedTestRepository;
import com.hotifi.session.services.implementations.SpeedTestServiceImpl;
import com.hotifi.session.services.interfaces.ISpeedTestService;
import com.hotifi.user.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpeedTestConfiguration {

    @Bean
    public ISpeedTestService speedTestService(UserRepository userRepository, SpeedTestRepository speedTestRepository){
        return new SpeedTestServiceImpl(userRepository, speedTestRepository);
    }
}
