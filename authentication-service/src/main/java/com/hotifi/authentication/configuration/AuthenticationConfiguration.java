package com.hotifi.authentication.configuration;

import com.hotifi.common.services.implementations.EmailServiceImpl;
import com.hotifi.common.services.implementations.VerificationServiceImpl;
import com.hotifi.common.services.interfaces.IEmailService;
import com.hotifi.common.services.interfaces.IVerificationService;
import com.hotifi.authentication.repositories.AuthenticationRepository;
import com.hotifi.authentication.repositories.RoleRepository;
import com.hotifi.authentication.services.implementations.AuthenticationServiceImpl;
import com.hotifi.authentication.services.interfaces.IAuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthenticationConfiguration {

    @Bean
    public IAuthenticationService authenticationService(AuthenticationRepository authenticationRepository, RoleRepository roleRepository, IVerificationService verificationService, IEmailService emailService) {
        return new AuthenticationServiceImpl(authenticationRepository, roleRepository, verificationService, emailService);
    }

    @Bean
    public IVerificationService verificationService() {
        return new VerificationServiceImpl();
    }

    @Bean
    public IEmailService emailService() {
        return new EmailServiceImpl();
    }
}
