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
public class UserEvent {

    private Long userId;

    private String email;

    private String firstName;

    private String lastName;

    private String emailOtp;

    private String errorDescription;

    private Date registrationEventTime;

}
