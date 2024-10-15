package com.hotifi.configuration;

import com.hotifi.repositories.AuthenticationRepository;
import com.hotifi.repositories.RoleRepository;
import com.hotifi.services.implementations.AuthenticationServiceImpl;
import com.hotifi.services.implementations.VerificationServiceImpl;
import com.hotifi.services.interfaces.IAuthenticationService;
import com.hotifi.services.interfaces.IVerificationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticationConfiguration {

    @Bean
    public IAuthenticationService authenticationService(AuthenticationRepository authenticationRepository, RoleRepository roleRepository, IVerificationService verificationService) {
        return new AuthenticationServiceImpl(authenticationRepository, roleRepository, verificationService);
    }

    @Bean
    public IVerificationService verificationService() {
        return new VerificationServiceImpl();
    }

}
