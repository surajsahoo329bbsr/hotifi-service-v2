package com.hotifi.user.web.controller;

import com.hotifi.common.constants.ApplicationConstants;
import com.hotifi.common.constants.SuccessMessages;
import com.hotifi.user.entitiies.Device;
import com.hotifi.common.exception.errors.ErrorMessages;
import com.hotifi.common.exception.errors.ErrorResponse;
import com.hotifi.user.services.interfaces.IDeviceService;
import com.hotifi.user.web.request.DeviceRequest;
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

import javax.validation.constraints.NotBlank;

@Validated
@RestController
@Api(tags = ApplicationConstants.DEVICE_TAG)
@RequestMapping(path = "/device")
public class DeviceController {

    @Autowired
    private IDeviceService deviceService;

    //TODO
    //@Autowired
    //private ICustomerAuthorizationService customerAuthorizationService;

    @GetMapping(path = "/{android-id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Get Device Details By Android-Id",
            notes = "Get Device Details By Android-Id",
            response = String.class)
    @ApiResponses(value = {
            @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class),
            @ApiResponse(code = 200, message = SuccessMessages.OK, response = Device.class)
    })
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<?> getDeviceByAndroidId(@PathVariable(value = "android-id")
                                                  @NotBlank(message = "{android.id.blank}")
                                                  @Length(max = 255, message = "{android.id.invalid}") String androidId) {
        Device device = //(customerAuthorizationService.isAuthorizedByAndroidId(androidId, AuthorizationUtils.getUserToken())) ?
                deviceService.getDeviceByAndroidId(androidId); //: null;
        return new ResponseEntity<>(device, HttpStatus.OK);
    }

    @PostMapping(path = "/", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Add Device Details",
            notes = "Add Device Details By Android Id, Device Name & Firebase Token",
            code = 204,
            response = String.class)
    @ApiResponses(value = @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class))
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<?> addDevice(@RequestBody @Validated DeviceRequest deviceRequest) {
        //if ((customerAuthorizationService.isAuthorizedByUserId(deviceRequest.getUserId(), AuthorizationUtils.getUserToken())))
            deviceService.addDevice(deviceRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PutMapping(path = "/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ApiOperation(
            value = "Update Device Details",
            notes = "Update Device Details",
            code = 204,
            response = String.class)
    @ApiResponses(value = @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class))
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<?> updateDevice(@RequestBody @Validated DeviceRequest deviceRequest) {
        //if (customerAuthorizationService.isAuthorizedByAndroidId(deviceRequest.getAndroidId(), AuthorizationUtils.getUserToken()))
            deviceService.updateDevice(deviceRequest);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping(path = "/delete/{user-id}")
    @ApiOperation(
            value = "Delete Device Details By User-Id",
            notes = "Delete Device Details By User-Id",
            code = 204,
            response = String.class)
    @ApiResponses(value = @ApiResponse(code = 500, message = ErrorMessages.INTERNAL_ERROR, response = ErrorResponse.class))
    @ApiImplicitParams(value = @ApiImplicitParam(name = "Authorization", value = "Bearer Token", required = true, dataType = "string", paramType = "header"))
    @PreAuthorize("hasAuthority('CUSTOMER')")
    public ResponseEntity<?> deleteUserDevices(@PathVariable("user-id") @Range(min = 1, message = "{user.id.invalid}") Long userId) {
        //if (customerAuthorizationService.isAuthorizedByUserId(userId, AuthorizationUtils.getUserToken()))
            deviceService.deleteUserDevices(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
