package com.hotifi.authentication.web.request;

import com.hotifi.common.constants.BusinessConstants;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class PhoneRequest {

    @NotBlank(message = "{email.blank}")
    @Email(message = "{email.pattern.invalid}")
    private String email;

    @NotBlank(message = "{phone.cc.blank}")
    @Pattern(regexp = BusinessConstants.VALID_COUNTRY_CODE_PATTERN, message = "{phone.cc.invalid}")
    private String countryCode;

    @NotBlank(message = "{phone.number.blank}")
    @Pattern(regexp = BusinessConstants.VALID_PHONE_PATTERN, message = "{phone.number.invalid}")
    private String phone;

    @NotBlank(message = "{token.blank}")
    private String token;

}