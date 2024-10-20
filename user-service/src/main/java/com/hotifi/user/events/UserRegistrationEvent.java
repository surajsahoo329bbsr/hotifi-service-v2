package com.hotifi.user.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserRegistrationEvent {

    private Long userId;

    private String email;

    private String firstName;

    private String lastName;

    private Date registrationEventTime;

}
