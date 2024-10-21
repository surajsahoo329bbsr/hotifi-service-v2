package com.hotifi.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class UserEventDTO implements Serializable {

    private Long userId;

    private String email;

    private String firstName;

    private String emailOtp;

    private String errorDescription;

}
