package com.hotifi.user.web.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
@AllArgsConstructor
public class CredentialsResponse {

    @Email(message = "{username.length.invalid}")
    private String username;

    @NotBlank
    @Length(max = 255, message = "{password.token.length.invalid}")
    private String passwordToken;

}
