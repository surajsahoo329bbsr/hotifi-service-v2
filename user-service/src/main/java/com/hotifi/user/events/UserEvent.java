package com.hotifi.user.events;

import lombok.*;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
public class UserEvent {

    private Long userId;

    private String email;

    private String firstName;

    private String lastName;

    private String emailOtp;

    private String errorDescription;

    private Date registrationEventTime;

}
