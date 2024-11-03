package com.hotifi.offer.web.controllers;

import com.hotifi.common.constants.ApplicationConstants;
import io.swagger.annotations.Api;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@Api(tags = ApplicationConstants.OFFER_TAG)
@RequestMapping(path = "/offer")
public class OfferController {



}
