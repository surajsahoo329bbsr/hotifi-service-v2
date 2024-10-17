package com.hotifi.user.web.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hotifi.common.constants.BusinessConstants;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Getter
@Setter
public class UserRequest {

    @NotBlank(message = "first.name.invalid")
    private String firstName;

    @NotBlank(message = "last.name.blank")
    private String lastName;

    @NotBlank(message = "Please provide a date of birth")
    @Pattern(regexp = BusinessConstants.VALID_USERNAME_PATTERN, message = "{username.invalid}")
    private String username;

    @ApiModelProperty(required = true,example = "02-01-1998")
    @JsonFormat(pattern = "dd-MM-yyyy")
    @NotNull(message = "{date.of.birth.blank}")
    private Date dateOfBirth;

    private String facebookId;

    private String googleId;

    @Pattern(regexp = BusinessConstants.VALID_URL_PATTERN, message = "{photo.url.invalid}")
    private String photoUrl;

    @Range(min = 1, message = "{authentication.id.invalid}")
    private Long authenticationId;

}
