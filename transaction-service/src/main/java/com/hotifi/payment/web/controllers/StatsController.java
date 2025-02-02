package com.hotifi.payment.web.controllers;


import com.hotifi.common.constants.ApplicationConstants;
import com.hotifi.common.constants.SuccessMessages;
import com.hotifi.common.exception.errors.ErrorMessages;
import com.hotifi.common.exception.errors.ErrorResponse;
import com.hotifi.payment.services.interfaces.IStatsService;
import com.hotifi.payment.web.responses.BuyerStatsResponse;
import com.hotifi.payment.web.responses.SellerStatsResponse;
import io.swagger.annotations.*;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@Api(tags = ApplicationConstants.STATS_TAG)
@RequestMapping(path = "/stats")
public class StatsController {

    @Autowired
    private IStatsService statsService;

    //@Autowired
   // private ICustomerAuthorizationService customerAuthorizationService;

    @GetMapping(path = "/seller/{user-id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Get Seller Stats By User Id",
            notes = "Get Seller Stats By User Id",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = SellerStatsResponse.class)
    })
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    //@PreAuthorize("hasAuthority('ADMINISTRATOR') or hasAuthority('CUSTOMER')")
    public ResponseEntity<?> getSellerStats(@PathVariable("user-id") @Range(min = 1, message = "{seller.id.invalid}") Long id) {
        SellerStatsResponse sellerStatsResponse =
                //AuthorizationUtils.isAdministratorRole() || customerAuthorizationService.isAuthorizedByUserId(id, AuthorizationUtils.getUserToken()) ?
                        statsService.getSellerStats(id); // : null;
        return new ResponseEntity<>(sellerStatsResponse, HttpStatus.OK);
    }

    @GetMapping(path = "/buyer/{user-id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Get Buyer Stats By User Id",
            notes = "Get Buyer Stats By User Id",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = BuyerStatsResponse.class)
    })
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    //@PreAuthorize("hasAuthority('ADMINISTRATOR') or hasAuthority('CUSTOMER')")
    public ResponseEntity<?> getBuyerStats(@PathVariable("user-id") @Range(min = 1, message = "{buyer.id.invalid}") Long id) {
        BuyerStatsResponse buyerStatsResponse =
                //AuthorizationUtils.isAdministratorRole() || customerAuthorizationService.isAuthorizedByUserId(id, AuthorizationUtils.getUserToken()) ?
                        statsService.getBuyerStats(id); // : null;
        return new ResponseEntity<>(buyerStatsResponse, HttpStatus.OK);
    }

}
