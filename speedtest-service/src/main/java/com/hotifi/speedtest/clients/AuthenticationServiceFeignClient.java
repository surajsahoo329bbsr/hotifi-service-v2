package com.hotifi.speedtest.clients;

import com.hotifi.authentication.entities.Authentication;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "AUTHENTICATION-SERVICE")
public interface AuthenticationServiceFeignClient {

    @GetMapping("/authentication/get/{id}")
    Authentication getAuthenticationById(@PathVariable("id") Long authenticationId);
}
