package com.hotifi.authentication.web.controller;

import com.hotifi.authentication.repositories.AuthenticationRepository;
import com.hotifi.common.constants.ApplicationConstants;
import com.hotifi.common.constants.BusinessConstants;
import com.hotifi.common.constants.SuccessMessages;
import com.hotifi.authentication.entities.Authentication;
import com.hotifi.common.dto.UserEventDTO;
import com.hotifi.common.exception.errors.ErrorMessages;
import com.hotifi.common.exception.errors.ErrorResponse;
import com.hotifi.authentication.services.interfaces.IAuthenticationService;
import com.hotifi.common.services.interfaces.IEmailService;
import com.hotifi.common.validator.SocialClient;
import com.hotifi.authentication.web.request.EmailOtpRequest;
import com.hotifi.authentication.web.request.PhoneRequest;
import com.hotifi.authentication.web.response.AvailabilityResponse;
import com.hotifi.authentication.web.response.CredentialsResponse;
import io.swagger.annotations.*;
import org.hibernate.validator.constraints.Length;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Validated
@RestController
@Api(tags = ApplicationConstants.AUTHENTICATION_TAG)
public class AuthenticationController {

    @Autowired
    private IAuthenticationService authenticationService;

    @Autowired
    private IEmailService emailService;

    @Autowired
    private AuthenticationRepository authenticationRepository;

    @GetMapping(path = "/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Get Authentication Details By Email For Customer",
            notes = "Get Authentication Details By Email For Customer",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = Authentication.class)
    })
    public ResponseEntity<?> getAuthentication(
            @PathVariable(value = "email")
            //@NotBlank(message = "{email.blank}")
            @Email(message = "{email.pattern.invalid}")
            String email) {
        Authentication authentication = authenticationService.getAuthentication(email);
        return new ResponseEntity<>(authentication, HttpStatus.OK);
    }

    @GetMapping(path = "/kafka/{listenerId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Starts Kafka Listener With ID",
            notes = "Starts Kafka Listener With ID",
            code = 204,
            response = String.class)
    @ApiResponses(value = @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class))
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    public ResponseEntity<?> startKafkaListener(@PathVariable(value = "listenerId") String listenerId) {
        //boolean isListenerStarted = emailService.startListener(listenerId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(path = "/get/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Get Authentication Details By Id",
            notes = "Get Authentication Details By Id",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = Authentication.class)
    })
    public ResponseEntity<?> getAuthentication(
            @PathVariable(value = "id") Long authenticationId) {
        Authentication authentication = authenticationRepository.findById(authenticationId).orElse(null);
        return new ResponseEntity<>(authentication, HttpStatus.OK);
    }

    @GetMapping(path = "/is-available/phone/{phone}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Checks If Phone Number Is Available Or Not",
            notes = "Checks If Phone Number Is Available Or Not",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = AvailabilityResponse.class)
    })public ResponseEntity<?> isPhoneAvailable(@PathVariable(value = "phone")
                                              @NotBlank(message = "{phone.blank}")
                                              @Pattern(regexp = BusinessConstants.VALID_PHONE_PATTERN, message = "{phone.invalid}") String phone) {
        //No need to check for role security here
        boolean isPhoneAvailable = authenticationService.isPhoneAvailable(phone);
        return new ResponseEntity<>(new AvailabilityResponse(null, isPhoneAvailable, null), HttpStatus.OK);
    }

    @GetMapping(path = "/is-available/email/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Checks If Email Is Available Or Not",
            notes = "Checks If Email Is Available Or Not",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = AvailabilityResponse.class)
    })public ResponseEntity<?> isEmailAvailable(@PathVariable(value = "email")
                                              @Email(message = "{email.invalid}") String email) {
        //No need to check for role security here
        boolean isEmailAvailable = authenticationService.isEmailAvailable(email);
        return new ResponseEntity<>(new AvailabilityResponse(null, null, isEmailAvailable), HttpStatus.OK);
    }

    @PostMapping(path = "/sign-up/social/{email}")
    @ApiOperation(
            value = "Post Authentication By Social Email",
            notes = "Post Authentication By Verified Or Unverified Email And Sends Password Token",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = CredentialsResponse.class)
    })public ResponseEntity<?> addSocialEmail(@PathVariable(value = "email")
                                            @NotBlank(message = "{email.blank}")
                                            @Email(message = "{email.pattern.invalid}") String email,
                                            @ApiParam(name = "identifier", type = "String")
                                            @NotBlank(message = "{identifier.blank}")
                                            @Length(max = 255, message = "{id.identifier.length.invalid}") @RequestParam String identifier,
                                            @NotBlank(message = "{token.blank}") @ApiParam(name = "token", type = "String")
                                            @Length(max = 4048, message = "{id.token.length.invalid}") @RequestParam String token,
                                            @NotBlank(message = "{social.client.blank}") @ApiParam(name = "social-client", type = "String")
                                            @SocialClient @RequestParam(name = "social-client") String socialClient) {
        CredentialsResponse credentialsResponse = authenticationService.addEmail(email, identifier, token, socialClient);
        return new ResponseEntity<>(credentialsResponse, HttpStatus.OK);
    }

    @PostMapping(path = "/sign-up/custom/{email}")
    @ApiOperation(
            value = "Post Authentication By Custom Email",
            notes = "Post Authentication By Verified Or Unverified Email And Sends Password Token",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = CredentialsResponse.class)
    })public ResponseEntity<?> addCustomEmail(@PathVariable(value = "email")
                                            @Email(message = "{email.pattern.invalid}") String email) {
        CredentialsResponse credentialsResponse = authenticationService.addEmail(email, null, null, null);
        return new ResponseEntity<>(credentialsResponse, HttpStatus.OK);
    }

    @PutMapping(path = "/sign-up/resend/otp/{email}")
    @ApiOperation(
            value = "Resend Email Otp For Sign-Up",
            notes = "Resend Email Otp For Sign-Up",
            code = 204,
            response = String.class)
    @ApiResponses(value = @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class))
    public ResponseEntity<?> resendEmailOtpSignUp(@PathVariable(value = "email")
                                                  @Email(message = "{email.pattern.invalid}") String email) {
        authenticationService.resendEmailOtpSignUp(email);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping(path = "/sign-up/verify/email", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Verify Email For Sign-Up",
            notes = "Verify Email For Sign-Up",
            code = 204,
            response = String.class)
    @ApiResponses(value = @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class))
    public ResponseEntity<?> verifyEmail(@RequestBody @Valid EmailOtpRequest emailOtpRequest) {
        authenticationService.verifyEmailOtpSignUp(emailOtpRequest.getEmail(), emailOtpRequest.getOtp());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping(path = "/verify/phone", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Verify Phone For Authentication",
            notes = "Verify Phone For Authentication",
            code = 204,
            response = String.class)
    @ApiResponses(value = @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class))
    public ResponseEntity<?> verifyPhone(@RequestBody @Valid PhoneRequest phoneRequest) {
        authenticationService.verifyPhone(phoneRequest.getEmail(), phoneRequest.getCountryCode(), phoneRequest.getPhone(), phoneRequest.getToken());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}