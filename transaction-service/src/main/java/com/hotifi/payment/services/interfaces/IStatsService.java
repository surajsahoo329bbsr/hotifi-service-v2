package com.hotifi.payment.services.interfaces;


import com.hotifi.payment.web.responses.BuyerStatsResponse;
import com.hotifi.payment.web.responses.SellerStatsResponse;

public interface IStatsService {

    BuyerStatsResponse getBuyerStats(Long buyerId);

    SellerStatsResponse getSellerStats(Long sellerId);
}
