package com.hotifi.speedtest.configurations;

import com.hotifi.speedtest.repositories.SpeedTestRepository;
import com.hotifi.speedtest.services.implementations.SpeedTestServiceImpl;
import com.hotifi.speedtest.services.interfaces.ISpeedTestService;
import com.hotifi.user.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableJpaRepositories(basePackages = {"com.hotifi.speedtest.repositories"})
public class SpeedTestConfiguration {

    @Bean
    public ISpeedTestService speedTestService(UserRepository userRepository, SpeedTestRepository speedTestRepository){
        return new SpeedTestServiceImpl(userRepository, speedTestRepository);
    }
}
