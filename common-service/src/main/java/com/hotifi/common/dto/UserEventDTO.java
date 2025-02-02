package com.hotifi.common.dto;

import lombok.*;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class UserEventDTO implements Serializable {

    private Long userId;

    private String email;

    private String firstName;

    private String lastName;

    private String emailOtp;

    private String errorDescription;

    private Date registrationEventTime;

}
