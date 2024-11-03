package com.hotifi.offer.configuration;

import com.hotifi.authentication.repositories.AuthenticationRepository;
import com.hotifi.offer.repositories.OfferRepository;
import com.hotifi.offer.repositories.ReferrerRepository;
import com.hotifi.offer.services.implementations.OfferServiceImpl;
import com.hotifi.offer.services.implementations.ReferentServiceImpl;
import com.hotifi.offer.services.implementations.ReferrerServiceImpl;
import com.hotifi.offer.services.interfaces.IOfferService;
import com.hotifi.offer.services.interfaces.IReferentService;
import com.hotifi.offer.services.interfaces.IReferrerService;
import com.hotifi.user.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "com.hotifi.offer.repositories")
public class OfferConfiguration {

    @Bean
    public IOfferService offerService(AuthenticationRepository authenticationRepository, OfferRepository offerRepository){
        return new OfferServiceImpl(authenticationRepository, offerRepository);
    }

    @Bean
    public IReferentService referentService(){
        return new ReferentServiceImpl();
    }

    @Bean
    public IReferrerService referrerService(ReferrerRepository referrerRepository, UserRepository userRepository, IOfferService offerService){
        return new ReferrerServiceImpl(referrerRepository, userRepository, offerService);
    }

}
