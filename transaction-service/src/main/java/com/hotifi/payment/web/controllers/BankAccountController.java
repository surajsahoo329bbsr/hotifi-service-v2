package com.hotifi.payment.web.controllers;

import com.hotifi.common.constants.ApplicationConstants;
import com.hotifi.common.constants.SuccessMessages;
import com.hotifi.common.exception.errors.ErrorMessages;
import com.hotifi.common.exception.errors.ErrorResponse;
import com.hotifi.payment.entities.BankAccount;
import com.hotifi.payment.services.interfaces.IBankAccountService;
import com.hotifi.payment.web.request.BankAccountRequest;
import com.hotifi.payment.web.responses.BankAccountAdminResponse;
import io.swagger.annotations.*;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@Api(tags = ApplicationConstants.BANK_ACCOUNT_TAG)
@RequestMapping(path = "/bank-account")
public class BankAccountController {

    @Autowired
    private IBankAccountService bankAccountService;

    //@Autowired
    //private ICustomerAuthorizationService customerAuthorizationService;


    @PostMapping(path = "/seller", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Add Bank Account Details Of A Customer",
            notes = "Add Bank Account Details Of A Customer",
            code = 204,
            response = String.class)
    @ApiResponses(value = @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class))
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<?> addBankAccount(@RequestBody @Validated BankAccountRequest bankAccountRequest) {
       // if (customerAuthorizationService.isAuthorizedByUserId(bankAccountRequest.getUserId(), AuthorizationUtils.getUserToken()))
            bankAccountService.addBankAccount(bankAccountRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping(path = "/seller/upi/{user-id}/{upi-id}")
    @ApiOperation(
            value = "Add UPI ID Of A Customer",
            notes = "Add UPI ID Of A Customer",
            code = 204,
            response = String.class)
    @ApiResponses(value = @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class))
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<?> addUpiId(@PathVariable(value = "user-id")
                                      @Range(min = 1, message = "{user.id.invalid}") Long userId,
                                      @PathVariable(value = "upi-id")
                                      @Length(max = 255, message = "{upi.id.length.invalid}") String upiId) {
        //if (customerAuthorizationService.isAuthorizedByUserId(userId, AuthorizationUtils.getUserToken()))
            bankAccountService.addUpiId(userId, upiId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping(path = "/seller/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Update Bank Account Details By Customer",
            notes = "Update Bank Account Details By Customer",
            code = 204,
            response = String.class)
    @ApiResponses(value = @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class))
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<?> updateBankAccountByCustomer(@RequestBody @Validated BankAccountRequest bankAccountRequest) {
        //if (customerAuthorizationService.isAuthorizedByUserId(bankAccountRequest.getUserId(), AuthorizationUtils.getUserToken()))
            bankAccountService.updateBankAccountByCustomer(bankAccountRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping(path = "/seller/update/upi/{user-id}/{upi-id}")
    @ApiOperation(
            value = "Update UPI ID By Customer",
            notes = "Update UPI ID By Customer",
            code = 204,
            response = String.class)
    @ApiResponses(value = @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class))
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<?> updateUpiIdByCustomer(@PathVariable(value = "user-id")
                                                   @Range(min = 1, message = "{user.id.invalid}") Long userId,
                                                   @PathVariable(value = "upi-id")
                                                   @Length(max = 255, message = "{upi.id.length.invalid}") String upiId) {
        //if (customerAuthorizationService.isAuthorizedByUserId(userId, AuthorizationUtils.getUserToken()))
            bankAccountService.updateUpiIdByCustomer(userId, upiId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping(path = "/admin/update/success/{user-id}/{linked-account-id}")
    @ApiOperation(
            value = "Update Successful Linked Account Of User By Admin",
            notes = "Update Successful Linked Account Of User By Admin",
            code = 204,
            response = String.class)
    @ApiResponses(value = @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class))
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<?> updateSuccessfulBankAccountByAdmin(@PathVariable(value = "user-id")
                                                                @Range(min = 1, message = "{user.id.invalid}") Long userId,
                                                                @PathVariable(value = "linked-account-id")
                                                                @Length(max = 255, message = "{linked.account.id.length.invalid}") String linkedAccountId) {
        bankAccountService.updateBankAccountByAdmin(userId, linkedAccountId, null);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping(path = "/update/admin/error/{user-id}/{error-description}")
    @ApiOperation(
            value = "Update Unsuccessful Linked Account Reason Of User By Admin",
            notes = "Update Unsuccessful Linked Account Reason Of User By Admin",
            code = 204,
            response = String.class)
    @ApiResponses(value = @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class))
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<?> updateErrorBankAccountByAdmin(@PathVariable(value = "user-id")
                                                           @Range(min = 1, message = "{user.id.invalid}") Long userId,
                                                           @PathVariable(value = "error-description", required = false)
                                                           @Length(max = 255, message = "{error.description.length.invalid}") String errorDescription) {
        bankAccountService.updateBankAccountByAdmin(userId, null, errorDescription);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(path = "/seller/{user-id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Get Bank Account Details By User Id",
            notes = "Get Bank Account Details By User Id",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = BankAccount.class)
    })
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    @PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<?> getBankAccountByUserId(@PathVariable(value = "user-id") @Range(min = 1, message = "{user.id.invalid}") Long userId) {
        BankAccount bankAccount =
                //(AuthorizationUtils.isAdministratorRole() || customerAuthorizationService.isAuthorizedByUserId(userId, AuthorizationUtils.getUserToken())) ?
                        bankAccountService.getBankAccountByUserId(userId); // : null;
        return new ResponseEntity<>(bankAccount, HttpStatus.OK);
    }

    @GetMapping(path = "/admin/unlinked", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Get Unlinked Bank Account Details",
            notes = "Get Unlinked Bank Account Details",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = BankAccountAdminResponse.class, responseContainer = "List")
    })
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<?> getUnlinkedBankAccounts() {
        List<BankAccountAdminResponse> bankAccountAdminResponses = bankAccountService.getUnlinkedBankAccounts();
        return new ResponseEntity<>(bankAccountAdminResponses, HttpStatus.OK);
    }

}
