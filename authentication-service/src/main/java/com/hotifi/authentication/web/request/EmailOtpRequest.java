package com.hotifi.authentication.web.request;

import com.hotifi.common.constants.BusinessConstants;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
public class EmailOtpRequest {

    @NotBlank(message = "{email.blank}")
    @Email(message = "{email.pattern.invalid}")
    private String email;

    @NotBlank(message = "{otp.blank}")
    @Pattern(regexp = BusinessConstants.VALID_OTP_PATTERN, message = "{otp.number.invalid}")
    private String otp;

}
