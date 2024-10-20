package com.hotifi.common.web.controllers;

import com.hotifi.common.constants.ApplicationConstants;
import com.hotifi.common.constants.BusinessConstants;
import com.hotifi.common.constants.SuccessMessages;
import com.hotifi.common.constants.codes.SocialCodes;
import com.hotifi.common.dto.UserRegistrationEventDTO;
import com.hotifi.common.exception.errors.ErrorMessages;
import com.hotifi.common.exception.errors.ErrorResponse;
import com.hotifi.common.services.interfaces.IEmailService;
import io.swagger.annotations.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Validated
@RestController
@Api(tags = ApplicationConstants.USER_TAG)
@RequestMapping(path = "/email")
public class EmailController {

    @Autowired
    private IEmailService emailService;

    @PostMapping(path = "/registration", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Add User Details",
            notes = "Add User Details",
            code = 204,
            response = String.class)
    @ApiResponses(value = @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class))
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    public ResponseEntity<?> sendWelcomeEmail(@RequestBody @Valid UserRegistrationEventDTO userRegistrationEventDTO) {
        emailService.sendWelcomeEmail(userRegistrationEventDTO);
        return new ResponseEntity<>(userRegistrationEventDTO, HttpStatus.OK);
    }

}
