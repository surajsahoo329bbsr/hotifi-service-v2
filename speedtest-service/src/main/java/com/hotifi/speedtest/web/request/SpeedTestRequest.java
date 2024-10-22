package com.hotifi.speedtest.web.request;

import com.hotifi.common.constants.BusinessConstants;
import com.hotifi.speedtest.validators.NetworkProvider;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class SpeedTestRequest {

    @NetworkProvider
    private String networkProvider;

    //both upload and download speeds in MBs
    @DecimalMin(value = BusinessConstants.MINIMUM_UPLOAD_SPEED_MEGABYTES, message = "{upload.speed.minimum.invalid}")
    @Digits(integer = 15, fraction = 2, message = "{upload.speed.format.invalid}")
    private double uploadSpeed;

    @DecimalMin(value = BusinessConstants.MINIMUM_DOWNLOAD_SPEED_MEGABYTES, message = "{download.speed.minimum.invalid}")
    @Digits(integer = 15, fraction = 2, message = "{download.speed.format.invalid}")
    private double downloadSpeed;

    @Range(min = 1, message = "{user.id.invalid}")
    private Long userId;

    @NotBlank(message = "{pincode.blank}")
    @Length(max = 12, message = "{pincode.length.invalid}")
    private String pinCode;

}
