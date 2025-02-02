package com.hotifi.payment.web.controllers;

import com.hotifi.common.constants.ApplicationConstants;
import com.hotifi.common.constants.BusinessConstants;
import com.hotifi.common.constants.SuccessMessages;
import com.hotifi.common.exception.errors.ErrorMessages;
import com.hotifi.common.exception.errors.ErrorResponse;
import com.hotifi.payment.entities.PurchaseOrder;
import com.hotifi.payment.services.interfaces.IPurchaseService;
import com.hotifi.payment.web.request.OrderRequest;
import com.hotifi.payment.web.request.PurchaseRequest;
import com.hotifi.payment.web.responses.*;
import io.swagger.annotations.*;
import org.hibernate.validator.constraints.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.DecimalMin;
import java.util.Date;
import java.util.List;

@Validated
@RestController
@Api(tags = ApplicationConstants.PURCHASE_TAG)
@RequestMapping(path = "/purchase")
public class PurchaseController {

    @Autowired
    private IPurchaseService purchaseService;

    //@Autowired
    //private ICustomerAuthorizationService customerAuthorizationService;

    @PostMapping(path = "/buyer/order", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Add Order For Purchase",
            notes = "Add Order For Purchase",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = PurchaseReceiptResponse.class)
    })
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    //@PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<?> addPurchaseOrder(@RequestBody @Validated OrderRequest orderRequest) {
        PurchaseOrder purchaseOrder =
                //(customerAuthorizationService.isAuthorizedByUserId(orderRequest.getBuyerId(), AuthorizationUtils.getUserToken())) ?
                        purchaseService.addPurchaseOrder(orderRequest); //: null;
        return new ResponseEntity<>(purchaseOrder, HttpStatus.OK);
    }

    @PostMapping(path = "/buyer", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Add Purchase",
            notes = "Add Purchase",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = PurchaseReceiptResponse.class)
    })
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    //@PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<?> addPurchase(@RequestBody @Validated PurchaseRequest purchaseRequest) {
        PurchaseReceiptResponse receiptResponse =
                //(customerAuthorizationService.isAuthorizedByUserId(purchaseRequest.getBuyerId(), AuthorizationUtils.getUserToken())) ?
                        purchaseService.addPurchase(purchaseRequest); //: null;
        return new ResponseEntity<>(receiptResponse, HttpStatus.OK);
    }

    @GetMapping(path = "/buyer/receipt/{purchase-id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Get Purchase Receipt Of A Buyer By Purchase Id",
            notes = "Get Purchase Receipt Of A Buyer By Purchase Id",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = PurchaseReceiptResponse.class)
    })
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    //@PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<?> getPurchaseReceipt(
            @PathVariable(value = "purchase-id")
            @Range(min = 1, message = "{purchase.id.invalid}") Long purchaseId) {
        PurchaseReceiptResponse receiptResponse =
                //(AuthorizationUtils.isAdministratorRole() || customerAuthorizationService.isAuthorizedByPurchaseId(purchaseId, AuthorizationUtils.getUserToken())) ?
                        purchaseService.getPurchaseReceipt(purchaseId); //: null;
        return new ResponseEntity<>(receiptResponse, HttpStatus.OK);
    }

    @PutMapping(path = "/buyer/wifi-service/start/{purchase-id}/{status}")
    @ApiOperation(
            value = "Start Buyer Wifi-Service",
            notes = "Start Buyer Wifi-Service",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = WifiStartTimeResponse.class)
    })
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    //@PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<?> startBuyerWifiService(
            @PathVariable(value = "purchase-id")
            @Range(min = 1, message = "{purchase.id.invalid}") Long purchaseId) {
        Date wifiStartTime = //(customerAuthorizationService.isAuthorizedByPurchaseId(purchaseId, AuthorizationUtils.getUserToken())) ?
                purchaseService.startBuyerWifiService(purchaseId); //: null;
        return new ResponseEntity<>(new WifiStartTimeResponse(wifiStartTime), HttpStatus.OK);
    }

    @PutMapping(path = "/buyer/wifi-service/update/{purchase-id}/{data-used}")
    @ApiOperation(
            value = "Update Buyer Wifi-Service",
            notes = "Update Buyer Wifi-Service",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = UpdateStatusResponse.class)
    })
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    //@PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<?> updateBuyerWifiService(
            @PathVariable(value = "purchase-id")
            @Range(min = 1, message = "{purchase.id.invalid}") Long purchaseId,
            @PathVariable(value = "data-used")
            @DecimalMin(BusinessConstants.MINIMUM_DATA_USED_MB) double dataUsed) {
        int updateStatus =
                //customerAuthorizationService.isAuthorizedByPurchaseId(purchaseId, AuthorizationUtils.getUserToken()) ?
                        purchaseService.updateBuyerWifiService(purchaseId, dataUsed);  //: -1; //-1 for failure
        return new ResponseEntity<>(new UpdateStatusResponse(updateStatus), HttpStatus.OK);
    }

    @PutMapping(path = "/buyer/wifi-service/finish/{purchase-id}/{data-used}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Finish Buyer Wifi-Service",
            notes = "Finish Buyer Wifi-Service",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = WifiSummaryResponse.class)
    })
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    //@PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<?> finishBuyerWifiService(
            @PathVariable(value = "purchase-id")
            @Range(min = 1, message = "{purchase.id.invalid}") Long purchaseId,
            @PathVariable(value = "data-used")
            @DecimalMin(BusinessConstants.MINIMUM_DATA_USED_MB) double dataUsed) {
        WifiSummaryResponse wifiSummaryResponse =
                //customerAuthorizationService.isAuthorizedByPurchaseId(purchaseId, AuthorizationUtils.getUserToken()) ?
                        purchaseService.finishBuyerWifiService(purchaseId, dataUsed); //: null;
        return new ResponseEntity<>(wifiSummaryResponse, HttpStatus.OK);
    }

    @GetMapping(path = "/buyer/wifi-summary/{purchase-id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Find Buyer WifiSummary By Purchase Id",
            notes = "Find Buyer WifiSummary By Purchase Id",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = WifiSummaryResponse.class)
    })
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    //@PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<?> findBuyerWifiSummary(
            @PathVariable(value = "purchase-id")
            @Range(min = 1, message = "{purchase.id.invalid}") Long purchaseId) {
        WifiSummaryResponse wifiSummaryResponse =
                //customerAuthorizationService.isAuthorizedByPurchaseId(purchaseId, AuthorizationUtils.getUserToken()) ?
                        purchaseService.findBuyerWifiSummary(purchaseId); //: null;
        return new ResponseEntity<>(wifiSummaryResponse, HttpStatus.OK);
    }

    @GetMapping(path = "/buyer/usages/date-time/{buyer-id}/{page}/{size}/{is-descending}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Get Sorted Date-Time Wifi Summary By Buyer Id And Pagination Values",
            notes = "Get Sorted Date-Time Wifi Summary By Buyer Id And Pagination Values",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = WifiSummaryResponse.class, responseContainer = "List")
    })
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    //@PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<?> getSortedWifiUsagesDateTime(
            @PathVariable(value = "buyer-id")
            @Range(min = 1, message = "{buyer.id.invalid}") Long buyerId,
            @PathVariable(value = "page")
            @Range(min = 0, max = Integer.MAX_VALUE, message = "{page.number.invalid}") int page,
            @PathVariable(value = "size")
            @Range(min = 1, max = Integer.MAX_VALUE, message = "{page.size.invalid}") int size,
            @PathVariable(value = "is-descending") boolean isDescending) {
        List<WifiSummaryResponse> wifiSummaryResponses =
                //(AuthorizationUtils.isAdministratorRole() || customerAuthorizationService.isAuthorizedByUserId(buyerId, AuthorizationUtils.getUserToken())) ?
                        purchaseService.getSortedWifiUsagesDateTime(buyerId, page, size, isDescending); //: null;
        return new ResponseEntity<>(wifiSummaryResponses, HttpStatus.OK);
    }

    @GetMapping(path = "/buyer/usages/data-used/{buyer-id}/{page}/{size}/{is-descending}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Get Sorted Data-Used Wifi Summary By Buyer Id And Pagination Values",
            notes = "Get Sorted Data-Used Wifi Summary By Buyer Id And Pagination Values",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = WifiSummaryResponse.class, responseContainer = "List")
    })
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    //@PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<?> getSortedWifiUsagesDataUsed(
            @PathVariable(value = "buyer-id")
            @Range(min = 1, message = "{buyer.id.invalid}") Long buyerId,
            @PathVariable(value = "page")
            @Range(min = 0, max = Integer.MAX_VALUE, message = "{page.number.invalid}") int page,
            @PathVariable(value = "size")
            @Range(min = 1, max = Integer.MAX_VALUE, message = "{page.size.invalid}") int size,
            @PathVariable(value = "is-descending") boolean isDescending) {
        List<WifiSummaryResponse> wifiSummaryResponses =
                //(AuthorizationUtils.isAdministratorRole() || customerAuthorizationService.isAuthorizedByUserId(buyerId, AuthorizationUtils.getUserToken())) ?
                        purchaseService.getSortedWifiUsagesDataUsed(buyerId, page, size, isDescending); //: null;
        return new ResponseEntity<>(wifiSummaryResponses, HttpStatus.OK);
    }

    @GetMapping(path = "/buyer/check-current-session/{buyer-id}/{session-id}/{data-to-be-used}")
    @ApiOperation(
            value = "Check If Current Session Is Legitimate",
            notes = "Check If Current Session Is Legitimate",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = BuyerCurrentSessionLegitResponse.class)
    })
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    //@PreAuthorize("hasAuthority('CUSTOMER') or hasAuthority('ADMINISTRATOR')")
    public ResponseEntity<?> isBuyerCurrentSessionLegit(@PathVariable(value = "buyer-id")
                                                        @Range(min = 1, message = "{buyer.id.invalid}") Long buyerId,
                                                        @PathVariable(value = "session-id")
                                                        @Range(min = 1, message = "{session.id.invalid}") Long sessionId,
                                                        @PathVariable(value = "data-to-be-used")
                                                        @Range(min = BusinessConstants.MINIMUM_BUYING_DATA_MB, max = BusinessConstants.MAXIMUM_SELLING_DATA_MB, message = "{min.data.invalid}") int dataToBeUsed) {
        boolean isBuyerCurrentSessionLegit = //(AuthorizationUtils.isAdministratorRole() ||
                //customerAuthorizationService.isAuthorizedByUserId(buyerId, AuthorizationUtils.getUserToken())) &&
                purchaseService.isCurrentSessionValid(buyerId, sessionId, dataToBeUsed);
        return new ResponseEntity<>(new BuyerCurrentSessionLegitResponse(isBuyerCurrentSessionLegit), HttpStatus.OK);
    }

}
