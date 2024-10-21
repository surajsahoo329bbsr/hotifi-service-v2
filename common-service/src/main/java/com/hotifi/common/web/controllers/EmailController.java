package com.hotifi.common.web.controllers;

import com.hotifi.common.constants.ApplicationConstants;
import com.hotifi.common.dto.UserEventDTO;
import com.hotifi.common.exception.errors.ErrorMessages;
import com.hotifi.common.exception.errors.ErrorResponse;
import com.hotifi.common.services.interfaces.IEmailService;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

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
    public ResponseEntity<?> sendWelcomeEmail(@RequestBody @Valid UserEventDTO userEventDTO) {
        emailService.sendWelcomeEmail(userEventDTO);
        return new ResponseEntity<>(userEventDTO, HttpStatus.OK);
    }

    @PostMapping(path = "/suspend", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Suspend User Details",
            notes = "Suspend User Details",
            code = 204,
            response = String.class)
    @ApiResponses(value = @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class))
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    public ResponseEntity<?> sendUserSuspendedEmail(@RequestBody @Valid UserEventDTO userEventDTO) {
        emailService.sendAccountFrozenEmail(userEventDTO);
        return new ResponseEntity<>(userEventDTO, HttpStatus.OK);
    }

}
