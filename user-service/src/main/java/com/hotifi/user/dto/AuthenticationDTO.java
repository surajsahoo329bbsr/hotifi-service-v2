package com.hotifi.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class AuthenticationDTO {

    private Long id;

    private String email;

    private String countryCode;

    private String phone;

    private String emailOtp;

    private String password;

    private Date createdAt;

    private Date modifiedAt;

    private boolean isEmailVerified ;

    private boolean isPhoneVerified;

    private boolean isActivated;

    private boolean isFrozen;

    private boolean isBanned;

    private boolean isDeleted;

}
