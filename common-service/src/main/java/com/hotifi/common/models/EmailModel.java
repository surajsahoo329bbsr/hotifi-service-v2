package com.hotifi.common.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Builder
public class EmailModel {

    @NotBlank(message = "{from.email.blank}")
    @Email(message = "{from.email.invalid}")
    private String fromEmail;

    @NotBlank(message = "{email.content.blank}")
    private String fromEmailPassword;

    @NotBlank(message = "{to.email.blank}")
    @Email(message = "{to.email.invalid}")
    private String toEmail;

    private String emailOtp;

    private String content;

}
